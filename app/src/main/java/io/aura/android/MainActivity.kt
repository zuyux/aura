package io.aura.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.aura.android.core.ui.theme.AuraTheme
import io.aura.android.feature.profile.ProfileViewModel
import io.aura.android.navigation.AuraAppNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
            AuraTheme(themeMode = profileUiState.themeMode) {
                AuraAppNavHost(profileViewModel = profileViewModel)
            }
        }
    }
}
