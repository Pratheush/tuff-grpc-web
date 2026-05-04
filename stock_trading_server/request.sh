#!/usr/bin/env bash

#to compile and generate java classes run in intellij terminal using mvn of this project > mvnw.cmd clean package -DskipTests & to run spring-boot-application using project maven : > mvnw.cmd spring-boot:run -DskipTests

#to run this file > sh request.sh

#run this file in git-bash after chmod +x then to run this file > sh request.sh

#grpcurl -plaintext -d '{"stock_name": "AAPL"}' localhost:9090 com.mylearning.stock_trading_server.StockTradingService.GetStockPrice

# BELOW TWO GRPCURL COMMANDS DO NOT WORK SINCE IT NEEDS FULLY QUALIFIED SERVICE NAME
#grpcurl -plaintext -d '{"stock_name": "AMZN"}' localhost:9090 StockTradingService.GetStockPrice
#grpcurl -plaintext -d '{"stock_name": "AMZN"}' -H "Authorization: Basic $(echo -n 'Anuj:anuj123' | base64)" localhost:9090 StockTradingService.GetStockPrice

grpcurl -plaintext -d '{"stock_name": "AMZN"}' -H "Authorization: Basic $(echo -n 'Anuj:anuj123' | base64)" localhost:9090 com.mylearning.stock_trading_server.StockTradingService.GetStockPrice

# list all services exposed by your server with:
#grpcurl -H "Authorization: Basic $(echo -n 'Anuj:anuj123' | base64)" -plaintext localhost:9090 list

# list methods of a service: This shows you the exact names you must use in grpcurl and in your security config.
# If you declare a package in your proto, gRPC always prepends it to the service name.
# That’s why you need the full com.mylearning.stock_trading_server.StockTradingService.GetStockPrice in grpcurl and in your security config.
# If you don’t want the long prefix, remove or change the package line in your proto.
#grpcurl -H "Authorization: Basic $(echo -n 'Anuj:anuj123' | base64)" -plaintext localhost:9090 list com.mylearning.stock_trading_server.StockTradingService


#to check health of the GRPC-Server this health check is for GRPC specifically
#grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check
