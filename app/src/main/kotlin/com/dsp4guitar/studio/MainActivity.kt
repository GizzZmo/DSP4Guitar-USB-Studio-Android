package com.dsp4guitar.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dsp4guitar.studio.audio.AudioEngineJni
import com.dsp4guitar.studio.ui.theme.DSP4GuitarTheme
import com.dsp4guitar.studio.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {

    private val audioEngine = AudioEngineJni()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioEngine.create()

        setContent {
            DSP4GuitarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(audioEngine = audioEngine)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        audioEngine.start()
    }

    override fun onStop() {
        super.onStop()
        audioEngine.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.destroy()
    }
}
