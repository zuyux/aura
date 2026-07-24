package io.aura.android.data.guardian

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.GuardianContact
import java.util.Locale
import javax.inject.Inject

class GuardianSosNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun canSendSms(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

    fun sendSmsFallback(contacts: List<GuardianContact>, message: String): SmsFallbackResult {
        if (contacts.isEmpty()) return SmsFallbackResult.NoContacts
        if (!canSendSms()) return SmsFallbackResult.PermissionMissing

        val smsManager = context.getSystemService(SmsManager::class.java)
        contacts.forEach { contact ->
            smsManager.sendMultipartTextMessage(
                contact.phoneNumber,
                null,
                smsManager.divideMessage(message),
                null,
                null,
            )
        }
        return SmsFallbackResult.Sent(contacts.size)
    }
}

sealed interface SmsFallbackResult {
    data class Sent(val contactCount: Int) : SmsFallbackResult
    data object NoContacts : SmsFallbackResult
    data object PermissionMissing : SmsFallbackResult
}

fun guardianSosMessage(location: AuraLocation?): String {
    val locationText = if (location == null) {
        "Ubicación aún no disponible."
    } else {
        val latitude = String.format(Locale.US, "%.5f", location.latitude)
        val longitude = String.format(Locale.US, "%.5f", location.longitude)
        "GPS: $latitude, $longitude. Mapa: https://maps.google.com/?q=$latitude,$longitude"
    }
    return "AURA SOS: necesito ayuda ahora. $locationText Por favor contactame o verifica si estoy bien."
}
