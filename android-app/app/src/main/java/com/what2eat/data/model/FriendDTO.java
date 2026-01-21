package com.what2eat.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * 好友数据传输对象
 */
public class FriendDTO {

    @SerializedName("userId")
    private String userId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("online")
    private Boolean online;

    public FriendDTO() {
    }

    public FriendDTO(String userId, String nickname, String avatar, Boolean online) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.online = online;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
