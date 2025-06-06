package com.example.projet.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class userServiceTest {

    @Autowired
    private userService service;

    @Test
    public void testServiceNotNull() {
        assertNotNull(service);
    }

    @Test
    public void testCreateEntity() {
        // TODO : compléter le test
    }
}