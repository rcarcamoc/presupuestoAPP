package com.aranthalion.presupuesto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aranthalion.presupuesto.domain.model.Transaction
import com.aranthalion.presupuesto.domain.model.TransactionSource
import com.aranthalion.presupuesto.domain.model.TransactionType
import java.math.BigDecimal
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val date: Long,
    val category: String,
    val type: String,
    val source: String
) {
    fun toDomainModel(): Transaction = Transaction(
        id = id,
        amount = BigDecimal(amount),
        description = description,
        date = Date(date),
        category = category,
        type = TransactionType.valueOf(type),
        source = TransactionSource.valueOf(source)
    )

    companion object {
        fun fromDomainModel(transaction: Transaction): TransactionEntity = TransactionEntity(
            id = transaction.id,
            amount = transaction.amount.toDouble(),
            description = transaction.description,
            date = transaction.date.time,
            category = transaction.category,
            type = transaction.type.name,
            source = transaction.source.name
        )
    }
} 