
package com.finance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CategorySummary {

    private String category;
    private BigDecimal totalAmount;
}
