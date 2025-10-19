package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.MealOrder;
import com.irctc_backend.irctc.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealOrderRepository extends JpaRepository<MealOrder, Long> {

    List<MealOrder> findByUserOrderByCreatedAtDesc(User user);

    Page<MealOrder> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<MealOrder> findByUserAndOrderStatus(User user, MealOrder.OrderStatus orderStatus);

    Optional<MealOrder> findByOrderNumber(String orderNumber);

    List<MealOrder> findByVendorIdAndOrderStatus(Long vendorId, MealOrder.OrderStatus orderStatus);

    List<MealOrder> findByDeliveryStationCodeAndOrderStatus(String stationCode, MealOrder.OrderStatus orderStatus);

    @Query("SELECT o FROM MealOrder o WHERE o.deliveryTime BETWEEN :startTime AND :endTime AND o.orderStatus = :status")
    List<MealOrder> findByDeliveryTimeBetweenAndOrderStatus(@Param("startTime") LocalDateTime startTime, 
                                                           @Param("endTime") LocalDateTime endTime, 
                                                           @Param("status") MealOrder.OrderStatus status);

    @Query("SELECT o FROM MealOrder o WHERE o.user = :user AND o.createdAt >= :fromDate ORDER BY o.createdAt DESC")
    List<MealOrder> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(@Param("user") User user, 
                                                                   @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(o) FROM MealOrder o WHERE o.vendor.id = :vendorId AND o.orderStatus = :status")
    Long countByVendorIdAndOrderStatus(@Param("vendorId") Long vendorId, @Param("status") MealOrder.OrderStatus status);
}
