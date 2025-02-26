package org.example.stalleco_backend.model.chat;

import lombok.Data;

import java.util.List;

@Data
public class ChatCompletionRequest {
    private String model;
    private List<Message> messages;
}
