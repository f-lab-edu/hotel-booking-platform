package dev.muho.user.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record UserPrincipal(
        Long id,
        String email,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    // UserDetails 인터페이스의 다른 메서드들...
    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return this.email; } // username으로 email 사용

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }
}
