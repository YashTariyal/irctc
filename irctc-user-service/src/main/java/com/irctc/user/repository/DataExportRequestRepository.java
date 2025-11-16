package com.irctc.user.repository;

import com.irctc.user.entity.DataExportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DataExportRequestRepository extends JpaRepository<DataExportRequest, Long> {
    Optional<DataExportRequest> findByRequestId(String requestId);
    
    List<DataExportRequest> findByUserId(Long userId);
    
    List<DataExportRequest> findByUserIdAndStatus(Long userId, String status);
    
    @Query("SELECT d FROM DataExportRequest d WHERE d.userId = :userId AND d.status = 'COMPLETED' AND d.expiresAt > :now ORDER BY d.completedAt DESC")
    List<DataExportRequest> findValidExports(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}

