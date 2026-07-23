package io.aura.android.feature.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.aura.android.core.ui.theme.AuraGreen
import io.aura.android.core.ui.theme.AuraRed

@Composable
fun OnboardingScreen(
    uiState: ProfileUiState,
    onNameChanged: (String) -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onSendSmsCode: () -> Unit,
    onSmsCodeChanged: (String) -> Unit,
    onSmsCodeDetected: (String) -> Unit,
    onSmsPermissionDenied: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasSmsPermission by remember { mutableStateOf(context.hasSmsPermission()) }
    var showSmsRationale by remember { mutableStateOf(false) }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_SMS] == true
        hasSmsPermission = granted
        if (granted) {
            context.findLatestSmsCode()?.let(onSmsCodeDetected)
        } else {
            onSmsPermissionDenied()
        }
    }

    if (hasSmsPermission) {
        DisposableEffect(Unit) {
            context.findLatestSmsCode()?.let(onSmsCodeDetected)
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    context.findLatestSmsCode()?.let(onSmsCodeDetected)
                }
            }
            context.contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, observer)
            onDispose {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    if (showSmsRationale) {
        AlertDialog(
            onDismissRequest = { showSmsRationale = false },
            title = { Text("Verificar por SMS") },
            text = {
                Text("AURA puede leer el SMS de verificacion para completar el codigo automaticamente.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSmsRationale = false
                        smsPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_SMS,
                                Manifest.permission.RECEIVE_SMS,
                            ),
                        )
                    },
                ) {
                    Text("Permitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsRationale = false }) {
                    Text("Ahora no")
                }
            },
        )
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Configura tu perfil",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Usaremos tu nombre para personalizar AURA y tu telefono para verificar el inicio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre") },
                singleLine = true,
            )
            CountryPhoneInput(
                value = uiState.phoneNumber,
                onValueChange = onPhoneNumberChanged,
            )

            Button(
                onClick = onSendSmsCode,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSendingSms && uiState.smsResendSecondsRemaining == 0,
            ) {
                Icon(imageVector = Icons.Outlined.Sms, contentDescription = null)
                Text(
                    text = when {
                        uiState.isSendingSms -> "Enviando código..."
                        uiState.smsResendSecondsRemaining > 0 ->
                            "Reenviar código en ${uiState.smsResendSecondsRemaining} s"
                        uiState.smsCodeSent -> "Reenviar código"
                        else -> "Enviar código"
                    },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Button(
                onClick = {
                    if (hasSmsPermission) {
                        context.findLatestSmsCode()?.let(onSmsCodeDetected)
                    } else {
                        showSmsRationale = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Outlined.Sms, contentDescription = null)
                Text(
                    text = if (hasSmsPermission) "Autodetectar SMS" else "Permitir lectura de SMS",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            OutlinedTextField(
                value = uiState.smsCode,
                onValueChange = onSmsCodeChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Codigo SMS") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            uiState.successMessage?.let { message ->
                Text(text = message, color = AuraGreen, style = MaterialTheme.typography.bodyMedium)
            }
            uiState.errorMessage?.let { message ->
                Text(text = message, color = AuraRed, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(if (uiState.isSaving) "Guardando..." else "Verificar y entrar")
            }
        }
    }
}

private fun Context.hasSmsPermission(): Boolean {
    val readSms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
    return readSms == PackageManager.PERMISSION_GRANTED
}

private fun Context.findLatestSmsCode(): String? {
    val projection = arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE)
    return runCatching {
        contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            repeat(minOf(cursor.count, 10)) {
                if (!cursor.moveToNext()) return@use null
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val body = cursor.getString(bodyIndex).orEmpty()
                SMS_CODE_REGEX.find(body)?.value?.let { return@use it }
            }
            null
        }
    }.getOrNull()
}

private val SMS_CODE_REGEX = Regex("""\b\d{4,6}\b""")
