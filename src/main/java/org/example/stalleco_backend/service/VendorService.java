package org.example.stalleco_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class VendorService {
    
    @Autowired
    private VendorRepository vendorRepository;

    private final ObjectMapper objectMapper;

    public VendorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }
    
    public Vendor getVendorById(Long id) {
        Optional<Vendor> vendor = vendorRepository.findById(id);
        return vendor.orElse(null);
    }
    
    public Vendor registerVendor(Vendor vendor) {
        // 检查用户名是否已存在
        if (vendorRepository.findByUsername(vendor.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        return vendorRepository.save(vendor);
    }
    
    public Vendor loginVendor(String username, String password) {
        Optional<Vendor> optionalVendor = vendorRepository.findByUsername(username);
        if (optionalVendor.isPresent()) {
            Vendor vendor = optionalVendor.get();
            // 简单明文密码比对（生产环境中请使用加密算法）
            if (vendor.getPassword().equals(password)) {
                return vendor;
            }
        }
        return null;
    }

    public Vendor updateVendor(Long id, Vendor vendorDetails) {
        Optional<Vendor> optionalVendor = vendorRepository.findById(id);
        if (optionalVendor.isEmpty()) {
            return null;
        }

        Vendor vendor = optionalVendor.get();
        vendor.setDescription(vendorDetails.getDescription());

        // 更新摊贩类型
        if (vendorDetails.getVendorCategory() != null) {
            vendor.setVendorCategory(vendorDetails.getVendorCategory());
        }

        // 更新摊贩经营方式
        if (vendorDetails.getVendorType() != null) {
            vendor.setVendorType(vendorDetails.getVendorType());
        }

        // 更新固定摊贩位置
        if (vendorDetails.getFixedLocation() != null) {
            vendor.setFixedLocation(vendorDetails.getFixedLocation());
        }

        // 更新地理位置信息
        if (vendorDetails.getLongitude() != null) {
            vendor.setLongitude(vendorDetails.getLongitude());
        }
        if (vendorDetails.getLatitude() != null) {
            vendor.setLatitude(vendorDetails.getLatitude());
        }

        // 如果经纬度信息有更新，设置摊贩为在线状态
        if (vendorDetails.getLongitude() != null && vendorDetails.getLatitude() != null) {
            vendor.setActive(true);
        }

        return vendorRepository.save(vendor);
    }
    
    public List<Vendor> searchVendors(String query) {
        if (query != null && !query.isEmpty()) {
            return vendorRepository.findByUsernameContaining(query);
        } else {
            return vendorRepository.findAll();
        }
    }
    
    public List<Vendor> getActiveVendors() {
        return vendorRepository.findByIsActiveTrue();
    }

    public JsonNode getVendorDetails(Vendor vendor) {
        ObjectNode resultJson = objectMapper.createObjectNode();
        resultJson.put("vendorCategory", vendor.getVendorCategory());
        resultJson.put("vendorType",vendor.getVendorType());
        resultJson.put("location", vendor.getFixedLocation());
        return resultJson;
    }
}