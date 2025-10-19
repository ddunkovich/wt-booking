package com.wtplanner.wtbooking.remote.repository;

import com.wtplanner.wtbooking.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
