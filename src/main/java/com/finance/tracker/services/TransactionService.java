
package com.finance.tracker.services;

import com.finance.tracker.dto.CategorySummary;
import com.finance.tracker.dto.MonthlySummary;
import com.finance.tracker.dto.TransactionSummary;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            UserRepository userRepository) {

        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    // Get logged-in user's ID from JWT authentication
    private String getLoggedInUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    // Create a transaction
    public Transaction createTransaction(Transaction transaction) {

        String userId = getLoggedInUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        transaction.setId(null);
        transaction.setUserId(userId);
        transaction.setUsername(user.getUsername());
        transaction.setCreatedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    // Get all transactions for logged-in user
    public List<Transaction> getAllTransactions() {

        String userId = getLoggedInUserId();

        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    // Get transactions by type
    public List<Transaction> getTransactionsByType(String type) {

        validateType(type);

        String userId = getLoggedInUserId();

        return transactionRepository.findByUserIdAndTypeOrderByDateDesc(
                userId, type.trim().toUpperCase());
    }

    // Get one transaction belonging to logged-in user
    public Transaction getTransactionById(String id) {

        String userId = getLoggedInUserId();

        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Transaction not found"));
    }

    // Update an existing transaction
    public Transaction updateTransaction(
            String id,
            Transaction updatedTransaction) {

        Transaction existingTransaction = getTransactionById(id);

        existingTransaction.setTitle(updatedTransaction.getTitle());
        existingTransaction.setAmount(updatedTransaction.getAmount());
        existingTransaction.setType(updatedTransaction.getType());
        existingTransaction.setCategory(updatedTransaction.getCategory());
        existingTransaction.setDate(updatedTransaction.getDate());
        existingTransaction.setDescription(
                updatedTransaction.getDescription());

        return transactionRepository.save(existingTransaction);
    }

    // Delete a transaction
    public void deleteTransaction(String id) {

        Transaction transaction = getTransactionById(id);

        transactionRepository.delete(transaction);
    }

    // Calculate overall income, expenses, and balance
    public TransactionSummary getTransactionSummary() {

        String userId = getLoggedInUserId();

        List<Transaction> transactions =
                transactionRepository.findByUserIdOrderByDateDesc(userId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {

            BigDecimal amount = transaction.getAmount();

            if (amount == null) {
                continue;
            }

            if (isIncome(transaction)) {
                totalIncome = totalIncome.add(amount);

            } else if (isExpense(transaction)) {
                totalExpenses = totalExpenses.add(amount);
            }
        }

        return new TransactionSummary(
                totalIncome,
                totalExpenses,
                totalIncome.subtract(totalExpenses)
        );
    }

    // Get transactions within a date range
    public List<Transaction> getTransactionsByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        validateDateRange(startDate, endDate);

        String userId = getLoggedInUserId();

        return transactionRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(
                        userId, startDate, endDate);
    }

    // Get transactions by type within a date range
    public List<Transaction> getTransactionsByTypeAndDateRange(
            String type,
            LocalDate startDate,
            LocalDate endDate) {

        validateType(type);
        validateDateRange(startDate, endDate);

        String userId = getLoggedInUserId();

        return transactionRepository
                .findByUserIdAndTypeAndDateBetweenOrderByDateDesc(
                        userId,
                        type.trim().toUpperCase(),
                        startDate,
                        endDate);
    }

    // Get paginated transactions
    public Page<Transaction> getTransactionsPaginated(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        String userId = getLoggedInUserId();

        Pageable pageable = PageRequest.of(page, size);

        return transactionRepository
                .findByUserIdOrderByDateDesc(userId, pageable);
    }

    // Get all transactions for the selected month
    private List<Transaction> getTransactionsForMonth(YearMonth month) {

        if (month == null) {
            throw new IllegalArgumentException(
                    "Month is required in YYYY-MM format");
        }

        String userId = getLoggedInUserId();

        // Fetch all transactions for this user
        List<Transaction> allTransactions =
                transactionRepository.findByUserIdOrderByDateDesc(userId);

        List<Transaction> monthlyTransactions = new ArrayList<>();

        // Filter by selected month in Java
        for (Transaction transaction : allTransactions) {

            LocalDate transactionDate = transaction.getDate();

            if (transactionDate == null) {
                continue;
            }

            if (YearMonth.from(transactionDate).equals(month)) {
                monthlyTransactions.add(transaction);
            }
        }

        return monthlyTransactions;
    }

    // Get monthly income, expenses, and balance
    public MonthlySummary getMonthlySummary(YearMonth month) {

        List<Transaction> transactions = getTransactionsForMonth(month);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {

            BigDecimal amount = transaction.getAmount();

            if (amount == null) {
                continue;
            }

            if (isIncome(transaction)) {
                totalIncome = totalIncome.add(amount);

            } else if (isExpense(transaction)) {
                totalExpenses = totalExpenses.add(amount);
            }
        }

        return new MonthlySummary(
                month.toString(),
                totalIncome,
                totalExpenses,
                totalIncome.subtract(totalExpenses)
        );
    }

    // Get expense totals grouped by category for all dates
    public List<CategorySummary> getExpenseSummaryByCategory() {

        String userId = getLoggedInUserId();

        List<Transaction> transactions =
                transactionRepository
                        .findByUserIdAndTypeOrderByDateDesc(
                                userId, "EXPENSE");

        return groupExpensesByCategory(transactions);
    }

    // Get monthly expense totals grouped by category
    public List<CategorySummary> getMonthlyExpenseSummaryByCategory(
            YearMonth month) {

        List<Transaction> transactions = getTransactionsForMonth(month);

        List<Transaction> monthlyExpenses = new ArrayList<>();

        for (Transaction transaction : transactions) {

            if (isExpense(transaction)) {
                monthlyExpenses.add(transaction);
            }
        }

        return groupExpensesByCategory(monthlyExpenses);
    }

    // Check transaction type
    private boolean isIncome(Transaction transaction) {

        return transaction.getType() != null
                && "INCOME".equalsIgnoreCase(
                transaction.getType().trim());
    }

    private boolean isExpense(Transaction transaction) {

        return transaction.getType() != null
                && "EXPENSE".equalsIgnoreCase(
                transaction.getType().trim());
    }

    // Group expense amounts by category and sort highest to lowest
    private List<CategorySummary> groupExpensesByCategory(
            List<Transaction> transactions) {

        Map<String, BigDecimal> categoryTotals = new HashMap<>();

        for (Transaction transaction : transactions) {

            BigDecimal amount = transaction.getAmount();

            if (amount == null) {
                continue;
            }

            String category = transaction.getCategory();

            if (category == null || category.isBlank()) {
                category = "Uncategorized";
            } else {
                category = category.trim();
            }

            categoryTotals.merge(
                    category,
                    amount,
                    BigDecimal::add
            );
        }

        List<CategorySummary> summaries = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry
                : categoryTotals.entrySet()) {

            summaries.add(new CategorySummary(
                    entry.getKey(),
                    entry.getValue()
            ));
        }

        summaries.sort(
                Comparator.comparing(CategorySummary::getTotalAmount)
                        .reversed()
        );

        return summaries;
    }

    // Validate date range
    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null
                || endDate == null
                || startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "Start date must be on or before end date");
        }
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

    // Get monthly trends for the last N months
    public List<MonthlySummary> getMonthlyTrends(int months) {

        if (months < 1 || months > 24) {
            throw new IllegalArgumentException(
                    "Months must be between 1 and 24");
        }

        YearMonth currentMonth = YearMonth.now();

        List<MonthlySummary> trends = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {

            YearMonth month = currentMonth.minusMonths(i);

            trends.add(getMonthlySummary(month));
        }

        return trends;
    }
}
