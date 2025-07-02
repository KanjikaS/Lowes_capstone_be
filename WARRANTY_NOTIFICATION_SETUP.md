# Warranty Expiry Notification System

## Overview
This system automatically sends email notifications to homeowners 7 days before their appliance warranties expire. It includes a scheduled cron job that runs daily and manual trigger endpoints for testing.

## Features
- **Automated Daily Check**: Runs every day at 9:00 AM to check for warranties expiring in 7 days
- **Email Notifications**: Sends detailed warranty expiry alerts to homeowner email addresses
- **Manual Triggers**: Admin endpoints to manually trigger checks for testing
- **Flexible Scheduling**: Can check warranties expiring in any number of days

## Setup Instructions

### 1. Email Configuration
Update `application.properties` with your email server settings:

```properties
# === Email Configuration ===
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

**For Gmail Setup:**
1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password: Google Account → Security → App passwords
3. Use the generated app password (not your regular password)

### 2. Dependencies
The following dependency has been added to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### 3. Scheduling Configuration
Scheduling is enabled in the main application class with `@EnableScheduling`.

## API Endpoints

### Admin-Only Endpoints
All notification endpoints require admin authentication.

#### 1. Manual Warranty Check
```http
POST /api/notifications/check-warranty-expiry
Authorization: Bearer {admin-jwt-token}
```
**Description**: Manually triggers warranty expiry check for appliances expiring in 7 days.

#### 2. Check Specific Days
```http
POST /api/notifications/check-warranty-expiry/{days}
Authorization: Bearer {admin-jwt-token}
```
**Description**: Triggers warranty expiry check for appliances expiring in specified number of days.
**Example**: `POST /api/notifications/check-warranty-expiry/3` checks warranties expiring in 3 days.

#### 3. View Appliances Expiring Soon
```http
GET /api/notifications/expiring-soon
Authorization: Bearer {admin-jwt-token}
```
**Description**: Returns list of appliances with warranties expiring in 7 days.

#### 4. View Appliances Expiring in Specific Days
```http
GET /api/notifications/expiring-in/{days}
Authorization: Bearer {admin-jwt-token}
```
**Description**: Returns list of appliances with warranties expiring in specified days.

#### 5. View Appliances Expiring Next Month
```http
GET /api/notifications/expiring-next-month
Authorization: Bearer {admin-jwt-token}
```
**Description**: Returns list of appliances with warranties expiring in the next 30 days.

## Scheduled Job Details

### Cron Expression
The warranty check runs with cron expression: `"0 0 9 * * ?"`
- **0 seconds**
- **0 minutes**
- **9 hours** (9:00 AM)
- **Every day of month**
- **Every month**
- **Any day of week**

### What the Job Does
1. Calculates date 7 days from current date
2. Queries database for appliances with warranty expiring on that date
3. For each appliance found:
   - Sends email notification to homeowner
   - Logs SMS notification message
   - Logs success/failure for tracking

## Notification Content

### Email Notification
- **Subject**: "Warranty Expiry Alert - {Brand} {Model}"
- **Content**: Personalized message with:
  - Homeowner name
  - Appliance details (brand, model, serial number)
  - Warranty expiry date
  - Recommendations for maintenance
  - Contact information
