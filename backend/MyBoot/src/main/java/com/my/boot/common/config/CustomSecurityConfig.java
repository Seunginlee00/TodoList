package com.my.boot.common.config;


import com.my.boot.auth.security.handler.CustomAccessDeniedHandler;
import com.my.boot.auth.security.socket.DuplicateLoginAlertSender;
import com.my.boot.auth.security.util.JWTUtil;
import com.my.boot.auth.service.JwtTokenStoreService;
import com.my.boot.auth.service.RSAService;
import com.my.boot.config.filter.CustomAuthenticationProvider;
import com.my.boot.config.filter.CustomLoginFilter;
import com.my.boot.config.filter.JWTCheckFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@Log4j2
@RequiredArgsConstructor
@EnableMethodSecurity // 버전업되면서 수정된 기능
public class CustomSecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final RSAService rsaService;
//    private final LoginFailService loginFailService;
//    private final HrAuthFailService ldapAuthFailService;
    private final JwtTokenStoreService jwtTokenStoreService;
    private final DuplicateLoginAlertSender duplicateLoginAlertSender;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final JWTUtil jwtUtil;

    /**
     * 커스텀 AuthenticationManager 등록
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(customAuthenticationProvider) // ✅ 등록
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 세션 비활성화 (JWT 기반)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CSRF 비활성화
        http.csrf(csrf -> csrf.disable());
        // REST API 서버일 경우 보통 CSRF는 비활성화함. (토큰 기반 인증 사용 시)

        http.formLogin(config -> config.disable());

        // ✅ 보안 헤더 중 Cache 관련 제거 (프리플라이트 응답 캐시 허용)
        http.headers(headers -> headers
                .cacheControl(cache -> cache.disable())
        );

        // ✅ 커스텀 로그인 필터 등록
        // AuthenticationManager 가져오기
        AuthenticationManager authManager = authenticationManager(http);
        CustomLoginFilter filter = new CustomLoginFilter(authManager,rsaService);

//        CustomLoginFilter filter = new CustomLoginFilter(authManager,rsaService,loginFailService,ldapAuthFailService,jwtTokenStoreService,jwtUtil);
        http.addFilterAt(filter, UsernamePasswordAuthenticationFilter.class);

        // JWT 필터 등록
        http.addFilterBefore(new JWTCheckFilter(jwtTokenStoreService, duplicateLoginAlertSender,jwtUtil), UsernamePasswordAuthenticationFilter.class);
        // UsernamePasswordAuthenticationFilter 앞에 JWTCheckFilter를 삽입하여, JWT 토큰을 먼저 체크하도록 구성.
        // 보통 JWTCheckFilter는 요청 헤더에 있는 토큰을 분석하고 인증 객체를 만들어 SecurityContext에 저장함.

        // 경로별 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        // Health / Access check
                        "/check-access",

                        // Public Key
                        "/api/public-key",
                        "/api/pub-key",
                        "/api/jwt-pub-key",

                        // User / Member (비로그인 허용)
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/refresh",
                        "/api/user/photo/**",
                        "/api/member/login",
                        "/api/member/register",
                        "/api/member/refresh",

                        // Swagger & OpenAPI
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs/main-api/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()
                // ✅ 회원가입/로그인은 인증 없이 통과
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/user/register", "/api/user/login").permitAll()
                // ✅ CORS 프리플라이트 허용 (프론트 호출 시 필수)
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
        );



        http.exceptionHandling(config -> {
            config.accessDeniedHandler(new CustomAccessDeniedHandler());
        });
        // 인가 실패 (403) 발생 시 호출되는 커스텀 핸들러 지정.


        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "https://smartx.05rg.com",
                "*"
        ));
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);  // 1시간 동안 preflight 결과 캐시

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
