package org.example.stalleco_backend.service;

import jakarta.transaction.Transactional;
import org.example.stalleco_backend.model.StallSession;
import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.repository.StallSessionRepository;
import org.example.stalleco_backend.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class StallService {
    @Autowired
    private VendorRepository vendorRepository;
    
    @Autowired
    private StallSessionRepository stallSessionRepository;
    
    @Value("${app.upload.dir}")
    private String uploadDir;

    public StallSession startStall(Long vendorId, Double latitude, Double longitude) {
        // 检查是否已有进行中的摆摊
        stallSessionRepository.findByVendorIdAndEndTimeIsNull(vendorId)
            .ifPresent(session -> {
                throw new IllegalStateException("已有正在进行的摆摊");
            });

        // 获取摊贩信息
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new IllegalArgumentException("摊贩不存在"));

        // 创建新的摆摊会话
        StallSession session = new StallSession();
        session.setVendor(vendor);
        session.setStartTime(LocalDateTime.now());
        session.setLatitude(latitude);
        session.setLongitude(longitude);

        // 更新摊贩状态
        vendor.setActive(true);
        vendor.setLatitude(latitude);
        vendor.setLongitude(longitude);
        vendorRepository.save(vendor);

        return stallSessionRepository.save(session);
    }

    public StallSession endStall(Long sessionId, MultipartFile photo, int cleanlinessScore) {
        StallSession session = stallSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("会话不存在"));

        if (session.getEndTime() != null) {
            throw new IllegalStateException("该摆摊会话已结束");
        }

        // 保存照片
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = savePhoto(photo);
            session.setPhotoUrl(photoUrl);
            session.setCleanlinessScore(cleanlinessScore);
            session.getVendor().setScore((session.getVendor().getScore() + cleanlinessScore) / 2);
        }

        // 更新会话状态
        session.setEndTime(LocalDateTime.now());
        
        // 更新摊贩状态
        Vendor vendor = session.getVendor();
        vendor.setActive(false);
        vendorRepository.save(vendor);

        return stallSessionRepository.save(session);
    }

    private String savePhoto(MultipartFile photo) {
        try {
            String fileName = UUID.randomUUID().toString() + getFileExtension(photo.getOriginalFilename());
            Path targetPath = Path.of(uploadDir, fileName);
            Files.copy(photo.getInputStream(), targetPath);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("保存照片失败", e);
        }
    }

    private double assessCleanliness(String photoUrl) {
        // TODO: 调用大模型评估照片卫生状况
        return Math.random() * 5 + 5;
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(f.lastIndexOf(".")))
            .orElse(".jpg");
    }
}