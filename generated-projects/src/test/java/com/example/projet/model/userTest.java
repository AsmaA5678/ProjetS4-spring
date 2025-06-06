package com.example.projet.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class userTest {

    @Test
    public void testEntityNotNull() {
        user entity = new user();
        assertNotNull(entity);
    }

    @Test
    public void testEntityProperties() {
        // TODO: compléter les tests des propriétés de user
    }
}

