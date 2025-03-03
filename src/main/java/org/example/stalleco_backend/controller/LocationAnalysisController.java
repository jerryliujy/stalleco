package org.example.stalleco_backend.controller;

import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.service.LocationAnalysisService;
import org.example.stalleco_backend.service.VendorService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationAnalysisController {

    private final LocationAnalysisService locationAnalysisService;
    private final VendorService vendorService;
    
    @Autowired
    public LocationAnalysisController(LocationAnalysisService locationAnalysisService, VendorService vendorService) {
        this.locationAnalysisService = locationAnalysisService;
        this.vendorService = vendorService;
    }
    
    @PostMapping("/analyze/{vendorId}")
    public ResponseEntity<JsonNode> analyzeLocation(
            @PathVariable Long vendorId,
            @RequestParam(required = false) List<MultipartFile> photos) {
        
        try {
            // 获取摊主信息
            Vendor vendor = vendorService.getVendorById(vendorId);
            if (vendor == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 分析位置
            JsonNode result = locationAnalysisService.analyzeLocation(vendor, photos);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/analyze/coordinates")
    public ResponseEntity<JsonNode> analyzeLocationByCoordinates(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) List<MultipartFile> photos) {
        
        try {
            // 创建临时Vendor对象
            Vendor tempVendor = new Vendor();
            tempVendor.setLongitude(longitude);
            tempVendor.setLatitude(latitude);
            tempVendor.setStallName(productType);
            
            // 分析位置
            JsonNode result = locationAnalysisService.analyzeLocation(tempVendor, photos);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}