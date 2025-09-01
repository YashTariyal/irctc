package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.entity.Passenger;
import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.repository.PassengerRepository;
import com.irctc_backend.irctc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PassengerService {
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @CacheEvict(value = {"passengers", "user-sessions"}, allEntries = true)
    public Passenger createPassenger(Passenger passenger) {
        // Validate user exists
        User user = userRepository.findById(passenger.getUser().getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        passenger.setUser(user);
        passenger.setCreatedAt(LocalDateTime.now());
        passenger.setUpdatedAt(LocalDateTime.now());
        
        return passengerRepository.save(passenger);
    }
    
    @Cacheable(value = "passengers", key = "#id")
    public Optional<Passenger> findById(Long id) {
        return passengerRepository.findById(id);
    }
    
    @Cacheable(value = "passengers", key = "'all-passengers'")
    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }
    
    @Cacheable(value = "passengers", key = "'user-' + #userId")
    public List<Passenger> getPassengersByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return passengerRepository.findByUser(user);
    }
    
    @CacheEvict(value = {"passengers", "user-sessions"}, allEntries = true)
    public Passenger updatePassenger(Passenger passenger) {
        Passenger existingPassenger = passengerRepository.findById(passenger.getId())
            .orElseThrow(() -> new RuntimeException("Passenger not found"));
        
        // Update fields
        existingPassenger.setFirstName(passenger.getFirstName());
        existingPassenger.setLastName(passenger.getLastName());
        existingPassenger.setAge(passenger.getAge());
        existingPassenger.setGender(passenger.getGender());
        existingPassenger.setPassengerType(passenger.getPassengerType());
        existingPassenger.setIdProofType(passenger.getIdProofType());
        existingPassenger.setIdProofNumber(passenger.getIdProofNumber());
        existingPassenger.setIsSeniorCitizen(passenger.getIsSeniorCitizen());
        existingPassenger.setIsLadiesQuota(passenger.getIsLadiesQuota());
        existingPassenger.setIsHandicapped(passenger.getIsHandicapped());
        existingPassenger.setUpdatedAt(LocalDateTime.now());
        
        return passengerRepository.save(existingPassenger);
    }
    
    @CacheEvict(value = {"passengers", "user-sessions"}, allEntries = true)
    public void deletePassenger(Long id) {
        passengerRepository.deleteById(id);
    }
} 