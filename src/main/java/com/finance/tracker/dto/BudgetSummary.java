
package com.finance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BudgetSummary {

    private String category;
    private String month;
    private BigDecimal limitAmount;
    private BigDecimal amountSpent;
    private BigDecimal remainingAmount;
    private boolean overBudget;
}
