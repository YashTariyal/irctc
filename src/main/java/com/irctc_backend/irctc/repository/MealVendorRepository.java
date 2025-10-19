package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.MealVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealVendorRepository extends JpaRepository<MealVendor, Long> {

    List<MealVendor> findByIsActiveTrue();

    List<MealVendor> findByStationCodeAndIsActiveTrue(String stationCode);

    Optional<MealVendor> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT v FROM MealVendor v WHERE v.stationCode = :stationCode AND v.isActive = true ORDER BY v.rating DESC")
    List<MealVendor> findByStationCodeOrderByRatingDesc(@Param("stationCode") String stationCode);

    @Query("SELECT DISTINCT v.stationCode FROM MealVendor v WHERE v.isActive = true")
    List<String> findDistinctStationCodes();

    @Query("SELECT v FROM MealVendor v WHERE v.isActive = true AND v.rating >= :minRating ORDER BY v.rating DESC")
    List<MealVendor> findByRatingGreaterThanEqualOrderByRatingDesc(@Param("minRating") Double minRating);
}
