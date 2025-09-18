package com.devwonder.blogservice.repository;

import com.devwonder.blogservice.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    List<Blog> findByShowOnHomepageTrueAndIsDeletedFalse();

    List<Blog> findByIsDeletedFalse();

    List<Blog> findByIsDeletedTrue();

    List<Blog> findByIsDeletedFalseAndIdNot(Long id);

}