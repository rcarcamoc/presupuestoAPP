package com.aranthalion.presupuesto.data.repository

import com.aranthalion.presupuesto.data.local.dao.TransactionDao
import com.aranthalion.presupuesto.data.local.entity.TransactionEntity
import com.aranthalion.presupuesto.domain.model.Transaction
import com.aranthalion.presupuesto.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(TransactionEntity.fromDomainModel(transaction))
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(TransactionEntity.fromDomainModel(transaction))
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(TransactionEntity.fromDomainModel(transaction))
    }

    override fun getTransactions(): Flow<List<Transaction>> {
        return transactionDao.getTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTransactionsByDateRange(start: Date, end: Date): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(start.time, end.time).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomainModel()
    }
} 