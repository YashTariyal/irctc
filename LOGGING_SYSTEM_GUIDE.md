# 📝 IRCTC Backend Logging System Guide

## 🎯 **Overview**

The IRCTC backend now includes a comprehensive Log4j2 logging system that provides:
- **30-day log retention** with automatic rotation and cleanup
- **Structured logging** with JSON format support
- **Multiple log files** for different concerns (API, Database, Security, Kafka, etc.)
- **Performance monitoring** with request timing and slow request detection
- **MDC (Mapped Diagnostic Context)** support for request tracing

## 📁 **Log File Structure**

```
logs/
├── irctc-application.log      # General application logs
├── irctc-api.log             # API request/response logs
├── irctc-database.log        # Database operation logs
├── irctc-security.log        # Security-related logs
├── irctc-kafka.log           # Kafka event logs
├── irctc-error.log           # Error logs only
├── irctc-json.log            # Structured JSON logs
└── archive/                  # Compressed rotated logs
    ├── irctc-2024-08-17-1.log.gz
    ├── irctc-api-2024-08-17-1.log.gz
    └── ...
```

## ⚙️ **Configuration Details**

### **Log Rotation Policy**
- **Time-based**: Daily rotation at midnight
- **Size-based**: 100MB maximum file size
- **Retention**: 30 days maximum
- **Compression**: GZIP compression for archived files

### **Log Levels**
- **ERROR**: System errors and exceptions
- **WARN**: Warning conditions
- **INFO**: General information (default)
- **DEBUG**: Detailed debugging information
- **TRACE**: Very detailed debugging information

## 🔧 **Log Categories**

### **1. Application Logs (`irctc-application.log`)**
- General application events
- Business logic operations
- Service layer activities

### **2. API Logs (`irctc-api.log`)**
- HTTP request/response details
- API endpoint access
- Request timing and performance

### **3. Database Logs (`irctc-database.log`)**
- SQL queries (when debug enabled)
- Database connection events
- Transaction management

### **4. Security Logs (`irctc-security.log`)**
- Authentication events
- Authorization failures
- Security-related activities

### **5. Kafka Logs (`irctc-kafka.log`)**
- Event publishing
- Consumer activities
- Topic management

### **6. Error Logs (`irctc-error.log`)**
- All ERROR level logs
- Exception stack traces
- System failures

### **7. JSON Logs (`irctc-json.log`)**
- Structured logging in JSON format
- Machine-readable logs
- Easy parsing for log analysis tools

## 🛠️ **Usage Examples**

### **Structured Logging with MDC**

```java
// Set request context
LoggingUtil.setRequestContext(requestId, userId, operation);

// Log business operation
LoggingUtil.logBusinessOperation("CREATE_BOOKING", "BOOKING", bookingId, userId, details);

// Log database operation
LoggingUtil.logDatabaseOperation("CREATE", "BOOKING", bookingId, requestId, startTime);

// Log Kafka event
LoggingUtil.logKafkaEvent("BOOKING_CONFIRMED", "booking-confirmed", requestId, "SUCCESS");

// Log security event
LoggingUtil.logSecurityEvent("LOGIN", userId, "User logged in successfully", "SUCCESS");

// Log error with context
LoggingUtil.logError("CREATE_BOOKING", "BOOKING", bookingId, userId, "Failed to create booking", exception);
```

### **Automatic API Logging**

The `LoggingInterceptor` automatically logs:
- Request start with timing
- Request completion with status code and duration
- Slow requests (> 1 second)
- Errors and exceptions

## 📊 **Log Patterns**

### **Standard Log Pattern**
```
2024-08-17 21:05:30.123 [http-nio-8080-exec-1] INFO  c.i.i.controller.UserController - API Request Started - Endpoint: /api/users, Method: GET, RequestId: a1b2c3d4e5f6g7h8
```

### **JSON Log Pattern**
```json
{
  "timestamp": "2024-08-17 21:05:30.123",
  "level": "INFO",
  "logger": "com.irctc_backend.irctc.controller.UserController",
  "thread": "http-nio-8080-exec-1",
  "message": "API Request Started - Endpoint: /api/users, Method: GET, RequestId: a1b2c3d4e5f6g7h8",
  "requestId": "a1b2c3d4e5f6g7h8",
  "userId": "SYSTEM",
  "operation": "GET /api/users"
}
```

