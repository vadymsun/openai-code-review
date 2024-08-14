package cn.ssh.sdk.infrastrcture.openai;

import cn.ssh.sdk.infrastrcture.openai.dto.ChatCompletionRequestDTO;
import cn.ssh.sdk.infrastrcture.openai.dto.ChatCompletionSyncResponseDTO;

public interface OpenAi {

    /**
     * 调用大模型接口获取代码评审结果
     * @param requestDTO
     * @return
     * @throws Exception
     */
    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;
}
