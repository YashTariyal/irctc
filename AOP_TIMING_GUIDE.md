# AOP Timing Guide for IRCTC Application

## Overview

This guide explains how to use the Aspect-Oriented Programming (AOP) timing functionality implemented in the IRCTC application to measure and monitor API execution times.

## Features

### 1. Automatic API Timing
- **All REST Controller methods are automatically timed** without requiring any annotations
- Provides detailed logging with HTTP method, endpoint path, and execution time
- Includes warnings for slow APIs (execution time > 2 seconds)

### 2. Custom Method Timing
- Use `@ExecutionTime` annotation to time specific service methods
- Provides detailed logging with custom descriptions
- Includes warnings for slow operations (execution time > 1 second)

### 3. Comprehensive Logging
- Start and completion logs for each operation
- Error logging with timing information for failed operations
- Performance warnings for slow operations
- Request/response tracking for APIs

## Implementation Details

### Dependencies Added
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Key Components

#### 1. ExecutionTime Annotation
**Location**: `src/main/java/com/irctc_backend/irctc/annotation/ExecutionTime.java`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionTime {
    String value() default "";
}
```

#### 2. ExecutionTimeAspect
**Location**: `src/main/java/com/irctc_backend/irctc/aspect/ExecutionTimeAspect.java`

This aspect provides two main functionalities:
- **Custom method timing**: For methods annotated with `@ExecutionTime`
- **Automatic API timing**: For all REST controller methods

#### 3. AOP Configuration
**Location**: `src/main/java/com/irctc_backend/irctc/config/AopConfig.java`

Enables AOP functionality with `@EnableAspectJAutoProxy(proxyTargetClass = true)`

## Usage Examples

### 1. Automatic API Timing

All REST controller methods are automatically timed. No additional code required:

```java
@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        // This method will be automatically timed
        // Logs will show: "🌐 API Request: GET /{id} - getBookingById in class: BookingController"
        // And: "✅ API Response: GET /{id} - getBookingById in class: BookingController - Execution time: XXXms"
    }
}
```

### 2. Custom Method Timing

Add `@ExecutionTime` annotation to service methods:

```java
@Service
public class BookingService {
    
    @ExecutionTime("Create Train Booking")
    public Booking createBooking(Booking booking) {
        // This method will be timed with custom description
        // Logs will show: "🚀 Starting execution of: Create Train Booking in class: BookingService"
        // And: "✅ Completed: Create Train Booking in class: BookingService - Execution time: XXXms"
    }
    
    @ExecutionTime("Find Booking by PNR")
    public Optional<Booking> findByPnrNumber(String pnrNumber) {
        // Custom timing for PNR lookup
    }
    
    @ExecutionTime("Update Booking Status")
    public Booking updateBookingStatus(Long bookingId, Booking.BookingStatus status) {
        // Custom timing for status updates
    }
}
```

### 3. Service Layer Timing

```java
@Service
public class TrainService {
    
    @ExecutionTime("Create Train")
    public Train createTrain(Train train) {
        // Timing for train creation
    }
    
    @ExecutionTime("Search Trains Between Stations")
    public List<Train> getTrainsBetweenStations(String sourceStationCode, String destStationCode) {
        // Timing for train search operations
    }
}
```

## Log Output Examples

### API Timing Logs
```
🌐 API Request: GET /api/bookings/123 - getBookingById in class: BookingController
✅ API Response: GET /api/bookings/123 - getBookingById in class: BookingController - Execution time: 45ms
```

### Custom Method Timing Logs
```
🚀 Starting execution of: Create Train Booking in class: BookingService
✅ Completed: Create Train Booking in class: BookingService - Execution time: 234ms
```

### Slow Operation Warnings
```
⚠️  Slow API detected: POST /api/bookings - createBooking in class: BookingController took 2500ms
⚠️  Slow execution detected: Create Train Booking in class: BookingService took 1500ms
```

### Error Logs with Timing
```
❌ API Error: GET /api/bookings/999 - getBookingById in class: BookingController - Execution time: 12ms - Error: Booking not found
❌ Failed: Create Train Booking in class: BookingService - Execution time: 156ms - Error: Train not found
```

## Configuration

### Console Logging
The AOP timing logs are configured to appear in the console by default. The logs will show:
- **Real-time timing information** for all API calls and annotated methods
- **Performance warnings** for slow operations
- **Error tracking** with timing information
- **Emoji indicators** for easy visual identification

### Log4j2 Configuration
The timing logs use Log4j2 and will appear in both console and log files. The configuration in `src/main/resources/log4j2.xml` includes:

```xml
<!-- AOP Timing Aspect Logger - Ensures console output for timing logs -->
<Logger name="com.irctc_backend.irctc.aspect.ExecutionTimeAspect" level="INFO" additivity="false">
    <AppenderRef ref="Console"/>
    <AppenderRef ref="APIFile"/>
    <AppenderRef ref="JSONFile"/>
    <AppenderRef ref="ErrorRollingFile" level="error"/>
