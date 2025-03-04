package org.example.stalleco_backend.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.service.AIAnalysisService;
import org.example.stalleco_backend.service.LocationAnalysisService;
import org.example.stalleco_backend.service.VendorService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-analysis")
public class VendorAnalysisController {

    private final LocationAnalysisService locationAnalysisService;
    private final AIAnalysisService aiAnalysisService;
    private final VendorService vendorService;
    
    @Autowired
    public VendorAnalysisController(
            LocationAnalysisService locationAnalysisService,
            AIAnalysisService aiAnalysisService,
            VendorService vendorService) {
        this.locationAnalysisService = locationAnalysisService;
        this.aiAnalysisService = aiAnalysisService;
        this.vendorService = vendorService;
    }
    
    /**
     * 分析位置并获取AI评估
     * 支持通过摊主ID或坐标进行分析
     */
    @PostMapping
    public ResponseEntity<String> analyzeVendorLocation(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) List<MultipartFile> photos) {

        try {
            Vendor vendor;

            // 根据提供的参数决定使用哪种方式获取摊主信息
            if (vendorId != null) {
                // 通过ID获取现有摊主
                vendor = vendorService.getVendorById(vendorId);
                if (vendor == null) {
                    return ResponseEntity.notFound().build();
                }
            } else if (longitude != null && latitude != null) {
                // 创建临时Vendor对象
                vendor = new Vendor();
                vendor.setLongitude(longitude);
                vendor.setLatitude(latitude);
                vendor.setStallName(productType);
                vendor.setUsername(vendorName);
            } else {
                return ResponseEntity.badRequest().body("Missing required parameters");
            }

            // 分析位置
            JsonNode locationData = locationAnalysisService.analyzeLocation(vendor, photos);
            List<String> photoUrls = locationData.findValuesAsText("photos");
            ObjectNode objectNode = (ObjectNode) locationData;
            // 删除特定的key
            objectNode.remove("keyToRemove");

            // 获取摊主详情(假设vendorService.getVendorDetails返回JsonNode
            JsonNode vendorData = vendorService.getVendorDetails(vendor.getId());
            String combinedData = objectNode + vendorData.toString();

            // 调用AI进行评估并返回JsonNode而非String
            String aiRecommendation = aiAnalysisService.getVendingRecommendation(combinedData, photoUrls);

            return ResponseEntity.ok(aiRecommendation);

        } catch (Exception e) {
            // 创建错误响应
            return ResponseEntity.badRequest().body("Bad request");
        }
    }
}