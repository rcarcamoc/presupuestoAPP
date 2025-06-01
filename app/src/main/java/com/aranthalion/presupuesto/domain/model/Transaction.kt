package com.aranthalion.presupuesto.domain.model

import java.math.BigDecimal
import java.util.Date

data class Transaction(
    val id: Long = 0,
    val amount: BigDecimal,
    val description: String,
    val date: Date,
    val category: String,
    val type: TransactionType,
    val source: TransactionSource
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionSource {
    MANUAL,
    EMAIL,
    BANK_API
} 