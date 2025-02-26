package org.example.stalleco_backend.model.chat;

import lombok.Data;

import java.util.List;

@Data
public class ChatCompletionResponse {
    private List<Choice> choices;
    @Data
    public static class Choice {
        private Message message;
    }
}
