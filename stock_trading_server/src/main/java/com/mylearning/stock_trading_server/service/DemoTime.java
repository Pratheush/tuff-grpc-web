package com.mylearning.stock_trading_server.service;

import java.time.LocalDateTime;

public class DemoTime {
    private static LocalDateTime timeStamp=LocalDateTime.now();

    public static void main(String[] args) {
        System.out.println("Current Time: " + timeStamp);
    }
}
