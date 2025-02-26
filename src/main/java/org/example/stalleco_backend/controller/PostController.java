package org.example.stalleco_backend.controller;

import org.example.stalleco_backend.dto.CommentRequest;
import org.example.stalleco_backend.dto.PostRequest;
import org.example.stalleco_backend.model.post.Comment;
import org.example.stalleco_backend.model.post.Post;
import org.example.stalleco_backend.model.Vendor;
import org.example.stalleco_backend.model.post.Tag;
import org.example.stalleco_backend.repository.CommentRepository;
import org.example.stalleco_backend.repository.PostRepository;
import org.example.stalleco_backend.repository.VendorRepository;
import org.example.stalleco_backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    // 1. 创建帖子接口（摊贩发帖）
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostRequest postRequest) {
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setImageUrls(postRequest.getImageUrls());
        post.setCreatedAt(LocalDateTime.now());

        // 验证摊主是否存在
        if (postRequest.getVendorId() != null) {
            Optional<Vendor> vendorOpt = vendorRepository.findById(postRequest.getVendorId());
            if (!vendorOpt.isPresent()) {
                return ResponseEntity.badRequest().body(null);
            }
            post.setVendor(vendorOpt.get());
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        // 处理标签
        if (postRequest.getTagNames() != null && !postRequest.getTagNames().isEmpty()) {
            for (String tagName : postRequest.getTagNames()) {
                Optional<Tag> tagOpt = tagRepository.findByName(tagName);
                Tag tag = tagOpt.orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                });
                post.getTags().add(tag);
            }
        }

        // 处理引用的帖子
        if (postRequest.getReferencedPostIds() != null && !postRequest.getReferencedPostIds().isEmpty()) {
            for (Long refPostId : postRequest.getReferencedPostIds()) {
                Optional<Post> refPostOpt = postRepository.findById(refPostId);
                refPostOpt.ifPresent(refPost -> post.getReferencedPosts().add(refPost));
            }
        }

        Post savedPost = postRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }

    // 2. 获取所有帖子
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return ResponseEntity.ok(posts);
    }

    // 3. 获取特定帖子
    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        return postOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4. 搜索帖子
    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String keyword) {
        List<Post> posts = postRepository.searchByKeyword(keyword);
        return ResponseEntity.ok(posts);
    }

    // 5. 根据标签搜索帖子
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<List<Post>> getPostsByTag(@PathVariable String tagName) {
        List<Post> posts = postRepository.findByTagsName(tagName);
        return ResponseEntity.ok(posts);
    }

    // 6. 在指定帖子下添加评论
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody CommentRequest commentRequest) {
        // 验证帖子是否存在
        Optional<Post> postOpt = postRepository.findById(postId);
        if (!postOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setPost(postOpt.get());
        comment.setCreatedAt(LocalDateTime.now());

        // 验证摊主是否存在
        if (commentRequest.getVendorId() != null) {
            Optional<Vendor> vendorOpt = vendorRepository.findById(commentRequest.getVendorId());
            if (!vendorOpt.isPresent()) {
                return ResponseEntity.badRequest().body(null);
            }
            comment.setVendor(vendorOpt.get());
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        // 处理引用的帖子
        if (commentRequest.getReferencedPostId() != null) {
            Optional<Post> refPostOpt = postRepository.findById(commentRequest.getReferencedPostId());
            refPostOpt.ifPresent(comment::setReferencedPost);
        }

        // 处理引用的评论
        if (commentRequest.getReferencedCommentId() != null) {
            Optional<Comment> refCommentOpt = commentRepository.findById(commentRequest.getReferencedCommentId());
            refCommentOpt.ifPresent(comment::setReferencedComment);
        }

        Comment savedComment = commentRepository.save(comment);
        return ResponseEntity.ok(savedComment);
    }

    // 7. 获取帖子下的所有评论
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        // 验证帖子是否存在
        if (!postRepository.existsById(postId)) {
            return ResponseEntity.notFound().build();
        }
        List<Comment> comments = commentRepository.findByPostId(postId);
        return ResponseEntity.ok(comments);
    }
}

