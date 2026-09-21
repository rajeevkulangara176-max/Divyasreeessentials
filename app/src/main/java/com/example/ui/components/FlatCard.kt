package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FlatCard(
    item: FlatWithPaymentStatus,
    onMarkPaidClick: () -> Unit,
    onSendReminderClick: () -> Unit,
    onViewReceiptClick: () -> Unit,
    onEditFlatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flat = item.flat
    val payment = item.payment
    val isPaid = item.isPaid
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("flat_card_${flat.flatNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Flat badge, Block, Resident Type, and Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = flat.flatNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${flat.block} • Fl ${flat.floor}",
                        fontSize = 12.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (flat.residentType == "Owner") MaterialTheme.colorScheme.secondaryContainer else Slate100)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = flat.residentType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (flat.residentType == "Owner") BluePrimary else Slate600
                        )
                    }
                }

                IconButton(
                    onClick = onEditFlatClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("edit_flat_${flat.flatNumber}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Flat Details",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resident Name & Contact Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = flat.residentName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = flat.phone,
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }

                // Monthly Fee Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.0f", item.effectiveAmountDue)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Monthly Fee",
                        fontSize = 10.sp,
                        color = Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Bar & Actions
            if (isPaid) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(EmeraldLight)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldPaid,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PAID",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPaid
                                )
                            }
                        }

                        if (payment?.paidAt != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dateFormatter.format(Date(payment.paidAt)),
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onViewReceiptClick,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("view_receipt_${flat.flatNumber}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Receipt",
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Pending status row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AmberLight)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = AmberPending,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PAYMENT DUE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberPending
                                )
                            }
                        }

                        if ((payment?.reminderCount ?: 0) > 0) {
                            Text(
                                text = "${payment?.reminderCount} reminder(s) sent",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSendReminderClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("remind_flat_${flat.flatNumber}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AmberPending
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Remind",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = onMarkPaidClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(38.dp)
                                .testTag("mark_paid_flat_${flat.flatNumber}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluePrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mark Paid",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
