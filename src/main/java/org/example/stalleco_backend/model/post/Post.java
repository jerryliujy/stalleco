package org.example.stalleco_backend.model.post;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.example.stalleco_backend.model.Vendor;

@Data
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 帖子标题
    @Column(nullable = false)
    private String title;

    // 帖子内容
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 图片 URL 列表
    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();

    // 帖子创建时间
    private LocalDateTime createdAt;

    // 发布帖子摊贩（多对一关系）
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    // 与评论的一对多关系（级联删除、保持数据一致）
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 帖子标签
    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    // 引用的其他帖子
    @ManyToMany
    @JoinTable(
            name = "post_references",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "referenced_post_id")
    )
    private List<Post> referencedPosts = new ArrayList<>();
}
