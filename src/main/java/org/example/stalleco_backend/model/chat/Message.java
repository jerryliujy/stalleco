package org.example.stalleco_backend.model.chat;

import lombok.Data;

import java.util.List;

@Data
public class Message {
    private String role;
    private List<Content> contentList;
    private String content; // 用于响应
}
