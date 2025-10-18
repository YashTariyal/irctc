package com.irctc_backend.irctc.config;

import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Data Initializer for creating basic dummy data for testing
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class SimpleDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleDataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("🚀 Initializing simple dummy data for testing...");
        
        // Only create dummy data if no users exist (except admin)
        if (userRepository.count() <= 1) {
            createSimpleDummyData();
            logger.info("✅ Simple dummy data created successfully!");
        } else {
            logger.info("📊 Dummy data already exists, skipping initialization");
        }
    }
    
    private void createSimpleDummyData() {
        // Create additional stations
        createStations();
        
        // Create additional trains
        createTrains();
        
        // Create additional users
        createUsers();
        
        logger.info("🎉 Simple dummy data initialization completed!");
    }
    
    private void createStations() {
        List<Station> stations = new ArrayList<>();
        
        Station station1 = new Station();
        station1.setStationCode("NDLS");
        station1.setStationName("New Delhi");
        station1.setCity("Delhi");
        station1.setState("Delhi");
        station1.setZone("North");
        station1.setStationType(Station.StationType.TERMINAL);
        station1.setIsActive(true);
        stations.add(station1);
        
        Station station2 = new Station();
        station2.setStationCode("MUMB");
        station2.setStationName("Mumbai Central");
        station2.setCity("Mumbai");
        station2.setState("Maharashtra");
        station2.setZone("West");
        station2.setStationType(Station.StationType.TERMINAL);
        station2.setIsActive(true);
        stations.add(station2);
        
        Station station3 = new Station();
        station3.setStationCode("CHEN");
        station3.setStationName("Chennai Central");
        station3.setCity("Chennai");
        station3.setState("Tamil Nadu");
        station3.setZone("South");
        station3.setStationType(Station.StationType.TERMINAL);
        station3.setIsActive(true);
        stations.add(station3);
        
        Station station4 = new Station();
        station4.setStationCode("KOLK");
        station4.setStationName("Howrah");
        station4.setCity("Kolkata");
        station4.setState("West Bengal");
        station4.setZone("East");
        station4.setStationType(Station.StationType.TERMINAL);
        station4.setIsActive(true);
        stations.add(station4);
        
        stationRepository.saveAll(stations);
        logger.info("📍 Created {} stations", stations.size());
    }
    
    private void createTrains() {
        // Get existing stations
        Station delhi = stationRepository.findByStationCode("NDLS").orElse(null);
        Station mumbai = stationRepository.findByStationCode("MUMB").orElse(null);
        Station chennai = stationRepository.findByStationCode("CHEN").orElse(null);
        Station kolkata = stationRepository.findByStationCode("KOLK").orElse(null);
        
        if (delhi == null || mumbai == null || chennai == null || kolkata == null) {
            logger.warn("⚠️ Some stations not found, skipping train creation");
            return;
        }
        
        List<Train> trains = new ArrayList<>();
        
        Train train1 = new Train();
        train1.setTrainNumber("12951");
        train1.setTrainName("Mumbai Rajdhani");
        train1.setSourceStation(delhi);
        train1.setDestinationStation(mumbai);
        train1.setDepartureTime(LocalTime.of(16, 35));
        train1.setArrivalTime(LocalTime.of(8, 30));
        train1.setJourneyDuration(990);
        train1.setTotalDistance(1384.0);
        train1.setTrainType(Train.TrainType.RAJDHANI);
        train1.setStatus(Train.TrainStatus.ACTIVE);
        train1.setIsRunning(true);
        trains.add(train1);
        
        Train train2 = new Train();
        train2.setTrainNumber("12801");
        train2.setTrainName("Puri Howrah Express");
        train2.setSourceStation(kolkata);
        train2.setDestinationStation(chennai);
        train2.setDepartureTime(LocalTime.of(22, 20));
        train2.setArrivalTime(LocalTime.of(6, 0));
        train2.setJourneyDuration(460);
        train2.setTotalDistance(500.0);
        train2.setTrainType(Train.TrainType.EXPRESS);
        train2.setStatus(Train.TrainStatus.ACTIVE);
        train2.setIsRunning(true);
        trains.add(train2);
        
        Train train3 = new Train();
        train3.setTrainNumber("12615");
        train3.setTrainName("Grand Trunk Express");
        train3.setSourceStation(delhi);
        train3.setDestinationStation(chennai);
        train3.setDepartureTime(LocalTime.of(11, 0));
        train3.setArrivalTime(LocalTime.of(6, 30));
        train3.setJourneyDuration(1170);
        train3.setTotalDistance(2190.0);
        train3.setTrainType(Train.TrainType.EXPRESS);
        train3.setStatus(Train.TrainStatus.ACTIVE);
        train3.setIsRunning(true);
        trains.add(train3);
        
        trainRepository.saveAll(trains);
        logger.info("🚂 Created {} trains", trains.size());
    }
    
    private void createUsers() {
        List<User> users = new ArrayList<>();
        
        User user1 = new User();
        user1.setUsername("rajesh_kumar");
        user1.setEmail("rajesh@example.com");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setFirstName("Rajesh");
        user1.setLastName("Kumar");
        user1.setPhoneNumber("9876543213");
        user1.setDateOfBirth(LocalDateTime.of(1985, 5, 15, 0, 0));
        user1.setGender(User.Gender.MALE);
        user1.setIdProofType(User.IdProofType.AADHAR);
        user1.setIdProofNumber("123456789013");
        user1.setIsActive(true);
        user1.setIsVerified(true);
        user1.setRole(User.UserRole.USER);
        users.add(user1);
        
        User user2 = new User();
        user2.setUsername("priya_sharma");
        user2.setEmail("priya@example.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setFirstName("Priya");
        user2.setLastName("Sharma");
        user2.setPhoneNumber("9876543214");
        user2.setDateOfBirth(LocalDateTime.of(1990, 8, 22, 0, 0));
        user2.setGender(User.Gender.FEMALE);
        user2.setIdProofType(User.IdProofType.PAN);
        user2.setIdProofNumber("ABCDE1234F");
        user2.setIsActive(true);
        user2.setIsVerified(true);
        user2.setRole(User.UserRole.USER);
        users.add(user2);
        
        User user3 = new User();
        user3.setUsername("amit_patel");
        user3.setEmail("amit@example.com");
        user3.setPassword(passwordEncoder.encode("password123"));
        user3.setFirstName("Amit");
        user3.setLastName("Patel");
        user3.setPhoneNumber("9876543215");
        user3.setDateOfBirth(LocalDateTime.of(1988, 12, 10, 0, 0));
        user3.setGender(User.Gender.MALE);
        user3.setIdProofType(User.IdProofType.AADHAR);
        user3.setIdProofNumber("123456789014");
        user3.setIsActive(true);
        user3.setIsVerified(true);
        user3.setRole(User.UserRole.USER);
        users.add(user3);
        
        userRepository.saveAll(users);
        logger.info("👥 Created {} users", users.size());
    }
}
