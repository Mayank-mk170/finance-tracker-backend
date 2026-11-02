
package com.finance.tracker.repository;

import com.finance.tracker.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends MongoRepository<Transaction, String> {


    Optional<Transaction> findByIdAndUserId(
            String id,
            String userId
    );

    List<Transaction> findByUserIdAndTypeOrderByDateDesc(
            String userId,
            String type
    );


    List<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(
            String userId,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    );

    List<Transaction> findByUserIdAndTypeAndDateBetweenOrderByDateDesc(
            String userId,
            String type,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    );



    List<Transaction> findByUserIdOrderByDateDesc(String userId);

    Page<Transaction> findByUserIdOrderByDateDesc(
            String userId,
            Pageable pageable
    );

}
