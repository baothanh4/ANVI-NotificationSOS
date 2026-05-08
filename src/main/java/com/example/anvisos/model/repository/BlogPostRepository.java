package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findAllByOrderByCreatedAtDesc();
    List<BlogPost> findByCategoryOrderByCreatedAtDesc(String category);
}
