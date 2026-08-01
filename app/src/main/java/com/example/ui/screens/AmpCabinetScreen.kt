package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.model.EffectType
import com.example.ui.components.RotaryKnob
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AmpCabinetScreen(viewModel: MainViewModel) {
    val effectsChain by viewModel.effectsChain.collectAsState()

    val ampUnit = effectsChain.find { it.type == EffectType.AMP_SIM }
    val cabUnit = effectsChain.find { it.type == EffectType.CAB_IR }

    var ampModelName by remember { mutableStateOf("Plexi 100W Tube Head") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(12.dp)
    ) {
        // Tube Amplifier Chassis Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(2.dp, StudioBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Amp Header & Tubes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = ampModelName.uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TubeOrange,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Class-A Vacuum Tube Amplifier Emulation",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Illuminated Vacuum Tubes Canvas
                    VacuumTubesVisualizer(isPoweredOn = ampUnit?.enabled ?: true)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tone Stack Chicken-Head Knobs
                ampUnit?.let { amp ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        amp.parameters.forEach { param ->
                            RotaryKnob(
                                label = param.label,
                                value = param.value,
                                minValue = param.minValue,
                                maxValue = param.maxValue,
                                unit = param.unit,
                                accentColor = TubeOrange,
                                size = 68.dp,
                                onValueChange = { newVal ->
                                    viewModel.updateParameter(amp.id, param.key, newVal)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cabinet IR & Microphone Positioning Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "SPEAKER CABINET IMPULSE RESPONSE (IR)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGlow
                )

                Spacer(modifier = Modifier.height(8.dp))

                cabUnit?.let { cab ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cabinet Model",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val cabTypeVal = cab.getParamValue("cab_type")
                            val cabName = when {
                                cabTypeVal > 0.75f -> "4x12 Vintage 30 Slant Cab"
                                cabTypeVal > 0.5f -> "4x12 Greenback Metal Cab"
                                cabTypeVal > 0.25f -> "2x12 Open Back Tweed Combo"
                                else -> "1x12 Deluxe Clean Cab"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StudioCardBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = cabName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Mic Placement Knob
                        RotaryKnob(
                            label = "Mic Distance",
                            value = cab.getParamValue("mic_pos"),
                            minValue = 0f,
                            maxValue = 1f,
                            unit = "%",
                            accentColor = NeonCyan,
                            size = 64.dp,
                            onValueChange = { newVal ->
                                viewModel.updateParameter(cab.id, "mic_pos", newVal)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VacuumTubesVisualizer(isPoweredOn: Boolean) {
    val glowAlpha by animateFloatAsState(targetValue = if (isPoweredOn) 1.0f else 0.15f, label = "tube")

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) {
            Canvas(modifier = Modifier.size(24.dp, 40.dp)) {
                val width = size.width
                val height = size.height

                // Tube Glass Outline
                drawRoundRect(
                    color = StudioBorder,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )

                // Filament Glow
                if (isPoweredOn) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                TubeOrange.copy(alpha = glowAlpha),
                                AmberGlow.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(width / 2, height / 2),
                            radius = width
                        ),
                        radius = width / 1.5f,
                        center = Offset(width / 2, height / 2)
                    )
                }

                // Internal Anode Filament Line
                drawLine(
                    color = if (isPoweredOn) TubeOrange else Color.Gray,
                    start = Offset(width / 2, 8f),
                    end = Offset(width / 2, height - 8f),
                    strokeWidth = 3f
                )
            }
        }
    }
}
