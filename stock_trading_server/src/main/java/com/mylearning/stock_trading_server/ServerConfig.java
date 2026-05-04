package com.mylearning.stock_trading_server;

import com.mylearning.stock_trading_server.service.StockTradingRepositoryImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
@Configuration
public class ServerConfig {

    private final StockTradingRepositoryImpl serviceImpl;

    public ServerConfig(StockTradingRepositoryImpl serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    @Bean
    public Server grpcServer() {
        return ServerBuilder.forPort(9090)
                .addService(serviceImpl)
                .build();
    }
}
*/
