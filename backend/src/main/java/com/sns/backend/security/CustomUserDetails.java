package com.sns.backend.security;

import com.sns.backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails /*, java.io.Serializable*/ {

    private final User user;

    public CustomUserDetails(User user) { this.user = user; }

    public Long getUserId() { return user.getUserId(); }
    public String getLoginId() { return user.getLoginId(); }
    public String getDisplayName() { return user.getDisplayName(); }
    public User.Visibility getVisibility() { return user.getVisibility(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getLoginId(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    @Override
    public String toString() {
        return "CustomUserDetails{userId=" + getUserId() + ", loginId=" + getUsername() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomUserDetails that)) return false;
        return java.util.Objects.equals(this.getUserId(), that.getUserId());
    }
    @Override
    public int hashCode() { return java.util.Objects.hash(getUserId()); }
}
