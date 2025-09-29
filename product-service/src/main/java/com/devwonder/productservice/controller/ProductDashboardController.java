package com.devwonder.productservice.controller;

import com.devwonder.productservice.service.ProductDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product-service/dashboard")
@RequiredArgsConstructor
@Slf4j
public class ProductDashboardController {

    private final ProductDashboardService dashboardService;

    @GetMapping("/inventory-alerts")
    public InventoryAlertsDto getInventoryAlerts(@RequestHeader("X-API-Key") String apiKey) {
        log.debug("Getting inventory alerts");
        return dashboardService.getInventoryAlerts();
    }

    @GetMapping("/low-stock-products")
    public List<ProductStockDto> getLowStockProducts(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestParam(defaultValue = "10") int threshold) {
        log.debug("Getting low stock products with threshold: {}", threshold);
        return dashboardService.getLowStockProducts(threshold);
    }

    @GetMapping("/product-counts")
    public Map<String, Integer> getProductCounts(@RequestHeader("X-API-Key") String apiKey) {
        log.debug("Getting product counts");
        return dashboardService.getProductCounts();
    }

    @GetMapping("/urgent-product")
    public String getUrgentProduct(@RequestHeader("X-API-Key") String apiKey) {
        log.debug("Getting urgent product");
        return dashboardService.getUrgentProduct();
    }
}

// DTOs for dashboard responses
class InventoryAlertsDto {
    public Integer lowStockCount;
    public Integer overstockCount;
    public String urgentProduct;
}

class ProductStockDto {
    public Long productId;
    public String productName;
    public Integer inStockCount;
    public Integer allocatedCount;
    public Integer soldCount;
}