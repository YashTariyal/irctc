package com.irctc_backend.irctc.config;

import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private CoachRepository coachRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no data exists
        if (userRepository.count() == 0) {
            initializeData();
        }
    }
    
    private void initializeData() {
        // Create Stations
        Station newDelhi = createStation("NDLS", "New Delhi", Station.StationType.JUNCTION, "Delhi", "Delhi", "Northern");
        Station mumbai = createStation("BCT", "Mumbai Central", Station.StationType.TERMINAL, "Mumbai", "Maharashtra", "Western");
        Station bangalore = createStation("SBC", "Bangalore City", Station.StationType.JUNCTION, "Bangalore", "Karnataka", "South Western");
        Station chennai = createStation("MAS", "Chennai Central", Station.StationType.TERMINAL, "Chennai", "Tamil Nadu", "Southern");
        Station kolkata = createStation("HWH", "Howrah Junction", Station.StationType.JUNCTION, "Kolkata", "West Bengal", "Eastern");
        
        // Create Users
        User admin = createUser("admin", "admin@irctc.com", "admin123", "Admin", "User", "9876543210", User.UserRole.ADMIN);
        User user1 = createUser("john_doe", "john@example.com", "password123", "John", "Doe", "9876543211", User.UserRole.USER);
        User user2 = createUser("jane_smith", "jane@example.com", "password123", "Jane", "Smith", "9876543212", User.UserRole.USER);
        
        // Create Trains
        Train rajdhani = createTrain("12345", "Rajdhani Express", newDelhi, mumbai, 
            LocalTime.of(16, 0), LocalTime.of(8, 30), Train.TrainType.RAJDHANI);
        
        Train shatabdi = createTrain("12019", "Shatabdi Express", newDelhi, bangalore, 
            LocalTime.of(6, 0), LocalTime.of(23, 0), Train.TrainType.SHATABDI);
        
        Train duronto = createTrain("12213", "Duronto Express", mumbai, chennai, 
            LocalTime.of(23, 0), LocalTime.of(20, 0), Train.TrainType.DURONTO);
        
        // Create Coaches for Rajdhani
        Coach ac1 = createCoach(rajdhani, "A1", Coach.CoachType.AC_FIRST_CLASS, 20, 20, 
            new BigDecimal("5000"), new BigDecimal("5000"), new BigDecimal("5000"), new BigDecimal("1000"));
        
        Coach ac2 = createCoach(rajdhani, "B1", Coach.CoachType.AC_2_TIER, 50, 50, 
            new BigDecimal("2500"), new BigDecimal("2500"), new BigDecimal("2500"), new BigDecimal("500"));
        
        Coach sleeper = createCoach(rajdhani, "S1", Coach.CoachType.SLEEPER_CLASS, 72, 72, 
            new BigDecimal("800"), new BigDecimal("800"), new BigDecimal("800"), new BigDecimal("200"));
        
        // Create Seats for AC1
        createSeatsForCoach(ac1, 20, Seat.SeatType.WINDOW, Seat.BerthType.LOWER);
        
        // Create Seats for AC2
        createSeatsForCoach(ac2, 50, Seat.SeatType.WINDOW, Seat.BerthType.LOWER);
        
        // Create Seats for Sleeper
        createSeatsForCoach(sleeper, 72, Seat.SeatType.WINDOW, Seat.BerthType.LOWER);
        
        System.out.println("Sample data initialized successfully!");
    }
    
    private Station createStation(String code, String name, Station.StationType type, String city, String state, String zone) {
        Station station = new Station();
        station.setStationCode(code);
        station.setStationName(name);
        station.setStationType(type);
        station.setCity(city);
        station.setState(state);
        station.setZone(zone);
        station.setPlatformCount(10);
        station.setIsActive(true);
        station.setCreatedAt(LocalDateTime.now());
        station.setUpdatedAt(LocalDateTime.now());
        return stationRepository.save(station);
    }
    
    private User createUser(String username, String email, String password, String firstName, String lastName, 
                           String phoneNumber, User.UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setGender(User.Gender.MALE);
        user.setIdProofType(User.IdProofType.AADHAR);
        user.setIdProofNumber("123456789012");
        user.setIsVerified(true);
        user.setIsActive(true);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    private Train createTrain(String trainNumber, String trainName, Station source, Station destination, 
                             LocalTime departure, LocalTime arrival, Train.TrainType type) {
        Train train = new Train();
        train.setTrainNumber(trainNumber);
        train.setTrainName(trainName);
        train.setSourceStation(source);
        train.setDestinationStation(destination);
        train.setDepartureTime(departure);
        train.setArrivalTime(arrival);
        train.setJourneyDuration(calculateDuration(departure, arrival));
        train.setTotalDistance(1500.0); // Approximate distance
        train.setTrainType(type);
        train.setStatus(Train.TrainStatus.ACTIVE);
        train.setIsRunning(true);
        train.setCreatedAt(LocalDateTime.now());
        train.setUpdatedAt(LocalDateTime.now());
        return trainRepository.save(train);
    }
    
    private Coach createCoach(Train train, String coachNumber, Coach.CoachType type, int totalSeats, int availableSeats,
                             BigDecimal baseFare, BigDecimal acFare, BigDecimal sleeperFare, BigDecimal tatkalFare) {
        Coach coach = new Coach();
        coach.setTrain(train);
        coach.setCoachNumber(coachNumber);
        coach.setCoachType(type);
        coach.setTotalSeats(totalSeats);
        coach.setAvailableSeats(availableSeats);
        coach.setBaseFare(baseFare);
        coach.setAcFare(acFare);
        coach.setSleeperFare(sleeperFare);
        coach.setTatkalFare(tatkalFare);
        coach.setLadiesQuota(5);
        coach.setSeniorCitizenQuota(3);
        coach.setIsActive(true);
        coach.setCreatedAt(LocalDateTime.now());
        coach.setUpdatedAt(LocalDateTime.now());
        return coachRepository.save(coach);
    }
    
    private void createSeatsForCoach(Coach coach, int seatCount, Seat.SeatType seatType, Seat.BerthType berthType) {
        for (int i = 1; i <= seatCount; i++) {
            Seat seat = new Seat();
            seat.setCoach(coach);
            seat.setSeatNumber(String.valueOf(i));
            seat.setBerthNumber(String.valueOf(i));
            seat.setSeatType(seatType);
            seat.setBerthType(berthType);
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setIsLadiesQuota(false);
            seat.setIsSeniorCitizenQuota(false);
            seat.setIsHandicappedFriendly(false);
            seat.setCreatedAt(LocalDateTime.now());
            seat.setUpdatedAt(LocalDateTime.now());
            seatRepository.save(seat);
        }
    }
    
    private Integer calculateDuration(LocalTime departure, LocalTime arrival) {
        int departureMinutes = departure.getHour() * 60 + departure.getMinute();
        int arrivalMinutes = arrival.getHour() * 60 + arrival.getMinute();
        
        int duration = arrivalMinutes - departureMinutes;
        if (duration < 0) {
            duration += 24 * 60; // Add 24 hours if arrival is next day
        }
        
        return duration;
    }
} 