package cn.ssh.sdk;

import cn.ssh.sdk.domain.service.imp.OpenAiCodeReviewService;
import cn.ssh.sdk.infrastrcture.git.GitOperation;
import cn.ssh.sdk.infrastrcture.openai.OpenAi;
import cn.ssh.sdk.infrastrcture.openai.imp.ChatGLM;
import cn.ssh.sdk.infrastrcture.weixin.WeiXinOperation;
import cn.ssh.sdk.model.ChatCompletionRequest;
import cn.ssh.sdk.model.ChatCompletionSyncResponse;
import cn.ssh.sdk.model.Model;
import cn.ssh.sdk.types.utils.TokenUtils;
import cn.ssh.sdk.model.Message;
import cn.ssh.sdk.types.utils.WXAccessTokenUtils;
import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class OpenAiCodeReview {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    public static void main(String[] args) {
        // 创建组件实例
        GitOperation gitOperation = new GitOperation(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );
        WeiXinOperation weiXin = new WeiXinOperation(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );
        OpenAi openAI = new ChatGLM(
                getEnv("CHATGLM_APIHOST"),
                getEnv("CHATGLM_APIKEYSECRET")
        );

        // 启动服务
        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitOperation, openAI, weiXin);
        openAiCodeReviewService.execute();

        logger.debug("Done!");
    }

    /**
     * 获取环境变量的方法
     * @param name
     * @return
     */
    private static String getEnv(String name){
        String value = System.getenv(name);
        if(value == null || value.isEmpty()){
            throw new RuntimeException("value is null");
        }
        return value;
    }

}
