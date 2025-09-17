package com.my.boot.config.filter;

import com.my.boot.common.util.PasswordUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        throw new UnsupportedOperationException("Encoding not supported");
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // ❌ 여기선 salt가 없기 때문에 matches는 항상 false 처리
        return false;
    }

    public boolean matches(CharSequence rawPassword, String salt, String encodedPassword) {
        return PasswordUtil.hashSSHA(rawPassword.toString(), salt).equals(encodedPassword);
    }
}
