package org.example.stalleco_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIAnalysisService {

    @Value("${openai.base-url}")
    private String aiModelApiUrl;

    @Value("${openai.api-key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AIAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private String requestAIModel(ObjectNode requestBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 发送请求到AI模型API
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            String response = restTemplate.postForObject(aiModelApiUrl, request, String.class);

            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.get("choice").get(0).get("message").get("content").toString();
        } catch (Exception e) {
            throw new RuntimeException("AI模型请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据位置信息调用大模型进行智能分析
     */
    public String getVendingRecommendation(String locationData, List<String> photoUrls) {
        try {
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // 添加系统提示词，指导AI如何分析数据
            requestBody.put("system", "你是一个专业的街边摊位分析助手，请根据提供的地理位置数据、周边环境、交通和人流量信息，分析该位置是否适合摆摊，并给出具体理由和建议。");
            
            // 用户消息包含位置数据
            requestBody.put("user", "请分析这个位置是否适合摆摊: " + locationData);
            return requestAIModel(requestBody);
        } catch (Exception e) {
            throw new RuntimeException("AI调用失败");
        }
    }

    public int getCleaniessEvaluation(List<String> photoUrls) {
        try {
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();

            // 添加系统提示词，指导AI如何分析数据
            requestBody.put("system", "你是一个专业的摊位环境测评师，你需要为这个摊位的卫生环境进行打分，从1-5分进行评分。请不要吝啬5分。你的评价指标是：摊位是否收理干净，是否有垃圾存留。你只需要关注摊位及周边环境。环境的简陋不会影响最终得分。你不能返回任何分析，只返回数字。");

            // 用户消息包含位置数据
            requestBody.put("user", "请评分: ");

            return extractCleaniessScore(requestAIModel(requestBody));
        } catch (Exception e) {
            throw new RuntimeException("AI调用失败");
        }
    }


    
    /**
     * 从AI响应中提取是否推荐的结论
     */
    private boolean extractRecommendation(JsonNode aiResponse) {
        // TODO
        return true;
    }

    private int extractCleaniessScore(String content) {
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