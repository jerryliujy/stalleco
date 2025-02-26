package org.example.stalleco_backend.model.post;

import jakarta.persistence.*;
import lombok.Data;
import org.example.stalleco_backend.model.Vendor;

import java.time.LocalDateTime;

@Data
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 评论内容
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 评论创建时间
    private LocalDateTime createdAt;

    // 评论所属的帖子
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // 发布评论的摊贩
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    // 引用的帖子
    @ManyToOne
    @JoinColumn(name = "referenced_post_id")
    private Post referencedPost;

    // 引用的评论
    @ManyToOne
    @JoinColumn(name = "referenced_comment_id")
    private Comment referencedComment;
}