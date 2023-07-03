package com.tkg.gscm.config.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class CsrfCustomizer implements Customizer<CsrfConfigurer<HttpSecurity>> {

    @Override
    public void customize(CsrfConfigurer<HttpSecurity> csrfConfigurer) {
        // CsrfConfigurer 설정을 커스터마이즈하는 로직을 구현합니다.
        csrfConfigurer
//                .csrfTokenRepository(customCsrfTokenRepository()) // 커스텀 CSRF 토큰 리포지토리 설정
                .requireCsrfProtectionMatcher(customCsrfProtectionMatcher()) // 커스텀 CSRF 보호 매처 설정
                .disable(); //무력화
    }

    private CsrfTokenRepository customCsrfTokenRepository() {
        // 커스텀 CSRF 토큰 리포지토리를 생성하여 반환하는 로직을 구현합니다.
        // 필요에 따라 적절한 CSRF 토큰 저장 및 관리 방식을 선택하고 설정합니다.
        // 예시: HttpSession을 이용한 CSRF 토큰 저장 및 관리 방식을 선택합니다.
        return new HttpSessionCsrfTokenRepository();
    }

    private RequestMatcher customCsrfProtectionMatcher() {
        // CSRF 보호를 적용할 URL 패턴을 지정합니다.
        // 예시: "/api/**" 패턴에 CSRF 보호를 적용하도록 설정합니다.
        return new AntPathRequestMatcher("/api/**");
    }
}
