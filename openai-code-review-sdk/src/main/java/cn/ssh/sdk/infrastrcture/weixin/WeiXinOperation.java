package cn.ssh.sdk.infrastrcture.weixin;

import cn.ssh.sdk.infrastrcture.weixin.dto.TempleteMessageDTO;
import cn.ssh.sdk.types.utils.WXAccessTokenUtils;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class WeiXinOperation {
    private final Logger logger = LoggerFactory.getLogger(WeiXinOperation.class);

    private final String appID;
    private final String toUser;
    private final String secret;
    private final String templeteID;


    public WeiXinOperation(String appID, String toUser, String secret, String templeteID) {
        this.appID = appID;
        this.toUser = toUser;
        this.secret = secret;
        this.templeteID = templeteID;
    }

    public void pushTempleteMessage(String logUrl,Map<String, Map<String, String>> data   ) throws IOException {
        // 1. 获取微信token
        String accessToken = WXAccessTokenUtils.getAccessToken(appID, secret);

        // 2. 组装消息
        TempleteMessageDTO templeteMessageDTO = new TempleteMessageDTO(toUser, templeteID, logUrl, data);

        // 3. 推送消息
        // 创建http链接
        URL url = new URL(String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        // 发送数据
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(templeteMessageDTO).getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }

        // 打印微信消息发送结果日志
        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            logger.debug(response);
        }

    }
}
