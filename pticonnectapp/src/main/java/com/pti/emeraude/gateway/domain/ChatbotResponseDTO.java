package com.pti.emeraude.gateway.domain;

//Kadsuke
import java.util.List;

public class ChatbotResponseDTO {
    private List<ChatMessageDTO> history;

    public List<ChatMessageDTO> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessageDTO> history) {
        this.history = history;
    }

    

    
    public ChatbotResponseDTO() {
    }

    @Override
    public String toString() {
        return "ChatbotResponseDTO{" + "history=" + history + '}';
    }
    
    

}
