package com.mylearning.stock_trading_client.dto;

import com.google.protobuf.Timestamp;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class StockPriceResponseDTO {
    private String stockName;
    private double price;
    private String currency;
    //private Timestamp timestamp;
    private LocalDateTime timestamp;
    private String authenticationName;
    private String authenticationPrincipal;
}
