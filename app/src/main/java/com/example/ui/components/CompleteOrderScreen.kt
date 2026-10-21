package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerJobEntity
import com.example.util.GoogleDriveMediaHelper
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file"
private const val DRIVE_ACCOUNT = "hurifixservices@gmail.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteOrderScreen(
    job: CustomerJobEntity,
    onDismiss: () -> Unit,
    onConfirmComplete: (CustomerJobEntity, Float, String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var rating by remember { mutableStateOf(5) }
    var feedback by remember { mutableStateOf("") }
    var beforeUri by remember { mutableStateOf<Uri?>(null) }
    var afterUri by remember { mutableStateOf<Uri?>(null) }
    var showConfirm by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var signInAction by remember { mutableStateOf<((String) -> Unit)?>(null) }
    val beforePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { beforeUri = it }
    val afterPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { afterUri = it }
    val oauthClient = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestScopes(Scope(DRIVE_SCOPE)).build()
        GoogleSignIn.getClient(context, options)
    }
    val authLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = accountTask.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account.email != DRIVE_ACCOUNT) {
                busy = false
                Toast.makeText(context, "Please select $DRIVE_ACCOUNT for Drive uploads.", Toast.LENGTH_LONG).show()
            } else {
                val action = signInAction
                scope.launch {
                    try {
                        val token = withContext(Dispatchers.IO) {
                            GoogleAuthUtil.getToken(context, account.account!!, "oauth2:$DRIVE_SCOPE")
                        }
                        action?.invoke(token)
                    } catch (e: Exception) {
                        busy = false
                        Toast.makeText(context, "Google Drive authorization failed: ${e.localizedMessage ?: "Try again"}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            busy = false
            Toast.makeText(context, "Drive sign-in was cancelled or failed.", Toast.LENGTH_LONG).show()
        }
    }

    fun authorizeAndUpload() {
        busy = true
        signInAction = { token ->
            scope.launch {
                try {
                    var beforeUrl = job.technicianBeforePhotoUrl
                    var afterUrl = job.technicianAfterPhotoUrl
                    if (beforeUri != null) beforeUrl = GoogleDriveMediaHelper.uploadUris(context, token, listOf(beforeUri!!), "Order_${job.orderNumberFormatted}", "Technician_Before").first().url
                    if (afterUri != null) afterUrl = GoogleDriveMediaHelper.uploadUris(context, token, listOf(afterUri!!), "Order_${job.orderNumberFormatted}", "Technician_After").first().url
                    val updated = job.copy(technicianBeforePhotoUrl = beforeUrl, technicianAfterPhotoUrl = afterUrl)
                    busy = false
                    showConfirm = false
                    onConfirmComplete(updated, rating.toFloat(), feedback.trim().ifBlank { null })
                } catch (e: Exception) {
                    busy = false
                    Toast.makeText(context, "Upload failed; order was not completed: ${e.localizedMessage ?: "Try again"}", Toast.LENGTH_LONG).show()
                }
            }
        }
        if (beforeUri == null && afterUri == null) {
            busy = false
            showConfirm = false
            onConfirmComplete(job, rating.toFloat(), feedback.trim().ifBlank { null })
            return
        }
        val existing = GoogleSignIn.getLastSignedInAccount(context)
        if (existing != null && existing.email == DRIVE_ACCOUNT && GoogleSignIn.hasPermissions(existing, Scope(DRIVE_SCOPE))) {
            scope.launch {
                try {
                    val token = withContext(Dispatchers.IO) { GoogleAuthUtil.getToken(context, existing.account!!, "oauth2:$DRIVE_SCOPE") }
                    signInAction?.invoke(token)
                } catch (e: Exception) {
                    busy = false
                    Toast.makeText(context, "Drive authorization failed: ${e.localizedMessage ?: "Please try again"}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            authLauncher.launch(oauthClient.signInIntent)
        }
    }

    BackHandler(onBack = onDismiss)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Complete Order #${job.orderNumberFormatted}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Before/After photos and review", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                } },
                navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Button(onClick = { showConfirm = true }, enabled = !busy, modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(if (busy) "Uploading media…" else "Save & Complete Order", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(job.customerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${job.serviceType} • ${job.customerPhone}")
                        Text("Assigned technician: ${job.assignedExpertName ?: "Not specified"}")
                    }
                }
            }
            item { mediaCard("Technician Before Photo", beforeUri, job.technicianBeforePhotoUrl) { beforePicker.launch("image/*") } }
            item { mediaCard("Technician After Photo", afterUri, job.technicianAfterPhotoUrl) { afterPicker.launch("image/*") } }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Technician Rating", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            (1..5).forEach { n -> Icon(Icons.Default.Star, contentDescription = "$n stars", tint = if (n <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(5.dp).clickable { rating = n }) }
                        }
                        OutlinedTextField(value = feedback, onValueChange = { feedback = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Review / Work Notes (optional)") }, minLines = 3)
                    }
                }
            }
        }
    }
    if (showConfirm) AlertDialog(
        onDismissRequest = { if (!busy) showConfirm = false },
        title = { Text("Confirm Order Completion") },
        text = { Text("Save the rating and review, upload selected Before/After photos to $DRIVE_ACCOUNT, and mark Order #${job.orderNumberFormatted} completed?") },
        confirmButton = { TextButton(enabled = !busy, onClick = { authorizeAndUpload() }) { Text("Confirm & Save") } },
        dismissButton = { TextButton(enabled = !busy, onClick = { showConfirm = false }) { Text("Cancel") } }
    )
}

@Composable
private fun mediaCard(title: String, selected: Uri?, savedUrl: String?, onPick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, fontWeight = FontWeight.SemiBold)
            }
            Text(when { selected != null -> "New photo selected"; !savedUrl.isNullOrBlank() -> "Existing photo saved to Drive"; else -> "No photo selected (optional)" }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            OutlinedButton(onClick = onPick, modifier = Modifier.fillMaxWidth()) { Text(if (selected != null || !savedUrl.isNullOrBlank()) "Choose / Replace Photo" else "Choose Photo") }
        }
    }
}
