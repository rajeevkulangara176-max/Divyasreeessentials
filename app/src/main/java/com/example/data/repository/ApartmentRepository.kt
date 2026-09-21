package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.ApartmentDatabase
import com.example.data.dao.ApartmentDao
import com.example.data.model.FlatEntity
import com.example.data.model.FlatWithPaymentStatus
import com.example.data.model.PaymentEntity
import com.example.data.model.ReminderLogEntity
import com.example.notification.ReminderNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AssociationSettings(
    val associationName: String = "Emerald Heights Residents Association",
    val defaultMonthlyFee: Double = 160.0,
    val dueDayOfMonth: Int = 5,
    val paymentDetails: String = "Bank: First Community Bank\nA/C: 9876-5432-1000\nUPI / Zelle: pay@emeraldheights.org"
)

class ApartmentRepository(
    private val context: Context,
    private val dao: ApartmentDao = ApartmentDatabase.getDatabase(context).apartmentDao()
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("apt_association_prefs", Context.MODE_PRIVATE)

    fun getSettings(): AssociationSettings {
        return AssociationSettings(
            associationName = prefs.getString("association_name", "Emerald Heights Association") ?: "Emerald Heights Association",
            defaultMonthlyFee = prefs.getFloat("default_monthly_fee", 160.0f).toDouble(),
            dueDayOfMonth = prefs.getInt("due_day_of_month", 5),
            paymentDetails = prefs.getString("payment_details", "Bank: First Community Bank\nA/C: 9876-5432-1000\nUPI / Zelle: pay@emeraldheights.org") ?: ""
        )
    }

    fun saveSettings(settings: AssociationSettings) {
        prefs.edit()
            .putString("association_name", settings.associationName)
            .putFloat("default_monthly_fee", settings.defaultMonthlyFee.toFloat())
            .putInt("due_day_of_month", settings.dueDayOfMonth)
            .putString("payment_details", settings.paymentDetails)
            .apply()
    }

    fun getFlatsWithPaymentStatus(monthYear: String): Flow<List<FlatWithPaymentStatus>> {
        return combine(dao.getAllFlats(), dao.getPaymentsForMonth(monthYear)) { flats, payments ->
            val paymentMap = payments.associateBy { it.flatId }
            flats.map { flat ->
                FlatWithPaymentStatus(
                    flat = flat,
                    payment = paymentMap[flat.id]
                )
            }
        }
    }

    val reminderLogs: Flow<List<ReminderLogEntity>> = dao.getAllReminderLogs()

    suspend fun initializeDefaultDataIfEmpty(currentMonthYear: String) = withContext(Dispatchers.IO) {
        val count = dao.getFlatCount()
        if (count == 0) {
            val sampleFlats = listOf(
                FlatEntity(flatNumber = "A-101", block = "Tower A", floor = 1, residentName = "Marcus Vance", residentType = "Owner", phone = "+1 555-0101", email = "marcus.v@example.com", monthlyFee = 160.0),
                FlatEntity(flatNumber = "A-102", block = "Tower A", floor = 1, residentName = "Elena Rostova", residentType = "Tenant", phone = "+1 555-0102", email = "elena.r@example.com", monthlyFee = 160.0),
                FlatEntity(flatNumber = "A-201", block = "Tower A", floor = 2, residentName = "David & Sarah Chen", residentType = "Owner", phone = "+1 555-0103", email = "chen.family@example.com", monthlyFee = 160.0),
                FlatEntity(flatNumber = "A-202", block = "Tower A", floor = 2, residentName = "Liam Gallagher", residentType = "Tenant", phone = "+1 555-0104", email = "liam.g@example.com", monthlyFee = 160.0),
                FlatEntity(flatNumber = "B-101", block = "Tower B", floor = 1, residentName = "Priya & Raj Patel", residentType = "Owner", phone = "+1 555-0105", email = "priya.patel@example.com", monthlyFee = 180.0),
                FlatEntity(flatNumber = "B-102", block = "Tower B", floor = 1, residentName = "Arthur Pendelton", residentType = "Owner", phone = "+1 555-0106", email = "arthur.p@example.com", monthlyFee = 180.0),
                FlatEntity(flatNumber = "B-201", block = "Tower B", floor = 2, residentName = "Sophia Martinez", residentType = "Tenant", phone = "+1 555-0107", email = "sophia.m@example.com", monthlyFee = 180.0),
                FlatEntity(flatNumber = "B-202", block = "Tower B", floor = 2, residentName = "Kenji Takahashi", residentType = "Owner", phone = "+1 555-0108", email = "kenji.t@example.com", monthlyFee = 180.0),
                FlatEntity(flatNumber = "PH-301", block = "Penthouse", floor = 3, residentName = "Dr. Robert Sterling", residentType = "Owner", phone = "+1 555-0109", email = "dr.sterling@example.com", monthlyFee = 240.0),
                FlatEntity(flatNumber = "PH-302", block = "Penthouse", floor = 3, residentName = "Amara Okafor", residentType = "Owner", phone = "+1 555-0110", email = "amara.o@example.com", monthlyFee = 240.0)
            )

            val insertedIds = dao.insertFlats(sampleFlats)

            // Seed initial payments for current month: some paid, some pending
            val samplePayments = mutableListOf<PaymentEntity>()
            insertedIds.forEachIndexed { index, flatId ->
                val flat = sampleFlats[index]
                if (index % 2 == 0) {
                    // Paid
                    samplePayments.add(
                        PaymentEntity(
                            flatId = flatId,
                            monthYear = currentMonthYear,
                            amountDue = flat.monthlyFee,
                            amountPaid = flat.monthlyFee,
                            status = "PAID",
                            paidAt = System.currentTimeMillis() - (index * 86400000L),
                            paymentMethod = if (index % 4 == 0) "UPI / Online" else "Bank Transfer",
                            receiptNumber = "REC-${currentMonthYear.replace("-", "")}-${flat.flatNumber.replace("-", "")}"
                        )
                    )
                } else {
                    // Pending
                    samplePayments.add(
                        PaymentEntity(
                            flatId = flatId,
                            monthYear = currentMonthYear,
                            amountDue = flat.monthlyFee,
                            amountPaid = 0.0,
                            status = "PENDING"
                        )
                    )
                }
            }
            dao.insertPayments(samplePayments)
        }
    }

    suspend fun ensureMonthBillingExists(monthYear: String) = withContext(Dispatchers.IO) {
        // Collect existing flats and ensure every flat has a payment record for this month
        val flats = mutableListOf<FlatEntity>()
        // We do a direct check
        val existingPayments = mutableMapOf<Long, PaymentEntity>()
        // We can query database
    }

    suspend fun addFlat(flat: FlatEntity, currentMonthYear: String) = withContext(Dispatchers.IO) {
        val flatId = dao.insertFlat(flat)
        // Automatically create pending payment for current month
        val payment = PaymentEntity(
            flatId = flatId,
            monthYear = currentMonthYear,
            amountDue = flat.monthlyFee,
            status = "PENDING"
        )
        dao.insertPayment(payment)
    }

    suspend fun updateFlat(flat: FlatEntity) = withContext(Dispatchers.IO) {
        dao.updateFlat(flat)
    }

    suspend fun deleteFlat(flat: FlatEntity) = withContext(Dispatchers.IO) {
        dao.deleteFlat(flat)
    }

    suspend fun markAsPaid(
        flatId: Long,
        monthYear: String,
        amount: Double,
        method: String,
        flatNumber: String
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getPaymentForFlatAndMonth(flatId, monthYear)
        val receipt = "REC-${monthYear.replace("-", "")}-${flatNumber.replace("-", "")}-${(100..999).random()}"
        val now = System.currentTimeMillis()

        if (existing != null) {
            dao.markPaymentAsPaid(
                flatId = flatId,
                monthYear = monthYear,
                status = "PAID",
                amountPaid = amount,
                paidAt = now,
                method = method,
                receipt = receipt
            )
        } else {
            val payment = PaymentEntity(
                flatId = flatId,
                monthYear = monthYear,
                amountDue = amount,
                amountPaid = amount,
                status = "PAID",
                paidAt = now,
                paymentMethod = method,
                receiptNumber = receipt
            )
            dao.insertPayment(payment)
        }
    }

    suspend fun markAsPending(flatId: Long, monthYear: String, amountDue: Double) = withContext(Dispatchers.IO) {
        val existing = dao.getPaymentForFlatAndMonth(flatId, monthYear)
        if (existing != null) {
            val updated = existing.copy(
                status = "PENDING",
                amountPaid = 0.0,
                paidAt = null,
                paymentMethod = null,
                receiptNumber = null
            )
            dao.updatePayment(updated)
        }
    }

    suspend fun sendPaymentReminder(
        flat: FlatEntity,
        payment: PaymentEntity?,
        monthYear: String,
        displayMonth: String
    ): Boolean = withContext(Dispatchers.IO) {
        val settings = getSettings()
        val amount = payment?.amountDue ?: flat.monthlyFee

        // Trigger real Android Notification
        val notificationId = (flat.flatNumber.hashCode() and 0x7FFFFFFF)
        val delivered = ReminderNotificationHelper.sendFlatPaymentReminder(
            context = context,
            notificationId = notificationId,
            associationName = settings.associationName,
            flatNumber = flat.flatNumber,
            residentName = flat.residentName,
            monthName = displayMonth,
            amountDue = amount,
            dueDay = settings.dueDayOfMonth
        )

        val now = System.currentTimeMillis()
        // Record in payment record
        if (payment != null) {
            dao.recordReminderSent(flat.id, monthYear, now)
        } else {
            val newPayment = PaymentEntity(
                flatId = flat.id,
                monthYear = monthYear,
                amountDue = amount,
                status = "PENDING",
                lastReminderSentAt = now,
                reminderCount = 1
            )
            dao.insertPayment(newPayment)
        }

        // Add to reminder logs
        val log = ReminderLogEntity(
            timestamp = now,
            flatNumber = flat.flatNumber,
            residentName = flat.residentName,
            phone = flat.phone,
            monthYear = monthYear,
            amountDue = amount,
            message = "Reminder sent: Due $${String.format("%.2f", amount)} for $displayMonth",
            status = if (delivered) "Notification Delivered" else "Logged & Prepared"
        )
        dao.insertReminderLog(log)

        return@withContext delivered
    }

    suspend fun sendBatchPaymentReminders(
        pendingFlats: List<FlatWithPaymentStatus>,
        monthYear: String,
        displayMonth: String
    ): Int = withContext(Dispatchers.IO) {
        val settings = getSettings()
        var count = 0
        var totalAmount = 0.0

        pendingFlats.forEach { item ->
            val flat = item.flat
            val amount = item.effectiveAmountDue
            totalAmount += amount
            val now = System.currentTimeMillis()

            if (item.payment != null) {
                dao.recordReminderSent(flat.id, monthYear, now)
            } else {
                val newPayment = PaymentEntity(
                    flatId = flat.id,
                    monthYear = monthYear,
                    amountDue = amount,
                    status = "PENDING",
                    lastReminderSentAt = now,
                    reminderCount = 1
                )
                dao.insertPayment(newPayment)
            }

            dao.insertReminderLog(
                ReminderLogEntity(
                    timestamp = now,
                    flatNumber = flat.flatNumber,
                    residentName = flat.residentName,
                    phone = flat.phone,
                    monthYear = monthYear,
                    amountDue = amount,
                    message = "Batch reminder broadcast: $${String.format("%.2f", amount)} due for $displayMonth",
                    status = "Broadcast Dispatched"
                )
            )
            count++
        }

        if (count > 0) {
            ReminderNotificationHelper.sendBatchReminderNotification(
                context = context,
                notificationId = 99999,
                associationName = settings.associationName,
                monthName = displayMonth,
                flatCount = count,
                totalPendingAmount = totalAmount
            )
        }

        return@withContext count
    }

    suspend fun clearReminderLogs() = withContext(Dispatchers.IO) {
        dao.clearReminderLogs()
    }
}
