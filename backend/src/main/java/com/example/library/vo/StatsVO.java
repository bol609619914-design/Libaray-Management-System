package com.example.library.vo;

import java.math.BigDecimal;

public record StatsVO(long readers, long books, long activeBorrows, long overdueBorrows, BigDecimal unpaidFines) {
}
