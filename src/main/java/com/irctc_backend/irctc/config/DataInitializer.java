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
 * Data Initializer for creating dummy data for testing
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired
    private CoachRepository coachRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
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
        logger.info("🚀 Initializing dummy data for testing...");
        
        // Only create dummy data if no bookings exist
        if (bookingRepository.count() == 0) {
            createDummyData();
            logger.info("✅ Dummy data created successfully!");
        } else {
            logger.info("📊 Dummy data already exists, skipping initialization");
        }
    }
    
    private void createDummyData() {
        // Create additional stations
        createStations();
        
        // Create additional trains
        createTrains();
        
        // Create additional users
        createUsers();
        
        // Create bookings with passengers
        createBookings();
        
        // Create payments
        createPayments();
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
        
        userRepository.saveAll(users);
        logger.info("👥 Created {} users", users.size());
    }
    
    private void createBookings() {
        // Get users and trains
        User user1 = userRepository.findByUsername("rajesh_kumar").orElse(null);
        User user2 = userRepository.findByUsername("priya_sharma").orElse(null);
        
        Train train1 = trainRepository.findByTrainNumber("12951").orElse(null);
        Train train2 = trainRepository.findByTrainNumber("12801").orElse(null);
        
        if (user1 == null || user2 == null || train1 == null || train2 == null) {
            logger.warn("⚠️ Some users or trains not found, skipping booking creation");
            return;
        }
        
        // Create passengers first
        Passenger passenger1 = new Passenger();
        passenger1.setUser(user1);
        passenger1.setFirstName("Rajesh");
        passenger1.setLastName("Kumar");
        passenger1.setAge(35);
        passenger1.setGender(Passenger.Gender.MALE);
        passenger1.setPassengerType(Passenger.PassengerType.ADULT);
        passenger1.setIdProofType(Passenger.IdProofType.AADHAR);
        passenger1.setIdProofNumber("123456789013");
        passenger1.setIsSeniorCitizen(false);
        passenger1.setIsLadiesQuota(false);
        passenger1.setIsHandicapped(false);
        
        Passenger passenger2 = new Passenger();
        passenger2.setUser(user2);
        passenger2.setFirstName("Priya");
        passenger2.setLastName("Sharma");
        passenger2.setAge(28);
        passenger2.setGender(Passenger.Gender.FEMALE);
        passenger2.setPassengerType(Passenger.PassengerType.ADULT);
        passenger2.setIdProofType(Passenger.IdProofType.PAN);
        passenger2.setIdProofNumber("ABCDE1234F");
        passenger2.setIsSeniorCitizen(false);
        passenger2.setIsLadiesQuota(false);
        passenger2.setIsHandicapped(false);
        
        List<Passenger> passengers = List.of(passenger1, passenger2);
        passengerRepository.saveAll(passengers);
        
        // Create dummy coaches for the bookings
        Coach coach1 = new Coach();
        coach1.setTrain(train1);
        coach1.setCoachNumber("B1");
        coach1.setCoachType(Coach.CoachType.AC_2_TIER);
        coach1.setTotalSeats(48);
        coach1.setAvailableSeats(46);
        coach1.setIsActive(true);
        
        Coach coach2 = new Coach();
        coach2.setTrain(train2);
        coach2.setCoachNumber("S1");
        coach2.setCoachType(Coach.CoachType.SLEEPER_CLASS);
        coach2.setTotalSeats(72);
        coach2.setAvailableSeats(71);
        coach2.setIsActive(true);
        
        List<Coach> coaches = List.of(coach1, coach2);
        coachRepository.saveAll(coaches);
        
        // Create dummy seats
        Seat seat1 = new Seat();
        seat1.setCoach(coach1);
        seat1.setSeatNumber("12");
        seat1.setBerthType(Seat.BerthType.LOWER);
        seat1.setSeatType(Seat.SeatType.WINDOW);
        seat1.setStatus(Seat.SeatStatus.BOOKED);
        
        Seat seat2 = new Seat();
        seat2.setCoach(coach2);
        seat2.setSeatNumber("25");
        seat2.setBerthType(Seat.BerthType.LOWER);
        seat2.setSeatType(Seat.SeatType.WINDOW);
        seat2.setStatus(Seat.SeatStatus.BOOKED);
        
        List<Seat> seats = List.of(seat1, seat2);
        seatRepository.saveAll(seats);
        
        // Create bookings
        Booking booking1 = new Booking();
        booking1.setPnrNumber("PNR123456");
        booking1.setUser(user1);
        booking1.setTrain(train1);
        booking1.setPassenger(passenger1);
        booking1.setCoach(coach1);
        booking1.setSeat(seat1);
        booking1.setJourneyDate(LocalDate.now().plusDays(7));
        booking1.setBookingDate(LocalDateTime.now());
        booking1.setTotalFare(new BigDecimal("2500.00"));
        booking1.setBaseFare(new BigDecimal("2000.00"));
        booking1.setConvenienceFee(new BigDecimal("50.00"));
        booking1.setGstAmount(new BigDecimal("450.00"));
        booking1.setStatus(Booking.BookingStatus.CONFIRMED);
        booking1.setPaymentStatus(Booking.PaymentStatus.COMPLETED);
        booking1.setQuotaType(Booking.QuotaType.GENERAL);
        booking1.setIsTatkal(false);
        booking1.setIsCancelled(false);
        booking1.setBookingSource("WEB");
        
        Booking booking2 = new Booking();
        booking2.setPnrNumber("PNR789012");
        booking2.setUser(user2);
        booking2.setTrain(train2);
        booking2.setPassenger(passenger2);
        booking2.setCoach(coach2);
        booking2.setSeat(seat2);
        booking2.setJourneyDate(LocalDate.now().plusDays(10));
        booking2.setBookingDate(LocalDateTime.now().minusHours(2));
        booking2.setTotalFare(new BigDecimal("1800.00"));
        booking2.setBaseFare(new BigDecimal("1500.00"));
        booking2.setConvenienceFee(new BigDecimal("30.00"));
        booking2.setGstAmount(new BigDecimal("270.00"));
        booking2.setStatus(Booking.BookingStatus.CONFIRMED);
        booking2.setPaymentStatus(Booking.PaymentStatus.COMPLETED);
        booking2.setQuotaType(Booking.QuotaType.GENERAL);
        booking2.setIsTatkal(false);
        booking2.setIsCancelled(false);
        booking2.setBookingSource("MOBILE_APP");
        
        List<Booking> bookings = List.of(booking1, booking2);
        bookingRepository.saveAll(bookings);
        
        logger.info("🎫 Created {} bookings", bookings.size());
    }
    
    
    private void createPayments() {
        // Get bookings
        Booking booking1 = bookingRepository.findByPnrNumber("PNR123456").orElse(null);
        Booking booking2 = bookingRepository.findByPnrNumber("PNR789012").orElse(null);
        
        if (booking1 == null || booking2 == null) {
            logger.warn("⚠️ Some bookings not found, skipping payment creation");
            return;
        }
        
        List<Payment> payments = new ArrayList<>();
        
        // Successful payment for booking1
        Payment payment1 = new Payment();
        payment1.setBooking(booking1);
        payment1.setAmount(new BigDecimal("2500.00"));
        payment1.setStatus(Payment.PaymentStatus.COMPLETED);
        payment1.setPaymentMethod(Payment.PaymentMethod.RAZORPAY);
        payment1.setTransactionId("TXN123456789");
        payment1.setPaymentDate(LocalDateTime.now().minusHours(1));
        payment1.setGatewayResponse("Payment successful");
        payment1.setGatewayTransactionId("RZP_TXN_123456789");
        payment1.setCurrency("INR");
        payment1.setGatewayFee(new BigDecimal("75.00"));
        payments.add(payment1);
        
        // Successful payment for booking2
        Payment payment2 = new Payment();
        payment2.setBooking(booking2);
        payment2.setAmount(new BigDecimal("1800.00"));
        payment2.setStatus(Payment.PaymentStatus.COMPLETED);
        payment2.setPaymentMethod(Payment.PaymentMethod.UPI);
        payment2.setTransactionId("TXN987654321");
        payment2.setPaymentDate(LocalDateTime.now().minusHours(2));
        payment2.setGatewayResponse("Payment successful");
        payment2.setGatewayTransactionId("UPI_TXN_987654321");
        payment2.setCurrency("INR");
        payment2.setGatewayFee(new BigDecimal("0.00"));
        payments.add(payment2);
        
        paymentRepository.saveAll(payments);
        logger.info("💳 Created {} payments", payments.size());
    }
}