package cn.ssh.sdk.domain.service;

import cn.ssh.sdk.infrastrcture.git.GitOperation;
import cn.ssh.sdk.infrastrcture.openai.OpenAi;
import cn.ssh.sdk.infrastrcture.weixin.WeiXinOperation;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractOpenAiCodeReviewService  implements IOpenAiCodeReviewService{

    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAiCodeReviewService.class);
    protected final GitOperation gitOperation;
    protected final OpenAi openAi;
    protected final WeiXinOperation weiXinOperation;

    public AbstractOpenAiCodeReviewService(GitOperation gitOperation, OpenAi openAi, WeiXinOperation weiXinOperation) {
        this.gitOperation = gitOperation;
        this.openAi = openAi;
        this.weiXinOperation = weiXinOperation;
    }

    @Override
    public void execute() {
        try {
            logger.debug("OpenAi code review start ...");
            // 1. 代码检出
            String diff = getDiff();

            // 2. 调用OpenAI接口获取评审结果
            String codeReview = getCodeReview(diff);

            // 3. 评审结果写入git仓库
            String link = saveCodeReview(codeReview);

            // 4. 把日志链接推送到微信企业号
            pushMessage(link);

        }catch (Exception e){
            logger.error("OpenAi code review error.");
        }
    }

    protected abstract String getDiff() throws IOException, InterruptedException;

    protected abstract String getCodeReview(String diff) throws Exception;

    protected abstract String saveCodeReview(String codeReview) throws GitAPIException, IOException;

    protected abstract void pushMessage(String link) throws IOException;
}
