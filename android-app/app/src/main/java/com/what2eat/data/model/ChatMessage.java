package com.what2eat.data.model;

import java.io.Serializable;

/**
 * 聊天消息模型
 */
public class ChatMessage implements Serializable {

    private String id;
    private String content;
    private boolean isUser; // true: 用户消息, false: AI消息
    private long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String content, boolean isUser) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.content = content;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
