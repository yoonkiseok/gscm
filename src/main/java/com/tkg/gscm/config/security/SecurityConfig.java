package com.tkg.gscm.config.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig  {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
//                                .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
                                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                        // 추가적인 규칙을 여기에 추가하십시오.
                        // 예시:
                        // .requestMatchers(PathRequest.to("/api/**")).authenticated()
                        // .requestMatchers(PathRequest.to("/private/**")).hasAnyRole("ADMIN", "USER")
                );
        http.csrf(new CsrfCustomizer());
        http.cors(new CorsCustomizer()); //아래방법으로 하면 가독성 때문에 클래스로 나누었음
//                http.cors(corsCustomizer -> corsCustomizer
//                .configurationSource(request -> {
//                    CorsConfiguration corsConfiguration = new CorsConfiguration();
//                    corsConfiguration.addAllowedOrigin("http://localhost:4200"); // 허용할 오리진을 지정합니다.
//                    corsConfiguration.addAllowedMethod("*"); // 허용할 HTTP 메서드를 지정합니다. 필요에 따라 세부적으로 설정할 수도 있습니다.
//                    corsConfiguration.addAllowedHeader("*"); // 허용할 요청 헤더를 지정합니다. 필요에 따라 세부적으로 설정할 수도 있습니다.
//                    return corsConfiguration;
//                })
//        )


        return http.build();
    }
//@Bean
//public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http.csrf(new CsrfCustomizer());
//    http.authorizeRequests()
//            .antMatchers("/home").permitAll()
//            .antMatchers("/mypage").authenticated()
//            .anyRequest().authenticated()
//    return http.build();
//}


}
