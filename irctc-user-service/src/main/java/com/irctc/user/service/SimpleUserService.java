package com.irctc.user.service;

import com.irctc.user.entity.SimpleUser;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * User Service with Caching Support
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class SimpleUserService {

    @Autowired
    private SimpleUserRepository userRepository;

    @Cacheable(value = "users", key = "#id", unless = "#result.isEmpty()")
    public Optional<SimpleUser> getUserById(Long id) {
        Optional<SimpleUser> user = userRepository.findById(id);
        // Validate tenant access
        if (user.isPresent() && TenantContext.hasTenant()) {
            SimpleUser u = user.get();
            if (!TenantContext.getTenantId().equals(u.getTenantId())) {
                return Optional.empty();
            }
        }
        return user;
    }

    @Cacheable(value = "users-by-email", key = "#email", unless = "#result.isEmpty()")
    public Optional<SimpleUser> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "users", key = "#username", unless = "#result.isEmpty()")
    public Optional<SimpleUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<SimpleUser> getAllUsers() {
        List<SimpleUser> users = userRepository.findAll();
        // Filter by tenant if context is set
        if (TenantContext.hasTenant()) {
            String tenantId = TenantContext.getTenantId();
            return users.stream()
                .filter(u -> tenantId.equals(u.getTenantId()))
                .toList();
        }
        return users;
    }

    @CacheEvict(value = {"users", "users-by-email"}, allEntries = false)
    public SimpleUser createUser(SimpleUser user) {
        // Set tenant ID from context
        if (TenantContext.hasTenant()) {
            user.setTenantId(TenantContext.getTenantId());
        }
        return userRepository.save(user);
    }

    @CacheEvict(value = {"users", "users-by-email"}, key = "#id", allEntries = false)
    public SimpleUser updateUser(Long id, SimpleUser user) {
        SimpleUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        
        return userRepository.save(existingUser);
    }

    @CacheEvict(value = {"users", "users-by-email"}, key = "#id", allEntries = false)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

