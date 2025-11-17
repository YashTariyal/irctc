package com.irctc.booking.service;

import com.irctc.booking.client.ReferralServiceClient;
import com.irctc.booking.entity.SimpleBooking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

class ReferralRewardServiceTest {

    @Mock
    private ReferralServiceClient referralServiceClient;

    @InjectMocks
    private ReferralRewardService referralRewardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldInvokeReferralClient() {
        SimpleBooking booking = new SimpleBooking();
        booking.setId(1L);
        booking.setUserId(5L);
        booking.setPnrNumber("PNR123");

        referralRewardService.recordReferralForBooking(booking);

        ArgumentCaptor<com.irctc.booking.dto.ReferralRewardRequest> captor =
            ArgumentCaptor.forClass(com.irctc.booking.dto.ReferralRewardRequest.class);
        verify(referralServiceClient).awardReferralReward(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getReferredUserId()).isEqualTo(5L);
    }
}

