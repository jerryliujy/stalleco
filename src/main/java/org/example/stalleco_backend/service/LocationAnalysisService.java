package org.example.stalleco_backend.service;

import com.google.common.collect.Maps;
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
     *
     * @return 位置分析的JSON数据
     */
    public JsonNode analyzeLocation(Double longitude, Double latitude) throws IOException {
        // 2. 调用高德逆地理编码API获取位置信息
        Map<String, Object> locationInfo = getLocationInfo(longitude, latitude);

        // 4. 获取人流量估计（这里是模拟数据，实际可能需要调用其他API）
        Map<String, Object> weatherInfo = getWeather((String) locationInfo.get("adcode"));

        // 6. 整合所有信息为JSON
        ObjectNode resultJson = objectMapper.createObjectNode();
        resultJson.set("locationInfo", objectMapper.valueToTree(locationInfo));
        resultJson.set("weatherInfo", objectMapper.valueToTree(weatherInfo));

        return resultJson;
    }

    /**
     * 调用高德地图API获取位置信息
     */
    private Map<String, Object> getLocationInfo(double longitude, double latitude) {
        String url = "https://restapi.amap.com/v3/geocode/geo?key={key}&location={location}";
        Map<String, String> map = Maps.newHashMapWithExpectedSize(2);
        map.put("key", amapKey);
        map.put("location", longitude + "," + latitude);
        String response;
        try {
            response = restTemplate.getForObject(url, String.class, map);
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.path("status").asText().equals("1")) {
                JsonNode regeocode = root.path("regeocode");
                result.put("formattedAddress", regeocode.path("formatted_address").asText());

                // 提取行政区信息
                JsonNode addressComponent = regeocode.path("addressComponent");
                result.put("province", addressComponent.path("province").asText());
                result.put("city", addressComponent.path("city").asText());
                result.put("district", addressComponent.path("district").asText());
                result.put("township", addressComponent.path("township").asText());
                result.put("adcode", addressComponent.path("adcode").asText());

                // 提取道路信息
                List<String> roads = new ArrayList<>();
                JsonNode roadsNode = addressComponent.path("streetNumber");
                if (roadsNode.isArray()) {
                    for (JsonNode building : roadsNode) {
                        roads.add(building.path("street").asText());
                    }
                }
                result.put("streets", roads);

                // 提取建筑信息
                List<Map<String, String>> buildings = new ArrayList<>();
                JsonNode buildingsNode = addressComponent.path("building");
                if (buildingsNode.isArray()) {
                    for (JsonNode building : buildingsNode) {
                        Map<String, String> ne = new HashMap<>();
                        ne.put("name", building.path("name").asText());
                        ne.put("type", building.path("type").asText());
                        buildings.add(ne);
                    }
                }
                result.put("buildings", buildings);

                // 提取商圈信息
                List<String> businessAreas = new ArrayList<>();
                JsonNode businessAreasNode = addressComponent.path("businessAreas");
                if (businessAreasNode.isArray()) {
                    for (JsonNode area : businessAreasNode) {
                        businessAreas.add(area.path("name").asText());
                    }
                }
                result.put("businessAreas", businessAreas);

                // 提取邻近的poi（100米内）
                List<Map<String, String>> pois = new ArrayList<>();
                JsonNode poisNode = regeocode.path("pois");
                if (poisNode.isArray()) {
                    for (JsonNode poi : poisNode) {
                        if (poi.path("distance").asInt() > 100) {
                            break;
                        }
                        Map<String, String> ne = new HashMap<>();
                        ne.put("name", poi.path("name").asText());
                        ne.put("type", poi.path("type").asText());
                        ne.put("businessarea", poi.path("businessarea").asText());
                        buildings.add(ne);
                    }
                }
                result.put("pois", pois);
            }

        } catch (Exception e) {
            result.put("error", "解析位置信息失败: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> getWeather(String adcode) {
        String url = "https://restapi.amap.com/v3/weather/weatherInfo?key={key}&city={city}";
        Map<String, String> map = Maps.newHashMapWithExpectedSize(2);
        map.put("key", amapKey);
        map.put("city", adcode);
        String response;
        try {
            response = restTemplate.getForObject(url, String.class, map);
        } catch (Exception e) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.path("status").asText().equals("1")) {
                // 提取行政区信息
                JsonNode weatherComponent = root.path("lives").path("0");
                result.put("weather", weatherComponent.path("weather").asText());
                result.put("temperature", weatherComponent.path("temperature").asText());
                result.put("windpower", weatherComponent.path("windpower").asText());
                result.put("humidity", weatherComponent.path("humidity").asText());
            }
        } catch (Exception e) {
            result.put("error", "解析位置信息失败: " + e.getMessage());
        }

        return result;
    }
}