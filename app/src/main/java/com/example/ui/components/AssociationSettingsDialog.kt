package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AssociationSettings

@Composable
fun AssociationSettingsDialog(
    currentSettings: AssociationSettings,
    onSave: (AssociationSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentSettings.associationName) }
    var feeText by remember { mutableStateOf(String.format("%.2f", currentSettings.defaultMonthlyFee)) }
    var dueDayText by remember { mutableStateOf(currentSettings.dueDayOfMonth.toString()) }
    var paymentDetails by remember { mutableStateOf(currentSettings.paymentDetails) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("association_settings_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Association Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Association / Society Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("association_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("Default Fee ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("default_fee_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it },
                        label = { Text("Due Day (e.g. 5)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("due_day_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = paymentDetails,
                    onValueChange = { paymentDetails = it },
                    label = { Text("Bank / UPI Payment Instructions") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("payment_details_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeText.toDoubleOrNull() ?: currentSettings.defaultMonthlyFee
                    val dueDay = dueDayText.toIntOrNull()?.coerceIn(1, 31) ?: currentSettings.dueDayOfMonth
                    onSave(
                        currentSettings.copy(
                            associationName = name.ifBlank { currentSettings.associationName },
                            defaultMonthlyFee = fee,
                            dueDayOfMonth = dueDay,
                            paymentDetails = paymentDetails
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_settings_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
