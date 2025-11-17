package com.irctc.booking.service;

import com.irctc.booking.client.TrainServiceClient;
import com.irctc.booking.dto.offline.*;
import com.irctc.booking.entity.OfflineAction;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.entity.SimplePassenger;
import com.irctc.booking.repository.SimpleBookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class OfflineSyncService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineSyncService.class);
    private static final long CACHE_TTL_MINUTES = 30;

    private final SimpleBookingRepository bookingRepository;
    private final TrainServiceClient trainServiceClient;
    private final OfflineActionService offlineActionService;
    private final Map<Long, CachedSchedule> scheduleCache = new ConcurrentHashMap<>();

    public OfflineSyncService(SimpleBookingRepository bookingRepository,
                              TrainServiceClient trainServiceClient,
                              OfflineActionService offlineActionService) {
        this.bookingRepository = bookingRepository;
        this.trainServiceClient = trainServiceClient;
        this.offlineActionService = offlineActionService;
    }

    public OfflineSyncResponse generateOfflineBundle(OfflineSyncRequest request) {
        OfflineSyncResponse response = new OfflineSyncResponse();
        OfflineSyncResponse.SyncMetadata metadata = new OfflineSyncResponse.SyncMetadata();
        metadata.setLastSyncTime(request.getLastSyncTime());
        metadata.setIncremental(request.getLastSyncTime() != null);

        if (request.isIncludeTickets() && request.getUserId() != null) {
            List<OfflineTicketDTO> tickets = toOfflineTickets(bookingRepository.findByUserId(request.getUserId()));
            response.setTickets(tickets);
            metadata.setTicketCount(tickets.size());
        } else {
            metadata.setTicketCount(0);
        }

        if (request.isIncludeSchedules()) {
            List<OfflineTrainScheduleDTO> schedules = buildScheduleSnapshot(request);
            response.setTrainSchedules(schedules);
            metadata.setScheduleCount(schedules.size());
        } else {
            metadata.setScheduleCount(0);
        }

        if (request.getUserId() != null) {
            List<OfflineActionResponse> pending = offlineActionService.getPendingActions(request.getUserId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            response.setPendingActions(pending);
            metadata.setPendingActionCount(pending.size());
        } else {
            metadata.setPendingActionCount(0);
        }

        metadata.setGeneratedAt(LocalDateTime.now());
        response.setMetadata(metadata);
        return response;
    }

    public List<OfflineTicketDTO> getTicketsForUser(Long userId) {
        return toOfflineTickets(bookingRepository.findByUserId(userId));
    }

    private List<OfflineTicketDTO> toOfflineTickets(List<SimpleBooking> bookings) {
        return bookings.stream()
            .map(this::mapToOfflineTicket)
            .sorted(Comparator.comparing(OfflineTicketDTO::getBookingTime).reversed())
            .toList();
    }

    private OfflineTicketDTO mapToOfflineTicket(SimpleBooking booking) {
        OfflineTicketDTO dto = new OfflineTicketDTO();
        dto.setBookingId(booking.getId());
        dto.setPnrNumber(booking.getPnrNumber());
        dto.setTrainId(booking.getTrainId());
        dto.setStatus(booking.getStatus());
        dto.setTotalFare(booking.getTotalFare());
        dto.setBookingTime(booking.getBookingTime());
        dto.setLastUpdated(LocalDateTime.now());

        if (booking.getPassengers() != null) {
            List<OfflinePassengerDTO> passengers = booking.getPassengers().stream()
                .map(this::mapToPassengerDto)
                .toList();
            dto.setPassengers(passengers);
        } else {
            dto.setPassengers(Collections.emptyList());
        }
        return dto;
    }

    private OfflinePassengerDTO mapToPassengerDto(SimplePassenger passenger) {
        OfflinePassengerDTO dto = new OfflinePassengerDTO();
        dto.setPassengerId(passenger.getId());
        dto.setName(passenger.getName());
        dto.setAge(passenger.getAge());
        dto.setGender(passenger.getGender());
        dto.setSeatNumber(passenger.getSeatNumber());
        dto.setIdProofType(passenger.getIdProofType());
        return dto;
    }

    private List<OfflineTrainScheduleDTO> buildScheduleSnapshot(OfflineSyncRequest request) {
        Set<Long> trainIds = new HashSet<>();
        if (request.getTrainIds() != null) {
            trainIds.addAll(request.getTrainIds());
        }
        if (request.getUserId() != null) {
            bookingRepository.findByUserId(request.getUserId()).stream()
                .map(SimpleBooking::getTrainId)
                .forEach(trainIds::add);
        }

        List<String> trainNumbers = request.getTrainNumbers() != null ? request.getTrainNumbers() : Collections.emptyList();

        List<OfflineTrainScheduleDTO> schedules = new ArrayList<>();

        for (Long trainId : trainIds) {
            fetchScheduleByTrainId(trainId).ifPresent(schedules::add);
        }

        for (String trainNumber : trainNumbers) {
            fetchScheduleByTrainNumber(trainNumber).ifPresent(dto -> {
                boolean duplicate = schedules.stream()
                    .anyMatch(existing -> Objects.equals(existing.getTrainNumber(), dto.getTrainNumber()));
                if (!duplicate) {
                    schedules.add(dto);
                }
            });
        }

        return schedules;
    }

    private Optional<OfflineTrainScheduleDTO> fetchScheduleByTrainId(Long trainId) {
        CachedSchedule cached = scheduleCache.get(trainId);
        if (cached != null && !cached.isExpired()) {
            return Optional.of(cached.payload());
        }
        try {
            TrainServiceClient.TrainResponse train = trainServiceClient.getTrainById(trainId);
            OfflineTrainScheduleDTO dto = mapToScheduleDto(train);
            scheduleCache.put(trainId, new CachedSchedule(dto));
            return Optional.of(dto);
        } catch (Exception ex) {
            logger.warn("Unable to fetch train schedule for trainId {}: {}", trainId, ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<OfflineTrainScheduleDTO> fetchScheduleByTrainNumber(String trainNumber) {
        try {
            TrainServiceClient.TrainResponse train = trainServiceClient.getTrainByNumber(trainNumber);
            OfflineTrainScheduleDTO dto = mapToScheduleDto(train);
            if (train.getId() != null) {
                scheduleCache.put(train.getId(), new CachedSchedule(dto));
            }
            return Optional.of(dto);
        } catch (Exception ex) {
            logger.warn("Unable to fetch train schedule for trainNumber {}: {}", trainNumber, ex.getMessage());
            return Optional.empty();
        }
    }

    private OfflineTrainScheduleDTO mapToScheduleDto(TrainServiceClient.TrainResponse train) {
        OfflineTrainScheduleDTO dto = new OfflineTrainScheduleDTO();
        dto.setTrainId(train.getId());
        dto.setTrainNumber(train.getTrainNumber());
        dto.setTrainName(train.getTrainName());
        dto.setSourceStation(train.getSourceStation());
        dto.setDestinationStation(train.getDestinationStation());
        dto.setTrainType(train.getTrainType());
        dto.setTrainClass(train.getTrainClass());
        dto.setAvailableSeats(train.getAvailableSeats());
        dto.setBaseFare(train.getBaseFare());
        dto.setSnapshotTime(LocalDateTime.now());
        return dto;
    }

    private OfflineActionResponse toResponse(OfflineAction action) {
        OfflineActionResponse response = new OfflineActionResponse();
        response.setId(action.getId());
        response.setUserId(action.getUserId());
        response.setBookingId(action.getBookingId());
        response.setActionType(action.getActionType());
        response.setStatus(action.getStatus());
        response.setFailureReason(action.getFailureReason());
        response.setQueuedAt(action.getQueuedAt());
        response.setProcessedAt(action.getProcessedAt());
        return response;
    }

    private record CachedSchedule(OfflineTrainScheduleDTO payload, LocalDateTime cachedAt) {
        CachedSchedule(OfflineTrainScheduleDTO payload) {
            this(payload, LocalDateTime.now());
        }

        boolean isExpired() {
            return cachedAt.plusMinutes(CACHE_TTL_MINUTES).isBefore(LocalDateTime.now());
        }
    }
}

