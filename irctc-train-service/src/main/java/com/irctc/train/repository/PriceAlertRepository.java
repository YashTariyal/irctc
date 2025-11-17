package com.irctc.train.repository;

import com.irctc.train.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByUserId(Long userId);
    List<PriceAlert> findByStatus(String status);
    List<PriceAlert> findByStatusAndTravelDateBetween(String status, LocalDate start, LocalDate end);
}

