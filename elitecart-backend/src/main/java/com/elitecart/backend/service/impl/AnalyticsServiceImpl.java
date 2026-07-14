package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.analytics.CategorySalesPoint;
import com.elitecart.backend.dto.analytics.DashboardStatsResponse;
import com.elitecart.backend.dto.analytics.MonthlySalesPoint;
import com.elitecart.backend.dto.order.OrderResponse;
import com.elitecart.backend.dto.product.ProductResponse;
import com.elitecart.backend.entity.Order;
import com.elitecart.backend.entity.RoleName;
import com.elitecart.backend.mapper.OrderMapper;
import com.elitecart.backend.mapper.ProductMapper;
import com.elitecart.backend.repository.OrderRepository;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.repository.UserRepository;
import com.elitecart.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates data across Orders, Products and Users into the admin dashboard
 * payload: headline stats, latest orders, top products, a 12-month revenue
 * trend (for the Recharts line/bar chart), and revenue-by-category (for the
 * Recharts pie/bar chart). Also produces a plain-text CSV export of every
 * order for the "Export CSV" requirement.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        BigDecimal totalRevenue = orderRepository.sumRevenue();
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalCustomers = userRepository.countByRolesName(RoleName.ROLE_CUSTOMER);

        List<OrderResponse> latestOrders = orderRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());

        Pageable top5 = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "unitsSold"));
        List<ProductResponse> topProducts = productRepository.findAll(top5).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        List<MonthlySalesPoint> monthlySales = buildMonthlySales();
        List<CategorySalesPoint> categorySales = buildCategorySales();

        return DashboardStatsResponse.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalCustomers(totalCustomers)
                .latestOrders(latestOrders)
                .topProducts(topProducts)
                .monthlySales(monthlySales)
                .categorySales(categorySales)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportOrdersCsv() {
        StringBuilder csv = new StringBuilder("Order Number,Date,Customer Email,Status,Grand Total\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Order order : orderRepository.findAll()) {
            csv.append(escapeCsv(order.getOrderNumber())).append(",")
                    .append(order.getCreatedAt().format(formatter)).append(",")
                    .append(escapeCsv(order.getUser().getEmail())).append(",")
                    .append(order.getStatus()).append(",")
                    .append(order.getGrandTotal())
                    .append("\n");
        }
        return csv.toString();
    }

    private List<MonthlySalesPoint> buildMonthlySales() {
        LocalDateTime from = LocalDateTime.now().minusMonths(11).withDayOfMonth(1).toLocalDate().atStartOfDay();
        List<Object[]> rows = orderRepository.findMonthlySales(from);

        return rows.stream()
                .map(row -> MonthlySalesPoint.builder()
                        .month((String) row[0])
                        .revenue(new BigDecimal(row[1].toString()))
                        .orderCount(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CategorySalesPoint> buildCategorySales() {
        List<Object[]> rows = orderRepository.findCategorySales();
        return rows.stream()
                .map(row -> CategorySalesPoint.builder()
                        .categoryName((String) row[0])
                        .revenue(new BigDecimal(row[1].toString()))
                        .build())
                .collect(Collectors.toList());
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
