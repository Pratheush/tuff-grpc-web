#!/usr/bin/env bash

curl -X GET http://localhost:8081/client/AAPL


curl -X GET http://localhost:8081/client/AMZN

#grpcurl -plaintext -d '{"stock_name": "AAPL"}' localhost:9090 com.mylearning.stock_trading_server.StockTradingService.GetStockPrice
