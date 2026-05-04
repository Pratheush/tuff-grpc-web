package com.mylearning.stock_trading_server;

import com.mylearning.grpc.StockPriceRequest;
import com.mylearning.grpc.StockPriceResponse;
import com.mylearning.grpc.StockTradingServiceGrpc;
import com.mylearning.stock_trading_server.repository.StockTradingRepo;
import com.mylearning.stock_trading_server.service.StockTradingRepositoryImpl;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Spring does NOT inject dependencies into static fields
 * @TestInstance(TestInstance.Lifecycle.PER_METHOD) : A new test class instance is created per test. Method 'setup' annotated with '@BeforeAll' should be static
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Use a single test instance lifecycle:
@SpringBootTest(properties = {
        "spring.grpc.server.enabled=false" // this prevents nettyGrpcServerLifecycle port-binding and conflict
})
class StockTradingServerApplicationTests {

    private Server server;
    private ManagedChannel channel ;
            /*= ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();*/
    private StockTradingServiceGrpc.StockTradingServiceBlockingStub blockingStub;

    @Autowired
    private StockTradingRepositoryImpl stockService;

    @BeforeAll
    void setup() throws Exception {
        String serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(stockService)
                .build()
                .start();

        channel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();

        blockingStub = StockTradingServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void getStockPrice() {
        StockPriceRequest priceRequest = StockPriceRequest.newBuilder().setStockName("AMZN").build();

        StockPriceResponse priceResponse = blockingStub.getStockPrice(priceRequest);
        assertEquals("AMZN", priceResponse.getStockName());
        assertEquals(3888.75, priceResponse.getPrice());
        assertEquals("USD", priceResponse.getCurrency());
    }


}
