package cn.ssh.sdk;

import cn.ssh.sdk.types.utils.TokenUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class OpenAiCodeReview {
    public static void main(String[] args) throws Exception {
        System.out.println("测试执行");

        String githubToken = System.getenv("GITHUB_TOKEN");
        if(githubToken == null || githubToken.isEmpty()){
            throw  new RuntimeException();
        }

        // 1. 代码检出
        // java自带的Linux命令执行工具
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        // 设置工作目录
        processBuilder.directory(new File("."));
        // 执行命令
        Process process = processBuilder.start();

        // 获取到所有不一样的行
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);

        System.out.println("diff code：" + diffCode.toString());

        // 2. 调用 chatglm 进行代码评审 获取评审的结果
        String log = codeReview(diffCode.toString());

        // 审查日志写入
        String link = writeLog(githubToken, log);
        System.out.println("link:"+ link);



    }

    private static String codeReview(String diffCode) throws Exception {

        // chatglm 密钥
        String apiKeySecret = "941fab44c4e16cc56b4a0d4c5ca899a2.DeExIOpaOP001csA";
        // 通过密钥获取token
        String token = TokenUtils.getToken(apiKeySecret);
        // chatglm 大模型api地址 构造http连接
        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置http请求头信息
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        String jsonInputData = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + diffCode + "\""
                + "    }"
                + "]"
                + "}";



        try(OutputStream outputStream = connection.getOutputStream()) {
            byte[] bytes = jsonInputData.getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null){
            content.append(inputLine);
        }

        bufferedReader.close();
        connection.disconnect();

        return content.toString();
    }

    private static String writeLog(String token, String log) throws Exception {
        // 获取github仓库
        Git git = Git.cloneRepository()
                .setURI("https://github.com/vadymsun/openai-code-review-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                .call();
        // 代码审查日志按照日期分类
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }
        // 写入
        String fileName = generateRandomString(12) + ".md";
        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(log);
        }

        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new file via GitHub Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

        System.out.println("Changes have been pushed to the repository.");
        // 返回地址
        return "https://github.com/vadymsun/openai-code-review-log/blob/master/" + dateFolderName + "/" + fileName;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

}
