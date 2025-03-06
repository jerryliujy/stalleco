package org.example.stalleco_backend.model;

import lombok.Data;

import java.util.Date;

@Data
public class Location {
    private Long userId;
    private Double latitude;
    private Double longitude;
    private Date updateTime;
}
