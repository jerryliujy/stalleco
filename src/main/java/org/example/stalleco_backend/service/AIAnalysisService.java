package org.example.stalleco_backend.service;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.example.stalleco_backend.model.chat.ChatCompletionRequest;
import org.example.stalleco_backend.model.chat.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AIAnalysisService {

    @Value("${openai.api-key}")
    private String apiKey;

    /**
     * 调用千问多模态对话接口，传入系统提示、用户提示和图片 URL 列表，返回回答文本
     */
    private String callQianwen(String systemPrompt, String userPrompt, List<String> photoUrls)
            throws ApiException, NoApiKeyException, UploadFileException, IOException {
        MultiModalConversation conv = new MultiModalConversation();

        // 构造消息内容列表：先添加系统提示、再添加用户提示，再附加图片信息（若有）
        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(Collections.singletonMap("text", systemPrompt));
        contents.add(Collections.singletonMap("text", userPrompt));
        for (String url : photoUrls) {
            contents.add(Collections.singletonMap("image_url", url));
        }

        // 构造多模态消息，role 采用 USER
        MultiModalMessage message = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();

        // 构造对话调用参数，此处示例使用 qwen-vl-plus 模型，可根据需求修改
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model("qwen-vl-plus")
                .message(message)
                .build();

        // 调用千问 API
        MultiModalConversationResult result = conv.call(param);
        String content = result.getOutput().getChoices().get(0).getMessage().getContent().toString();
        log.info("千问 API 返回结果：{}", content);
        // 此处直接返回 JSON 格式的结果字符串，可根据实际返回结构进一步解析
        return content;
    }

    /**
     * 根据位置数据及图片获取摆摊推荐意见
     */
    public String getVendingRecommendation(String locationData, List<String> photoUrls) {
        try {
            String systemPrompt = "你是一个专业的街边摊位分析助手，请根据提供的地理位置数据、周边环境、交通和人流量信息，"
                    + "分析该位置是否适合摆摊，并给出具体理由和建议。";
            String userPrompt = "请分析这个位置是否适合摆摊: " + locationData;
            return callQianwen(systemPrompt, userPrompt, photoUrls);
        } catch (Exception e) {
            throw new RuntimeException("AI调用失败: " + e.getMessage());
        }
    }

    /**
     * 根据图片数据获取摊位环境的卫生评分（1-5分）
     */
    public int getCleaniessEvaluation(List<String> photoUrls) {
        try {
            String systemPrompt = "你是一个专业的摊位环境测评师，你需要为这个摊位的卫生环境进行打分，从1-5分进行评分。"
                    + "请不要吝啬5分。你的评价指标是：摊位是否收理干净，是否有垃圾存留。"
                    + "你只需要关注摊位及周边环境。环境的简陋不会影响最终得分。你不能返回任何分析，只返回数字。";
            String userPrompt = "请评分: ";
            String response = callQianwen(systemPrompt, userPrompt, photoUrls);
            return extractCleaniessScore(response);
        } catch (Exception e) {
            throw new RuntimeException("AI调用失败: " + e.getMessage());
        }
    }

    /**
     * 从 AI 返回的文本中提取数字评分
     */
    private int extractCleaniessScore(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }
}
