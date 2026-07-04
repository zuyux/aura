package io.aura.android.feature.guardian

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.aura.android.core.ui.components.AuraEmptyState
import io.aura.android.core.ui.components.AuraSectionHeader
import io.aura.android.core.ui.theme.AuraGreen
import io.aura.android.core.ui.theme.AuraRed
import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionStatus

@Composable
fun GuardianScreen(
    modifier: Modifier = Modifier,
    viewModel: GuardianViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLocationRationale by remember { mutableStateOf(false) }
    var showContactsRationale by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.shareLocation()
        }
    }
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val contactUri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && contactUri != null) {
            val pickedContact = context.readPickedPhoneContact(contactUri)
            if (pickedContact == null) {
                viewModel.onContactPickUnavailable()
            } else {
                viewModel.onContactPicked(
                    displayName = pickedContact.displayName,
                    phoneNumber = pickedContact.phoneNumber,
                    photoUri = pickedContact.photoUri,
                )
            }
        }
    }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            contactPickerLauncher.launch(phoneContactPickerIntent())
        } else {
            viewModel.onContactsPermissionDenied()
        }
    }

    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = { showLocationRationale = false },
            title = { Text("Compartir ubicacion") },
            text = {
                Text(
                    "AURA guardara tu ubicacion localmente para esta sesion privada de Red Guardian.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationRationale = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    },
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationRationale = false }) {
                    Text("Ahora no")
                }
            },
        )
    }

    if (showContactsRationale) {
        AlertDialog(
            onDismissRequest = { showContactsRationale = false },
            title = { Text("Elegir contacto") },
            text = {
                Text(
                    "AURA usa tus contactos solo para que elijas personas de confianza. El contacto se guarda localmente.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showContactsRationale = false
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactsRationale = false }) {
                    Text("Ahora no")
                }
            },
        )
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AuraSectionHeader(
                title = "Red Guardián",
                subtitle = "Comparte tu estado con contactos de confianza durante una situacion de riesgo.",
            )

            SessionCard(
                session = uiState.activeSession,
                contactCount = uiState.contacts.size,
            )

            Button(
                onClick = {
                    if (uiState.hasActiveSession) {
                        viewModel.triggerSos()
                    } else {
                        viewModel.startSession()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isBusy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuraRed,
                    contentColor = Color.White,
                ),
            ) {
                Icon(imageVector = Icons.Outlined.Shield, contentDescription = null)
                Text(
                    text = if (uiState.hasActiveSession) "Activar SOS" else "Iniciar sesion",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Button(
                onClick = {
                    if (context.hasLocationPermission()) {
                        viewModel.shareLocation()
                    } else {
                        showLocationRationale = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.hasActiveSession && !uiState.isBusy,
            ) {
                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null)
                Text(
                    text = "Compartir ubicación",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = viewModel::markSafe,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.hasActiveSession && !uiState.isBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraGreen,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                    Text(
                        text = "Estoy bien",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                OutlinedButton(
                    onClick = viewModel::endSession,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.hasActiveSession && !uiState.isBusy,
                ) {
                    Icon(imageVector = Icons.Outlined.StopCircle, contentDescription = null)
                    Text(
                        text = "Finalizar",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            GuardianContactActions(
                uiState = uiState,
                onCall = {
                    val contact = uiState.primaryContact
                    when {
                        contact == null -> viewModel.onContactActionUnavailable()
                        !context.openDialer(contact.phoneNumber) -> viewModel.onExternalActionUnavailable()
                    }
                },
                onSms = {
                    val contact = uiState.primaryContact
                    when {
                        contact == null -> viewModel.onContactActionUnavailable()
                        !context.openSmsDraft(
                            phoneNumber = contact.phoneNumber,
                            body = uiState.guardianSmsBody(),
                        ) -> viewModel.onExternalActionUnavailable()
                    }
                },
            )

            uiState.message?.let { message ->
                Text(
                    text = message,
                    color = AuraGreen,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = AuraRed,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            AddContactCard(
                uiState = uiState,
                onPickContact = {
                    if (context.hasContactsPermission()) {
                        contactPickerLauncher.launch(phoneContactPickerIntent())
                    } else {
                        showContactsRationale = true
                    }
                },
                onAddContact = viewModel::addContact,
            )

            ContactsSection(contacts = uiState.contacts)
        }
    }
}

@Composable
private fun GuardianContactActions(
    uiState: GuardianUiState,
    onCall: () -> Unit,
    onSms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onCall,
            modifier = Modifier.weight(1f),
            enabled = uiState.primaryContact != null && !uiState.isBusy,
        ) {
            Icon(imageVector = Icons.Outlined.Call, contentDescription = null)
            Text(
                text = "Llamar",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        OutlinedButton(
            onClick = onSms,
            modifier = Modifier.weight(1f),
            enabled = uiState.primaryContact != null && !uiState.isBusy,
        ) {
            Icon(imageVector = Icons.Outlined.Sms, contentDescription = null)
            Text(
                text = "SMS",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun SessionCard(
    session: SafetySession?,
    contactCount: Int,
    modifier: Modifier = Modifier,
) {
    val isSos = session?.status == SafetySessionStatus.SOS_TRIGGERED
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (isSos) AuraRed else MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
            Text(
                text = when (session?.status) {
                    SafetySessionStatus.ACTIVE -> "Compartiendo estado"
                    SafetySessionStatus.SOS_TRIGGERED -> "SOS activo"
                    else -> "Sesion lista"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (session == null) {
                    "Agrega contactos e inicia una sesion privada cuando necesites apoyo."
                } else {
                    "$contactCount contacto(s) de confianza vinculados a esta sesion local."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AddContactCard(
    uiState: GuardianUiState,
    onPickContact: () -> Unit,
    onAddContact: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraSectionHeader(
                title = "Contacto de confianza",
                subtitle = "Elige una persona de tus contactos del telefono.",
            )
            Button(
                onClick = onPickContact,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isBusy,
            ) {
                Icon(imageVector = Icons.Outlined.ContactPhone, contentDescription = null)
                Text(text = "Elegir contacto", modifier = Modifier.padding(start = 8.dp))
            }
            if (uiState.selectedContactName.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ContactAvatar(
                            photoUri = uiState.selectedPhotoUri,
                            displayName = uiState.selectedContactName,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = uiState.selectedContactName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = uiState.selectedPhoneNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onAddContact,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedPhoneNumber.isNotBlank() && !uiState.isBusy,
            ) {
                Icon(imageVector = Icons.Outlined.ContactPhone, contentDescription = null)
                Text(text = "Guardar contacto", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun ContactsSection(
    contacts: List<GuardianContact>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AuraSectionHeader(title = "Contactos guardados")
        if (contacts.isEmpty()) {
            AuraEmptyState(
                title = "Sin contactos",
                body = "Agrega al menos un contacto para probar Red Guardian.",
                icon = Icons.Outlined.ContactPhone,
            )
        } else {
            contacts.forEach { contact ->
                ContactRow(contact = contact)
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: GuardianContact,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ContactAvatar(
                photoUri = contact.photoUri,
                displayName = contact.displayName,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ContactAvatar(
    photoUri: String?,
    displayName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri.isNullOrBlank()) {
            Text(
                text = displayName.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        } else {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun Context.hasLocationPermission(): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED
}

private fun Context.hasContactsPermission(): Boolean {
    val contacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
    return contacts == PackageManager.PERMISSION_GRANTED
}

private fun phoneContactPickerIntent(): Intent =
    Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

private fun Context.openDialer(phoneNumber: String): Boolean =
    startExternalActivity(
        Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
        },
    )

private fun Context.openSmsDraft(phoneNumber: String, body: String): Boolean =
    startExternalActivity(
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
            putExtra("sms_body", body)
        },
    )

private fun Context.startExternalActivity(intent: Intent): Boolean {
    return runCatching {
        startActivity(intent)
        true
    }.getOrDefault(false)
}

private fun GuardianUiState.guardianSmsBody(): String {
    val session = activeSession
    val location = session?.lastLocation
    val statusText = when (session?.status) {
        SafetySessionStatus.SOS_TRIGGERED -> "SOS activo"
        SafetySessionStatus.ACTIVE -> "Estoy en una sesion de seguridad"
        else -> "Necesito apoyo"
    }
    val locationText = if (location == null) {
        "Ubicacion aun no disponible."
    } else {
        val latitude = String.format(Locale.US, "%.5f", location.latitude)
        val longitude = String.format(Locale.US, "%.5f", location.longitude)
        "Ubicacion aproximada: $latitude, $longitude. Mapa: https://maps.google.com/?q=$latitude,$longitude"
    }
    return "AURA: $statusText. $locationText Por favor contactame o verifica si estoy bien."
}

private fun Context.readPickedPhoneContact(contactUri: android.net.Uri): PickedPhoneContact? {
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
    )
    return contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
        val displayName = cursor.getString(nameIndex).orEmpty()
        val phoneNumber = cursor.getString(numberIndex).orEmpty()
        val photoUri = cursor.getString(photoIndex)
        if (displayName.isBlank() || phoneNumber.isBlank()) {
            null
        } else {
            PickedPhoneContact(displayName = displayName, phoneNumber = phoneNumber, photoUri = photoUri)
        }
    }
}

private data class PickedPhoneContact(
    val displayName: String,
    val phoneNumber: String,
    val photoUri: String?,
)
