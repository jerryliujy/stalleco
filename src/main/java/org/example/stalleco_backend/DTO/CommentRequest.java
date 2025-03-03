package org.example.stalleco_backend.DTO;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long vendorId;
    private Long referencedPostId;
    private Long referencedCommentId;
}
