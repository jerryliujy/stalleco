package org.example.stalleco_backend.repository;

import org.example.stalleco_backend.model.post.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 根据内容搜索评论
    @Query("SELECT c FROM Comment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Comment> searchByKeyword(@Param("keyword") String keyword);

    // 根据帖子ID查找评论
    List<Comment> findByPostId(Long postId);

    // 根据摊主查找评论
    List<Comment> findByVendorId(Long vendorId);
}
