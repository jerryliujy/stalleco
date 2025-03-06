package org.example.stalleco_backend.service;

import org.example.stalleco_backend.model.Vendor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationAnalysisService {

    @Value("${amap.key}")
    private String amapKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public LocationAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 分析位置是否适合摆摊
     * @return 位置分析的JSON数据
     */
    public JsonNode analyzeLocation(Double longitude, Double latitude) throws IOException {
        // 2. 调用高德逆地理编码API获取位置信息
        Map<String, Object> locationInfo = getLocationInfo(longitude, latitude);
        
        // 3. 获取周边环境信息（POI信息）
        List<Map<String, Object>> poiInfo = getNearbyPOI(longitude, latitude);
        
        // 4. 获取人流量估计（这里是模拟数据，实际可能需要调用其他API）
        Map<String, Object> trafficInfo = getTrafficInfo(longitude, latitude);
        
        // 6. 整合所有信息为JSON
        ObjectNode resultJson = objectMapper.createObjectNode();
        resultJson.put("longitude", longitude);
        resultJson.put("latitude", latitude);
        resultJson.set("locationInfo", objectMapper.valueToTree(locationInfo));
        resultJson.set("poiInfo", objectMapper.valueToTree(poiInfo));
        resultJson.set("trafficInfo", objectMapper.valueToTree(trafficInfo));

        return resultJson;
    }
    
    /**
     * 调用高德地图API获取位置信息
     */
    private Map<String, Object> getLocationInfo(double longitude, double latitude) {
        String url = String.format(
            "https://restapi.amap.com/v3/geocode/regeo?output=json&location=%f,%f&key=%s&radius=1000&extensions=all",
            longitude, latitude, amapKey);
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Map<String, Object> result = new HashMap<>();
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("status").asText().equals("1")) {
                JsonNode regeocode = root.path("regeocode");
                result.put("formattedAddress", regeocode.path("formatted_address").asText());
                
                // 提取行政区信息
                JsonNode addressComponent = regeocode.path("addressComponent");
                result.put("province", addressComponent.path("province").asText());
                result.put("city", addressComponent.path("city").asText());
                result.put("district", addressComponent.path("district").asText());
                result.put("township", addressComponent.path("township").asText());
                
                // 提取道路信息
                result.put("roadName", addressComponent.path("streetNumber").path("street").asText());
                
                // 提取商圈信息
                List<String> businessAreas = new ArrayList<>();
                JsonNode businessAreasNode = addressComponent.path("businessAreas");
                if (businessAreasNode.isArray()) {
                    for (JsonNode area : businessAreasNode) {
                        businessAreas.add(area.path("name").asText());
                    }
                }
                result.put("businessAreas", businessAreas);
            }
        } catch (Exception e) {
            result.put("error", "解析位置信息失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取周边POI信息
     */
    private List<Map<String, Object>> getNearbyPOI(double longitude, double latitude) {
        String url = String.format(
            "https://restapi.amap.com/v3/place/around?output=json&location=%f,%f&key=%s&radius=500&types=商场,学校,医院,餐饮,交通设施",
            longitude, latitude, amapKey);
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<Map<String, Object>> poiList = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("status").asText().equals("1")) {
                JsonNode pois = root.path("pois");
                if (pois.isArray()) {
                    for (JsonNode poi : pois) {
                        Map<String, Object> poiInfo = new HashMap<>();
                        poiInfo.put("name", poi.path("name").asText());
                        poiInfo.put("type", poi.path("type").asText());
                        poiInfo.put("distance", poi.path("distance").asText());
                        poiList.add(poiInfo);
                    }
                }
            }
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", "获取POI信息失败: " + e.getMessage());
            poiList.add(errorInfo);
        }
        
        return poiList;
    }
    
    /**
     * 获取人流量信息（示例方法，实际可能需要调用其他API）
     */
    private Map<String, Object> getTrafficInfo(double longitude, double latitude) {
        // 这里只是模拟数据，实际应用中可能需要调用专门的API
        Map<String, Object> trafficInfo = new HashMap<>();
        
        // 可以调用高德交通态势API或其他数据源
        String url = String.format(
            "https://restapi.amap.com/v3/traffic/status/rectangle?key=%s&rectangle=%f,%f;%f,%f",
            amapKey, 
            longitude - 0.01, latitude - 0.01,
            longitude + 0.01, latitude + 0.01);
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            // 解析交通态势数据
            if (root.path("status").asText().equals("1")) {
                // 这里添加解析逻辑
                trafficInfo.put("congestionLevel", root.path("trafficinfo").path("evaluation").path("expedite").asText());
            }
            
            // 添加模拟的人流量数据
            trafficInfo.put("peakHours", "12:00-14:00, 17:00-19:00");
            trafficInfo.put("estimatedFootTraffic", "中等");
            
        } catch (Exception e) {
            trafficInfo.put("error", "获取交通信息失败: " + e.getMessage());
            // 添加默认值
            trafficInfo.put("peakHours", "未知");
            trafficInfo.put("estimatedFootTraffic", "未知");
        }
        
        return trafficInfo;
    }
}