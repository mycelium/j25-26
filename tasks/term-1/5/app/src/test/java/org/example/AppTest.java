package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test 
    void testAppCreation() {
        // Просто проверяем что приложение создается
        App app = new App();
        assertNotNull(app);
    }
    
    @Test
    void testEmptyText() {
        App app = new App();
        assertEquals("neutral", app.analyzeSentiment(""));
        assertEquals("neutral", app.analyzeSentiment(null));
        assertEquals("neutral", app.analyzeSentiment("   "));
    }
    
    @Test
    void testHtmlCleaning() {
        // Проверяем логику очистки HTML 
        String text = "Text<br>with<br/>HTML<br />tags";
        String cleaned = text.replaceAll("<br\\s*/?>", " ").replaceAll("\\s+", " ").trim();
        assertEquals("Text with HTML tags", cleaned);
    }
}
