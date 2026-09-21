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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlatWithPaymentStatus
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentReceiptDialog(
    item: FlatWithPaymentStatus,
    associationName: String,
    displayMonth: String,
    onDismiss: () -> Unit,
    onRevertToPending: () -> Unit
) {
    val flat = item.flat
    val payment = item.payment ?: return
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    val paidDateStr = payment.paidAt?.let { dateFormatter.format(Date(it)) } ?: "Recorded"

    val receiptText = """
        🧾 OFFICIAL MAINTENANCE RECEIPT
        $associationName
        ----------------------------------
        Receipt No: ${payment.receiptNumber ?: "N/A"}
        Billing Month: $displayMonth
        Flat No: ${flat.flatNumber} (${flat.block})
        Resident: ${flat.residentName} (${flat.residentType})
        ----------------------------------
        Amount Paid: $${String.format("%.2f", payment.amountPaid)}
        Payment Mode: ${payment.paymentMethod ?: "Online"}
        Date: $paidDateStr
        Status: SUCCESSFUL / PAID
        ----------------------------------
        Thank you for keeping our society running smoothly!
        Management Committee, $associationName
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("payment_receipt_dialog"),
        confirmButton = {
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Maintenance Receipt - Flat ${flat.flatNumber}")
                        putExtra(Intent.EXTRA_TEXT, receiptText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
                },
                modifier = Modifier.testTag("share_receipt_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Receipt")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onRevertToPending()
                    onDismiss()
                }
            ) {
                Text("Mark as Unpaid", color = Color(0xFFE11D48), fontSize = 12.sp)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPaid,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Payment Receipt",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Text(
                    text = associationName.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate600
                )
                Text(
                    text = "RECEIPT #${payment.receiptNumber ?: "N/A"}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                ReceiptRow(label = "Flat", value = "${flat.flatNumber} (${flat.block})")
                ReceiptRow(label = "Resident", value = flat.residentName)
                ReceiptRow(label = "Period", value = displayMonth)
                ReceiptRow(label = "Payment Method", value = payment.paymentMethod ?: "Online")
                ReceiptRow(label = "Paid On", value = paidDateStr)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL PAID",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${String.format("%.2f", payment.amountPaid)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPaid
                    )
                }
            }
        }
    )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Slate600
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
