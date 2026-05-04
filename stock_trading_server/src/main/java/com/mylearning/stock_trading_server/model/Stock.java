package com.mylearning.stock_trading_server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter
@Setter
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", unique = true, nullable = false)
    private String name;

    private double price;

    private String currency;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
