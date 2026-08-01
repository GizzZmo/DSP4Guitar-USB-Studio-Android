package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.model.FactoryPresets
import com.example.dsp.model.PresetData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PresetsScreen(viewModel: MainViewModel) {
    val userPresets by viewModel.userPresetsFlow.collectAsState(initial = emptyList())
    val factoryPresets = remember { FactoryPresets.getFactoryPresets() }

    var selectedCategory by remember { mutableStateOf("All") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Rock", "Metal", "Blues", "Clean", "Custom")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(12.dp)
    ) {
        // Save New Custom Preset Button Banner
        Button(
            onClick = { showSaveDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGlow, contentColor = StudioBlack),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Save Preset")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SAVE CURRENT RIG AS CUSTOM PRESET",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberGlow,
                        selectedLabelColor = StudioBlack
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // All Presets Combined List
        val allPresetsList = (factoryPresets + userPresets).filter {
            selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(allPresetsList) { preset ->
                PresetCardItem(
                    preset = preset,
                    onLoad = { viewModel.loadPreset(preset) },
                    onDelete = if (preset.id > 0) { { viewModel.deleteUserPreset(preset.id) } } else null
                )
            }
        }
    }

    if (showSaveDialog) {
        SavePresetDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { title, cat, desc ->
                viewModel.saveUserPreset(title, cat, desc)
                showSaveDialog = false
            }
        )
    }
}

@Composable
fun PresetCardItem(
    preset: PresetData,
    onLoad: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Preset",
                        tint = AmberGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = preset.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioPanelBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Text(
                        text = preset.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = preset.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Included Pedals List Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                preset.effects.take(5).forEach { unit ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(unit.colorHex).copy(alpha = 0.3f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = unit.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }

                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = StudioBlack),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Load")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LOAD PRESET", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SavePresetDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Custom") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkMetal,
        title = {
            Text("Save Custom Preset", color = AmberGlow, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Preset Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGlow,
                        unfocusedBorderColor = StudioBorder
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Rock, Metal, Blues...)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGlow,
                        unfocusedBorderColor = StudioBorder
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGlow,
                        unfocusedBorderColor = StudioBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, category, description) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGlow, contentColor = StudioBlack)
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
