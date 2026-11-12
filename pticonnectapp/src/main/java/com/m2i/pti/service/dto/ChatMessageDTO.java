package com.m2i.pti.service.dto;

//Kadsuke
public class ChatMessageDTO {
    private String role;
    private String content;

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ChatMessageDTO [role=" + role + ", content=" + content + "]";
    }
}
