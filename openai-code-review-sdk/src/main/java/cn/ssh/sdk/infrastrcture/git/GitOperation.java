package cn.ssh.sdk.infrastrcture.git;

import cn.ssh.sdk.types.utils.RandomStringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GitOperation {

    private final String githubReviewLogRepoUri;
    private final String githubReviewLogRepoToken;
    private final String project;
    private final String branch;
    private final String author;

    private final String commitMessage;

    public GitOperation(String githubReviewLogRepoUri, String githubReviewLogRepoToken, String project, String branch, String author, String commitMessage) {
        this.githubReviewLogRepoUri = githubReviewLogRepoUri;
        this.githubReviewLogRepoToken = githubReviewLogRepoToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.commitMessage = commitMessage;
    }


    /**
     * 获取最近两次提交的代码的不同
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getDiff() throws IOException, InterruptedException {
        // 获取上次提交的hash
        ProcessBuilder processBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        processBuilder.directory(new File("."));
        Process process = processBuilder.start();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String latestHash = bufferedReader.readLine();
        bufferedReader.close();
        process.waitFor();


        // 比较当前提交和上次提交,获取不同的行
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestHash + "^", latestHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        StringBuilder diffCode = new StringBuilder();
        String line;
        while ((line = diffReader.readLine()) != null){
            diffCode.append(line);
        }
        diffReader.close();

        int exitCode = diffProcess.waitFor();
        if(exitCode != 0){
            throw new RuntimeException("Failed to get diff code" + exitCode);
        }

        return diffCode.toString();

    }

    /**
     * 把代码审查日志写入到到github仓库，并且返回文件的链接
     * @param reviewLog
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public String commitAndPush(String reviewLog) throws GitAPIException, IOException {
        // 获取仓库实例
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogRepoUri + ".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubReviewLogRepoToken, ""))
                .call();


        // 代码审查日志按照日期分类
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = project + "-" + branch + "-" + author + RandomStringUtils.generateRandomString(12) + ".md";
        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(reviewLog);
        }

        // 提交
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new file via GitHub Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubReviewLogRepoToken      , "")).call();

        System.out.println("Changes have been pushed to the repository." + project);

        // 返回地址
        return githubReviewLogRepoUri + "/blob/master/" + dateFolderName + "/" + fileName;
    }
}
