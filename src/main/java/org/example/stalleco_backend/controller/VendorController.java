package org.example.stalleco_backend.controller;

import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.service.AIAnalysisService;
import org.example.stalleco_backend.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;
    @Autowired
    private AIAnalysisService aiAnalysisService;

    // 摊贩注册
    @PostMapping("/register")
    public ResponseEntity<Vendor> registerVendor(@RequestBody Vendor vendor) {
        try {
            Vendor savedVendor = vendorService.registerVendor(vendor);
            return ResponseEntity.ok(savedVendor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 登录接口：根据用户名和密码验证用户身份
    @PostMapping("/login")
    public ResponseEntity<Vendor> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Vendor vendor = vendorService.loginVendor(username, password);
        if (vendor != null) {
            return ResponseEntity.ok(vendor);
        }
        
        // 用户不存在或密码错误
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 根据关键字查询摊贩，例如通过摊位名称检索
    @GetMapping
    public ResponseEntity<List<Vendor>> searchVendors(@RequestParam(value = "q", required = false) String query) {
        List<Vendor> vendors = vendorService.searchVendors(query);
        return ResponseEntity.ok(vendors);
    }

    // 更新摊位信息：假设更新摊位名称、描述、位置等
    @PutMapping("/{id}")
    public ResponseEntity<Vendor> updateVendor(@PathVariable Long id, @RequestBody Vendor updatedVendor) {
        Vendor vendor = vendorService.updateVendor(id, updatedVendor);
        if (vendor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Vendor>> getActiveVendors() {
        List<Vendor> activeVendors = vendorService.getActiveVendors();
        return ResponseEntity.ok(activeVendors);
    }

    @GetMapping("/policy")
    public ResponseEntity<String> getRecommendedPolicy(@PathVariable Long id) {
        Vendor vendor = vendorService.getVendorById(id);
        if (vendor == null) {
            return ResponseEntity.notFound().build();
        }
        String policy = aiAnalysisService.getPolicy(vendorService.getVendorDetails(vendor).toString());
        return ResponseEntity.ok(policy);
    }
}
