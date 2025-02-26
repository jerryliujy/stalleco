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

    // 摊位名称
    private String stallName;

    // 摊位描述
    private String description;

    // 地理位置：经度与纬度（可扩展为其它格式）
    private Double longitude;
    private Double latitude;
    private boolean isActive;

    private double score = 0.0;
}
