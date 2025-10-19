package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.MealOrderRequest;
import com.irctc_backend.irctc.dto.MealOrderResponse;
import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MealBookingService {

    @Autowired
    private MealVendorRepository mealVendorRepository;

    @Autowired
    private MealItemRepository mealItemRepository;

    @Autowired
    private MealOrderRepository mealOrderRepository;

    @Autowired
    private MealOrderItemRepository mealOrderItemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private UserRepository userRepository;

    public List<MealVendor> getVendorsByStation(String stationCode) {
        return mealVendorRepository.findByStationCodeAndIsActiveTrue(stationCode);
    }

    public List<MealVendor> getAllActiveVendors() {
        return mealVendorRepository.findByIsActiveTrue();
    }

    public List<MealItem> getMenuByVendor(Long vendorId) {
        return mealItemRepository.findByVendorIdAndIsAvailableTrue(vendorId);
    }

    public List<MealItem> getMenuByVendorAndCategory(Long vendorId, MealItem.MealCategory category) {
        return mealItemRepository.findByVendorIdAndCategoryAndIsAvailableTrue(vendorId, category);
    }

    public List<MealItem> getMenuByStation(String stationCode) {
        return mealItemRepository.findByStationCodeOrderByVendorRatingAndPrice(stationCode);
    }

    public MealOrderResponse placeOrder(MealOrderRequest request, Long userId) {
        // Validate booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Validate train
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new RuntimeException("Train not found"));

        // Validate vendor
        MealVendor vendor = mealVendorRepository.findByIdAndIsActiveTrue(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create order
        MealOrder order = new MealOrder();
        order.setUser(user);
        order.setBooking(booking);
        order.setTrain(train);
        order.setVendor(vendor);
        order.setOrderNumber(generateOrderNumber());
        order.setDeliveryStationCode(request.getDeliveryStationCode());
        order.setDeliveryStationName(request.getDeliveryStationName());
        order.setDeliveryTime(request.getDeliveryTime());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setContactPhone(request.getContactPhone());
        order.setSeatNumber(request.getSeatNumber());
        order.setCoachNumber(request.getCoachNumber());
        order.setOrderStatus(MealOrder.OrderStatus.PENDING);
        order.setPaymentStatus(MealOrder.PaymentStatus.PENDING);

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (MealOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            MealItem mealItem = mealItemRepository.findByIdAndIsAvailableTrue(itemRequest.getMealItemId())
                    .orElseThrow(() -> new RuntimeException("Meal item not found: " + itemRequest.getMealItemId()));

            BigDecimal itemTotal = mealItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        order = mealOrderRepository.save(order);

        // Create order items
        for (MealOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            MealItem mealItem = mealItemRepository.findByIdAndIsAvailableTrue(itemRequest.getMealItemId())
                    .orElseThrow(() -> new RuntimeException("Meal item not found: " + itemRequest.getMealItemId()));

            MealOrderItem orderItem = new MealOrderItem();
            orderItem.setOrder(order);
            orderItem.setMealItem(mealItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(mealItem.getPrice());
            orderItem.setTotalPrice(mealItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());

            mealOrderItemRepository.save(orderItem);
        }

        return convertToResponse(order);
    }

    public MealOrderResponse getOrderByOrderNumber(String orderNumber) {
        MealOrder order = mealOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToResponse(order);
    }

    public List<MealOrderResponse> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<MealOrder> orders = mealOrderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<MealOrderResponse> getUserOrdersPaginated(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<MealOrder> orders = mealOrderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return orders.map(this::convertToResponse);
    }

    public MealOrderResponse updateOrderStatus(String orderNumber, MealOrder.OrderStatus status) {
        MealOrder order = mealOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setOrderStatus(status);
        order = mealOrderRepository.save(order);

        return convertToResponse(order);
    }

    public MealOrderResponse cancelOrder(String orderNumber) {
        MealOrder order = mealOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() == MealOrder.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        order.setOrderStatus(MealOrder.OrderStatus.CANCELLED);
        order = mealOrderRepository.save(order);

        return convertToResponse(order);
    }

    public List<MealOrderResponse> getVendorOrders(Long vendorId, MealOrder.OrderStatus status) {
        List<MealOrder> orders = mealOrderRepository.findByVendorIdAndOrderStatus(vendorId, status);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private String generateOrderNumber() {
        return "MO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private MealOrderResponse convertToResponse(MealOrder order) {
        MealOrderResponse response = new MealOrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setBookingId(order.getBooking().getId());
        response.setPnrNumber(order.getBooking().getPnrNumber());
        response.setTrainId(order.getTrain().getId());
        response.setTrainNumber(order.getTrain().getTrainNumber());
        response.setTrainName(order.getTrain().getTrainName());
        response.setVendorId(order.getVendor().getId());
        response.setVendorName(order.getVendor().getVendorName());
        response.setStationCode(order.getVendor().getStationCode());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderStatus(order.getOrderStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setDeliveryStationCode(order.getDeliveryStationCode());
        response.setDeliveryStationName(order.getDeliveryStationName());
        response.setDeliveryTime(order.getDeliveryTime());
        response.setSpecialInstructions(order.getSpecialInstructions());
        response.setContactPhone(order.getContactPhone());
        response.setSeatNumber(order.getSeatNumber());
        response.setCoachNumber(order.getCoachNumber());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Set order items
        List<MealOrderItem> orderItems = mealOrderItemRepository.findByOrderId(order.getId());
        List<MealOrderResponse.OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
        response.setOrderItems(itemResponses);

        return response;
    }

    private MealOrderResponse.OrderItemResponse convertToItemResponse(MealOrderItem orderItem) {
        MealOrderResponse.OrderItemResponse response = new MealOrderResponse.OrderItemResponse();
        response.setId(orderItem.getId());
        response.setMealItemId(orderItem.getMealItem().getId());
        response.setItemName(orderItem.getMealItem().getItemName());
        response.setDescription(orderItem.getMealItem().getDescription());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setTotalPrice(orderItem.getTotalPrice());
        response.setSpecialInstructions(orderItem.getSpecialInstructions());
        return response;
    }
}
