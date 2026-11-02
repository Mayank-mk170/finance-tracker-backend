
package com.finance.tracker.controller;

import com.finance.tracker.dto.CategorySummary;
import com.finance.tracker.dto.MonthlySummary;
import com.finance.tracker.dto.TransactionRequest;
import com.finance.tracker.dto.TransactionSummary;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.services.TransactionService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Create a transaction
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction createTransaction(
            @Valid @RequestBody TransactionRequest request) {

        validateType(request.getType());

        Transaction transaction = new Transaction();
        transaction.setTitle(request.getTitle().trim());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType().trim().toUpperCase());
        transaction.setCategory(request.getCategory().trim());
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());

        return transactionService.createTransaction(transaction);
    }

    // Get all transactions
    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    // Get transactions by type
    @GetMapping("/type/{type}")
    public List<Transaction> getTransactionsByType(
            @PathVariable String type) {

        validateType(type);
        return transactionService.getTransactionsByType(
                type.trim().toUpperCase());
    }

    // Get one transaction
    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable String id) {
        return transactionService.getTransactionById(id);
    }

    // Update a transaction
    @PutMapping("/{id}")
    public Transaction updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request) {

        validateType(request.getType());

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setTitle(request.getTitle().trim());
        updatedTransaction.setAmount(request.getAmount());
        updatedTransaction.setType(request.getType().trim().toUpperCase());
        updatedTransaction.setCategory(request.getCategory().trim());
        updatedTransaction.setDate(request.getDate());
        updatedTransaction.setDescription(request.getDescription());

        return transactionService.updateTransaction(id, updatedTransaction);
    }

    // Delete a transaction
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
    }

    // Overall income, expenses, and balance
    @GetMapping("/summary")
    public TransactionSummary getTransactionSummary() {
        return transactionService.getTransactionSummary();
    }

    // Filter transactions by date range
    @GetMapping("/date-range")
    public List<Transaction> getTransactionsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return transactionService.getTransactionsByDateRange(
                startDate, endDate);
    }

    // Filter transactions by type and date range
    @GetMapping("/filter")
    public List<Transaction> getTransactionsByTypeAndDateRange(
            @RequestParam String type,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return transactionService.getTransactionsByTypeAndDateRange(
                type, startDate, endDate);
    }

    // Paginated transactions
    @GetMapping("/page")
    public Page<Transaction> getTransactionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return transactionService.getTransactionsPaginated(page, size);
    }

    // Monthly summary
    @GetMapping("/monthly-summary")
    public MonthlySummary getMonthlySummary(@RequestParam String month) {
        return transactionService.getMonthlySummary(parseMonth(month));
    }

    // Expense summary by category across all dates
    @GetMapping("/category-summary")
    public List<CategorySummary> getExpenseSummaryByCategory() {
        return transactionService.getExpenseSummaryByCategory();
    }

    // Expense summary by category for a selected month
    @GetMapping("/category-summary/monthly")
    public List<CategorySummary> getMonthlyExpenseSummaryByCategory(
            @RequestParam String month) {

        return transactionService
                .getMonthlyExpenseSummaryByCategory(parseMonth(month));
    }

    // Monthly income, expenses, and balance trends
    @GetMapping("/monthly-trends")
    public List<MonthlySummary> getMonthlyTrends(
            @RequestParam(defaultValue = "6") int months) {

        return transactionService.getMonthlyTrends(months);
    }

    // Validate transaction type
    private void validateType(String type) {
        if (type == null
                || !(type.trim().equalsIgnoreCase("INCOME")
                || type.trim().equalsIgnoreCase("EXPENSE"))) {
            throw new IllegalArgumentException(
                    "Type must be INCOME or EXPENSE");
        }
    }

    // Parse month in YYYY-MM format
    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "Invalid month format. Use YYYY-MM, for example 2026-10.");
        }
    }
}
