package com.irctc.train.repository;

import com.irctc.train.entity.SimpleTrain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimpleTrainRepository extends JpaRepository<SimpleTrain, Long> {
    Optional<SimpleTrain> findByTrainNumber(String trainNumber);
    List<SimpleTrain> findBySourceStationAndDestinationStation(String source, String destination);
}
