package com.mylearning.stock_trading_server.service;

import com.google.protobuf.Timestamp;
import com.mylearning.grpc.StockPriceRequest;
import com.mylearning.grpc.StockPriceResponse;
import com.mylearning.grpc.StockTradingServiceGrpc;
import com.mylearning.stock_trading_server.model.Stock;
import com.mylearning.stock_trading_server.repository.StockTradingRepo;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class StockTradingRepositoryImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

    private final StockTradingRepo stockTradingRepo;

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();


    @Override
    public void getStockPrice(StockPriceRequest request, StreamObserver<StockPriceResponse> responseObserver) {

        SecurityContext securityContext = securityContextHolderStrategy.getContext();
        Authentication authentication = securityContext.getAuthentication();

        // my way of doing safety check null check
        if(authentication == null) {
            throw new SecurityException("Unauthenticated request");
        }
            String authenticationNameNonNull = authentication.getName();
            Object authenticationPrincipalNonNull = authentication.getPrincipal();
            Object authenticationDetailsNonNull = authentication.getDetails();
            Collection<? extends GrantedAuthority> authenticationAuthoritiesNonNull = authentication.getAuthorities();
            Object authenticationCredentialsNonNull = authentication.getCredentials();


        // THIS IS type-safety check ONLY WHEN you are sure that the authentication is not null
        // Objects.requireNonNull(authentication) IS RISK ❌ Problem: If request is unauthenticated → crash
        /*String authenticationNameNonNull = Objects.requireNonNull(authentication).getName();
        Object authenticationPrincipalNonNull = Objects.requireNonNull(authentication).getPrincipal();
        Object authenticationDetailsNonNull = Objects.requireNonNull(authentication).getDetails();
        Collection<? extends GrantedAuthority> authenticationAuthoritiesNonNull = Objects.requireNonNull(authentication).getAuthorities();
        Object authenticationCredentialsNonNull = Objects.requireNonNull(authentication).getCredentials();*/

        log.info("Authentication Name: {}", authenticationNameNonNull);
        log.info("Authentication Principal: {}", authenticationPrincipalNonNull);
        log.info("Authentication Details: {}", authenticationDetailsNonNull);
        log.info("Authentication Credentials: {}", authenticationCredentialsNonNull);
        log.info("Authentication Authorities: {}", authenticationAuthoritiesNonNull);

        String stockName = request.getStockName();
        Stock stock = stockTradingRepo.findByName(stockName);
        if(stock != null) {

            StockPriceResponse stockPriceResponse = StockPriceResponse.newBuilder()
                    .setStockName(stock.getName())
                    .setPrice(stock.getPrice())
                    .setCurrency(stock.getCurrency())
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .build())

                    // 🚨 This is a security vulnerability exposing: credentials, authentication internals REMOVE authenticationCredentials,  authenticationDetails
                    .setAuthenticationName(Objects.requireNonNullElseGet(authenticationNameNonNull, () -> "I am Null AUTHENTICATION NAME"))
                    .setAuthenticationPrincipal(Objects.requireNonNullElseGet(authenticationPrincipalNonNull, () -> "I am Null AUTHENTICATION PRINCIPAL").toString())

                    .build();

            log.info("Stock Price Response: {}", stockPriceResponse);
            responseObserver.onNext(stockPriceResponse);
            responseObserver.onCompleted();
        }
    }
}
