package com.devwonder.blogservice.mapper;

import com.devwonder.blogservice.dto.BlogResponse;
import com.devwonder.blogservice.entity.Blog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    
    @Mapping(target = "category", source = "categoryBlog.name")
    BlogResponse toBlogResponse(Blog blog);
    
}