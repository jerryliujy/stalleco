package org.example.stalleco_backend.service;

import org.example.stalleco_backend.config.OpenAIConfig;
import org.example.stalleco_backend.model.chat.*;
import org.example.stalleco_backend.model.chat.ChatCompletionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CleaniessEvaluationService {

    private final WebClient webClient;
    private final OpenAIConfig config;

    @Autowired
    public CleaniessEvaluationService(OpenAIConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build();
    }

    public int evaluateImage(String imagePath) throws IOException {
        // 读取图片文件
        Path path = Paths.get(imagePath);
        byte[] imageData = Files.readAllBytes(path);

        // 获取文件扩展名
        String extension = getFileExtension(imagePath);

        // 将图片转换为Base64编码
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        String imageUrl = "data:image/" + extension + ";base64," + base64Image;

        // 构建请求
        ChatCompletionRequest request = createChatCompletionRequest(imageUrl);

        // 发送请求并获取响应
        ChatCompletionResponse response = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .block();

        // 解析响应中的数字
        if (response != null && !response.getChoices().isEmpty()) {
            String content = response.getChoices().get(0).getMessage().getContent();
            return extractNumber(content);
        }

        return 0; // 默认返回0表示评估失败
    }

    private ChatCompletionRequest createChatCompletionRequest(String imageUrl) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(config.getModel());

        // 创建消息
        Message message = new Message();
        message.setRole("user");

        // 创建内容列表
        List<Content> contentList = new ArrayList<>();

        // 添加文本内容
        Content textContent = new Content();
        textContent.setType("text");
        textContent.setText("你是一个专业的摊位环境测评师，你需要为这个摊位的卫生环境进行打分，从1-5分进行评分。请不要吝啬5分。你的评价指标是：摊位是否收理干净，是否有垃圾存留。你只需要关注摊位及周边环境。环境的简陋不会影响最终得分。你不能返回任何分析，只返回数字。");
        contentList.add(textContent);

        // 添加图片内容
        Content imageContent = new Content();
        imageContent.setType("image_url");
        ImageUrl imgUrl = new ImageUrl();
        imgUrl.setUrl(imageUrl);
        imageContent.setImageUrl(imgUrl);
        contentList.add(imageContent);

        // 设置消息内容
        message.setContentList(contentList);

        // 设置请求消息
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        request.setMessages(messages);

        return request;
    }

    private String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filePath.substring(lastDotIndex + 1);
        }
        return "png"; // 默认扩展名
    }

    private int extractNumber(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        // 使用正则表达式提取数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        return 0;
    }
}
