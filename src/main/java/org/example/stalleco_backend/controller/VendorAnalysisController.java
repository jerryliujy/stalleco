package org.example.stalleco_backend.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.stalleco_backend.model.Location;
import org.example.stalleco_backend.model.StallSession;
import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.service.AIAnalysisService;
import org.example.stalleco_backend.service.LocationAnalysisService;
import org.example.stalleco_backend.service.LocationService;
import org.example.stalleco_backend.service.VendorService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    @PostMapping("/analyse")
    public ResponseEntity<String> analyse(@RequestParam("vendorId") Long vendorId, @RequestParam(required = false) MultipartFile image) {
        try {
            // 保存上传的图片到临时文件
            Path tempFile = Files.createTempFile("upload_", "_" + UUID.randomUUID());
            Files.copy(image.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            String locationData = analyzeVendorLocation(vendorId);

            // 评估图片
            String comment = aiAnalysisService.getVendingRecommendation(locationData, Collections.singletonList(tempFile.toString()));

            // 删除临时文件
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok(comment);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/analyse-local")
    public ResponseEntity<String> analyseLocal(@RequestParam("vendorId") Long vendorId, @RequestParam("path") String imagePath) {
        try {
            String locationData = analyzeVendorLocation(vendorId);

            String comment = aiAnalysisService.getVendingRecommendation(locationData, Collections.singletonList(imagePath));

            return ResponseEntity.ok(comment);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 分析位置并获取AI评估
     * 支持通过摊主ID或坐标进行分析
     */
    private String analyzeVendorLocation(Long vendorId) throws IOException {
        // 根据摊主ID或坐标获取摊位信息
        Vendor vendor = vendorService.getVendorById(vendorId);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor not found");
        }

        Double longitude = 0;
        Double latitude = 0;

        JsonNode locationData = locationAnalysisService.analyzeLocation(longitude, latitude);

        JsonNode vendorData = vendorService.getVendorDetails(vendor);

        return locationData + vendorData.toString();
    }

}