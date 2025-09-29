package com.devwonder.reportservice.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceMock {

    public Object getDashboardData() {
        Map<String, Object> response = new HashMap<>();

        // KPI Metrics
        Map<String, Object> kpiMetrics = new HashMap<>();

        Map<String, Object> todayRevenue = new HashMap<>();
        todayRevenue.put("value", 5420000L);
        todayRevenue.put("growth", 12.5);
        todayRevenue.put("comparison", "so với hôm qua");
        kpiMetrics.put("todayRevenue", todayRevenue);

        Map<String, Object> completedOrders = new HashMap<>();
        completedOrders.put("value", 28L);
        completedOrders.put("total", 35L);
        completedOrders.put("label", "tổng đơn hôm nay");
        kpiMetrics.put("completedOrders", completedOrders);

        Map<String, Object> monthAgents = new HashMap<>();
        monthAgents.put("value", 523L);
        monthAgents.put("growth", 8.7);
        monthAgents.put("comparison", "so với tháng trước");
        kpiMetrics.put("monthAgents", monthAgents);

        Map<String, Object> lowStockProducts = new HashMap<>();
        lowStockProducts.put("value", 3L);
        lowStockProducts.put("total", 12L);
        lowStockProducts.put("label", "tổng sản phẩm");
        kpiMetrics.put("lowStockProducts", lowStockProducts);

        response.put("kpiMetrics", kpiMetrics);

        // Inventory Alerts
        Map<String, Object> inventoryAlerts = new HashMap<>();
        inventoryAlerts.put("lowStockCount", 3);
        inventoryAlerts.put("overstockCount", 2);
        inventoryAlerts.put("urgentProduct", "Tai nghe SCS Pro Max");
        response.put("inventoryAlerts", inventoryAlerts);

        // Top Performers
        Map<String, Object> topPerformers = new HashMap<>();

        Map<String, Object> topAgent = new HashMap<>();
        topAgent.put("name", "Nguyễn Văn An");
        topAgent.put("totalSpent", 45800000L);
        topAgent.put("totalOrders", 28);
        topPerformers.put("topAgent", topAgent);

        Map<String, Object> topProduct = new HashMap<>();
        topProduct.put("name", "Tai nghe SCS Sport");
        topProduct.put("soldQuantity", 203);
        topProduct.put("growth", 25.3);
        topPerformers.put("topProduct", topProduct);

        Map<String, Object> revenueHighlight = new HashMap<>();
        revenueHighlight.put("value", 5420000L);
        revenueHighlight.put("growth", 12.5);
        topPerformers.put("todayRevenueHighlight", revenueHighlight);

        response.put("topPerformers", topPerformers);

        // Charts Data
        Map<String, Object> chartsData = new HashMap<>();

        Object[] revenueComparison = {
            createRevenueComparison("Hôm qua", 4830000L, "Hôm qua"),
            createRevenueComparison("Hôm nay", 5420000L, "Hôm nay"),
            createRevenueComparison("Tuần trước", 32400000L, "Tuần trước"),
            createRevenueComparison("Tuần này", 36800000L, "Tuần này"),
            createRevenueComparison("Tháng trước", 124500000L, "Tháng trước"),
            createRevenueComparison("Tháng này", 143200000L, "Tháng này")
        };
        chartsData.put("revenueComparison", Arrays.asList(revenueComparison));

        Object[] revenueGrowth = {
            createRevenueGrowth("Ngày", 12.5, "Hôm nay vs Hôm qua"),
            createRevenueGrowth("Tuần", 13.6, "Tuần này vs Tuần trước"),
            createRevenueGrowth("Tháng", 15.2, "Tháng này vs Tháng trước"),
            createRevenueGrowth("Năm", 18.7, "Năm nay vs Năm trước")
        };
        chartsData.put("revenueGrowth", Arrays.asList(revenueGrowth));

        response.put("chartsData", chartsData);

        // Top Lists
        Map<String, Object> topLists = new HashMap<>();

        Object[] agents = {
            createTopAgent(1, "Nguyễn Văn An", 45800000L),
            createTopAgent(2, "Trần Thị Bích", 38200000L),
            createTopAgent(3, "Lê Hoàng Minh", 32500000L)
        };
        topLists.put("agents", Arrays.asList(agents));

        Object[] products = {
            createTopProduct(1, "Tai nghe SCS Sport", 203, 365400000L, 25.3),
            createTopProduct(2, "Tai nghe SCS Pro Max", 156, 546000000L, 18.7),
            createTopProduct(3, "Tai nghe SCS Wireless", 134, 375200000L, 12.4),
            createTopProduct(4, "Tai nghe SCS Premium", 98, 539000000L, 31.2),
            createTopProduct(5, "Tai nghe SCS Gaming", 89, 195800000L, -5.1)
        };
        topLists.put("products", Arrays.asList(products));

        response.put("topLists", topLists);

        // Metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
        metadata.put("cacheExpiry", 300);
        metadata.put("dataSource", "real_time");
        response.put("metadata", metadata);

        return response;
    }

    private Map<String, Object> createRevenueComparison(String period, Long current, String label) {
        Map<String, Object> rc = new HashMap<>();
        rc.put("period", period);
        rc.put("current", current);
        rc.put("label", label);
        return rc;
    }

    private Map<String, Object> createRevenueGrowth(String period, Double growth, String label) {
        Map<String, Object> rg = new HashMap<>();
        rg.put("period", period);
        rg.put("growth", growth);
        rg.put("label", label);
        return rg;
    }

    private Map<String, Object> createTopAgent(Integer rank, String name, Long totalSpent) {
        Map<String, Object> agent = new HashMap<>();
        agent.put("rank", rank);
        agent.put("name", name);
        agent.put("totalSpent", totalSpent);
        return agent;
    }

    private Map<String, Object> createTopProduct(Integer rank, String name, Integer soldQuantity, Long revenue, Double growth) {
        Map<String, Object> product = new HashMap<>();
        product.put("rank", rank);
        product.put("name", name);
        product.put("soldQuantity", soldQuantity);
        product.put("revenue", revenue);
        product.put("growth", growth);
        return product;
    }
}