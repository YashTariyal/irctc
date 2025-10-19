package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    List<MealItem> findByVendorIdAndIsAvailableTrue(Long vendorId);

    List<MealItem> findByVendorIdAndCategoryAndIsAvailableTrue(Long vendorId, MealItem.MealCategory category);

    List<MealItem> findByVendorIdAndMealTypeAndIsAvailableTrue(Long vendorId, MealItem.MealType mealType);

    List<MealItem> findByVendorIdAndIsVegetarianAndIsAvailableTrue(Long vendorId, Boolean isVegetarian);

    Optional<MealItem> findByIdAndIsAvailableTrue(Long id);

    @Query("SELECT m FROM MealItem m WHERE m.vendor.id = :vendorId AND m.isAvailable = true ORDER BY m.category, m.itemName")
    List<MealItem> findByVendorIdOrderByCategoryAndItemName(@Param("vendorId") Long vendorId);

    @Query("SELECT DISTINCT m.category FROM MealItem m WHERE m.vendor.id = :vendorId AND m.isAvailable = true")
    List<MealItem.MealCategory> findDistinctCategoriesByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT DISTINCT m.mealType FROM MealItem m WHERE m.vendor.id = :vendorId AND m.isAvailable = true")
    List<MealItem.MealType> findDistinctMealTypesByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT m FROM MealItem m WHERE m.vendor.stationCode = :stationCode AND m.isAvailable = true ORDER BY m.vendor.rating DESC, m.price ASC")
    List<MealItem> findByStationCodeOrderByVendorRatingAndPrice(@Param("stationCode") String stationCode);
}
