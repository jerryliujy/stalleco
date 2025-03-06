package org.example.stalleco_backend.controller;

import org.example.stalleco_backend.model.StallSession;
import org.example.stalleco_backend.service.AIAnalysisService;
import org.example.stalleco_backend.service.StallService;
import org.example.stalleco_backend.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/stall")
public class StallController {
    @Autowired
    private StallService stallService;
    
    @Autowired
    private AIAnalysisService aiAnalysisService;
    
    @Autowired
    private VendorService vendorService;

    private int cleaniessScore = 0;

    @PostMapping("/start")
    public ResponseEntity<StallSession> startStall(
            @RequestParam("vendorId") Long vendorId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude) {
        
        try {
            // 首先验证vendor是否存在
            if (vendorService.getVendorById(vendorId) == null) {
                return ResponseEntity.badRequest().build();
            }
            
            StallSession session = stallService.startStall(vendorId, latitude, longitude);
            return ResponseEntity.ok(session);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Integer> evaluateImage(@RequestParam("image") MultipartFile image) {
        try {
            // 保存上传的图片到临时文件
            Path tempFile = Files.createTempFile("upload_", "_" + UUID.randomUUID());
            Files.copy(image.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 评估图片
            cleaniessScore = aiAnalysisService.getCleaniessEvaluation(Collections.singletonList(tempFile.toString()));

            // 删除临时文件
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok(cleaniessScore);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/evaluate-local")
    public ResponseEntity<Integer> evaluateLocalImage(@RequestParam("path") String imagePath) {
        cleaniessScore = aiAnalysisService.getCleaniessEvaluation(Collections.singletonList(imagePath));
        return ResponseEntity.ok(cleaniessScore);
    }

    @PostMapping("/end")
    public ResponseEntity<StallSession> endStall(
            @RequestParam("sessionId") Long sessionId,
            @RequestParam("photo") MultipartFile photo) {
        try {
            StallSession session = stallService.endStall(sessionId, photo, cleaniessScore);
            return ResponseEntity.ok(session);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}