package com.irctc.payment.repository;

import com.irctc.payment.entity.GatewayStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GatewayStatisticsRepository extends JpaRepository<GatewayStatistics, Long> {
    Optional<GatewayStatistics> findByGatewayName(String gatewayName);
}

