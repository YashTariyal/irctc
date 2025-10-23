package com.irctc.booking.repository;

import com.irctc.booking.entity.SimpleBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimpleBookingRepository extends JpaRepository<SimpleBooking, Long> {
    Optional<SimpleBooking> findByPnrNumber(String pnrNumber);
    List<SimpleBooking> findByUserId(Long userId);
}
