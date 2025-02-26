package org.example.stalleco_backend.repository;

import org.example.stalleco_backend.model.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 根据标题或内容搜索帖子
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> searchByKeyword(@Param("keyword") String keyword);

    // 根据标签搜索帖子
    List<Post> findByTagsName(String tagName);

    // 根据摊主查找帖子
    List<Post> findByVendorId(Long vendorId);
}
