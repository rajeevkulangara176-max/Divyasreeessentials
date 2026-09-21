package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object ReminderNotificationHelper {

    const val CHANNEL_ID = "apt_payment_reminders"
    private const val CHANNEL_NAME = "Apartment Payment Reminders"
    private const val CHANNEL_DESC = "Notifications for monthly flat maintenance fee reminders and dues"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canSendNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendFlatPaymentReminder(
        context: Context,
        notificationId: Int,
        associationName: String,
        flatNumber: String,
        residentName: String,
        monthName: String,
        amountDue: Double,
        dueDay: Int
    ): Boolean {
        createNotificationChannel(context)

        if (!canSendNotifications(context)) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Due Reminder: Flat $flatNumber"
        val message = "Hi $residentName, $monthName maintenance fee of $${String.format("%.2f", amountDue)} is due by the ${dueDay}th. Please pay to $associationName."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    fun sendBatchReminderNotification(
        context: Context,
        notificationId: Int,
        associationName: String,
        monthName: String,
        flatCount: Int,
        totalPendingAmount: Double
    ): Boolean {
        createNotificationChannel(context)

        if (!canSendNotifications(context)) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Payment Reminders Dispatched"
        val message = "Sent $monthName dues reminder to $flatCount flats (Total pending: $${String.format("%.2f", totalPendingAmount)}) for $associationName."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    fun buildShareableReminderText(
        associationName: String,
        flatNumber: String,
        residentName: String,
        monthName: String,
        amountDue: Double,
        dueDay: Int,
        paymentInfo: String
    ): String {
        return """
            🏢 $associationName
            📢 PAYMENT REMINDER FOR $monthName
            
            Dear $residentName (Flat $flatNumber),
            
            This is a friendly reminder that your apartment maintenance fee for $monthName is due by the ${dueDay}th of this month.
            
            💰 Amount Due: $${String.format("%.2f", amountDue)}
            
            💳 Payment Details:
            $paymentInfo
            
            Thank you for your cooperation in maintaining our community!
            - Management Committee, $associationName
        """.trimIndent()
    }
}
