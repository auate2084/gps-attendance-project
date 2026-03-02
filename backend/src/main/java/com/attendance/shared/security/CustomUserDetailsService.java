package com.attendance.shared.security;

import com.attendance.user.domain.User;
import com.attendance.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    // 로그인 아이디로 사용자를 조회해 스프링 시큐리티 인증 객체로 변환한다.
    public UserSession loadUserByUsername(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleLevel().name()));
        if (user.isHrAuthority()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_HR"));
        }

        return new UserSession(
                user.getId(),
                user.getLoginId(),
                user.getPasswordHash(),
                user.isActive(),
                true,
                true,
                true,
                authorities
        );
    }
}
