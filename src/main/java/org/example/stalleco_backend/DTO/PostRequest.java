package org.example.stalleco_backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class PostRequest {
    private String title;
    private String content;
    private List<String> imageUrls;
    private Long vendorId;
    private List<String> tagNames;
    private List<Long> referencedPostIds;
}