</Logger>
```

### Application Properties
The console logging is also configured in `src/main/resources/application.properties`:

```properties
logging.level.com.irctc_backend.irctc.aspect.ExecutionTimeAspect=INFO
```

### Performance Thresholds
You can modify the performance thresholds in `ExecutionTimeAspect.java`:

- **API slow threshold**: Currently 2000ms (2 seconds)
- **Service method slow threshold**: Currently 1000ms (1 second)

## Best Practices

### 1. When to Use @ExecutionTime
- **Use for complex business operations** that might be slow
- **Use for database-intensive operations** like searches and reports
- **Use for external service calls** like payment processing
- **Don't use for simple getters/setters** or trivial operations

### 2. Naming Conventions
- Use descriptive names in the annotation value
- Follow the format: "Action + Entity" (e.g., "Create Train Booking")
- Be consistent across similar operations

### 3. Performance Monitoring
- Monitor logs for slow operations
- Set up alerts for operations exceeding thresholds
- Use timing data for performance optimization

## Running the Application

### Start the Application
To see the AOP timing logs in real-time, run the application:

```bash
./mvnw spring-boot:run
```

### Console Output Example
When you run the application and make API calls, you'll see timing logs in the console like this:

```
🌐 API Request: GET /api/bookings/123 - getBookingById in class: BookingController
✅ API Response: GET /api/bookings/123 - getBookingById in class: BookingController - Execution time: 45ms

🚀 Starting execution of: Create Train Booking in class: BookingService
✅ Completed: Create Train Booking in class: BookingService - Execution time: 234ms

⚠️  Slow API detected: POST /api/bookings - createBooking in class: BookingController took 2500ms
⚠️  Slow execution detected: Create Train Booking in class: BookingService took 1500ms

❌ API Error: GET /api/bookings/999 - getBookingById in class: BookingController - Execution time: 12ms - Error: Booking not found
```

### Demo Script
Run the demo script to see the timing functionality in action:

```bash
./demo-aop-timing.sh
```

## Troubleshooting

### 1. AOP Not Working
- Ensure `@EnableAspectJAutoProxy` is present on main application class
- Check that `spring-boot-starter-aop` dependency is included
- Verify aspect class is in component scan path

### 2. No Timing Logs in Console
- Check that the ExecutionTimeAspect logger is configured in log4j2.xml
- Ensure logging level is set to INFO or lower
- Verify the aspect is being applied to target methods
- Check that console appender is included in the logger configuration

### 3. No Timing Logs
- Check log level configuration
- Ensure Log4j2 is properly configured
- Verify aspect is being applied to target methods

### 4. Performance Issues
- Monitor for excessive logging overhead
- Consider adjusting log levels for production
- Use sampling for high-frequency operations

## Extending the Functionality

### 1. Adding Custom Metrics
You can extend the aspect to collect additional metrics:

```java
@Around("@annotation(executionTime)")
public Object measureExecutionTime(ProceedingJoinPoint joinPoint, ExecutionTime executionTime) throws Throwable {
    // Add custom metrics collection here
    // e.g., Prometheus metrics, custom timing storage, etc.
}
```

### 2. Database Timing
Add specific timing for database operations:

```java
@Around("execution(* com.irctc_backend.irctc.repository.*.*(..))")
public Object measureDatabaseExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    // Database-specific timing logic
}
```

### 3. External Service Timing
Add timing for external service calls:

```java
@Around("execution(* com.irctc_backend.irctc.service.*.*(..)) && @annotation(ExternalService)")
public Object measureExternalServiceTime(ProceedingJoinPoint joinPoint) throws Throwable {
    // External service timing logic
}
```

## Conclusion

The AOP timing implementation provides comprehensive performance monitoring for your IRCTC application. It automatically tracks API performance and allows custom timing for critical business operations. Use this functionality to identify performance bottlenecks and optimize your application accordingly.
