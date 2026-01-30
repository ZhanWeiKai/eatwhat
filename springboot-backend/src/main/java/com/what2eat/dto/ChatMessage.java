package com.what2eat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @JsonProperty("role")
    private String role; // system, user, assistant

    @JsonProperty("content")
    private String content;
}
