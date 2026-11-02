
package com.finance.tracker.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;

    private BigDecimal amount;

    private String type; // INCOME or EXPENSE

    private String category;

    private LocalDate date;

    private String description;

    private LocalDateTime createdAt;
    private String username;

    public Transaction() {
    }
}
