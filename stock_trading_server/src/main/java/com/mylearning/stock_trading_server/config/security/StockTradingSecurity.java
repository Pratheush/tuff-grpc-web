package com.mylearning.stock_trading_server.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Set;

@Configuration
public class StockTradingSecurity {

    /*@Bean
    PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder();
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
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

    // Defining Security for GRPC

    /**
     * The matcher is comparing against the canonical gRPC method names, which include the package.
     * A gRPC method is identified as package.Service/Method
     * If you only wrote "StockTradingService/GetStockPrice", it wouldn’t match, because the actual method string includes the package prefix.
     *
     * gRPC always uses fully qualified names (package.Service/Method).
     *
     * grpcurl and Spring Security interceptors must match those exact names.
     *
     * That’s why you can’t shorten it to just StockTradingService.GetStockPrice.
     *
     * If you declare a package in your proto, gRPC always prepends it to the service name.
     *
     * That’s why you need the full com.mylearning.stock_trading_server.StockTradingService.GetStockPrice in grpcurl and in your security config.
     *
     * If you don’t want the long prefix, remove or change the package line in your proto.
     * ===================================================================================================================================================================
     *
     * @GlobalServerInterceptor
     * What it does:
     * 1. Intercepts every gRPC request
     * 2. Applies security rules
     * 3. Equivalent to Spring Security filter chain (for HTTP)
     *
     * authorizeRequests(...)  👉   .methods("...GetStockPrice").authenticated()    👉 Only authenticated users can call this method
     * .methods("grpc../*").permitAll()   👉 Allows: health checks, reflection
     * .allRequests().denyAll()  👉 Default: deny everything else
     *
     * 🔹 .oauth2ResourceServer(jwt())  👉  .oauth2ResourceServer(oAuth2 -> oAuth2.jwt())
     * What it does:
     * 1. Extracts:  Authorization: Bearer <token>
     * 2. Validates: signature (via JWKS) , expiry, issuer
     * 3. Creates:  Authentication object
     *
     * @GlobalServerInterceptor → This interceptor applies to all gRPC calls globally.
     * authorizeRequests() → Defines which gRPC methods require authentication:
     *      1. StockTradingService/GetStockPrice → must be authenticated (requires valid JWT).
     *      2. grpc.../* → permit all (system-level reflection or health checks).
     *      3. allRequests().denyAll() → deny everything else by default.
     *
     * oauth2ResourceServer().jwt() → Configures this gRPC server as an OAuth2 resource server that validates JWT tokens.
     *  -   It uses the jwk-set-uri and issuer-uri from application.properties to validate tokens. HERE WE USED issuer-uri IN OUR PROJECT.
     * Why needed: This ensures only authenticated clients (with valid tokens from your oauth_server) can call GetStockPrice.
     *
     * Ensure the scope and authorities claims are included in the JWT (you already customized this in oauth_server).
     * @param grpcSecurity
     * @return
     * @throws Exception
     */
    @Bean
    @GlobalServerInterceptor // this is global interceptor we can also do per channel interceptor
    AuthenticationProcessInterceptor authenticationProcessInterceptor(GrpcSecurity grpcSecurity) throws Exception{
        return grpcSecurity
                .authorizeRequests(requestMapperConfigurer ->
                        requestMapperConfigurer
                                //.methods("StockTradingService/GetStockPrice").authenticated()
                                //.methods("com.mylearning.stock_trading_server.**").authenticated()
                                .methods("com.mylearning.stock_trading_server.StockTradingService/GetStockPrice").authenticated()
                                .methods("com.mylearning.grpc.StockTradingService/GetStockPrice").authenticated() // NEED TO BE VERIFIED
                                .methods("grpc.*/*").permitAll()
                                .allRequests().denyAll()

                        )
                //.httpBasic(Customizer.withDefaults()) // not using basic auth
                .oauth2ResourceServer(oAuth2ResourceServerConfigurer ->
                        oAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults())) // using oauth2 resource server
                .build();
    }
}
