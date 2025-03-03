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

@Service
public class AIAnalysisService {

    @Value("${ai.model.api.url:https://api.example.com/ai}")
    private String aiModelApiUrl;
    
    @Value("${ai.model.api.key:demo-api-key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AIAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 根据位置信息调用大模型进行智能分析
     */
    public JsonNode getVendingRecommendation(JsonNode locationData) {
        try {
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // 添加系统提示词，指导AI如何分析数据
            requestBody.put("system", "你是一个专业的街边摊位分析助手，请根据提供的地理位置数据、周边环境、交通和人流量信息，分析该位置是否适合摆摊，并给出具体理由和建议。");
            
            // 用户消息包含位置数据
            requestBody.put("user", "请分析这个位置是否适合摆摊: " + locationData.toString());
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 发送请求到AI模型API
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            String response = restTemplate.postForObject(aiModelApiUrl, request, String.class);
            
            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response);
            
            // 构建结果
            ObjectNode result = objectMapper.createObjectNode();
            result.put("isRecommended", extractRecommendation(responseJson));
            result.set("analysis", responseJson.path("content"));
            result.set("originalData", locationData);
            
            return result;
        } catch (Exception e) {
            ObjectNode errorResult = objectMapper.createObjectNode();
            errorResult.put("error", "AI分析失败: " + e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * 从AI响应中提取是否推荐的结论
     */
    private boolean extractRecommendation(JsonNode aiResponse) {
        // 从AI响应中提取文本内容
        String content = aiResponse.path("content").asText();
        
        // 简单分析文本中是否包含积极推荐的关键词
        boolean hasPositiveKeywords = content.toLowerCase().contains("推荐") || 
                                     content.toLowerCase().contains("适合") || 
                                     content.toLowerCase().contains("建议");
                                     
        boolean hasNegativeKeywords = content.toLowerCase().contains("不推荐") || 
                                     content.toLowerCase().contains("不适合") || 
                                     content.toLowerCase().contains("不建议");
        
        // 如果同时包含正面和负面关键词，进一步分析文本整体倾向
        if (hasPositiveKeywords && hasNegativeKeywords) {
            // 计算正面关键词和负面关键词的出现次数
            int positiveCount = countOccurrences(content, new String[]{"推荐", "适合", "建议", "优势", "机会"});
            int negativeCount = countOccurrences(content, new String[]{"不推荐", "不适合", "不建议", "风险", "劣势", "问题"});
            
            return positiveCount > negativeCount;
        }
        
        // 如果只有正面关键词，返回true
        return hasPositiveKeywords && !hasNegativeKeywords;
    }
    
    /**
     * 计算关键词在文本中出现的次数
     */
    private int countOccurrences(String text, String[] keywords) {
        int count = 0;
        for (String keyword : keywords) {
            int index = 0;
            while (index != -1) {
                index = text.indexOf(keyword, index);
                if (index != -1) {
                    count++;
                    index += keyword.length();
                }
            }
        }
        return count;
    }
}