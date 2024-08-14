package cn.ssh.sdk.infrastrcture.openai.imp;

import cn.ssh.sdk.infrastrcture.openai.OpenAi;
import cn.ssh.sdk.infrastrcture.openai.dto.ChatCompletionRequestDTO;
import cn.ssh.sdk.infrastrcture.openai.dto.ChatCompletionSyncResponseDTO;
import cn.ssh.sdk.types.utils.TokenUtils;
import com.alibaba.fastjson2.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatGLM implements OpenAi {

    private final String host;
    private final String keySecret;

    public ChatGLM(String host, String keySecret) {
        this.host = host;
        this.keySecret = keySecret;
    }

    @Override
    public ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception{
        // 通过密钥获取token
        String token = TokenUtils.getToken(keySecret);


        // chatglm 大模型api地址 构造http连接
        URL url = new URL(host);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 设置http请求头信息
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        // 传输http数据
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(requestDTO).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        // 获取代码评审结果
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder reviewLog = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            reviewLog.append(line);
        }
        in.close();
        connection.disconnect();

        // 返回结果
        return JSON.parseObject(reviewLog.toString(), ChatCompletionSyncResponseDTO.class);
    }
}
