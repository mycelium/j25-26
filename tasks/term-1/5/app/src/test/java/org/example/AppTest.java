package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    @Test
    public void testAppStarts() {
        // Test simplement que l'app peut démarrer sans erreur
        assertDoesNotThrow(() -> App.main(new String[]{}));
    }
    
    @Test 
    public void testAppExists() {
        App app = new App();
        assertNotNull(app);
    }
}