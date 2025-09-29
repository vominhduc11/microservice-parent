package com.devwonder.productservice.service;

import com.devwonder.productservice.repository.ProductRepository;
import com.devwonder.productservice.repository.ProductSerialRepository;
import com.devwonder.productservice.enums.ProductSerialStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDashboardService {

    private final ProductRepository productRepository;
    private final ProductSerialRepository productSerialRepository;

    public InventoryAlertsDto getInventoryAlerts() {
        int lowStockThreshold = 10;
        int overstockThreshold = 100;

        List<Object[]> stockCounts = productSerialRepository.getProductStockCounts();

        int lowStockCount = 0;
        int overstockCount = 0;
        String urgentProduct = null;
        int lowestStock = Integer.MAX_VALUE;

        for (Object[] row : stockCounts) {
            Long productId = (Long) row[0];
            String productName = (String) row[1];
            Long inStockCountLong = (Long) row[2];
            int inStockCount = inStockCountLong.intValue();

            if (inStockCount < lowStockThreshold) {
                lowStockCount++;
                if (inStockCount < lowestStock) {
                    lowestStock = inStockCount;
                    urgentProduct = productName;
                }
            }

            if (inStockCount > overstockThreshold) {
                overstockCount++;
            }
        }

        InventoryAlertsDto alerts = new InventoryAlertsDto();
        alerts.lowStockCount = lowStockCount;
        alerts.overstockCount = overstockCount;
        alerts.urgentProduct = urgentProduct != null ? urgentProduct : "Tai nghe SCS Pro Max";

        return alerts;
    }

    public List<ProductStockDto> getLowStockProducts(int threshold) {
        List<Object[]> stockData = productSerialRepository.getProductStockCounts();
        List<ProductStockDto> lowStockProducts = new ArrayList<>();

        for (Object[] row : stockData) {
            Long productId = (Long) row[0];
            String productName = (String) row[1];
            Long inStockCountLong = (Long) row[2];
            int inStockCount = inStockCountLong.intValue();

            if (inStockCount < threshold) {
                ProductStockDto product = new ProductStockDto();
                product.productId = productId;
                product.productName = productName;
                product.inStockCount = inStockCount;

                // Get other counts
                int allocatedCount = productSerialRepository.countByProductIdAndStatus(productId, ProductSerialStatus.ALLOCATED_TO_DEALER);
                int soldCount = productSerialRepository.countByProductIdAndStatus(productId, ProductSerialStatus.SOLD_TO_CUSTOMER);

                product.allocatedCount = allocatedCount;
                product.soldCount = soldCount;

                lowStockProducts.add(product);
            }
        }

        return lowStockProducts;
    }

    public Map<String, Integer> getProductCounts() {
        Map<String, Integer> counts = new HashMap<>();

        int totalProducts = (int) productRepository.count();
        int lowStockProducts = getLowStockProducts(10).size();
        int inStockProducts = productSerialRepository.countByStatus(ProductSerialStatus.IN_STOCK);
        int allocatedProducts = productSerialRepository.countByStatus(ProductSerialStatus.ALLOCATED_TO_DEALER);

        counts.put("total_products", totalProducts);
        counts.put("low_stock", lowStockProducts);
        counts.put("in_stock_items", inStockProducts);
        counts.put("allocated_items", allocatedProducts);

        return counts;
    }

    public String getUrgentProduct() {
        List<Object[]> stockCounts = productSerialRepository.getProductStockCounts();

        String urgentProduct = "Tai nghe SCS Pro Max"; // Default
        int lowestStock = Integer.MAX_VALUE;

        for (Object[] row : stockCounts) {
            String productName = (String) row[1];
            Long inStockCountLong = (Long) row[2];
            int inStockCount = inStockCountLong.intValue();

            if (inStockCount < lowestStock) {
                lowestStock = inStockCount;
                urgentProduct = productName;
            }
        }

        return urgentProduct;
    }
}