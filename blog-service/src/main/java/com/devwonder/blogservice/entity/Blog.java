package com.devwonder.blogservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@Where(clause = "delete_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String image;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String introduction;
    
    @Column(name = "show_on_homepage")
    private Boolean showOnHomepage = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;
    
    @Column(name = "delete_at")
    private LocalDateTime deleteAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ip_category_blog", nullable = false)
    private CategoryBlog categoryBlog;
}