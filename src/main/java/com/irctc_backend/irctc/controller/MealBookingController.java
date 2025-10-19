package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.MealOrderRequest;
import com.irctc_backend.irctc.dto.MealOrderResponse;
import com.irctc_backend.irctc.entity.MealItem;
import com.irctc_backend.irctc.entity.MealOrder;
import com.irctc_backend.irctc.entity.MealVendor;
import com.irctc_backend.irctc.service.MealBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-booking")
@Tag(name = "Meal Booking", description = "APIs for meal and catering booking")
public class MealBookingController {

    @Autowired
    private MealBookingService mealBookingService;

    @GetMapping("/health")
    @Operation(summary = "Health check for meal booking service")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Meal Booking Service is running!");
    }

    @GetMapping("/vendors")
    @Operation(summary = "Get all active meal vendors")
    public ResponseEntity<List<MealVendor>> getAllVendors() {
        List<MealVendor> vendors = mealBookingService.getAllActiveVendors();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/vendors/station/{stationCode}")
    @Operation(summary = "Get vendors by station code")
    public ResponseEntity<List<MealVendor>> getVendorsByStation(
            @Parameter(description = "Station code") @PathVariable String stationCode) {
        List<MealVendor> vendors = mealBookingService.getVendorsByStation(stationCode);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/menu/vendor/{vendorId}")
    @Operation(summary = "Get menu by vendor ID")
    public ResponseEntity<List<MealItem>> getMenuByVendor(
            @Parameter(description = "Vendor ID") @PathVariable Long vendorId) {
        List<MealItem> menu = mealBookingService.getMenuByVendor(vendorId);
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/menu/vendor/{vendorId}/category/{category}")
    @Operation(summary = "Get menu by vendor and category")
    public ResponseEntity<List<MealItem>> getMenuByVendorAndCategory(
            @Parameter(description = "Vendor ID") @PathVariable Long vendorId,
            @Parameter(description = "Meal category") @PathVariable MealItem.MealCategory category) {
        List<MealItem> menu = mealBookingService.getMenuByVendorAndCategory(vendorId, category);
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/menu/station/{stationCode}")
    @Operation(summary = "Get menu by station code")
    public ResponseEntity<List<MealItem>> getMenuByStation(
            @Parameter(description = "Station code") @PathVariable String stationCode) {
        List<MealItem> menu = mealBookingService.getMenuByStation(stationCode);
        return ResponseEntity.ok(menu);
    }

    @PostMapping("/order")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Place a meal order")
    public ResponseEntity<MealOrderResponse> placeOrder(
            @RequestBody MealOrderRequest request,
            @Parameter(description = "User ID") @RequestParam Long userId) {
        MealOrderResponse response = mealBookingService.placeOrder(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<MealOrderResponse> getOrderByOrderNumber(
            @Parameter(description = "Order number") @PathVariable String orderNumber) {
        MealOrderResponse response = mealBookingService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user orders")
    public ResponseEntity<List<MealOrderResponse>> getUserOrders(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        List<MealOrderResponse> orders = mealBookingService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/user/{userId}/paginated")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user orders with pagination")
    public ResponseEntity<Page<MealOrderResponse>> getUserOrdersPaginated(
            @Parameter(description = "User ID") @PathVariable Long userId,
            Pageable pageable) {
        Page<MealOrderResponse> orders = mealBookingService.getUserOrdersPaginated(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/order/{orderNumber}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Update order status")
    public ResponseEntity<MealOrderResponse> updateOrderStatus(
            @Parameter(description = "Order number") @PathVariable String orderNumber,
            @Parameter(description = "New order status") @RequestParam MealOrder.OrderStatus status) {
        MealOrderResponse response = mealBookingService.updateOrderStatus(orderNumber, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/order/{orderNumber}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<MealOrderResponse> cancelOrder(
            @Parameter(description = "Order number") @PathVariable String orderNumber) {
        MealOrderResponse response = mealBookingService.cancelOrder(orderNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/vendor/{vendorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Get vendor orders by status")
    public ResponseEntity<List<MealOrderResponse>> getVendorOrders(
            @Parameter(description = "Vendor ID") @PathVariable Long vendorId,
            @Parameter(description = "Order status") @RequestParam MealOrder.OrderStatus status) {
        List<MealOrderResponse> orders = mealBookingService.getVendorOrders(vendorId, status);
        return ResponseEntity.ok(orders);
    }
}
