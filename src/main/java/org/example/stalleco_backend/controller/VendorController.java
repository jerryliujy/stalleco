package org.example.stalleco_backend.controller;

import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/vendors")
public class VendorController {

    @Autowired
    private VendorRepository vendorRepository;

    // 摊贩注册
    @PostMapping("/register")
    public ResponseEntity<Vendor> registerVendor(@RequestBody Vendor vendor) {
        // 检查用户名是否已存在
        if (vendorRepository.findByUsername(vendor.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Vendor savedVendor = vendorRepository.save(vendor);
        return ResponseEntity.ok(savedVendor);
    }

    // 登录接口：根据用户名和密码验证用户身份
    @PostMapping("/login")
    public ResponseEntity<Vendor> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<Vendor> optionalVendor = vendorRepository.findByUsername(username);
        if (optionalVendor.isPresent()) {
            Vendor vendor = optionalVendor.get();
            // 简单明文密码比对（生产环境中请使用加密算法）
            if (vendor.getPassword().equals(password)) {
                return ResponseEntity.ok(vendor);
            }
        }
        // 用户不存在或密码错误
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 根据关键字查询摊贩，例如通过摊位名称检索
    @GetMapping
    public ResponseEntity<List<Vendor>> searchVendors(@RequestParam(value = "q", required = false) String query) {
        List<Vendor> vendors;
        if (query != null && !query.isEmpty()) {
            vendors = vendorRepository.findByStallNameContaining(query);
        } else {
            vendors = vendorRepository.findAll();
        }
        return ResponseEntity.ok(vendors);
    }

    // 更新摊位信息：假设更新摊位名称、描述、位置等
    @PutMapping("/{id}")
    public ResponseEntity<Vendor> updateVendor(@PathVariable Long id, @RequestBody Vendor updatedVendor) {
        Optional<Vendor> optionalVendor = vendorRepository.findById(id);
        if (!optionalVendor.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Vendor vendor = optionalVendor.get();
        vendor.setStallName(updatedVendor.getStallName());
        vendor.setDescription(updatedVendor.getDescription());
        vendor.setLongitude(updatedVendor.getLongitude());
        vendor.setLatitude(updatedVendor.getLatitude());
        vendorRepository.save(vendor);
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Vendor>> getActiveVendors() {
        List<Vendor> activeVendors = vendorRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeVendors);
    }
}
