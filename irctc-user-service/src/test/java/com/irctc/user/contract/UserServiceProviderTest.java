package com.irctc.user.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.irctc.user.entity.SimpleUser;
import com.irctc.user.repository.SimpleUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Provider Verification Test for User Service
 * 
 * This test verifies that User Service fulfills the contracts
 * defined by consumers (e.g., Booking Service).
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("user-service")
@PactFolder("src/test/resources/pacts")
@ActiveProfiles("test")
@ExtendWith(PactVerificationInvocationContextProvider.class)
public class UserServiceProviderTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SimpleUserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    /**
     * State: user with id 1 exists
     */
    @State("user with id 1 exists")
    void userExists() {
        // Setup: Create a user with id 1
        SimpleUser user = new SimpleUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("1234567890");
        user.setRoles("USER");
        user.setPassword(passwordEncoder.encode("password"));
        
        userRepository.save(user);
    }

    /**
     * State: user with id 999 does not exist
     */
    @State("user with id 999 does not exist")
    void userDoesNotExist() {
        // Setup: Ensure user 999 doesn't exist
        userRepository.deleteById(999L);
    }
}

