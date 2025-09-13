package com.devwonder.blogservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.blogservice.dto.BlogCreateRequest;
import com.devwonder.blogservice.dto.BlogResponse;
import com.devwonder.blogservice.dto.BlogUpdateRequest;
import com.devwonder.blogservice.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blog")
@Tag(name = "Blog Management", description = "Blog management endpoints")
@RequiredArgsConstructor
@Slf4j
public class BlogController {
    
    private final BlogService blogService;

    @GetMapping("/blogs")
    @Operation(
        summary = "Get All Blogs",
        description = "Retrieve all blogs for administrative purposes. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All blogs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<BlogResponse>>> getAllBlogs(
            @RequestParam(required = false) String fields) {

        log.info("Requesting all blogs by ADMIN user - fields: {}", fields);

        List<BlogResponse> blogs = blogService.getAllBlogs(fields);

        log.info("Retrieved {} blogs for ADMIN user", blogs.size());

        return ResponseEntity.ok(BaseResponse.success("All blogs retrieved successfully", blogs));
    }

    @GetMapping("/blogs/showhomepageandlimit6")
    @Operation(
        summary = "Get Homepage Blogs",
        description = "Retrieve 6 blogs to display on homepage with show_on_homepage=true. Public access - no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blogs retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<BlogResponse>>> getHomepageBlogs(
            @RequestParam(required = false) String fields) {
        
        log.info("Requesting homepage blogs - fields: {}", fields);
        
        List<BlogResponse> blogs = blogService.getHomepageBlogs(fields, 6);
        
        log.info("Retrieved {} homepage blogs", blogs.size());
        
        return ResponseEntity.ok(BaseResponse.success("Blogs retrieved successfully", blogs));
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Blog Details",
        description = "Retrieve detailed information about a specific blog by ID. Public access - no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blog details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Blog not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<BlogResponse>> getBlogById(@PathVariable Long id) {
        
        log.info("Requesting blog details for ID: {}", id);
        
        BlogResponse blog = blogService.getBlogById(id);
        
        log.info("Retrieved blog details for ID: {}", id);
        
        return ResponseEntity.ok(BaseResponse.success("Blog details retrieved successfully", blog));
    }
    
    @PostMapping("/blogs")
    @Operation(
        summary = "Create New Blog",
        description = "Create a new blog post. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Blog created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid blog data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<BlogResponse>> createBlog(@Valid @RequestBody BlogCreateRequest request) {
        
        log.info("Creating new blog with title: {} by ADMIN user", request.getTitle());
        
        BlogResponse blog = blogService.createBlog(request);
        
        log.info("Successfully created blog with ID: {} and title: {}", blog.getId(), blog.getTitle());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Blog created successfully", blog));
    }
    
    @PatchMapping("/{id}")
    @Operation(
        summary = "Update Blog",
        description = "Update an existing blog by ID. Only provided fields will be updated (PATCH behavior). Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blog updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid blog data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Blog or Category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<BlogResponse>> updateBlog(
            @PathVariable Long id, 
            @Valid @RequestBody BlogUpdateRequest request) {
        
        log.info("Updating blog with ID: {} by ADMIN user", id);
        
        BlogResponse blog = blogService.updateBlog(id, request);
        
        log.info("Successfully updated blog with ID: {} and title: {}", blog.getId(), blog.getTitle());
        
        return ResponseEntity.ok(BaseResponse.success("Blog updated successfully", blog));
    }
}