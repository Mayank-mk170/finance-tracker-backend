
package com.finance.tracker.controller;

import com.finance.tracker.dto.MonthlySummary;
import com.finance.tracker.services.BudgetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/summary")
    public MonthlySummary getMonthlySpendingSummary(
            @RequestParam String month) {

        return budgetService.getMonthlySpendingSummary(month);
    }
}
