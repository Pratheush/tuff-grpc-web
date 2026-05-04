package com.mylearning.stock_trading_server.repository;

import com.mylearning.stock_trading_server.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTradingRepo extends JpaRepository<Stock,Long> {
    Stock findByName(String name);
}
