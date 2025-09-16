package com.devwonder.blogservice.mapper;

import com.devwonder.blogservice.dto.CategoryBlogResponse;
import com.devwonder.blogservice.entity.CategoryBlog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryBlogMapper {

    CategoryBlogResponse toCategoryBlogResponse(CategoryBlog categoryBlog);
}