## 🔍 **Monitoring and Analysis**

### **Key Metrics to Monitor**
1. **Request Response Times**: Track API performance
2. **Error Rates**: Monitor system health
3. **Database Query Performance**: Identify slow queries
4. **Kafka Event Processing**: Monitor event system health
5. **Security Events**: Track authentication and authorization

### **Log Analysis Commands**

```bash
# Count errors in the last hour
grep "$(date '+%Y-%m-%d %H')" logs/irctc-error.log | wc -l

# Find slow requests (> 1 second)
grep "slow_request" logs/irctc-application.log

# Monitor API requests
tail -f logs/irctc-api.log

# Check Kafka events
grep "Kafka Event" logs/irctc-kafka.log

# Analyze JSON logs
jq '.level' logs/irctc-json.log | sort | uniq -c
```

## 🚀 **Performance Features**

### **Automatic Features**
- **Request ID Generation**: Unique ID for each request
- **Performance Tracking**: Automatic timing of operations
- **Slow Request Detection**: Alerts for requests > 1 second
- **Context Preservation**: MDC context across async operations

### **Manual Performance Logging**
```java
// Log performance metric
LoggingUtil.logPerformanceMetric("database_query_time", 150, "ms");
```

## 🔧 **Configuration Customization**

### **Modifying Log Levels**
In `application.properties`:
```properties
logging.level.com.irctc_backend.irctc=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.apache.kafka=INFO
```

### **Changing Log File Paths**
In `log4j2.xml`:
```xml
<Property name="LOG_DIR">/var/log/irctc</Property>
```

### **Adjusting Retention Period**
In `log4j2.xml`:
```xml
<Property name="MAX_FILES">60</Property>  <!-- 60 days -->
```

## 🛡️ **Security Considerations**

### **Sensitive Data Protection**
- Passwords and tokens are automatically filtered
- Authorization headers are not logged
- Personal data is masked in logs

### **Log File Permissions**
- Log files should have restricted permissions
- Archive directory should be secured
- Regular log rotation prevents disk space issues

## 📈 **Scaling Considerations**

### **High Volume Logging**
- JSON logs are more efficient for parsing
- Log rotation prevents memory issues
- Compression reduces storage requirements

### **Distributed Logging**
- Request IDs enable request tracing across services
- Structured logs work well with centralized logging systems
- JSON format is compatible with ELK stack

## 🔄 **Maintenance Tasks**

### **Daily Tasks**
- Monitor log file sizes
- Check for error patterns
- Verify log rotation is working

### **Weekly Tasks**
- Review slow request patterns
- Analyze security events
- Clean up old archive files if needed

### **Monthly Tasks**
- Review log retention policies
- Analyze performance trends
- Update logging configuration if needed

## 📞 **Troubleshooting**

### **Common Issues**
1. **Log files not created**: Check directory permissions
2. **Log rotation not working**: Verify file patterns and policies
3. **High disk usage**: Check log file sizes and retention settings
4. **Performance impact**: Monitor log level settings

### **Debug Commands**
```bash
# Check log4j2 configuration
java -Dlog4j2.debug=true -jar irctc.jar

# Monitor log file growth
watch -n 5 'ls -lh logs/'

# Check for configuration errors
grep "ERROR" logs/irctc-error.log | tail -10
```

---

## 🎉 **Benefits**

✅ **30-day log retention** with automatic cleanup  
✅ **Structured logging** for easy analysis  
✅ **Performance monitoring** with automatic timing  
✅ **Security event tracking** for audit trails  
✅ **Kafka event logging** for event system monitoring  
✅ **JSON format support** for machine-readable logs  
✅ **Request tracing** with unique request IDs  
✅ **Automatic log rotation** to prevent disk space issues  
✅ **Multiple log files** for different concerns  
✅ **MDC support** for contextual logging  

Your IRCTC backend now has enterprise-grade logging capabilities! 🚀 