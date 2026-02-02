package com.what2eat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 智谱AI请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // 不序列化null值
public class ZhipuAIRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<ChatMessage> messages;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("stream")
    private Boolean stream;  // 是否流式输出

    @JsonProperty("tools")
    private List<Tool> tools;  // 工具列表（联网搜索等）

    /**
     * 联网搜索工具
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Tool {
        @JsonProperty("type")
        private String type;  // 工具类型：web_search

        @JsonProperty("web_search")
        private WebSearch webSearch;
    }

    /**
     * Web搜索配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WebSearch {
        @JsonProperty("enable")
        private Boolean enable;  // 是否启用联网搜索

        @JsonProperty("search_result")
        private Boolean SearchResult;  // 是否返回搜索结果
    }
}
