package com.mylearning.stock_trading_client.controller;

import com.mylearning.grpc.StockPriceRequest;
import com.mylearning.grpc.StockPriceResponse;
import com.mylearning.grpc.StockTradingServiceGrpc;
import com.mylearning.stock_trading_client.dto.StockPriceResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

//@RestController
@Controller
@RequestMapping("/client")
public class StockController {

    private final StockTradingServiceGrpc.StockTradingServiceBlockingStub blockingStub;

    public StockController(StockTradingServiceGrpc.StockTradingServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    /*@GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("welcome to stock-trading-client");
    }*/

    //@GetMapping("/{name}")
    @GetMapping
    public String getStockPrice(@RequestParam String stockSymbol, Model model) {

        StockPriceRequest stockPriceRequest = StockPriceRequest
                .newBuilder()
                .setStockName(stockSymbol)
                .build();
        StockPriceResponse priceResponse = blockingStub.getStockPrice(stockPriceRequest);

        // mapping the response to the dto since in rest protobuf is not used instead json is returned as response
        StockPriceResponseDTO priceResponseDTO = new StockPriceResponseDTO();
        priceResponseDTO.setStockName(priceResponse.getStockName());
        priceResponseDTO.setPrice(priceResponse.getPrice());
        priceResponseDTO.setCurrency(priceResponse.getCurrency());
        //priceResponseDTO.setTimestamp(priceResponse.getTimestamp()); // this is used when google.protobuf.Timestamp was used
        priceResponseDTO.setTimestamp(Instant.ofEpochSecond(
                priceResponse.getTimestamp().getSeconds(),
                priceResponse.getTimestamp().getNanos()
        ).atZone(ZoneId.systemDefault()).toLocalDateTime());    // this is used when LocalDateTime was used here
        priceResponseDTO.setAuthenticationName(priceResponse.getAuthenticationName());
        priceResponseDTO.setAuthenticationPrincipal(priceResponse.getAuthenticationPrincipal());

        model.addAttribute("stockResponse",priceResponseDTO);

        return "stocks";
    }
}
