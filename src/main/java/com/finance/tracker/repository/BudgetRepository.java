
package com.finance.tracker.repository;

import com.finance.tracker.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends MongoRepository<Budget, String> {

    List<Budget> findByUserIdAndMonthOrderByCategoryAsc(
            String userId,
            String month
    );

    Optional<Budget> findByIdAndUserId(String id, String userId);

    Optional<Budget> findByUserIdAndCategoryAndMonth(
            String userId,
            String category,
            String month
    );
}
