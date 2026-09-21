package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DashboardMetrics
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate600

@Composable
fun MetricsDashboardCard(
    metrics: DashboardMetrics,
    displayMonth: String,
    onRemindAllPending: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = metrics.collectionRate,
        label = "progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("metrics_dashboard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Month Overview and Collection Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COLLECTION OVERVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Slate600
                    )
                    Text(
                        text = displayMonth,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (metrics.collectionRate >= 0.8f) EmeraldLight else AmberLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${(metrics.collectionRate * 100).toInt()}% Paid",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (metrics.collectionRate >= 0.8f) EmeraldPaid else AmberPending
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = BluePrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Column Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    title = "Collected",
                    amount = "$${String.format("%.0f", metrics.totalCollected)}",
                    subtitle = "${metrics.paidCount} Flats",
                    color = EmeraldPaid,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                MetricColumn(
                    title = "Pending",
                    amount = "$${String.format("%.0f", metrics.totalPending)}",
                    subtitle = "${metrics.pendingCount} Flats",
                    color = AmberPending,
                    icon = Icons.Default.PendingActions,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                MetricColumn(
                    title = "Total Dues",
                    amount = "$${String.format("%.0f", metrics.totalExpected)}",
                    subtitle = "${metrics.totalFlats} Total",
                    color = BluePrimary,
                    icon = null,
                    modifier = Modifier.weight(1f)
                )
            }

            if (metrics.pendingCount > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onRemindAllPending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("remind_all_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPending,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Remind All",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notify All ${metrics.pendingCount} Pending Flats",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    title: String,
    amount: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Slate600,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate600
            )
        }
    }
}
