package me.silvernine.tutorial.config

import me.silvernine.tutorial.jwt.JwtAccessDeniedHandler
import me.silvernine.tutorial.jwt.JwtAuthenticationEntryPoint
import me.silvernine.tutorial.jwt.JwtFilter
import me.silvernine.tutorial.jwt.TokenProvider
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security 설정.
 *
 * 강의 영상(Spring Boot 2.4)은 WebSecurityConfigurerAdapter 를 상속했습니다. 이 클래스는 Spring Security 6에서
 * 사라졌고, 지금은 [SecurityFilterChain] 빈을 직접 만듭니다. Spring Boot가 웹 보안을 켜 주므로
 * `@EnableWebSecurity` 도 붙이지 않습니다.
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val tokenProvider: TokenProvider,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * 로그인 API 에서 쓰는 AuthenticationManager.
     * 강의 영상처럼 AuthenticationManagerBuilder 를 꺼내 쓰지 않고, Spring Security 공식 문서
     * (Servlet Authentication, Publish an AuthenticationManager bean)의 방식대로 직접 빈으로 만듭니다.
     */
    @Bean
    fun authenticationManager(userDetailsService: UserDetailsService, passwordEncoder: PasswordEncoder): AuthenticationManager {
        val authenticationProvider = DaoAuthenticationProvider(userDetailsService).apply {
            setPasswordEncoder(passwordEncoder)
        }
        return ProviderManager(authenticationProvider)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 세션 쿠키를 쓰지 않고 Authorization 헤더로 토큰을 보내므로 CSRF 보호가 필요 없습니다.
            .csrf(AbstractHttpConfigurer<*, *>::disable)
            // CorsConfig 의 UrlBasedCorsConfigurationSource 빈을 씁니다.
            .cors(withDefaults())

            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            .exceptionHandling {
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                it.accessDeniedHandler(jwtAccessDeniedHandler)
            }

            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/hello", "/api/signup", "/api/authenticate",
                    "/api/token/refresh", "/api/logout",
                ).permitAll()
                it.requestMatchers(PathRequest.toH2Console()).permitAll()
                it.anyRequest().authenticated()
            }

            // H2 콘솔은 iframe 을 쓰므로 같은 출처의 iframe 을 허용합니다.
            .headers { it.frameOptions(HeadersConfigurer<HttpSecurity>.FrameOptionsConfig::sameOrigin) }

            .addFilterBefore(JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
