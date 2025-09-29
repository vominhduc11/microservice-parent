package com.devwonder.reportservice.mapper;

import com.devwonder.reportservice.dto.DashboardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DashboardMapper {

    /**
     * Convert Map to DashboardResponse DTO
     */
    default DashboardResponse mapToDashboardResponse(Map<String, Object> dashboardData) {
        if (dashboardData == null) {
            return null;
        }

        DashboardResponse.DashboardResponseBuilder builder = DashboardResponse.builder();

        // Map KPI Metrics
        Object kpiDataObj = dashboardData.get("kpiMetrics");
        if (kpiDataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kpiData = (Map<String, Object>) kpiDataObj;
            builder.kpiMetrics(mapKpiMetrics(kpiData));
        }

        // Map Inventory Alerts
        Object inventoryDataObj = dashboardData.get("inventoryAlerts");
        if (inventoryDataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> inventoryData = (Map<String, Object>) inventoryDataObj;
            builder.inventoryAlerts(mapInventoryAlerts(inventoryData));
        }

        // Map Top Performers
        Object performersDataObj = dashboardData.get("topPerformers");
        if (performersDataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> performersData = (Map<String, Object>) performersDataObj;
            builder.topPerformers(mapTopPerformers(performersData));
        }

        // Map Charts Data
        Object chartsDataObj = dashboardData.get("chartsData");
        if (chartsDataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> chartsData = (Map<String, Object>) chartsDataObj;
            builder.chartsData(mapChartsData(chartsData));
        }

        // Map Top Lists
        Object topListsDataObj = dashboardData.get("topLists");
        if (topListsDataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> topListsData = (Map<String, Object>) topListsDataObj;
            builder.topLists(mapTopLists(topListsData));
        }

        // Map Metadata
        Object metadataDataObj = dashboardData.get("metadata");
        if (metadataDataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataData = (Map<String, Object>) metadataDataObj;
            builder.metadata(mapMetadata(metadataData));
        }

        return builder.build();
    }

    default DashboardResponse.KpiMetrics mapKpiMetrics(Map<String, Object> kpiData) {
        return DashboardResponse.KpiMetrics.builder()
                .todayRevenue(mapRevenueMetric(castToMap(kpiData.get("todayRevenue"))))
                .completedOrders(mapOrderMetric(castToMap(kpiData.get("completedOrders"))))
                .monthDealers(mapDealerMetric(castToMap(kpiData.get("monthDealers"))))
                .lowStockProducts(mapStockMetric(castToMap(kpiData.get("lowStockProducts"))))
                .build();
    }

    default DashboardResponse.RevenueMetric mapRevenueMetric(Map<String, Object> data) {
        if (data == null) return null;
        return DashboardResponse.RevenueMetric.builder()
                .value(getLongValue(data.get("value")))
                .growth(getDoubleValue(data.get("growth")))
                .comparison((String) data.get("comparison"))
                .build();
    }

    default DashboardResponse.OrderMetric mapOrderMetric(Map<String, Object> data) {
        if (data == null) return null;
        return DashboardResponse.OrderMetric.builder()
                .value(getLongValue(data.get("value")))
                .total(getLongValue(data.get("total")))
                .label((String) data.get("label"))
                .build();
    }

    default DashboardResponse.DealerMetric mapDealerMetric(Map<String, Object> data) {
        if (data == null) return null;
        return DashboardResponse.DealerMetric.builder()
                .value(getLongValue(data.get("value")))
                .growth(getDoubleValue(data.get("growth")))
                .comparison((String) data.get("comparison"))
                .build();
    }

    default DashboardResponse.StockMetric mapStockMetric(Map<String, Object> data) {
        if (data == null) return null;
        return DashboardResponse.StockMetric.builder()
                .value(getLongValue(data.get("value")))
                .total(getLongValue(data.get("total")))
                .label((String) data.get("label"))
                .build();
    }

    default DashboardResponse.InventoryAlerts mapInventoryAlerts(Map<String, Object> data) {
        return DashboardResponse.InventoryAlerts.builder()
                .lowStockCount(getIntegerValue(data.get("lowStockCount")))
                .overstockCount(getIntegerValue(data.get("overstockCount")))
                .urgentProduct((String) data.get("urgentProduct"))
                .build();
    }

    default DashboardResponse.TopPerformers mapTopPerformers(Map<String, Object> data) {
        return DashboardResponse.TopPerformers.builder()
                .topDealer(mapTopDealer(castToMap(data.get("topDealer"))))
                .topProduct(mapTopProduct(castToMap(data.get("topProduct"))))
                .todayRevenueHighlight(mapRevenueMetric(castToMap(data.get("todayRevenueHighlight"))))
                .build();
    }

    default DashboardResponse.TopDealer mapTopDealer(Map<String, Object> data) {
        if (data == null) return null;
        return DashboardResponse.TopDealer.builder()
                .name((String) data.get("name"))
                .totalSpent(getLongValue(data.get("totalSpent")))
                .totalOrders(getIntegerValue(data.get("totalOrders")))
                .build();
    }

    default DashboardResponse.TopProduct mapTopProduct(Map<String, Object> data) {
        if (data == null) return null;
        return DashboardResponse.TopProduct.builder()
                .name((String) data.get("name"))
                .soldQuantity(getIntegerValue(data.get("soldQuantity")))
                .growth(getDoubleValue(data.get("growth")))
                .build();
    }

    default DashboardResponse.ChartsData mapChartsData(Map<String, Object> data) {
        return DashboardResponse.ChartsData.builder()
                .revenueComparison(mapObjectArrayToList(data.get("revenueComparison"), this::mapRevenueComparison))
                .revenueGrowth(mapObjectArrayToList(data.get("revenueGrowth"), this::mapRevenueGrowth))
                .build();
    }

    default DashboardResponse.RevenueComparison mapRevenueComparison(Map<String, Object> data) {
        return DashboardResponse.RevenueComparison.builder()
                .period((String) data.get("period"))
                .current(getLongValue(data.get("current")))
                .label((String) data.get("label"))
                .build();
    }

    default DashboardResponse.RevenueGrowth mapRevenueGrowth(Map<String, Object> data) {
        return DashboardResponse.RevenueGrowth.builder()
                .period((String) data.get("period"))
                .growth(getDoubleValue(data.get("growth")))
                .label((String) data.get("label"))
                .build();
    }

    default DashboardResponse.TopLists mapTopLists(Map<String, Object> data) {
        return DashboardResponse.TopLists.builder()
                .dealers(mapObjectArrayToList(data.get("dealers"), this::mapRankedDealer))
                .products(mapObjectArrayToList(data.get("products"), this::mapRankedProduct))
                .build();
    }

    default DashboardResponse.RankedDealer mapRankedDealer(Map<String, Object> data) {
        return DashboardResponse.RankedDealer.builder()
                .rank(getIntegerValue(data.get("rank")))
                .name((String) data.get("name"))
                .totalSpent(getLongValue(data.get("totalSpent")))
                .build();
    }

    default DashboardResponse.RankedProduct mapRankedProduct(Map<String, Object> data) {
        return DashboardResponse.RankedProduct.builder()
                .rank(getIntegerValue(data.get("rank")))
                .name((String) data.get("name"))
                .soldQuantity(getIntegerValue(data.get("soldQuantity")))
                .revenue(getLongValue(data.get("revenue")))
                .growth(getDoubleValue(data.get("growth")))
                .build();
    }

    default DashboardResponse.Metadata mapMetadata(Map<String, Object> data) {
        return DashboardResponse.Metadata.builder()
                .lastUpdated((String) data.get("lastUpdated"))
                .cacheExpiry(getIntegerValue(data.get("cacheExpiry")))
                .dataSource((String) data.get("dataSource"))
                .build();
    }

    // Helper methods for type conversion
    @SuppressWarnings("unchecked")
    default Map<String, Object> castToMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    default <T> java.util.List<T> mapObjectArrayToList(Object arrayObj, java.util.function.Function<Map<String, Object>, T> mapper) {
        if (arrayObj instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> list = (java.util.List<Object>) arrayObj;
            return list.stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> castToMap(item))
                    .filter(map -> map != null)
                    .map(mapper)
                    .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    default Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    default Double getDoubleValue(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }

    default Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }
}