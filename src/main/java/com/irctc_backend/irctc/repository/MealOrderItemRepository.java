package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.MealOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealOrderItemRepository extends JpaRepository<MealOrderItem, Long> {

    List<MealOrderItem> findByOrderId(Long orderId);

    List<MealOrderItem> findByMealItemId(Long mealItemId);
}
