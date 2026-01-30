package com.what2eat.enums;

/**
 * SSE消息类型枚举
 */
public enum SSEMsgType {

    ADD("add", "消息追加，适用于流式stream推送"),
    FINISH("finish", "消息完成"),
    ERROR("error", "错误消息"),
    CONNECT("connect", "连接确认");

    public final String type;
    public final String value;

    SSEMsgType(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
