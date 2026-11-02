
package com.finance.tracker.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "budgets")
@CompoundIndex(
        name = "user_category_month_unique",
        def = "{'userId': 1, 'category': 1, 'month': 1}",
        unique = true
)
public class Budget {

    @Id
    private String id;

    private String userId;

    private String category;

    private BigDecimal limitAmount;

    private String month; // Format: YYYY-MM

    private LocalDateTime createdAt;

    public Budget() {
    }
}
