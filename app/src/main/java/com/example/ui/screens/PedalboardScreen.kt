package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.model.EffectUnit
import com.example.ui.components.FootswitchButton
import com.example.ui.components.RotaryKnob
import com.example.ui.components.VuMeter
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PedalboardScreen(viewModel: MainViewModel) {
    val effectsChain by viewModel.effectsChain.collectAsState()
    val selectedId by viewModel.selectedEffectId.collectAsState()
    val inputVu by viewModel.inputVu.collectAsState()
    val outputVu by viewModel.outputVu.collectAsState()
    val isClipping by viewModel.isClipping.collectAsState()

    val selectedEffect = effectsChain.find { it.id == selectedId } ?: effectsChain.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(12.dp)
    ) {
        // Top VU Telemetry Header Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            color = StudioDarkMetal,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "PEDALBOARD SIGNAL CHAIN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlow,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${effectsChain.size} Effects Active in Series",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    VuMeter(level = inputVu, label = "IN", isClipping = false)
                    Spacer(modifier = Modifier.width(16.dp))
                    VuMeter(level = outputVu, label = "OUT", isClipping = isClipping)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Pedal Rack Chain
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StudioPanelBg)
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            itemsIndexed(effectsChain, key = { _, unit -> unit.id }) { index, unit ->
                PedalRackItem(
                    unit = unit,
                    index = index,
                    totalCount = effectsChain.size,
                    isSelected = unit.id == selectedEffect?.id,
                    onSelect = { viewModel.selectEffect(unit.id) },
                    onToggle = { viewModel.toggleEffect(unit.id) },
                    onMoveLeft = { viewModel.moveEffect(index, index - 1) },
                    onMoveRight = { viewModel.moveEffect(index, index + 1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Pedal Detail Control Panel
        selectedEffect?.let { effect ->
            PedalDetailControlCard(
                effect = effect,
                onParamChange = { key, newVal ->
                    viewModel.updateParameter(effect.id, key, newVal)
                },
                onToggle = { viewModel.toggleEffect(effect.id) }
            )
        }
    }
}

@Composable
fun PedalRackItem(
    unit: EffectUnit,
    index: Int,
    totalCount: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggle: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit
) {
    val pedalColor = Color(unit.colorHex)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StudioCardBg else StudioDarkMetal
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) AmberGlow else StudioBorder
        ),
        modifier = Modifier
            .width(135.dp)
            .fillMaxHeight()
            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Pedal Header Stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(pedalColor.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.name.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1
                )
            }

            // Move Left / Right Reordering Controls
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onMoveLeft,
                    enabled = index > 0,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Move Left",
                        tint = if (index > 0) AmberGlow else StudioBorder,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "#${index + 1}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onMoveRight,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Move Right",
                        tint = if (index < totalCount - 1) AmberGlow else StudioBorder,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Footswitch Toggle
            FootswitchButton(
                isOn = unit.enabled,
                label = if (unit.enabled) "ON" else "BYPASS",
                accentColor = pedalColor,
                onToggle = onToggle
            )
        }
    }
}

@Composable
fun PedalDetailControlCard(
    effect: EffectUnit,
    onParamChange: (String, Float) -> Unit,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(effect.colorHex))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = effect.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${effect.type.category})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = effect.enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberGlow,
                        checkedTrackColor = StudioCardBg
                    )
                )
            }

            Divider(color = StudioBorder, modifier = Modifier.padding(vertical = 12.dp))

            // Rotary Knobs Grid
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                effect.parameters.forEach { param ->
                    RotaryKnob(
                        label = param.label,
                        value = param.value,
                        minValue = param.minValue,
                        maxValue = param.maxValue,
                        unit = param.unit,
                        accentColor = Color(effect.colorHex),
                        size = 72.dp,
                        onValueChange = { newVal ->
                            onParamChange(param.key, newVal)
                        }
                    )
                }
            }
        }
    }
}
