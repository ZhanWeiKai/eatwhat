package com.what2eat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 好友数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {
    private String userId;
    private String nickname;
    private String avatar;
    private Boolean online;  // true=在线, false=离线
}
