package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleAudioEngine(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check Audio Record Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.toggleAudioEngine(true)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            DspStudioTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val config by viewModel.configState.collectAsState()

    val tabs = listOf(
        NavTabItem("Pedals", Icons.Default.LinearScale, "tab_pedals"),
        NavTabItem("Amp & Cab", Icons.Default.Speaker, "tab_amp"),
        NavTabItem("Tuner", Icons.Default.MusicNote, "tab_tuner"),
        NavTabItem("USB I/O", Icons.Default.Usb, "tab_usb"),
        NavTabItem("Presets", Icons.Default.Bookmark, "tab_presets"),
        NavTabItem("Studio", Icons.Default.GraphicEq, "tab_studio")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = StudioDarkMetal,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudioBlack,
                            selectedTextColor = AmberGlow,
                            indicatorColor = AmberGlow,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioBlack)
        ) {
            when (selectedTab) {
                0 -> PedalboardScreen(viewModel)
                1 -> AmpCabinetScreen(viewModel)
                2 -> PrecisionTunerScreen(viewModel)
                3 -> UsbInterfaceScreen(viewModel)
                4 -> PresetsScreen(viewModel)
                5 -> StudioRecorderScreen(viewModel)
            }
        }
    }
}
