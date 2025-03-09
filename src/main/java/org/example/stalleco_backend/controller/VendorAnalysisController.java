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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 分析用户所处位置是否便于摆摊
 * 获取用户经纬度，得到用户周边信息，给出是否合适摆摊的评价
 */

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
     * 用户拍照上传图片
     */
    @PostMapping("/analyse")
    public ResponseEntity<String> analyse(
            @RequestParam("vendorId") Long vendorId,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        try {
            // 保存上传的图片到临时文件
            Path tempFile = Files.createTempFile("upload_", "_" + UUID.randomUUID());
            Files.copy(image.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            String locationData = analyzeVendorLocation(vendorId, latitude, longitude);

            // 评估图片
            String comment = aiAnalysisService.getVendingRecommendation(locationData, Collections.singletonList(tempFile.toString()));

            // 删除临时文件
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok(comment);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 上传本地图片
     */
    @PostMapping("/analyse-local")
    public ResponseEntity<String> analyseLocal(
            @RequestParam("vendorId") Long vendorId,
            @RequestParam("path") String imagePath,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        try {
            String locationData = analyzeVendorLocation(vendorId, latitude, longitude);

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
    private String analyzeVendorLocation(Long vendorId, Double latitude, Double longitude) throws IOException {
        // 根据摊主ID获取摊位信息
        Vendor vendor = vendorService.getVendorById(vendorId);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor not found");
        }

        JsonNode locationData = locationAnalysisService.analyzeLocation(longitude, latitude);
        JsonNode vendorData = vendorService.getVendorDetails(vendor);

        return locationData + vendorData.toString();
    }
}