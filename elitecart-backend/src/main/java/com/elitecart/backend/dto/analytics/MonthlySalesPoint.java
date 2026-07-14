package com.elitecart.backend.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesPoint {
    /** e.g. "2026-06" */
    private String month;
    private BigDecimal revenue;
    private long orderCount;
}
