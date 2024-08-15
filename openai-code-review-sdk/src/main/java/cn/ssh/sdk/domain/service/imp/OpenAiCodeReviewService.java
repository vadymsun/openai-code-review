package cn.ssh.sdk.domain.service.imp;

import cn.ssh.sdk.domain.service.AbstractOpenAiCodeReviewService;
import cn.ssh.sdk.infrastrcture.git.GitOperation;
import cn.ssh.sdk.infrastrcture.openai.OpenAi;
import cn.ssh.sdk.infrastrcture.openai.dto.ChatCompletionRequestDTO;
import cn.ssh.sdk.infrastrcture.openai.dto.ChatCompletionSyncResponseDTO;
import cn.ssh.sdk.infrastrcture.weixin.WeiXinOperation;
import cn.ssh.sdk.model.ChatCompletionRequest;
import cn.ssh.sdk.model.Model;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.ArrayList;

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitOperation gitOperation, OpenAi openAi, WeiXinOperation weiXinOperation) {
        super(gitOperation, openAi, weiXinOperation);
    }

    @Override
    protected String getDiff() throws IOException, InterruptedException {
        return gitOperation.getDiff();
    }

    @Override
    protected String getCodeReview(String diff) throws Exception {
        // 组装消息实体
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diff));
            }
        });

        // 获取代码评审结果消息实体
        ChatCompletionSyncResponseDTO completions = openAi.completions(chatCompletionRequest);

        // 从消息实体中获取字符串
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();
    }

    @Override
    protected String saveCodeReview(String codeReview) throws GitAPIException, IOException {
        return gitOperation.commitAndPush(codeReview);
    }

    @Override
    protected void pushMessage(String link) throws IOException {
        weiXinOperation.pushTempleteMessage(link);
    }
}
