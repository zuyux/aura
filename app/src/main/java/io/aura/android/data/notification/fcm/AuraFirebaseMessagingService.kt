@file:Suppress("OVERRIDE_DEPRECATION")

package io.aura.android.data.notification.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuraFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var messageHandler: FcmMessageHandler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch {
            messageHandler.handleNewToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        messageHandler.handleMessage(message)
    }
}
