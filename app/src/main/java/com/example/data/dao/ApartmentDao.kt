package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FlatEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.ReminderLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApartmentDao {

    @Query("SELECT * FROM flats WHERE active = 1 ORDER BY block ASC, flatNumber ASC")
    fun getAllFlats(): Flow<List<FlatEntity>>

    @Query("SELECT * FROM flats WHERE id = :id LIMIT 1")
    suspend fun getFlatById(id: Long): FlatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlat(flat: FlatEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlats(flats: List<FlatEntity>): List<Long>

    @Update
    suspend fun updateFlat(flat: FlatEntity)

    @Delete
    suspend fun deleteFlat(flat: FlatEntity)

    @Query("SELECT COUNT(*) FROM flats")
    suspend fun getFlatCount(): Int

    // Payments
    @Query("SELECT * FROM payments WHERE monthYear = :monthYear")
    fun getPaymentsForMonth(monthYear: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE flatId = :flatId AND monthYear = :monthYear LIMIT 1")
    suspend fun getPaymentForFlatAndMonth(flatId: Long, monthYear: String): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("UPDATE payments SET status = :status, amountPaid = :amountPaid, paidAt = :paidAt, paymentMethod = :method, receiptNumber = :receipt WHERE flatId = :flatId AND monthYear = :monthYear")
    suspend fun markPaymentAsPaid(
        flatId: Long,
        monthYear: String,
        status: String = "PAID",
        amountPaid: Double,
        paidAt: Long,
        method: String,
        receipt: String
    )

    @Query("UPDATE payments SET reminderCount = reminderCount + 1, lastReminderSentAt = :reminderTime WHERE flatId = :flatId AND monthYear = :monthYear")
    suspend fun recordReminderSent(flatId: Long, monthYear: String, reminderTime: Long)

    // Reminder Logs
    @Query("SELECT * FROM reminder_logs ORDER BY timestamp DESC")
    fun getAllReminderLogs(): Flow<List<ReminderLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderLog(log: ReminderLogEntity): Long

    @Query("DELETE FROM reminder_logs")
    suspend fun clearReminderLogs()
}
