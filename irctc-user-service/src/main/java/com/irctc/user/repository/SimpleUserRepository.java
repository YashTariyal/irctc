package com.irctc.user.repository;

import com.irctc.user.entity.SimpleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SimpleUserRepository extends JpaRepository<SimpleUser, Long> {
    Optional<SimpleUser> findByUsername(String username);
    Optional<SimpleUser> findByEmail(String email);
}
