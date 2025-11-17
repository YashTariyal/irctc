package com.irctc.booking.repository;

import com.irctc.booking.entity.OfflineAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OfflineActionRepository extends JpaRepository<OfflineAction, Long> {

    List<OfflineAction> findByUserIdAndStatusIn(Long userId, Collection<String> statuses);

    List<OfflineAction> findTop50ByStatusInOrderByQueuedAtAsc(Collection<String> statuses);
}

