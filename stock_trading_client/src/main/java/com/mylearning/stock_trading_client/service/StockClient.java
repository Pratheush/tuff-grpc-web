package com.mylearning.stock_trading_client.service;


import com.mylearning.grpc.StockPriceRequest;
import com.mylearning.grpc.StockPriceResponse;
import com.mylearning.grpc.StockTradingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class StockClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        StockTradingServiceGrpc.StockTradingServiceBlockingStub blockingStub = StockTradingServiceGrpc.newBlockingStub(channel);

        StockPriceRequest priceRequest = StockPriceRequest.newBuilder().setStockName("AMZN").build();

        StockPriceResponse priceResponse = blockingStub.getStockPrice(priceRequest);
        System.out.println("Response: " + priceResponse);

        channel.shutdown();
    }
}
