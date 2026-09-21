package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlatWithPaymentStatus
import com.example.data.repository.AssociationSettings
import com.example.notification.ReminderNotificationHelper
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.Slate600

@Composable
fun SendReminderDialog(
    item: FlatWithPaymentStatus,
    settings: AssociationSettings,
    displayMonth: String,
    onSendNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    val flat = item.flat
    val context = LocalContext.current
    val amount = item.effectiveAmountDue

    val shareableMessage = ReminderNotificationHelper.buildShareableReminderText(
        associationName = settings.associationName,
        flatNumber = flat.flatNumber,
        residentName = flat.residentName,
        monthName = displayMonth,
        amountDue = amount,
        dueDay = settings.dueDayOfMonth,
        paymentInfo = settings.paymentDetails
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("send_reminder_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AmberLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = AmberPending,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Send Due Reminder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Flat ${flat.flatNumber} (${flat.residentName})",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Notification Preview:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Due Reminder: Flat ${flat.flatNumber}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hi ${flat.residentName}, $displayMonth maintenance fee of $${String.format("%.2f", amount)} is due by the ${settings.dueDayOfMonth}th. Please pay to ${settings.associationName}.",
                            fontSize = 12.sp,
                            color = Slate600,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Recipient Details",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Phone: ${flat.phone} • Email: ${flat.email}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Maintenance Fee Due Reminder")
                            putExtra(Intent.EXTRA_TEXT, shareableMessage)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Send Reminder Via"))
                        onSendNotification() // records log and increments count
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_reminder_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMS/Share", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onSendNotification()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("send_notification_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Push Alert", fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Slate600)
            }
        }
    )
}
