package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerJobEntity

@Composable
fun ReviewDialog(
    job: CustomerJobEntity,
    isComplete: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (rating: Float, feedback: String?) -> Unit
) {
    var rating by remember { mutableFloatStateOf(5.0f) }
    var feedbackText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isComplete) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                Text(
                    text = if (isComplete) "Complete Order #${job.id}" else "Cancel Order #${job.id}",
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
                Text(
                    text = "Customer: ${job.customerName} (${job.serviceType})",
                    fontSize = 14.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isComplete) {
                    Text(
                        text = "Rate Performance:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i Stars",
                                tint = if (i <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { rating = i.toFloat() }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text(if (isComplete) "Customer Feedback / Notes" else "Reason for Cancellation") },
                    placeholder = { Text(if (isComplete) "e.g., Job done well and on time" else "e.g., Customer requested rescheduling") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(rating, feedbackText.trim().ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isComplete) Color(0xFF16A34A) else Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isComplete) "Confirm Completion" else "Confirm Cancel",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
