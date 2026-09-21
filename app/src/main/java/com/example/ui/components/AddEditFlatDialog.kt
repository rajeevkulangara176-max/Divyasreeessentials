package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlatEntity
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600

@Composable
fun AddEditFlatDialog(
    flatToEdit: FlatEntity?,
    defaultFee: Double,
    onSave: (flatNumber: String, block: String, floor: Int, residentName: String, residentType: String, phone: String, email: String, fee: Double) -> Unit,
    onDelete: ((FlatEntity) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var flatNumber by remember { mutableStateOf(flatToEdit?.flatNumber ?: "") }
    var block by remember { mutableStateOf(flatToEdit?.block ?: "Tower A") }
    var floorText by remember { mutableStateOf(flatToEdit?.floor?.toString() ?: "1") }
    var residentName by remember { mutableStateOf(flatToEdit?.residentName ?: "") }
    var residentType by remember { mutableStateOf(flatToEdit?.residentType ?: "Owner") }
    var phone by remember { mutableStateOf(flatToEdit?.phone ?: "") }
    var email by remember { mutableStateOf(flatToEdit?.email ?: "") }
    var feeText by remember { mutableStateOf(String.format("%.2f", flatToEdit?.monthlyFee ?: defaultFee)) }

    val isEditing = flatToEdit != null

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_edit_flat_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Edit Flat Details" else "Add New Flat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isEditing && onDelete != null) {
                    IconButton(onClick = {
                        onDelete(flatToEdit)
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Flat",
                            tint = Color(0xFFE11D48)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = flatNumber,
                        onValueChange = { flatNumber = it },
                        label = { Text("Flat No. (e.g. A-302)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("flat_number_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("Floor") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("floor_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = block,
                    onValueChange = { block = it },
                    label = { Text("Block / Tower") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = residentName,
                    onValueChange = { residentName = it },
                    label = { Text("Resident Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resident_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Resident Type Selection
                Text(
                    text = "Occupancy Status",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate600
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Owner", "Tenant").forEach { type ->
                        val selected = residentType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) BluePrimary else Slate100)
                                .clickable { residentType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Monthly Maintenance Fee ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_fee_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (flatNumber.isNotBlank() && residentName.isNotBlank()) {
                        val floor = floorText.toIntOrNull() ?: 1
                        val fee = feeText.toDoubleOrNull() ?: defaultFee
                        onSave(flatNumber, block, floor, residentName, residentType, phone, email, fee)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_flat_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isEditing) "Update Flat" else "Add Flat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
