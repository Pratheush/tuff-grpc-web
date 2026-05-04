package com.mylearning.stock_trading_client.config;

import com.mylearning.grpc.StockPriceRequest;
import com.mylearning.grpc.StockTradingServiceGrpc;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.client.interceptor.security.BearerTokenAuthenticationInterceptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * 🔄 PART 4 — END-TO-END FLOW (VALIDATED)
 *
 * Your system:
 *
 * ✔ Step-by-step:
 * 1. User opens client (stock_trading_client) → served on port 8081. >> User → /client/AAPL
 * 2. Not logged in → Client redirects to OAuth2 server (oauth_server) on port 8082.
 * 3. Login page shown
 * 4. User logs in
 * 5. OAuth server → sends authorization code
 * 6. Client exchanges code → gets access token
 * 7. Client stores token in session → attaches token to gRPC stub.
 * 8. Client calls gRPC resource server (stock_trading_server) on port 9090 with token.
 * 9. gRPC Resource server validates token
 * 10. Returns stock data
 * 11. Rendered in stocks.html : Client maps gRPC response → displays in stocks.html.
 *
 * ✔ This is correct + production-grade flow
 *
 *  Matching the OAuth2 Authorization Code flow requirements:
 * 1. Login page → triggers flow.  : Login page = where the user starts (/oauth2/authorization/{clientId}).
 * 2. Authorization endpoint → builds request   : where Spring builds the request (/oauth2/authorization).
 * 3. Redirection endpoint → handles callback with code  :  where the server calls back (/oauth2/code/*).
 *
 */
@Configuration
public class StockClientConfig {

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    public StockClientConfig(OAuth2AuthorizedClientService oAuth2AuthorizedClientService){
        this.oAuth2AuthorizedClientService=oAuth2AuthorizedClientService;
    }

    // how do we do the work of getting a token

    /**
     * lets get the current authenticated user what is going to happen they are going to localhost:8081 which is our client there is no validate no authenticated user in the
     * session no security context no authentication in the security context and so spring security is going to redirect us to the off-server there we are going to authenticate
     * we'll get redirected back to this oauth-client there ,there's a valid authenticated user in the session or atleast available for this user and then when somebody goes to the HTTP Controller
     * and inject this GRPC client we want to automatically transfer the knowledge about the current authenticated user from the current authenticated user in the security context
     * to the authorization header that's in the outgoing request to the downstream service we can do that transparently. we can do it manually as we make the call but its much more elegant to
     * do that in a single place thats what we are doing we are configuring the interceptor
     *
     * i called localhost:8081/ >> it redirects to localhost:8082/login/ hey you don't have authenticated user in here somewhere redirect me to off-server
     * this is like sign-in with off-server like sign-in with google. we have got few users in off-server which we created in InMemoryUserDetailsManager
     * so lets sign-in with one of the users and it redirected back to client i.e. localhost:8081/?continue
     * @param oAuth2AuthorizedClientManager
     * @return
     */
    // this is the token that we are going to use to authenticate the downstream service
    // TOKEN EXTRACTION LOGIC (RISKY)

    /**
     * 🔹 Method: token(...)
     * Purpose: Extracts the current user’s OAuth2 access token from the security context.
     * What it does:
     * Extracts logged-in user
     * Uses OAuth2AuthorizedClientManager
     * Retrieves access token
     * Flow:
     * Get Authentication : Gets the authenticated user from SecurityContextHolder.
     * If it’s an OAuth2AuthenticationToken, builds an OAuth2AuthorizeRequest.
     * Convert to OAuth2 token :
     * Call authorize()  :  Uses OAuth2AuthorizedClientManager to authorize and fetch the client.
     * Extract access token  :  Retrieves the access token String from the authorized client.
     * Why needed: This is how you attach the user’s token to gRPC calls so the resource server can validate it.
     *
     */
    // to do
    String token(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager){
        Authentication authenticatedUser = this.securityContextHolderStrategy.getContext().getAuthentication();
        if(authenticatedUser instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken){

            String clientId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

            OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientId)
                    .principal(authenticatedUser)
                    .build();

            OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientManager.authorize(oAuth2AuthorizeRequest); // TOKEN EXTRACTION LOGIC (RISKY)

            if (oAuth2AuthorizedClient == null) {
                throw new IllegalStateException("Failed to authorize client");
            }

            String tokenValue = oAuth2AuthorizedClient.getAccessToken().getTokenValue();
            return tokenValue;

        }
        return null;
    }

    // THIS METHOD IS JUST TO ILLUSTRATE THE TOKEN EXTRACTION LOGIC AND SET THE TOKEN INTO HTTP-HEADERS AND COMBINE WITH HTTP-ENTITY
    String token2(OAuth2AuthenticationToken oAuth2AuthenticationToken){

        String clientRegistrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
        String principalName = oAuth2AuthenticationToken.getName();
        OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(
                clientRegistrationId,
                principalName
        );
        String tokenValue = oAuth2AuthorizedClient.getAccessToken().getTokenValue();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(tokenValue);

        HttpEntity<StockPriceRequest> httpEntity = new HttpEntity<>(httpHeaders);

        // we can use rest-template or rest-client or web-client to make http calls to other services requesting data
        Assert.notNull(httpEntity,"httpEntity cannot be null");
        return tokenValue;
    }

    /**
     * 🔹 Bean: blockingStub  :  blockingStub(GrpcChannelFactory factory, OAuth2AuthorizedClientManager oauth2AuthorizedClientManager)
     * StockTradingServiceGrpc.newBlockingStub(channel)
     * What it does:
     * Creates gRPC client :  Purpose: Creates a gRPC blocking stub for StockTradingService.
     *
     * Steps:
     * 1. Builds a BearerTokenAuthenticationInterceptor that injects the token into gRPC metadata.
     * 2. Configures ChannelBuilderOptions with that interceptor.
     * 3. Creates a ManagedChannel to localhost:9090 (your resource server).
     * 4. Returns a StockTradingServiceGrpc.StockTradingServiceBlockingStub.
     * 5. BearerTokenAuthenticationInterceptor 👉 Injects:  Authorization: Bearer <token>
     *
     *  Why needed: This is the bridge between your client and the gRPC resource server. It ensures every gRPC call carries the OAuth2 token.
     *     Why needed:
     * ➡️ gRPC doesn’t use HTTP headers directly
     * ➡️ Interceptor simulates it
     *
     * ⚠️ 2. Blocking gRPC call  :  blockingStub.getStockPrice()  👉 not scalable under load
     */
    @Bean
    @Lazy
    public StockTradingServiceGrpc.StockTradingServiceBlockingStub blockingStub(GrpcChannelFactory factory ,OAuth2AuthorizedClientManager oauth2AuthorizedClientManager){

        // client sends token via: BearerTokenAuthenticationInterceptor
        // BearerTokenAuthenticationInterceptor 👉 Injects:  Authorization: Bearer <token>
        // Why needed:
        // ➡️ gRPC doesn’t use HTTP headers directly
        // ➡️ Interceptor simulates it
        BearerTokenAuthenticationInterceptor bearerTokenAuthenticationInterceptor
                = new BearerTokenAuthenticationInterceptor(() -> this.token(oauth2AuthorizedClientManager));

        ChannelBuilderOptions channelBuilderOptions = ChannelBuilderOptions
                .defaults()
                .withInterceptors(List.of(bearerTokenAuthenticationInterceptor));

        //ManagedChannel channel = factory.createChannel("localhost:9090"); // this is used when we are not using security based authentication : token
        ManagedChannel channel = factory.createChannel("localhost:9090", channelBuilderOptions);
        return StockTradingServiceGrpc.newBlockingStub(channel);
    }

    /**
     * 🔹 Bean: SecurityFilterChain
     *  Purpose: Configures Spring Security for the client web app.
     *  Key parts:
     * 1.  .authorizeHttpRequests() → permits static resources and /client/ endpoint, requires authentication for others.
     * 2.  .sessionManagement() → sets session policy to ALWAYS (session created for each login).
     * 3.  .oauth2Login() → configures OAuth2 login:
     *            - Custom login page at /oauth2/authorization/my-oidc-stock-trading-client.
     *            - Authorization endpoint base URI /oauth2/authorization.
     *            - Redirection endpoint /oauth2/code/*.
     * 4. .oauth2Client(Customizer.withDefaults()) → enables OAuth2 client support.
     *
     * Why needed: This ensures your client app can redirect users to the OAuth2 server, handle the authorization code flow, and store tokens in session.
     *
     * What it does:   .requestMatchers("/client/**", "/css/**", ...)  👉 Public endpoints
     * .anyRequest().authenticated()  ::: 👉 Everything else requires login
     * OAuth Login: 👉  .oauth2Login(login -> login.loginPage("/oauth2/authorization/my-oidc-stock-trading-client")) >> 👉 This triggers:  ➡️ Redirect to: http://localhost:8082/oauth2/authorize
     * Authorization Endpoint: 👉  .authorizationEndpoint().baseUri("/oauth2/authorization")  👉 Entry point for OAuth login
     * Redirection Endpoint: 👉  .redirectionEndpoint().baseUri("/oauth2/code/*") 👉 Handles callback from OAuth server
     *
     * /oauth2/authorization/**
     * ✔ Public — entry point to login
     * LOGIN FLOW : Protected endpoint → redirect → OAuth server → callback → session created
     * @param httpSecurity
     * @return
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
       return httpSecurity
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers("/client/","/css/**","/js/**","/images/**","/oauth2/authorization/**").permitAll() // CHECK THEN REMOVE THIS 👉 "/client/" > only welcome page
                                .anyRequest().authenticated())          // WE CAN ALSO DO THIS : .requestMatchers("/client/**").authenticated()
               /**
                * Forcing ALWAYS: Creates session for every request : Unnecessary overhead
                * ✅ Best Practice : .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                */
               .sessionManagement(httpSecuritySessionManagementConfigurer ->
                       httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))         // Forcing ALWAYS: Creates session for every request : Unnecessary overhead ✅ Best Practice : .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
               /**
                * .loginPage(...) 👉 It is NOT a UI page 👉 It is a trigger endpoint
                * loginPage("/oauth2/authorization/my-oidc-stock-trading-client")
                * ✅ Correct.
                * This is the client-side entry point that triggers the OAuth2 login flow.
                * When a user hits this URL, Spring Security redirects them to the authorization server (http://localhost:8082/oauth2/authorize) with the proper client ID and scopes.
                * Matches your application.properties client registration.
                *
                * Flow:
                * User → /client/AAPL
                * → Spring redirects to:
                * /oauth2/authorization/my-oidc-stock-trading-client
                * → Then to:
                * ➡️ Redirect to: http://localhost:8082/oauth2/authorize
                */
               .oauth2Login(httpSecurityOAuth2LoginConfigurer ->
                       httpSecurityOAuth2LoginConfigurer.loginPage("/oauth2/authorization/my-oidc-client")    // .loginPage("/oauth2/authorization/my-oidc-stock-trading-client") ✔ This is actually correct for OAuth2 login  👉 It is NOT a UI page  👉 It is a trigger endpoint ➡️ Redirect to: http://localhost:8082/oauth2/authorize
                               /**
                                * .authorizationEndpoint().baseUri("/oauth2/authorization")
                                * This is the local client-side endpoint where Spring Security builds the authorization request before redirecting to the server.
                                * It must match the loginPage prefix.
                                * You also correctly set the authorizationRequestRepository() to store state in the session.
                                * This defines: /oauth2/authorization/{registrationId}
                                * Example: /oauth2/authorization/my-oidc-stock-trading-client
                                * ✔ Matches your loginPage → GOOD
                                */
                               .authorizationEndpoint(authorizationEndpointConfig ->
                                       authorizationEndpointConfig.baseUri("/oauth2/authorization")    // 👉 Entry point for OAuth login
                                               /**
                                                * .authorizationRequestRepository(...)
                                                * Returns HttpSessionOAuth2AuthorizationRequestRepository.
                                                * Stores OAuth2 authorization requests in the HTTP session.
                                                * Why needed: Required for the authorization_code flow to track state between request and callback.
                                                */
                                               .authorizationRequestRepository(authorizationRequestRepository()))
                               /**
                                * .redirectionEndpoint().baseUri("/oauth2/code/*")
                                * This is the callback endpoint where the authorization server sends the authorization code.
                                * It must match the redirect URI you configured in application.properties:
                                * 👉  spring.security.oauth2.client.registration.my-oidc-client.redirect-uri=http://localhost:8081/login/oauth2/code/my-oidc-stock-trading-client
                                * Matches your config: redirect-uri=http://localhost:8081/login/oauth2/code/my-oidc-stock-trading-client
                                * Spring internally maps: /login/oauth2/code/{registrationId} it means : Spring expands /oauth2/code/* to handle /login/oauth2/code/{registrationId} automatically.
                                * THIS IS CUSTOM URI :: the code we wrote here tells client is expecting this "/login/oauth2/code/*" redirect-uri callback from oauth2-server
                                */
                               .redirectionEndpoint(redirectionEndpointConfig ->
                                       redirectionEndpointConfig.baseUri("/login/oauth2/code/*"))
               )   // 👉 Handles callback from OAuth server
               /**
                * .oauth2Client(Customizer.withDefaults())
                * 👉 Required because: You use OAuth2AuthorizedClientManager
                * Needed to manage tokens internally
                */
               .oauth2Client(Customizer.withDefaults())
                .build();
    }


    /**
     * 🔹 Bean: authorizationRequestRepository  :  authorizationRequestRepository()
     * Returns HttpSessionOAuth2AuthorizationRequestRepository.
     * Purpose: Stores OAuth2 authorization requests in the HTTP session.
     * Why needed: Required for the authorization_code flow to track state between request and callback.
     *
     * What it does:
     * Stores OAuth request in session
     * Used during redirect flow
     * @return
     */
    @Bean
    public HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository(){
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
}
