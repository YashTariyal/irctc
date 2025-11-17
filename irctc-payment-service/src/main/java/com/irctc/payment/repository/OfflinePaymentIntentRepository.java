package com.irctc.payment.repository;

import com.irctc.payment.entity.OfflinePaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfflinePaymentIntentRepository extends JpaRepository<OfflinePaymentIntent, Long> {

    List<OfflinePaymentIntent> findByUserIdAndStatusIn(Long userId, List<String> statuses);

    List<OfflinePaymentIntent> findTop50ByStatusInOrderByQueuedAtAsc(List<String> statuses);
}

