package com.irctc.booking.service;

import com.irctc.booking.client.PaymentServiceClient;
import com.irctc.booking.client.TrainServiceClient;
import com.irctc.booking.dto.*;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.entity.SimplePassenger;
import com.irctc.booking.exception.BusinessException;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingModificationServiceTest {
    
    @Mock
    private SimpleBookingRepository bookingRepository;
    
    @Mock
    private SimpleBookingService bookingService;
    
    @Mock
    private ModificationChargeCalculator chargeCalculator;
    
    @Mock
    private TrainServiceClient trainServiceClient;
    
    @Mock
    private PaymentServiceClient paymentServiceClient;
    
    @InjectMocks
    private BookingModificationService modificationService;
    
    private SimpleBooking testBooking;
    private LocalDateTime futureDate;
    
    @BeforeEach
    void setUp() {
        futureDate = LocalDateTime.now().plusDays(5);
        
        testBooking = new SimpleBooking();
        testBooking.setId(1L);
        testBooking.setUserId(100L);
        testBooking.setTrainId(200L);
        testBooking.setPnrNumber("PNR123456");
        testBooking.setBookingTime(futureDate);
        testBooking.setStatus("CONFIRMED");
        testBooking.setTotalFare(new BigDecimal("2000.00"));
        
        SimplePassenger passenger = new SimplePassenger();
        passenger.setId(1L);
        passenger.setName("John Doe");
        passenger.setAge(30);
        passenger.setGender("MALE");
        passenger.setSeatNumber("A1");
        testBooking.setPassengers(new ArrayList<>(Arrays.asList(passenger)));
    }
    
    @Test
    void testGetModificationOptions_Success() {
        // Given
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(chargeCalculator.calculateDateChangeCharge(any(), any())).thenReturn(new BigDecimal("200.00"));
        when(chargeCalculator.calculateSeatUpgradeCharge(anyString(), anyString(), any(), any()))
            .thenReturn(new BigDecimal("100.00"));
        when(chargeCalculator.calculateRouteChangeCharge(any(), any())).thenReturn(new BigDecimal("300.00"));
        when(chargeCalculator.calculatePassengerModificationCharge(anyInt(), anyInt(), any(), any()))
            .thenReturn(new BigDecimal("150.00"));
        
        // When
        ModificationOptionsResponse response = modificationService.getModificationOptions(1L);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getBookingId());
        assertEquals("CONFIRMED", response.getCurrentStatus());
        assertTrue(response.isCanModifyDate());
        assertTrue(response.isCanUpgradeSeat());
        assertTrue(response.isCanChangeRoute());
        assertTrue(response.isCanModifyPassengers());
        assertNotNull(response.getModificationCharges());
    }
    
    @Test
    void testGetModificationOptions_BookingNotFound() {
        // Given
        when(bookingService.getBookingById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            modificationService.getModificationOptions(999L);
        });
    }
    
    @Test
    void testGetModificationOptions_CannotModify() {
        // Given
        testBooking.setStatus("CANCELLED");
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            modificationService.getModificationOptions(1L);
        });
    }
    
    @Test
    void testModifyDate_Success() {
        // Given
        DateChangeRequest request = new DateChangeRequest();
        request.setBookingId(1L);
        request.setNewJourneyDate(LocalDateTime.now().plusDays(10));
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(chargeCalculator.calculateDateChangeCharge(any(), any())).thenReturn(new BigDecimal("200.00"));
        when(chargeCalculator.calculateFareDifference(any(), any())).thenReturn(BigDecimal.ZERO);
        when(chargeCalculator.calculateTotalAmount(any(), any())).thenReturn(new BigDecimal("200.00"));
        when(bookingRepository.save(any(SimpleBooking.class))).thenReturn(testBooking);
        
        // When
        ModificationResponse response = modificationService.modifyDate(request);
        
        // Then
        assertNotNull(response);
        assertEquals("DATE_CHANGE", response.getModificationType());
        assertEquals("SUCCESS", response.getStatus());
        verify(bookingRepository, times(1)).save(any(SimpleBooking.class));
    }
    
    @Test
    void testModifyDate_WithTrainServiceIntegration() {
        // Given
        DateChangeRequest request = new DateChangeRequest();
        request.setBookingId(1L);
        request.setNewJourneyDate(LocalDateTime.now().plusDays(10));
        request.setNewTrainId(300L);
        
        TrainServiceClient.TrainResponse trainResponse = new TrainServiceClient.TrainResponse();
        trainResponse.setId(300L);
        trainResponse.setBaseFare(1500.0);
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(trainServiceClient.getTrainById(300L)).thenReturn(trainResponse);
        when(chargeCalculator.calculateDateChangeCharge(any(), any())).thenReturn(new BigDecimal("200.00"));
        when(chargeCalculator.calculateFareDifference(any(), any())).thenReturn(new BigDecimal("-500.00"));
        when(chargeCalculator.calculateTotalAmount(any(), any())).thenReturn(new BigDecimal("-300.00"));
        when(bookingRepository.save(any(SimpleBooking.class))).thenReturn(testBooking);
        
        // When
        ModificationResponse response = modificationService.modifyDate(request);
        
        // Then
        assertNotNull(response);
        assertEquals("DATE_CHANGE", response.getModificationType());
        verify(trainServiceClient, times(1)).getTrainById(300L);
    }
    
    @Test
    void testModifyDate_InvalidDate() {
        // Given
        DateChangeRequest request = new DateChangeRequest();
        request.setBookingId(1L);
        request.setNewJourneyDate(LocalDateTime.now().minusDays(1)); // Past date
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            modificationService.modifyDate(request);
        });
    }
    
    @Test
    void testUpgradeSeat_Success() {
        // Given
        SeatUpgradeRequest request = new SeatUpgradeRequest();
        request.setBookingId(1L);
        request.setNewSeatClass("2AC");
        request.setNewFare(new BigDecimal("2500.00"));
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(chargeCalculator.calculateSeatUpgradeCharge(anyString(), anyString(), any(), any()))
            .thenReturn(new BigDecimal("100.00"));
        when(chargeCalculator.calculateFareDifference(any(), any())).thenReturn(new BigDecimal("500.00"));
        when(chargeCalculator.calculateTotalAmount(any(), any())).thenReturn(new BigDecimal("600.00"));
        when(paymentServiceClient.processPayment(any())).thenReturn(createPaymentResponse("COMPLETED"));
        when(bookingRepository.save(any(SimpleBooking.class))).thenReturn(testBooking);
        
        // When
        ModificationResponse response = modificationService.upgradeSeat(request);
        
        // Then
        assertNotNull(response);
        assertEquals("SEAT_UPGRADE", response.getModificationType());
        assertEquals("SUCCESS", response.getStatus());
        verify(bookingRepository, times(1)).save(any(SimpleBooking.class));
    }
    
    @Test
    void testModifyPassengers_AddPassenger() {
        // Given
        PassengerModificationRequest request = new PassengerModificationRequest();
        request.setBookingId(1L);
        
        SimplePassenger newPassenger = new SimplePassenger();
        newPassenger.setName("Jane Doe");
        newPassenger.setAge(25);
        newPassenger.setGender("FEMALE");
        newPassenger.setSeatNumber("A2");
        request.setPassengersToAdd(Arrays.asList(newPassenger));
        request.setPassengerIdsToRemove(Collections.emptyList());
        request.setAdditionalFare(new BigDecimal("1200.00"));
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(chargeCalculator.calculatePassengerModificationCharge(anyInt(), anyInt(), any(), any()))
            .thenReturn(new BigDecimal("150.00"));
        when(chargeCalculator.calculateTotalAmount(any(), any())).thenReturn(new BigDecimal("1350.00"));
        when(paymentServiceClient.processPayment(any())).thenReturn(createPaymentResponse("COMPLETED"));
        when(bookingRepository.save(any(SimpleBooking.class))).thenReturn(testBooking);
        
        // When
        ModificationResponse response = modificationService.modifyPassengers(request);
        
        // Then
        assertNotNull(response);
        assertEquals("PASSENGER_MODIFICATION", response.getModificationType());
        assertEquals("SUCCESS", response.getStatus());
        verify(bookingRepository, times(1)).save(any(SimpleBooking.class));
    }
    
    @Test
    void testModifyPassengers_RemovePassenger() {
        // Given
        PassengerModificationRequest request = new PassengerModificationRequest();
        request.setBookingId(1L);
        request.setPassengersToAdd(Collections.emptyList());
        request.setPassengerIdsToRemove(Arrays.asList(1L));
        request.setAdditionalFare(BigDecimal.ZERO);
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(chargeCalculator.calculatePassengerModificationCharge(anyInt(), anyInt(), any(), any()))
            .thenReturn(new BigDecimal("150.00"));
        when(chargeCalculator.calculateTotalAmount(any(), any())).thenReturn(new BigDecimal("150.00"));
        when(paymentServiceClient.processPayment(any())).thenReturn(createPaymentResponse("COMPLETED"));
        when(bookingRepository.save(any(SimpleBooking.class))).thenReturn(testBooking);
        
        // When
        ModificationResponse response = modificationService.modifyPassengers(request);
        
        // Then
        assertNotNull(response);
        assertEquals("PASSENGER_MODIFICATION", response.getModificationType());
        verify(bookingRepository, times(1)).save(any(SimpleBooking.class));
    }
    
    @Test
    void testChangeRoute_Success() {
        // Given
        RouteChangeRequest request = new RouteChangeRequest();
        request.setBookingId(1L);
        request.setNewSourceStation("Mumbai");
        request.setNewDestinationStation("Delhi");
        request.setNewFare(new BigDecimal("3000.00"));
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        when(chargeCalculator.calculateRouteChangeCharge(any(), any())).thenReturn(new BigDecimal("300.00"));
        when(chargeCalculator.calculateFareDifference(any(), any())).thenReturn(new BigDecimal("1000.00"));
        when(chargeCalculator.calculateTotalAmount(any(), any())).thenReturn(new BigDecimal("1300.00"));
        when(paymentServiceClient.processPayment(any())).thenReturn(createPaymentResponse("COMPLETED"));
        when(bookingRepository.save(any(SimpleBooking.class))).thenReturn(testBooking);
        
        // When
        ModificationResponse response = modificationService.changeRoute(request);
        
        // Then
        assertNotNull(response);
        assertEquals("ROUTE_CHANGE", response.getModificationType());
        assertEquals("SUCCESS", response.getStatus());
        verify(bookingRepository, times(1)).save(any(SimpleBooking.class));
    }
    
    @Test
    void testModifyDate_TooCloseToJourney() {
        // Given
        testBooking.setBookingTime(LocalDateTime.now().plusHours(2)); // Less than 4 hours
        DateChangeRequest request = new DateChangeRequest();
        request.setBookingId(1L);
        request.setNewJourneyDate(LocalDateTime.now().plusDays(10));
        
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(testBooking));
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            modificationService.modifyDate(request);
        });
    }
    
    @Test
    void testProcessPaymentForModification_Refund() {
        // Given
        BigDecimal negativeAmount = new BigDecimal("-500.00");
        List<PaymentServiceClient.PaymentResponse> existingPayments = new ArrayList<>();
        PaymentServiceClient.PaymentResponse lastPayment = new PaymentServiceClient.PaymentResponse();
        lastPayment.setId(100L);
        existingPayments.add(lastPayment);
        
        PaymentServiceClient.PaymentResponse refundResponse = new PaymentServiceClient.PaymentResponse();
        refundResponse.setStatus("REFUNDED");
        refundResponse.setTransactionId("REF123");
        
        when(paymentServiceClient.getPaymentsByBookingId(1L)).thenReturn(existingPayments);
        when(paymentServiceClient.processRefund(any())).thenReturn(refundResponse);
        
        // When
        String status = modificationService.processPaymentForModification(1L, negativeAmount, "Test refund");
        
        // Then
        assertEquals("REFUNDED", status);
        verify(paymentServiceClient, times(1)).processRefund(any());
    }
    
    // Helper method
    private PaymentServiceClient.PaymentResponse createPaymentResponse(String status) {
        PaymentServiceClient.PaymentResponse response = new PaymentServiceClient.PaymentResponse();
        response.setId(1L);
        response.setStatus(status);
        response.setTransactionId("TXN123");
        return response;
    }
}

