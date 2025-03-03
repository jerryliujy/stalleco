package org.example.stalleco_backend.service;

import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorService {
    
    @Autowired
    private VendorRepository vendorRepository;
    
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
        vendor.setStallName(vendorDetails.getStallName());
        vendor.setDescription(vendorDetails.getDescription());
        if (vendorDetails.getLongitude() != null) {
            vendor.setLongitude(vendorDetails.getLongitude());
        }
        if (vendorDetails.getLatitude() != null) {
            vendor.setLatitude(vendorDetails.getLatitude());
        }
        
        return vendorRepository.save(vendor);
    }
    
    public List<Vendor> searchVendors(String query) {
        if (query != null && !query.isEmpty()) {
            return vendorRepository.findByStallNameContaining(query);
        } else {
            return vendorRepository.findAll();
        }
    }
    
    public List<Vendor> getActiveVendors() {
        return vendorRepository.findByIsActiveTrue();
    }
}