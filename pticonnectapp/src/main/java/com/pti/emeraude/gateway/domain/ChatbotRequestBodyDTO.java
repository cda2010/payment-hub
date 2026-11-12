package com.pti.emeraude.gateway.domain;

// Kadsuke
import java.util.List;
import java.util.Map;

public class ChatbotRequestBodyDTO {
    private Object message;
    private List<Map<String, String>> history;

    public Object getMessage() {
        return message;
    }

    public List<Map<String, String>> getHistory() {
        return history;
    }
    
}
