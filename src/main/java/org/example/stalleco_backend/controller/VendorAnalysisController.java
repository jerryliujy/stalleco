package org.example.stalleco_backend.controller;

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
     * 根据已有摊主信息分析位置并获取AI评估
     */
    @PostMapping("/{vendorId}")
    public ResponseEntity<JsonNode> analyzeVendorLocation(
            @PathVariable Long vendorId,
            @RequestParam(required = false) List<MultipartFile> photos) {
        
        try {
            // 获取摊主信息
            Vendor vendor = vendorService.getVendorById(vendorId);
            if (vendor == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 分析位置
            JsonNode locationData = locationAnalysisService.analyzeLocation(vendor, photos);
            
            // 调用AI进行评估
            JsonNode aiRecommendation = aiAnalysisService.getVendingRecommendation(locationData);
            
            return ResponseEntity.ok(aiRecommendation);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据坐标分析位置并获取AI评估
     */
    @PostMapping("/coordinates")
    public ResponseEntity<JsonNode> analyzeLocationByCoordinates(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) List<MultipartFile> photos) {
        
        try {
            // 创建临时Vendor对象
            Vendor tempVendor = new Vendor();
            tempVendor.setLongitude(longitude);
            tempVendor.setLatitude(latitude);
            tempVendor.setStallName(productType);
            tempVendor.setUsername(vendorName);
            
            // 分析位置
            JsonNode locationData = locationAnalysisService.analyzeLocation(tempVendor, photos);
            
            // 调用AI进行评估
            JsonNode aiRecommendation = aiAnalysisService.getVendingRecommendation(locationData);
            
            return ResponseEntity.ok(aiRecommendation);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}