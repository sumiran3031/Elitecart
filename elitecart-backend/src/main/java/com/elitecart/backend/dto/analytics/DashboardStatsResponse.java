package com.elitecart.backend.dto.analytics;

import com.elitecart.backend.dto.order.OrderResponse;
import com.elitecart.backend.dto.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalProducts;
    private long totalCustomers;
    private List<OrderResponse> latestOrders;
    private List<ProductResponse> topProducts;
    private List<MonthlySalesPoint> monthlySales;
    private List<CategorySalesPoint> categorySales;
}
