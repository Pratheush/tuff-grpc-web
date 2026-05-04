package com.mylearning.oauth_server.config;

import com.mylearning.oauth_server.repo.UserRepo;
import com.mylearning.oauth_server.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 🔄  — YOUR FLOW (VALIDATION)
 *
 * Your flow is correct OAuth2 architecture:
 *
 * 1. Step-by-step:
 * 2. Client → /oauth2/authorize
 * 3. OAuth server → redirects to /login
 * 4. User logs in
 * 5. OAuth server → sends authorization code
 * 6. Client → exchanges code for access token
 * 7. Client → calls gRPC server with token
 * 8. gRPC server → validates JWT
 * 9. Returns stock data
 *
 *
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final UserRepo userRepo;

    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return new CustomUserDetailsService(userRepo);
    }

    /**
     * Bean 1: authorizationServerSecurityFilterChain : What it does:
     * Purpose: Handles OAuth2 Authorization Server endpoints (/oauth2/authorize, /oauth2/token, etc.).
     * Applies only to OAuth2 endpoints
     * Configures: - /oauth2/authorize
     *             - /oauth2/token
     *             - /oauth2/jwks
     *             - OIDC endpoints
     * Configures:
     * 1. OAuth2AuthorizationServerConfigurer → sets up authorization server + OIDC support.
     * 2.   .authorizeHttpRequests().anyRequest().authenticated() → all auth server endpoints require authentication.
     * 3.   .csrf().ignoringRequestMatchers(...) → disables CSRF for token endpoints.
     * 4.   .formLogin(loginPage("/login")) → uses your custom login page.
     * 👉 This chain ensures your OAuth2 server logic works correctly.
     * @param httpSecurity
     * @return
     * @throws Exception
     */
    @Bean
    @Order(1) //👉 Highest priority filter chain
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // configuring what security and endpoint what we need to setup
        OAuth2AuthorizationServerConfigurer oAuth2AuthorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        httpSecurity
                .securityMatcher(oAuth2AuthorizationServerConfigurer.getEndpointsMatcher())     // 👉 Applies only to OAuth endpoints
                .with(oAuth2AuthorizationServerConfigurer,oauth2AuthorizationServerConfigurer ->
                        oauth2AuthorizationServerConfigurer.oidc(Customizer.withDefaults()))        // 👉 Enables: OpenID Connect, /userinfo, /token
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry.anyRequest().authenticated())    // 👉 All OAuth endpoints require login
                .csrf(httpSecurityCsrfConfigurer ->
                        httpSecurityCsrfConfigurer.ignoringRequestMatchers(oAuth2AuthorizationServerConfigurer.getEndpointsMatcher())) // 👉 Required because: OAuth endpoints don't need CSRF : Token requests are POST-based, CSRF would block them
                //.formLogin(Customizer.withDefaults());
                .formLogin(httpSecurityFormLoginConfigurer -> httpSecurityFormLoginConfigurer.loginPage("/login").permitAll())      // 👉 Custom login UI instead of default
                .oauth2Login(httpSecurityOAuth2LoginConfigurer -> httpSecurityOAuth2LoginConfigurer.loginPage("/login"))
        ;

        return httpSecurity.build();
    }

    /**
     * Purpose: Handles your app’s normal web endpoints (like /login, static resources).
     * Configures:
     * 1.  .authorizeHttpRequests().requestMatchers("/login","/error","/css/**","/js/**","/image/**").permitAll() → allows login page and static files without authentication.
     * 2.  .formLogin(loginPage("/login").permitAll()) → ensures unauthenticated users are redirected to your custom login page.
     * 3.  .authenticationProvider(authenticationProvider()) → plugs in your custom user authentication logic.
     *  👉 This chain ensures your login page and static resources are accessible.
     * @param httpSecurity
     * @return
     * @throws Exception
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry.requestMatchers("/login","/error","/css/**","/js/**","/image/**").permitAll()    // 👉 Allows: Login page, Static assets
                )
                //.formLogin(Customizer.withDefaults())
                .formLogin(httpSecurityFormLoginConfigurer ->
                        httpSecurityFormLoginConfigurer.loginPage("/login").permitAll())        // 👉 Enables login authentication
                .oauth2Login(httpSecurityOAuth2LoginConfigurer ->
                        httpSecurityOAuth2LoginConfigurer.loginPage("/login")
                                .userInfoEndpoint(httpsecurityoauth2Loginconfigurer ->
                                        httpsecurityoauth2Loginconfigurer.userService(oAuth2UserService())
                                ))
                .authenticationProvider(authenticationProvider());          // 👉 Uses your custom DB-based authentication
        return httpSecurity.build();
    }

    /**
     * PasswordEncoder: BCrypt encoder for hashing passwords securely.
     * @return
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        //return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new BCryptPasswordEncoder();         // 👉 Encrypts passwords securely
    }

    /**
     * OAuth2TokenCustomizer<JwtEncodingContext>: Adds authorities claim into JWT access tokens so resource servers know user roles.
     * @return
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(){
        return jwtEncodingContext -> {
            Authentication jwtEncodingContextPrincipal = jwtEncodingContext.getPrincipal();
            if(OAuth2TokenType.ACCESS_TOKEN.equals(jwtEncodingContext.getTokenType())){
                Set<String> authorities = jwtEncodingContextPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                jwtEncodingContext.getClaims().claim("authorities",authorities);            // Adds roles into JWT why so important because 👉 Your gRPC server can read roles from token
            }
        };
    }

    /**
     * AuthenticationProvider: Uses DaoAuthenticationProvider with your CustomUserDetailsService + PasswordEncoder to authenticate users.
     * Uses : CustomUserDetailsService , PasswordEncoder
     * Validates: username + password from DB
     * Bean CustomUserDetailsService 👉 Loads user from DB > userRepo.findByUsername(username)
     * CustomUserDetailsService: Loads user info from DB (UserRepo) and wraps it in CustomUserDetails.
     * @return
     */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(customUserDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(){
        return oAuth2UserRequest -> {
            OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = oauth2UserService.loadUser(oAuth2UserRequest);
            log.info("oAuth2User We CAN SAVE IN DATABASE OR ANY OTHER OPERATION WE CAN PERFORM OR WITH DETAILS ETC HERE BEFORE RETURNING: {}", oAuth2User);
            return oAuth2User;
        };
    }


    /*@Bean
    Customizer<HttpSecurity> securityCustomizer() {
        return httpSecurity -> httpSecurity
                .oauth2AuthorizationServer(oAuth2AuthorizationServerConfigurer ->
                        oAuth2AuthorizationServerConfigurer
                                .oidc(Customizer.withDefaults())
                );
    }*/

    // in real life production we use JdbcUserDetailsManager not InMemoryUserDetailsManager
    /*@Bean
    InMemoryUserDetailsManager userDetailsManager(PasswordEncoder passwordEncoder){
        Set<UserDetails> userDetailsSet = Set.of(
                User.withUsername("Raj")
                        .roles("USER")
                        .password(passwordEncoder.encode("raj123"))
                        .build(),
                User.withUsername("Anuj")
                        .roles("USER")
                        .password(passwordEncoder.encode("anuj123"))
                        .build(),
                User.withUsername("Ankit")
                        .roles("USER")
                        .password(passwordEncoder.encode("ankit123"))
                        .build()
        );
        return new InMemoryUserDetailsManager(userDetailsSet);
    }*/
}
