package com.my.boot.user.service;


import com.my.boot.auth.service.RSAService;
import com.my.boot.common.dto.ResultDTO;
import com.my.boot.common.exception.ApiException;
import com.my.boot.common.exception.ExceptionData;
import com.my.boot.common.util.PasswordUtil;
import com.my.boot.user.dto.UsersRequest;
import com.my.boot.user.entity.User;
import com.my.boot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RSAService rsaService; // ✅ RSA 복호화를 위한 서비스 주입

/*
* 회원가입
* */
    @Transactional
    public ResultDTO<Void> userSignUp(UsersRequest request) {

        if (userRepository.existsByLoginId(request.loginId())) {
            throw new ApiException(ExceptionData.EXISTS_USER);
        }

        // ✅ RSA 복호화: 프론트엔드에서 암호화된 비밀번호 복호화
        String decryptedPassword = rsaService.decryptedText(request.passwd());
        log.info("🔐 회원가입 - 비밀번호 복호화 완료");

        String salt = PasswordUtil.getSalt();
        log.info("✅ 회원 가입 salt : {}", salt);

        // 복호화된 평문 비밀번호를 해시화
        String encrypted = PasswordUtil.hashSSHA(decryptedPassword, salt);
        log.info("✅ 비밀번호 해시화 완료");

        User user = request.toEntity(encrypted, salt);

        userRepository.save(user);

        return ResultDTO.<Void>builder()
                .success(true)
                .message("회원 가입 되었습니다.")
                .build();
    }


}
