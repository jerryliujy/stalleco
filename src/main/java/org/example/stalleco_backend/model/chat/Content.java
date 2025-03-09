package org.example.stalleco_backend.model.chat;

import lombok.Data;

/**
 * 帖子下面的评论
 */
@Data
public class Content {
    private String type;
    private String text;
    private ImageUrl imageUrl;
}
