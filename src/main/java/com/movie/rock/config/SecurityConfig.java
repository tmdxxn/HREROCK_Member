package com.movie.rock.config;

import com.movie.rock.member.service.CustomOAuth2UserService;
import com.movie.rock.member.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

// 김승준 - 회원
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomUserDetailsService customUserDetailsService;

    private final CorsConfigurationSource corsConfigurationSource;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호기능 비활성화

                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP 베이직 인증 비활성화

                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인 비활성화

                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // CORS 설정 적용

                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용X

                .authorizeHttpRequests(authrizeRequests -> authrizeRequests
                        .requestMatchers("/auth/**", "/**", "/static/", "/css/**", "/js/**", "/img/**").permitAll() // 인증X 사용가능 ( 로그인X )
                        .requestMatchers("/user/**").hasRole("USER") // 유저권한만 사용
                        .requestMatchers("/user/**", "/admin/**").hasRole("ADMIN") // 관리자 사용
                        .anyRequest().authenticated() // 그외 요청 인증 필요
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/auth/login") // OAuth2 로그인페이지 ( 구글 )
                        .defaultSuccessUrl("http://localhost:3000/", true)
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(customOAuth2UserService)) // OAuth2 사용자 정보 처리 서비스
                        .successHandler(oAuth2LoginSuccessHandler) // OAuth2 로그인 성공 핸들러
                );

        // JWT 인증 필터를 앞에 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
