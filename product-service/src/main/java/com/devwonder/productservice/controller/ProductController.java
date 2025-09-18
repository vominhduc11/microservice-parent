package com.devwonder.productservice.controller;

import com.devwonder.common.dto.BaseResponse;
import com.devwonder.productservice.dto.ProductCreateRequest;
import com.devwonder.productservice.dto.ProductResponse;
import com.devwonder.productservice.dto.ProductUpdateRequest;
import com.devwonder.productservice.service.ProductService;
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
@RequestMapping("/product")
@Tag(name = "Product Management", description = "Product management endpoints")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping("/products/showhomepageandlimit4")
    @Operation(
        summary = "Get Homepage Products",
        description = "Retrieve 4 products to display on homepage with show_on_homepage=true"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getHomepageProducts(
            @RequestParam(required = false) String fields) {
        
        log.info("Requesting homepage products - fields: {}", fields);
        
        List<ProductResponse> products = productService.getHomepageProducts(fields, 4);
        
        log.info("Retrieved {} homepage products", products.size());
        
        return ResponseEntity.ok(BaseResponse.success("Products retrieved successfully", products));
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Product Details",
        description = "Retrieve detailed information about a specific product by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        
        log.info("Requesting product details for ID: {}", id);
        
        ProductResponse product = productService.getProductById(id);
        
        log.info("Retrieved product details for ID: {}", id);
        
        return ResponseEntity.ok(BaseResponse.success("Product details retrieved successfully", product));
    }
    
    @GetMapping("/products/featuredandlimit1")
    @Operation(
        summary = "Get Featured Products",
        description = "Retrieve 1 featured product with is_featured=true"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Featured products retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getFeaturedProducts(
            @RequestParam(required = false) String fields) {
        
        log.info("Requesting featured products - fields: {}", fields);
        
        List<ProductResponse> products = productService.getFeaturedProducts(fields, 1);
        
        log.info("Retrieved {} featured products", products.size());
        
        return ResponseEntity.ok(BaseResponse.success("Featured products retrieved successfully", products));
    }

    @GetMapping("/products/related/{productId}")
    @Operation(
        summary = "Get Related Products",
        description = "Retrieve related products for a specific product. Public access - no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Related products retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getRelatedProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "4") int limit,
            @RequestParam(required = false) String fields) {

        log.info("Requesting related products for product ID: {} with limit: {}, fields: {}", productId, limit, fields);

        List<ProductResponse> products = productService.getRelatedProducts(productId, limit, fields);

        log.info("Retrieved {} related products for product ID: {}", products.size(), productId);

        return ResponseEntity.ok(BaseResponse.success("Related products retrieved successfully", products));
    }

    @GetMapping("/products")
    @Operation(
        summary = "Get All Products",
        description = "Retrieve all active products in the catalog (not deleted). Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getAllProducts() {

        log.info("Requesting all active products by ADMIN user");

        List<ProductResponse> products = productService.getAllProducts();

        log.info("Retrieved {} active products", products.size());

        return ResponseEntity.ok(BaseResponse.success("Active products retrieved successfully", products));
    }

    @GetMapping("/products/deleted")
    @Operation(
        summary = "Get All Deleted Products",
        description = "Retrieve all soft deleted products in the catalog. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deleted products retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getDeletedProducts() {

        log.info("Requesting all deleted products by ADMIN user");

        List<ProductResponse> products = productService.getDeletedProducts();

        log.info("Retrieved {} deleted products", products.size());

        return ResponseEntity.ok(BaseResponse.success("Deleted products retrieved successfully", products));
    }
    
    @PostMapping("/products")
    @Operation(
        summary = "Create New Product",
        description = "Create a new product in the catalog. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid product data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "409", description = "Conflict - Product SKU already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        
        log.info("Creating new product with SKU: {} by ADMIN user", request.getSku());
        
        ProductResponse product = productService.createProduct(request);
        
        log.info("Successfully created product with ID: {} and SKU: {}", product.getId(), product.getSku());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Product created successfully", product));
    }
    
    @PatchMapping("/{id}")
    @Operation(
        summary = "Update Product",
        description = "Update an existing product by ID. Only provided fields will be updated (PATCH behavior). Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid product data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "409", description = "Conflict - Product SKU already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductResponse>> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductUpdateRequest request) {
        
        log.info("Updating product with ID: {} by ADMIN user", id);
        
        ProductResponse product = productService.updateProduct(id, request);
        
        log.info("Successfully updated product with ID: {} and SKU: {}", product.getId(), product.getSku());
        
        return ResponseEntity.ok(BaseResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Soft Delete Product",
        description = "Soft delete an existing product by ID (sets isDeleted=true). Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product soft deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<String>> deleteProduct(@PathVariable Long id) {

        log.info("Soft deleting product with ID: {} by ADMIN user", id);

        productService.deleteProduct(id);

        log.info("Successfully soft deleted product with ID: {}", id);

        return ResponseEntity.ok(BaseResponse.success("Product soft deleted successfully", null));
    }

    @DeleteMapping("/{id}/hard")
    @Operation(
        summary = "Hard Delete Product",
        description = "Permanently delete an existing product by ID from database. Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product permanently deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<String>> hardDeleteProduct(@PathVariable Long id) {

        log.info("Hard deleting product with ID: {} by ADMIN user", id);

        productService.hardDeleteProduct(id);

        log.info("Successfully hard deleted product with ID: {}", id);

        return ResponseEntity.ok(BaseResponse.success("Product permanently deleted successfully", null));
    }

    @PatchMapping("/{id}/restore")
    @Operation(
        summary = "Restore Deleted Product",
        description = "Restore a soft deleted product by ID (sets isDeleted=false). Requires ADMIN role authentication via API Gateway.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product restored successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Product is not deleted"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<ProductResponse>> restoreProduct(@PathVariable Long id) {

        log.info("Restoring product with ID: {} by ADMIN user", id);

        ProductResponse product = productService.restoreProduct(id);

        log.info("Successfully restored product with ID: {}", id);

        return ResponseEntity.ok(BaseResponse.success("Product restored successfully", product));
    }
}