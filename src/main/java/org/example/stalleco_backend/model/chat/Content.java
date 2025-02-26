package org.example.stalleco_backend.model.chat;

import lombok.Data;

@Data
public class Content {
    private String type;
    private String text;
    private ImageUrl imageUrl;
}
