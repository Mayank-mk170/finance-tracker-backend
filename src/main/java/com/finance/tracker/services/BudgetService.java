
package com.finance.tracker.services;

import com.finance.tracker.dto.MonthlySummary;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.repository.TransactionRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetService {

    private final TransactionRepository transactionRepository;

    public BudgetService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // Get logged-in user's ID
    private String getLoggedInUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    // Get monthly financial summary
    public MonthlySummary getMonthlySpendingSummary(String month) {

        // Validate month format
        YearMonth yearMonth;

        try {
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Invalid month format. Use YYYY-MM, for example 2026-10."
            );
        }

        String userId = getLoggedInUserId();

        // Fetch all transactions for the logged-in user
        List<Transaction> allTransactions =
                transactionRepository.findByUserIdOrderByDateDesc(userId);

        // Filter transactions for the selected month
        List<Transaction> monthlyTransactions = new ArrayList<>();

        for (Transaction transaction : allTransactions) {

            LocalDate transactionDate = transaction.getDate();

            if (transactionDate == null) {
                continue;
            }

            if (YearMonth.from(transactionDate).equals(yearMonth)) {
                monthlyTransactions.add(transaction);
            }
        }

        // Calculate monthly income and expenses
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : monthlyTransactions) {

            BigDecimal amount = transaction.getAmount();

            if (amount == null || transaction.getType() == null) {
                continue;
            }

            String type = transaction.getType().trim();

            if ("INCOME".equalsIgnoreCase(type)) {
                totalIncome = totalIncome.add(amount);

            } else if ("EXPENSE".equalsIgnoreCase(type)) {
                totalExpenses = totalExpenses.add(amount);
            }
        }

        // Calculate remaining balance
        BigDecimal balance = totalIncome.subtract(totalExpenses);

        return new MonthlySummary(
                yearMonth.toString(),
                totalIncome,
                totalExpenses,
                balance
        );
    }
}
