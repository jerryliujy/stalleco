package org.example.stalleco_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 注册用户名（唯一）
    @Column(nullable = false, unique = true)
    private String username;

    // 密码（实际项目中需加密存储）
    @Column(nullable = false)
    private String password;

    // 摊位描述
    private String description;

    // 摊贩类型（如食物类、化妆品类等）
    private String vendorCategory;

    // 摊贩经营方式（流动摊贩/固定摊贩）
    private String vendorType; // "MOBILE" 或 "FIXED"

    // 固定摊贩的位置信息（如静安嘉里、大学路等）
    private String fixedLocation;

    // 地理位置：经度与纬度（可扩展为其它格式）
    // 注意：这些信息不会显示给其他用户
    private Double longitude;
    private Double latitude;
    private boolean isActive;

    private double cleaniessScore = 0.0;
}
