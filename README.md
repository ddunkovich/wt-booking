# WT Booking System

## Overview
WT Booking System is a Spring Boot monolith with REST API for managing bookings of Units.

## Features
Units can be 
 - added 
 - booked
 - cancelled
 - paid (emulated)
 - auto-canceled after 15 minutes if unpaid.

Additional feature: the number of available Units is cached (Redis) and updated on status changes.

## Implementation Notes
- Run docker:
```bash
docker compose -f docker/docker-compose.yml -p wt-booking up -d
```
- BigDecimal is used for monetary calculations
- DTOs and entities are separate to allow introducing Domain Objects later
- Mapper is simple, can be replaced with MapStruct
- Redis cache was chosen for supports multi-instance
- LazyCache used to minimize database reads
- ToDo: For transactional operations in Redis, using Lua scripts is recommended
- UUID used as ID to safely work with Lombok + JPA and avoid ID collisions
- app_user table is used instead of user to avoid unnecessary escaping
- ToDo: Implement global Spring @ControllerAdvice ErrorHandler for consistent API error responses
- ToDo: Add retry logic (i.e. resielnce4j) for transient failures (e.g., database or payment processing)
