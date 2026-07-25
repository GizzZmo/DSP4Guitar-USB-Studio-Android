package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeedleTunerCanvas
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PrecisionTunerScreen(viewModel: MainViewModel) {
    val tunerState by viewModel.tunerState.collectAsState()
    var selectedTuning by remember { mutableStateOf("Standard E") }

    val tunings = listOf("Standard E", "Drop D", "Half-Step Down", "D Standard", "Drop C", "Open G")
    val stringTargets = listOf(
        Pair("E4", 329.6f),
        Pair("B3", 246.9f),
        Pair("G3", 196.0f),
        Pair("D3", 146.8f),
        Pair("A2", 110.0f),
        Pair("E2", 82.4f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(12.dp)
    ) {
        // Main Chromatic Needle Tuner Display
        NeedleTunerCanvas(
            noteName = tunerState.noteName,
            centsOffset = tunerState.centsOffset,
            isInTune = tunerState.isInTune,
            stringNumber = tunerState.stringNumber,
            targetFreq = tunerState.targetFreq,
            detectedFreq = tunerState.detectedFreq,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Guitar Headstock String Buttons Bar
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "6-STRING GUITAR TARGETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    stringTargets.forEachIndexed { idx, pair ->
                        val stringNum = 6 - idx
                        val isMatched = tunerState.stringNumber == stringNum

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isMatched) AmberGlow else StudioCardBg)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isMatched) MeterGreen else StudioBorder,
                                    shape = CircleShape
                                )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = pair.first,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMatched) StudioBlack else Color.White
                                )
                                Text(
                                    text = "#$stringNum",
                                    fontSize = 9.sp,
                                    color = if (isMatched) StudioBlack else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Tuning Mode Selector
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "ALTERNATE TUNING MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tunings.take(3).forEach { mode ->
                        FilterChip(
                            selected = selectedTuning == mode,
                            onClick = { selectedTuning = mode },
                            label = { Text(mode, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberGlow,
                                selectedLabelColor = StudioBlack
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
