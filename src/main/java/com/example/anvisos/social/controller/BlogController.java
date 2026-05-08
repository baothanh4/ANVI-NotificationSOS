package com.example.anvisos.social.controller;

import com.example.anvisos.model.entity.BlogPost;
import com.example.anvisos.model.repository.BlogPostRepository;
import com.example.anvisos.social.dto.BlogPostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogPostRepository blogPostRepository;
    private final com.example.anvisos.model.repository.UserRepository userRepository;
    private final com.example.anvisos.auth.service.JwtService jwtService;

    @GetMapping
    public List<BlogPost> getAllPosts(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String category) {
        
        // If user is Admin (checked manually here because we want to show different data in the same GET)
        boolean isAdmin = false;
        if (token != null) {
            try {
                Long userId = jwtService.getUserIdFromToken(token);
                com.example.anvisos.model.entity.User user = userRepository.findById(userId).orElse(null);
                if (user != null && user.getRole() == com.example.anvisos.model.enums.UserRole.ADMIN) {
                    isAdmin = true;
                }
            } catch (Exception ignored) {}
        }

        if (isAdmin) {
            if (category != null && !category.isEmpty()) {
                return blogPostRepository.findByCategoryOrderByCreatedAtDesc(category);
            }
            return blogPostRepository.findAllByOrderByCreatedAtDesc();
        } else {
            return blogPostRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(p -> "APPROVED".equals(p.getStatus()))
                    .filter(p -> category == null || category.isEmpty() || category.equals(p.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    @PostMapping
    @com.example.anvisos.auth.annotation.RequiredRole({
        com.example.anvisos.model.enums.UserRole.OWNER,
        com.example.anvisos.model.enums.UserRole.ADMIN,
        com.example.anvisos.model.enums.UserRole.DOCTOR,
        com.example.anvisos.model.enums.UserRole.FAMILY_MEMBER
    })
    public BlogPost createPost(
            @RequestAttribute("currentUser") com.example.anvisos.model.entity.User user,
            @RequestBody BlogPostRequest request) {
        
        boolean isAdmin = user.getRole() == com.example.anvisos.model.enums.UserRole.ADMIN;
        
        BlogPost post = BlogPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .excerpt(request.getExcerpt())
                .thumbnailUrl(request.getThumbnailUrl())
                .category(request.getCategory())
                .author(user)
                .status(isAdmin ? "APPROVED" : "PENDING")
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();
        
        return blogPostRepository.save(post);
    }

    @PatchMapping("/{id}/approve")
    @com.example.anvisos.auth.annotation.RequiredRole(com.example.anvisos.model.enums.UserRole.ADMIN)
    public BlogPost approvePost(@PathVariable Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.setStatus("APPROVED");
        post.setUpdatedAt(java.time.Instant.now());
        
        return blogPostRepository.save(post);
    }

    @DeleteMapping("/{id}")
    @com.example.anvisos.auth.annotation.RequiredRole(com.example.anvisos.model.enums.UserRole.ADMIN)
    public void deletePost(@PathVariable Long id) {
        blogPostRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPost> getPostById(@PathVariable Long id) {
        return blogPostRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
