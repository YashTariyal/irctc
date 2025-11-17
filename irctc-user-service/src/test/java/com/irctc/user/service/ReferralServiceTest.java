package com.irctc.user.service;

import com.irctc.user.dto.ReferralRewardRequest;
import com.irctc.user.entity.SimpleUser;
import com.irctc.user.entity.UserReferral;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.repository.UserReferralRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReferralServiceTest {

    @Mock
    private SimpleUserRepository userRepository;

    @Mock
    private UserReferralRepository referralRepository;

    @InjectMocks
    private ReferralService referralService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAssignReferralCodeOnRegistration() {
        SimpleUser newUser = new SimpleUser();
        newUser.setId(2L);

        SimpleUser referrer = new SimpleUser();
        referrer.setId(1L);
        referrer.setReferralPoints(0);
        referrer.setReferralCode("REF123");

        when(userRepository.findByReferralCode("REF123")).thenReturn(Optional.of(referrer));
        when(userRepository.save(any(SimpleUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        referralService.handlePostRegistration(newUser, "REF123");

        verify(referralRepository).save(any(UserReferral.class));
        verify(userRepository, atLeast(2)).save(any(SimpleUser.class));
    }

    @Test
    void shouldRecordBookingReward() {
        UserReferral referral = new UserReferral();
        referral.setId(10L);
        referral.setReferrerUserId(1L);
        referral.setRewardPoints(50);

        SimpleUser referrer = new SimpleUser();
        referrer.setId(1L);
        referrer.setReferralPoints(100);

        when(referralRepository.findByReferredUserId(2L)).thenReturn(Optional.of(referral));
        when(userRepository.findById(1L)).thenReturn(Optional.of(referrer));
        when(referralRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReferralRewardRequest request = new ReferralRewardRequest();
        request.setReferredUserId(2L);
        request.setBookingId(99L);
        request.setRewardPoints(120);

        referralService.recordBookingReward(request);

        assertThat(referral.getRewardPoints()).isEqualTo(170);
        verify(userRepository).save(referrer);
    }
}

