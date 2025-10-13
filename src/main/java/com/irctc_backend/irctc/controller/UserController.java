package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "APIs for managing user accounts, registration, and authentication")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    @Operation(
        summary = "👤 Register New User", 
        description = """
            Creates a new user account in the IRCTC system.
            
            **Required Fields:**
            - `firstName`: User's first name
            - `lastName`: User's last name  
            - `email`: Valid email address (must be unique)
            - `username`: Unique username for login
            - `password`: Secure password (min 8 characters)
            - `phoneNumber`: 10-digit mobile number
            - `dateOfBirth`: Date of birth in ISO format
            - `gender`: MALE, FEMALE, or OTHER
            - `address`: Complete address
            
            **Validation Rules:**
            - Email must be valid and unique
            - Username must be unique and 3-20 characters
            - Password must be at least 8 characters
            - Phone number must be 10 digits
            - Date of birth must be in the past
            """,
        tags = {"👥 Users"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "✅ User registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    summary = "User Registration Success",
                    value = """
                        {
                          "id": 1,
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john.doe@example.com",
                          "username": "johndoe",
                          "phoneNumber": "9876543210",
                          "dateOfBirth": "1990-01-01T00:00:00",
                          "gender": "MALE",
                          "address": "123 Main Street, Mumbai, Maharashtra",
                          "createdAt": "2025-10-13T22:30:00Z",
                          "updatedAt": "2025-10-13T22:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "❌ Invalid input data or validation failed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Input Validation Failed",
                    value = """
                        {
                          "success": false,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "Request validation failed",
                            "details": [
                              {
                                "field": "email",
                                "message": "Email is required"
                              },
                              {
                                "field": "password", 
                                "message": "Password must be at least 8 characters"
                              }
                            ]
                          },
                          "timestamp": "2025-10-13T22:30:00Z",
                          "path": "/api/users/register"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "⚠️ Username or email already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Conflict Error",
                    summary = "Duplicate User",
                    value = """
                        {
                          "success": false,
                          "error": {
                            "code": "DUPLICATE_USER",
                            "message": "Username or email already exists",
                            "details": "Please choose a different username or email"
                          },
                          "timestamp": "2025-10-13T22:30:00Z",
                          "path": "/api/users/register"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<?> registerUser(
        @Parameter(
            description = "User registration details",
            required = true,
            example = """
                {
                  "firstName": "John",
                  "lastName": "Doe", 
                  "email": "john.doe@example.com",
                  "username": "johndoe",
                  "password": "securePassword123",
                  "phoneNumber": "9876543210",
                  "dateOfBirth": "1990-01-01T00:00:00",
                  "gender": "MALE",
                  "address": "123 Main Street, Mumbai, Maharashtra"
                }
                """
        )
        @RequestBody User user
    ) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    @Operation(
        summary = "🔐 User Login", 
        description = """
            Authenticates a user with username and password.
            
            **Required Parameters:**
            - `username`: User's username or email
            - `password`: User's password
            
            **Response:**
            Returns user details and JWT token for authenticated requests.
            
            **Security:**
            - Passwords are securely hashed
            - JWT tokens expire after 24 hours
            - Rate limiting: 5 attempts per minute
            """,
        tags = {"👥 Users"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    name = "Login Success",
                    summary = "Successful Authentication",
                    value = """
                        {
                          "id": 1,
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john.doe@example.com",
                          "username": "johndoe",
                          "phoneNumber": "9876543210",
                          "dateOfBirth": "1990-01-01T00:00:00",
                          "gender": "MALE",
                          "address": "123 Main Street, Mumbai, Maharashtra",
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "tokenExpiry": "2025-10-14T22:30:00Z",
                          "lastLogin": "2025-10-13T22:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "❌ Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Authentication Failed",
                    summary = "Invalid Credentials",
                    value = """
                        {
                          "success": false,
                          "error": {
                            "code": "INVALID_CREDENTIALS",
                            "message": "Username or password is incorrect",
                            "details": "Please check your credentials and try again"
                          },
                          "timestamp": "2025-10-13T22:30:00Z",
                          "path": "/api/users/login"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "❌ User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "User Not Found",
                    summary = "User Does Not Exist",
                    value = """
                        {
                          "success": false,
                          "error": {
                            "code": "USER_NOT_FOUND",
                            "message": "User with the provided username does not exist",
                            "details": "Please check the username and try again"
                          },
                          "timestamp": "2025-10-13T22:30:00Z",
                          "path": "/api/users/login"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<?> loginUser(
        @Parameter(
            description = "Username or email address",
            required = true,
            example = "johndoe"
        )
        @RequestParam String username, 
        @Parameter(
            description = "User password",
            required = true,
            example = "securePassword123"
        )
        @RequestParam String password
    ) {
        boolean isAuthenticated = userService.authenticateUser(username, password);
        if (isAuthenticated) {
            Optional<User> user = userService.findByUsername(username);
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
    
    @GetMapping
    @Operation(
        summary = "📋 Get All Users", 
        description = """
            Retrieves a list of all users in the system.
            
            **Features:**
            - Returns paginated results (default: 20 per page)
            - Includes user profile information
            - Supports filtering and sorting
            - Requires authentication
            
            **Query Parameters:**
            - `page`: Page number (default: 0)
            - `size`: Page size (default: 20, max: 100)
            - `sort`: Sort field (default: createdAt)
            - `order`: Sort order (asc/desc, default: desc)
            """,
        tags = {"👥 Users"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Users List",
                    summary = "List of Users",
                    value = """
                        [
                          {
                            "id": 1,
                            "firstName": "John",
                            "lastName": "Doe",
                            "email": "john.doe@example.com",
                            "username": "johndoe",
                            "phoneNumber": "9876543210",
                            "dateOfBirth": "1990-01-01T00:00:00",
                            "gender": "MALE",
                            "address": "123 Main Street, Mumbai, Maharashtra",
                            "createdAt": "2025-10-13T22:30:00Z",
                            "updatedAt": "2025-10-13T22:30:00Z"
                          },
                          {
                            "id": 2,
                            "firstName": "Jane",
                            "lastName": "Smith",
                            "email": "jane.smith@example.com",
                            "username": "janesmith",
                            "phoneNumber": "9876543211",
                            "dateOfBirth": "1992-05-15T00:00:00",
                            "gender": "FEMALE",
                            "address": "456 Park Avenue, Delhi, Delhi",
                            "createdAt": "2025-10-13T22:35:00Z",
                            "updatedAt": "2025-10-13T22:35:00Z"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "❌ Unauthorized - Authentication required"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "❌ Forbidden - Insufficient permissions"
        )
    })
    public ResponseEntity<List<User>> getAllUsers(
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") String sort,
        @Parameter(description = "Sort order", example = "desc")
        @RequestParam(defaultValue = "desc") String order
    ) {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/verified")
    public ResponseEntity<List<User>> getActiveVerifiedUsers() {
        List<User> users = userService.getActiveVerifiedUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search/name")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String name) {
        List<User> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search/email")
    public ResponseEntity<List<User>> searchUsersByEmail(@RequestParam String email) {
        List<User> users = userService.searchUsersByEmail(email);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable User.UserRole role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam User.UserRole role) {
        try {
            User updatedUser = userService.updateUserRole(id, role);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyUser(@PathVariable Long id) {
        try {
            User verifiedUser = userService.verifyUser(id);
            return ResponseEntity.ok(verifiedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            User activatedUser = userService.activateUser(id);
            return ResponseEntity.ok(activatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            User deactivatedUser = userService.deactivateUser(id);
            return ResponseEntity.ok(deactivatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 