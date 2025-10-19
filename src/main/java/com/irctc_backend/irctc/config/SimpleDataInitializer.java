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
    private CoachRepository coachRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private FareRuleRepository fareRuleRepository;
    
    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;
    
    @Autowired
    private RewardRepository rewardRepository;
    
    @Autowired
    private InsuranceProviderRepository insuranceProviderRepository;
    
    @Autowired
    private InsurancePlanRepository insurancePlanRepository;
    
    @Autowired
    private MealVendorRepository mealVendorRepository;
    
    @Autowired
    private MealItemRepository mealItemRepository;
    
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
        
        // Create coaches and seats for trains
        createCoachesAndSeats();
        
        // Create fare rules for trains
        createFareRules();
        
        // Create sample rewards
        createSampleRewards();
        
        // Create insurance providers and plans
        createInsuranceProvidersAndPlans();
        
        // Create meal vendors and menu items
        createMealVendorsAndMenu();
        
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
    
    private void createCoachesAndSeats() {
        List<Train> trains = trainRepository.findAll();
        if (trains.isEmpty()) {
            logger.warn("⚠️ No trains found, skipping coach and seat creation");
            return;
        }
        
        List<Coach> coaches = new ArrayList<>();
        List<Seat> seats = new ArrayList<>();
        
        for (Train train : trains) {
            // Create different types of coaches for each train
            Coach acCoach = createCoach(train, "A1", Coach.CoachType.AC_2_TIER, 48, BigDecimal.valueOf(2500));
            Coach sleeperCoach = createCoach(train, "S1", Coach.CoachType.SLEEPER_CLASS, 72, BigDecimal.valueOf(800));
            Coach chairCoach = createCoach(train, "CC1", Coach.CoachType.AC_CHAIR_CAR, 78, BigDecimal.valueOf(1200));
            
            coaches.add(acCoach);
            coaches.add(sleeperCoach);
            coaches.add(chairCoach);
            
            // Create seats for each coach
            seats.addAll(createSeatsForCoach(acCoach, 48, Coach.CoachType.AC_2_TIER));
            seats.addAll(createSeatsForCoach(sleeperCoach, 72, Coach.CoachType.SLEEPER_CLASS));
            seats.addAll(createSeatsForCoach(chairCoach, 78, Coach.CoachType.AC_CHAIR_CAR));
        }
        
        coachRepository.saveAll(coaches);
        seatRepository.saveAll(seats);
        logger.info("🚇 Created {} coaches and {} seats", coaches.size(), seats.size());
    }
    
    private Coach createCoach(Train train, String coachNumber, Coach.CoachType coachType, int totalSeats, BigDecimal baseFare) {
        Coach coach = new Coach();
        coach.setTrain(train);
        coach.setCoachNumber(coachNumber);
        coach.setCoachType(coachType);
        coach.setTotalSeats(totalSeats);
        coach.setAvailableSeats(totalSeats);
        coach.setBaseFare(baseFare);
        coach.setAcFare(baseFare.multiply(BigDecimal.valueOf(1.2)));
        coach.setSleeperFare(baseFare.multiply(BigDecimal.valueOf(0.8)));
        coach.setTatkalFare(baseFare.multiply(BigDecimal.valueOf(1.5)));
        coach.setLadiesQuota(totalSeats / 10); // 10% ladies quota
        coach.setSeniorCitizenQuota(totalSeats / 20); // 5% senior citizen quota
        coach.setIsActive(true);
        return coach;
    }
    
    private List<Seat> createSeatsForCoach(Coach coach, int totalSeats, Coach.CoachType coachType) {
        List<Seat> seats = new ArrayList<>();
        
        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = new Seat();
            seat.setCoach(coach);
            seat.setSeatNumber(String.valueOf(i));
            seat.setBerthNumber(String.valueOf(i));
            
            // Assign seat type based on position
            if (i % 6 == 1 || i % 6 == 6) {
                seat.setSeatType(Seat.SeatType.WINDOW);
            } else if (i % 6 == 2 || i % 6 == 5) {
                seat.setSeatType(Seat.SeatType.MIDDLE);
            } else {
                seat.setSeatType(Seat.SeatType.AISLE);
            }
            
            // Assign berth type based on position
            if (i % 6 == 1) {
                seat.setBerthType(Seat.BerthType.LOWER);
            } else if (i % 6 == 2) {
                seat.setBerthType(Seat.BerthType.MIDDLE);
            } else if (i % 6 == 3) {
                seat.setBerthType(Seat.BerthType.UPPER);
            } else if (i % 6 == 4) {
                seat.setBerthType(Seat.BerthType.SIDE_LOWER);
            } else if (i % 6 == 5) {
                seat.setBerthType(Seat.BerthType.SIDE_UPPER);
            } else {
                seat.setBerthType(Seat.BerthType.LOWER);
            }
            
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setIsLadiesQuota(i % 10 == 0); // Every 10th seat is ladies quota
            seat.setIsSeniorCitizenQuota(i % 20 == 0); // Every 20th seat is senior citizen quota
            seat.setIsHandicappedFriendly(i % 15 == 0); // Every 15th seat is handicapped friendly
            
            seats.add(seat);
        }
        
        return seats;
    }
    
    private void createFareRules() {
        List<Train> trains = trainRepository.findAll();
        if (trains.isEmpty()) {
            logger.warn("⚠️ No trains found, skipping fare rule creation");
            return;
        }
        
        List<FareRule> fareRules = new ArrayList<>();
        
        for (Train train : trains) {
            // Create fare rules for different coach types
            fareRules.add(createFareRule(train, Coach.CoachType.AC_2_TIER, 2500, 1384));
            fareRules.add(createFareRule(train, Coach.CoachType.SLEEPER_CLASS, 800, 1384));
            fareRules.add(createFareRule(train, Coach.CoachType.AC_CHAIR_CAR, 1200, 1384));
        }
        
        fareRuleRepository.saveAll(fareRules);
        logger.info("💰 Created {} fare rules", fareRules.size());
    }
    
    private FareRule createFareRule(Train train, Coach.CoachType coachType, int baseFare, int distanceKm) {
        FareRule fareRule = new FareRule();
        fareRule.setTrain(train);
        fareRule.setCoachType(coachType);
        fareRule.setBaseFare(BigDecimal.valueOf(baseFare));
        fareRule.setDistanceKm(distanceKm);
        
        // Set Tatkal fares
        fareRule.setTatkalFare(BigDecimal.valueOf(baseFare * 0.1)); // 10% of base fare
        fareRule.setPremiumTatkalFare(BigDecimal.valueOf(baseFare * 0.15)); // 15% of base fare
        
        // Set discount rates
        fareRule.setLadiesQuotaDiscount(BigDecimal.valueOf(0.05)); // 5% discount
        fareRule.setSeniorCitizenDiscount(BigDecimal.valueOf(0.10)); // 10% discount
        fareRule.setHandicappedDiscount(BigDecimal.valueOf(0.20)); // 20% discount
        
        // Set surge multipliers
        fareRule.setSurgeMultiplier(BigDecimal.valueOf(1.0)); // No base surge
        fareRule.setPeakHourMultiplier(BigDecimal.valueOf(1.2)); // 20% peak hour surge
        fareRule.setWeekendMultiplier(BigDecimal.valueOf(1.15)); // 15% weekend surge
        fareRule.setFestivalMultiplier(BigDecimal.valueOf(1.3)); // 30% festival surge
        
        fareRule.setIsActive(true);
        fareRule.setValidFrom(LocalDateTime.now().minusDays(1));
        fareRule.setValidUntil(LocalDateTime.now().plusYears(1));
        
        return fareRule;
    }
    
    private void createSampleRewards() {
        List<Reward> rewards = new ArrayList<>();
        
        // Travel vouchers
        rewards.add(createReward("₹100 Travel Voucher", "Get ₹100 off on your next train booking", 
                                Reward.RewardCategory.TRAVEL_VOUCHER, new BigDecimal("1000"), 
                                new BigDecimal("100"), null, null, 30, "BRONZE"));
        
        rewards.add(createReward("₹500 Travel Voucher", "Get ₹500 off on your next train booking", 
                                Reward.RewardCategory.TRAVEL_VOUCHER, new BigDecimal("5000"), 
                                new BigDecimal("500"), null, null, 30, "SILVER"));
        
        rewards.add(createReward("₹1000 Travel Voucher", "Get ₹1000 off on your next train booking", 
                                Reward.RewardCategory.TRAVEL_VOUCHER, new BigDecimal("10000"), 
                                new BigDecimal("1000"), null, null, 30, "GOLD"));
        
        // Cashback rewards
        rewards.add(createReward("5% Cashback", "Get 5% cashback on your booking", 
                                Reward.RewardCategory.CASHBACK, new BigDecimal("2000"), 
                                null, new BigDecimal("5.00"), new BigDecimal("500"), 60, "BRONZE"));
        
        rewards.add(createReward("10% Cashback", "Get 10% cashback on your booking", 
                                Reward.RewardCategory.CASHBACK, new BigDecimal("5000"), 
                                null, new BigDecimal("10.00"), new BigDecimal("1000"), 60, "SILVER"));
        
        // Upgrades
        rewards.add(createReward("Free Seat Upgrade", "Upgrade to next higher class", 
                                Reward.RewardCategory.UPGRADE, new BigDecimal("3000"), 
                                null, null, null, 90, "GOLD"));
        
        rewards.add(createReward("Premium Upgrade", "Upgrade to AC class", 
                                Reward.RewardCategory.UPGRADE, new BigDecimal("8000"), 
                                null, null, null, 90, "PLATINUM"));
        
        // Meal vouchers
        rewards.add(createReward("Free Meal Voucher", "Get free meal during your journey", 
                                Reward.RewardCategory.MEAL_VOUCHER, new BigDecimal("1500"), 
                                new BigDecimal("200"), null, null, 45, "BRONZE"));
        
        rewards.add(createReward("Premium Meal Voucher", "Get premium meal during your journey", 
                                Reward.RewardCategory.MEAL_VOUCHER, new BigDecimal("4000"), 
                                new BigDecimal("500"), null, null, 45, "SILVER"));
        
        // Lounge access
        rewards.add(createReward("Station Lounge Access", "Free access to station lounge", 
                                Reward.RewardCategory.LOUNGE_ACCESS, new BigDecimal("2500"), 
                                null, null, null, 30, "GOLD"));
        
        // Priority booking
        rewards.add(createReward("Priority Booking", "Get priority booking privileges", 
                                Reward.RewardCategory.PRIORITY_BOOKING, new BigDecimal("6000"), 
                                null, null, null, 180, "PLATINUM"));
        
        // Bonus points
        rewards.add(createReward("500 Bonus Points", "Get 500 additional loyalty points", 
                                Reward.RewardCategory.BONUS_POINTS, new BigDecimal("2000"), 
                                null, null, null, 365, "BRONZE"));
        
        rewards.add(createReward("1000 Bonus Points", "Get 1000 additional loyalty points", 
                                Reward.RewardCategory.BONUS_POINTS, new BigDecimal("4000"), 
                                null, null, null, 365, "SILVER"));
        
        // Merchandise
        rewards.add(createReward("IRCTC T-Shirt", "Get IRCTC branded T-shirt", 
                                Reward.RewardCategory.MERCHANDISE, new BigDecimal("3000"), 
                                new BigDecimal("500"), null, null, 365, "BRONZE"));
        
        rewards.add(createReward("IRCTC Travel Bag", "Get IRCTC branded travel bag", 
                                Reward.RewardCategory.MERCHANDISE, new BigDecimal("8000"), 
                                new BigDecimal("1500"), null, null, 365, "GOLD"));
        
        rewardRepository.saveAll(rewards);
        logger.info("🎁 Created {} sample rewards", rewards.size());
    }
    
    private Reward createReward(String name, String description, Reward.RewardCategory category, 
                               BigDecimal pointsRequired, BigDecimal cashValue, BigDecimal discountPercentage, 
                               BigDecimal maxDiscountAmount, Integer validityDays, String minTierRequired) {
        Reward reward = new Reward();
        reward.setName(name);
        reward.setDescription(description);
        reward.setCategory(category);
        reward.setPointsRequired(pointsRequired);
        reward.setCashValue(cashValue);
        reward.setDiscountPercentage(discountPercentage);
        reward.setMaxDiscountAmount(maxDiscountAmount);
        reward.setValidityDays(validityDays);
        reward.setMinTierRequired(minTierRequired);
        reward.setIsActive(true);
        reward.setIsFeatured(false);
        reward.setRedemptionLimit(100); // Limit to 100 redemptions per reward
        reward.setRedemptionCount(0);
        return reward;
    }
    
    private void createInsuranceProvidersAndPlans() {
        List<InsuranceProvider> providers = new ArrayList<>();
        
        // Create insurance providers
        InsuranceProvider provider1 = new InsuranceProvider();
        provider1.setProviderName("IRCTC Travel Shield");
        provider1.setCompanyName("IRCTC Insurance Services Ltd.");
        provider1.setDescription("Comprehensive travel insurance with 24x7 support and quick claim settlement");
        provider1.setContactEmail("support@irctcinsurance.com");
        provider1.setContactPhone("+91-11-39340000");
        provider1.setWebsiteUrl("https://www.irctcinsurance.com");
        provider1.setLogoUrl("https://irctc.com/images/insurance-logo.png");
        provider1.setBasePremiumRate(new BigDecimal("0.15")); // ₹0.15 per ₹1000 coverage
        provider1.setMinCoverageAmount(new BigDecimal("10000"));
        provider1.setMaxCoverageAmount(new BigDecimal("1000000"));
        provider1.setClaimSettlementRatio(new BigDecimal("98.50"));
        provider1.setAverageSettlementDays(5);
        provider1.setIsActive(true);
        provider1.setIsFeatured(true);
        provider1.setRating(new BigDecimal("4.8"));
        provider1.setTotalPoliciesSold(50000L);
        provider1.setTotalClaimsProcessed(1200L);
        providers.add(provider1);
        
        InsuranceProvider provider2 = new InsuranceProvider();
        provider2.setProviderName("Railway Travel Guard");
        provider2.setCompanyName("Railway Insurance Corporation");
        provider2.setDescription("Specialized railway travel insurance with comprehensive coverage");
        provider2.setContactEmail("info@railwayguard.com");
        provider2.setContactPhone("+91-22-22044044");
        provider2.setWebsiteUrl("https://www.railwayguard.com");
        provider2.setLogoUrl("https://railwayguard.com/images/logo.png");
        provider2.setBasePremiumRate(new BigDecimal("0.12")); // ₹0.12 per ₹1000 coverage
        provider2.setMinCoverageAmount(new BigDecimal("5000"));
        provider2.setMaxCoverageAmount(new BigDecimal("500000"));
        provider2.setClaimSettlementRatio(new BigDecimal("96.00"));
        provider2.setAverageSettlementDays(7);
        provider2.setIsActive(true);
        provider2.setIsFeatured(false);
        provider2.setRating(new BigDecimal("4.5"));
        provider2.setTotalPoliciesSold(25000L);
        provider2.setTotalClaimsProcessed(800L);
        providers.add(provider2);
        
        InsuranceProvider provider3 = new InsuranceProvider();
        provider3.setProviderName("Journey Safe Plus");
        provider3.setCompanyName("Journey Safe Insurance Ltd.");
        provider3.setDescription("Premium travel insurance with global coverage and emergency assistance");
        provider3.setContactEmail("contact@journeysafe.com");
        provider3.setContactPhone("+91-80-40404040");
        provider3.setWebsiteUrl("https://www.journeysafe.com");
        provider3.setLogoUrl("https://journeysafe.com/images/logo.png");
        provider3.setBasePremiumRate(new BigDecimal("0.20")); // ₹0.20 per ₹1000 coverage
        provider3.setMinCoverageAmount(new BigDecimal("25000"));
        provider3.setMaxCoverageAmount(new BigDecimal("2000000"));
        provider3.setClaimSettlementRatio(new BigDecimal("99.20"));
        provider3.setAverageSettlementDays(3);
        provider3.setIsActive(true);
        provider3.setIsFeatured(true);
        provider3.setRating(new BigDecimal("4.9"));
        provider3.setTotalPoliciesSold(75000L);
        provider3.setTotalClaimsProcessed(2000L);
        providers.add(provider3);
        
        insuranceProviderRepository.saveAll(providers);
        logger.info("🛡️ Created {} insurance providers", providers.size());
        
        // Create insurance plans
        List<InsurancePlan> plans = new ArrayList<>();
        
        // IRCTC Travel Shield Plans
        plans.add(createInsurancePlan(provider1, "Basic Travel Shield", "Essential coverage for basic travel needs", 
                                    InsurancePlan.PlanType.BASIC, new BigDecimal("0.10"), 
                                    new BigDecimal("10000"), new BigDecimal("100000"), 
                                    new BigDecimal("50"), new BigDecimal("500"), 30, 0, 80, true, false, false, true, false, true,
                                    new BigDecimal("50000"), new BigDecimal("25000"), new BigDecimal("10000"), new BigDecimal("100000"), new BigDecimal("1000")));
        
        plans.add(createInsurancePlan(provider1, "Standard Travel Shield", "Comprehensive coverage with standard benefits", 
                                    InsurancePlan.PlanType.STANDARD, new BigDecimal("0.15"), 
                                    new BigDecimal("25000"), new BigDecimal("500000"), 
                                    new BigDecimal("100"), new BigDecimal("2000"), 45, 0, 75, true, true, true, true, false, true,
                                    new BigDecimal("200000"), new BigDecimal("100000"), new BigDecimal("25000"), new BigDecimal("500000"), new BigDecimal("2000")));
        
        plans.add(createInsurancePlan(provider1, "Premium Travel Shield", "Premium coverage with enhanced benefits", 
                                    InsurancePlan.PlanType.PREMIUM, new BigDecimal("0.25"), 
                                    new BigDecimal("50000"), new BigDecimal("1000000"), 
                                    new BigDecimal("200"), new BigDecimal("5000"), 60, 0, 70, true, true, true, true, true, true,
                                    new BigDecimal("500000"), new BigDecimal("250000"), new BigDecimal("50000"), new BigDecimal("1000000"), new BigDecimal("5000")));
        
        // Railway Travel Guard Plans
        plans.add(createInsurancePlan(provider2, "Railway Basic", "Basic railway travel insurance", 
                                    InsurancePlan.PlanType.BASIC, new BigDecimal("0.08"), 
                                    new BigDecimal("5000"), new BigDecimal("100000"), 
                                    new BigDecimal("25"), new BigDecimal("300"), 30, 0, 85, true, false, false, true, false, true,
                                    new BigDecimal("30000"), new BigDecimal("15000"), new BigDecimal("5000"), new BigDecimal("100000"), new BigDecimal("500")));
        
        plans.add(createInsurancePlan(provider2, "Railway Standard", "Standard railway travel insurance", 
                                    InsurancePlan.PlanType.STANDARD, new BigDecimal("0.12"), 
                                    new BigDecimal("15000"), new BigDecimal("300000"), 
                                    new BigDecimal("75"), new BigDecimal("1000"), 45, 0, 80, true, true, true, true, false, true,
                                    new BigDecimal("150000"), new BigDecimal("75000"), new BigDecimal("15000"), new BigDecimal("300000"), new BigDecimal("1000")));
        
        // Journey Safe Plus Plans
        plans.add(createInsurancePlan(provider3, "Journey Safe Elite", "Elite travel insurance with global coverage", 
                                    InsurancePlan.PlanType.PREMIUM, new BigDecimal("0.30"), 
                                    new BigDecimal("100000"), new BigDecimal("2000000"), 
                                    new BigDecimal("500"), new BigDecimal("10000"), 90, 0, 65, true, true, true, true, true, true,
                                    new BigDecimal("1000000"), new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("2000000"), new BigDecimal("10000")));
        
        plans.add(createInsurancePlan(provider3, "Family Journey Safe", "Family coverage for multiple travelers", 
                                    InsurancePlan.PlanType.FAMILY, new BigDecimal("0.18"), 
                                    new BigDecimal("50000"), new BigDecimal("1000000"), 
                                    new BigDecimal("200"), new BigDecimal("3000"), 60, 0, 75, true, true, true, true, false, true,
                                    new BigDecimal("300000"), new BigDecimal("200000"), new BigDecimal("50000"), new BigDecimal("1000000"), new BigDecimal("3000")));
        
        insurancePlanRepository.saveAll(plans);
        logger.info("📋 Created {} insurance plans", plans.size());
    }
    
    private InsurancePlan createInsurancePlan(InsuranceProvider provider, String planName, String description, 
                                            InsurancePlan.PlanType planType, BigDecimal premiumRate,
                                            BigDecimal minCoverage, BigDecimal maxCoverage,
                                            BigDecimal minPremium, BigDecimal maxPremium, Integer coverageDuration,
                                            Integer ageMin, Integer ageMax, Boolean coversMedical, Boolean coversCancellation,
                                            Boolean coversBaggage, Boolean coversAccident, Boolean coversEvacuation, Boolean coversSupport,
                                            BigDecimal medicalLimit, BigDecimal cancellationLimit, BigDecimal baggageLimit,
                                            BigDecimal accidentLimit, BigDecimal deductible) {
        InsurancePlan plan = new InsurancePlan();
        plan.setProvider(provider);
        plan.setPlanName(planName);
        plan.setDescription(description);
        plan.setPlanType(planType);
        plan.setPremiumRate(premiumRate);
        plan.setMinCoverageAmount(minCoverage);
        plan.setMaxCoverageAmount(maxCoverage);
        plan.setMinPremium(minPremium);
        plan.setMaxPremium(maxPremium);
        plan.setCoverageDurationDays(coverageDuration);
        plan.setAgeMin(ageMin);
        plan.setAgeMax(ageMax);
        plan.setIsActive(true);
        plan.setIsFeatured(planType == InsurancePlan.PlanType.PREMIUM);
        plan.setPopularityScore(0);
        
        // Coverage details
        plan.setCoversMedicalExpenses(coversMedical);
        plan.setCoversTripCancellation(coversCancellation);
        plan.setCoversBaggageLoss(coversBaggage);
        plan.setCoversPersonalAccident(coversAccident);
        plan.setCoversEmergencyEvacuation(coversEvacuation);
        plan.setCovers24x7Support(coversSupport);
        
        // Coverage limits
        plan.setMedicalCoverageLimit(medicalLimit);
        plan.setTripCancellationLimit(cancellationLimit);
        plan.setBaggageCoverageLimit(baggageLimit);
        plan.setPersonalAccidentLimit(accidentLimit);
        plan.setDeductibleAmount(deductible);
        
        return plan;
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
    
    private void createMealVendorsAndMenu() {
        List<MealVendor> vendors = new ArrayList<>();
        
        // Create meal vendors for different stations
        MealVendor vendor1 = new MealVendor();
        vendor1.setVendorName("Delhi Food Court");
        vendor1.setStationCode("NDLS");
        vendor1.setStationName("New Delhi");
        vendor1.setContactEmail("delhi@foodcourt.com");
        vendor1.setContactPhone("+91-11-12345678");
        vendor1.setRating(new BigDecimal("4.5"));
        vendor1.setIsActive(true);
        vendors.add(vendor1);
        
        MealVendor vendor2 = new MealVendor();
        vendor2.setVendorName("Mumbai Central Kitchen");
        vendor2.setStationCode("MUMB");
        vendor2.setStationName("Mumbai Central");
        vendor2.setContactEmail("mumbai@centralkitchen.com");
        vendor2.setContactPhone("+91-22-87654321");
        vendor2.setRating(new BigDecimal("4.3"));
        vendor2.setIsActive(true);
        vendors.add(vendor2);
        
        MealVendor vendor3 = new MealVendor();
        vendor3.setVendorName("Chennai Express Meals");
        vendor3.setStationCode("CHEN");
        vendor3.setStationName("Chennai Central");
        vendor3.setContactEmail("chennai@expressmeals.com");
        vendor3.setContactPhone("+91-44-11223344");
        vendor3.setRating(new BigDecimal("4.7"));
        vendor3.setIsActive(true);
        vendors.add(vendor3);
        
        MealVendor vendor4 = new MealVendor();
        vendor4.setVendorName("Kolkata Spice Kitchen");
        vendor4.setStationCode("KOLK");
        vendor4.setStationName("Howrah");
        vendor4.setContactEmail("kolkata@spicekitchen.com");
        vendor4.setContactPhone("+91-33-55667788");
        vendor4.setRating(new BigDecimal("4.4"));
        vendor4.setIsActive(true);
        vendors.add(vendor4);
        
        mealVendorRepository.saveAll(vendors);
        logger.info("🍽️ Created {} meal vendors", vendors.size());
        
        // Create menu items for each vendor
        List<MealItem> menuItems = new ArrayList<>();
        
        // Delhi Food Court Menu
        menuItems.addAll(createMenuForVendor(vendor1, "Delhi"));
        
        // Mumbai Central Kitchen Menu
        menuItems.addAll(createMenuForVendor(vendor2, "Mumbai"));
        
        // Chennai Express Meals Menu
        menuItems.addAll(createMenuForVendor(vendor3, "Chennai"));
        
        // Kolkata Spice Kitchen Menu
        menuItems.addAll(createMenuForVendor(vendor4, "Kolkata"));
        
        mealItemRepository.saveAll(menuItems);
        logger.info("🍴 Created {} menu items", menuItems.size());
    }
    
    private List<MealItem> createMenuForVendor(MealVendor vendor, String city) {
        List<MealItem> items = new ArrayList<>();
        
        // Breakfast items
        items.add(createMealItem(vendor, "Masala Dosa", "Crispy dosa with spiced potato filling", 
                                new BigDecimal("120"), MealItem.MealCategory.BREAKFAST, MealItem.MealType.VEG, true, 10));
        
        items.add(createMealItem(vendor, "Idli Sambar", "Soft idlis with tangy sambar", 
                                new BigDecimal("80"), MealItem.MealCategory.BREAKFAST, MealItem.MealType.VEG, true, 8));
        
        items.add(createMealItem(vendor, "Poha", "Flattened rice with vegetables and spices", 
                                new BigDecimal("60"), MealItem.MealCategory.BREAKFAST, MealItem.MealType.VEG, true, 5));
        
        // Lunch items
        items.add(createMealItem(vendor, "Dal Makhani", "Creamy black lentils with butter", 
                                new BigDecimal("180"), MealItem.MealCategory.LUNCH, MealItem.MealType.VEG, true, 15));
        
        items.add(createMealItem(vendor, "Chicken Curry", "Spicy chicken curry with rice", 
                                new BigDecimal("220"), MealItem.MealCategory.LUNCH, MealItem.MealType.NON_VEG, false, 20));
        
        items.add(createMealItem(vendor, "Vegetable Biryani", "Aromatic rice with mixed vegetables", 
                                new BigDecimal("160"), MealItem.MealCategory.LUNCH, MealItem.MealType.VEG, true, 18));
        
        // Dinner items
        items.add(createMealItem(vendor, "Paneer Butter Masala", "Cottage cheese in rich tomato gravy", 
                                new BigDecimal("200"), MealItem.MealCategory.DINNER, MealItem.MealType.VEG, true, 15));
        
        items.add(createMealItem(vendor, "Fish Curry", "Traditional fish curry with rice", 
                                new BigDecimal("250"), MealItem.MealCategory.DINNER, MealItem.MealType.NON_VEG, false, 25));
        
        // Snacks
        items.add(createMealItem(vendor, "Samosa", "Crispy fried pastry with spiced filling", 
                                new BigDecimal("40"), MealItem.MealCategory.SNACKS, MealItem.MealType.VEG, true, 5));
        
        items.add(createMealItem(vendor, "Vada Pav", "Spicy potato fritter in bread", 
                                new BigDecimal("50"), MealItem.MealCategory.SNACKS, MealItem.MealType.VEG, true, 8));
        
        // Beverages
        items.add(createMealItem(vendor, "Masala Chai", "Spiced Indian tea", 
                                new BigDecimal("25"), MealItem.MealCategory.BEVERAGES, MealItem.MealType.VEG, true, 3));
        
        items.add(createMealItem(vendor, "Fresh Lime Soda", "Refreshing lime drink", 
                                new BigDecimal("35"), MealItem.MealCategory.BEVERAGES, MealItem.MealType.VEG, true, 2));
        
        // Desserts
        items.add(createMealItem(vendor, "Gulab Jamun", "Sweet milk dumplings in syrup", 
                                new BigDecimal("60"), MealItem.MealCategory.DESSERTS, MealItem.MealType.VEG, true, 5));
        
        items.add(createMealItem(vendor, "Kheer", "Rice pudding with nuts", 
                                new BigDecimal("80"), MealItem.MealCategory.DESSERTS, MealItem.MealType.VEG, true, 8));
        
        return items;
    }
    
    private MealItem createMealItem(MealVendor vendor, String itemName, String description, 
                                   BigDecimal price, MealItem.MealCategory category, MealItem.MealType mealType, 
                                   Boolean isVegetarian, Integer prepTime) {
        MealItem item = new MealItem();
        item.setVendor(vendor);
        item.setItemName(itemName);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setMealType(mealType);
        item.setIsVegetarian(isVegetarian);
        item.setIsAvailable(true);
        item.setPreparationTimeMinutes(prepTime);
        return item;
    }
}
