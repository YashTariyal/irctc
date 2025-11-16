package com.irctc.booking.service;

import com.irctc.booking.entity.CheckIn;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.exception.EntityNotFoundException;
import com.irctc.booking.repository.CheckInRepository;
import com.irctc.booking.repository.SimpleBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {
    
    @Mock
    private CheckInRepository checkInRepository;
    
    @Mock
    private SimpleBookingRepository bookingRepository;
    
    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;
    
    @InjectMocks
    private CheckInService checkInService;
    
    private SimpleBooking booking;
    private CheckIn checkIn;
    
    @BeforeEach
    void setUp() {
        booking = new SimpleBooking();
        booking.setId(1L);
        booking.setUserId(123L);
        booking.setTrainId(456L);
        booking.setPnrNumber("PNR123456");
        booking.setStatus("CONFIRMED");
        booking.setTotalFare(BigDecimal.valueOf(1000));
        booking.setBookingTime(LocalDateTime.now());
        
        checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setBookingId(1L);
        checkIn.setUserId(123L);
        checkIn.setPnrNumber("PNR123456");
        checkIn.setStatus("PENDING");
    }
    
    @Test
    void testPerformCheckIn_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(checkInRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        CheckIn result = checkInService.performCheckIn(1L, "MANUAL");
        
        assertNotNull(result);
        assertEquals("CHECKED_IN", result.getStatus());
        assertEquals("MANUAL", result.getCheckInMethod());
        assertNotNull(result.getSeatNumber());
        assertNotNull(result.getCoachNumber());
        assertNotNull(result.getCheckInTime());
        verify(checkInRepository, times(1)).save(any(CheckIn.class));
    }
    
    @Test
    void testPerformCheckIn_AlreadyCheckedIn() {
        checkIn.setStatus("CHECKED_IN");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(checkInRepository.findByBookingId(1L)).thenReturn(Optional.of(checkIn));
        
        CheckIn result = checkInService.performCheckIn(1L, "MANUAL");
        
        assertNotNull(result);
        assertEquals("CHECKED_IN", result.getStatus());
        verify(checkInRepository, never()).save(any(CheckIn.class));
    }
    
    @Test
    void testPerformCheckIn_BookingNotConfirmed() {
        booking.setStatus("PENDING");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        
        assertThrows(IllegalStateException.class, () -> {
            checkInService.performCheckIn(1L, "MANUAL");
        });
    }
    
    @Test
    void testGetCheckInStatus() {
        when(checkInRepository.findByBookingId(1L)).thenReturn(Optional.of(checkIn));
        
        CheckIn result = checkInService.getCheckInStatus(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
    }
    
    @Test
    void testGetCheckInStatus_NotFound() {
        when(checkInRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, () -> {
            checkInService.getCheckInStatus(1L);
        });
    }
    
    @Test
    void testGetPendingCheckIns() {
        when(checkInRepository.findPendingCheckInsForUser(eq(123L), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(checkIn));
        
        List<CheckIn> result = checkInService.getPendingCheckIns(123L);
        
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }
    
    @Test
    void testScheduleAutoCheckIn() {
        LocalDateTime departureTime = LocalDateTime.now().plusHours(24);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(checkInRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        CheckIn result = checkInService.scheduleAutoCheckIn(1L, departureTime);
        
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals("AUTO", result.getCheckInMethod());
        assertNotNull(result.getScheduledCheckInTime());
        assertEquals(departureTime, result.getDepartureTime());
        verify(checkInRepository, times(1)).save(any(CheckIn.class));
    }
    
    @Test
    void testScheduleAutoCheckIn_WithoutDepartureTime() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(checkInRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        CheckIn result = checkInService.scheduleAutoCheckIn(1L);
        
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getDepartureTime());
        assertNotNull(result.getScheduledCheckInTime());
    }
}

