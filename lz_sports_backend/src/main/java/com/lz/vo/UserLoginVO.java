package com.lz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Login VO
 */
public class UserLoginVO {
    private Long id;
    private String userName;
    private String type;
    private String avatarSrc;
    private String token;

    public UserLoginVO() {}

    public UserLoginVO(Long id, String userName, String type, String avatarSrc, String token) {
        this.id = id;
        this.userName = userName;
        this.type = type;
        this.avatarSrc = avatarSrc;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAvatarSrc() {
        return avatarSrc;
    }

    public void setAvatarSrc(String avatarSrc) {
        this.avatarSrc = avatarSrc;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static UserLoginVOBuilder builder() {
        return new UserLoginVOBuilder();
    }

    public static class UserLoginVOBuilder {
        private Long id;
        private String userName;
        private String type;
        private String avatarSrc;
        private String token;

        public UserLoginVOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserLoginVOBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public UserLoginVOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public UserLoginVOBuilder avatarSrc(String avatarSrc) {
            this.avatarSrc = avatarSrc;
            return this;
        }

        public UserLoginVOBuilder token(String token) {
            this.token = token;
            return this;
        }

        public UserLoginVO build() {
            return new UserLoginVO(id, userName, type, avatarSrc, token);
        }
    }
}
