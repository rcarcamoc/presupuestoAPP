package com.aranthalion.presupuesto.domain.repository

import com.aranthalion.presupuesto.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TransactionRepository {
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    fun getTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(start: Date, end: Date): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
} 