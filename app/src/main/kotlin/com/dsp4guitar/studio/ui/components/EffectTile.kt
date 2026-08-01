package com.dsp4guitar.studio.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dsp4guitar.studio.ui.theme.MatrixGreen
import com.dsp4guitar.studio.ui.theme.MatrixGreenDark
import com.dsp4guitar.studio.ui.theme.OnSurfaceVariant

/**
 * A single effect stage tile showing the stage name and a bypass LED.
 * Tapping the tile toggles the bypass state.
 */
@Composable
fun EffectTile(
    name: String,
    isBypassed: Boolean,
    onToggleBypass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor  = MatrixGreen
    val inactiveColor = MatrixGreenDark
    val ledColor     = if (isBypassed) inactiveColor else activeColor

    Surface(
        modifier = modifier
            .aspectRatio(0.8f)
            .border(
                width = 1.dp,
                color = if (isBypassed) OnSurfaceVariant else MatrixGreen,
                shape = MaterialTheme.shapes.small
            )
            .clickable { onToggleBypass() },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // LED indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .border(1.dp, ledColor, MaterialTheme.shapes.extraSmall)
                    .alpha(if (isBypassed) 0.4f else 1.0f)
            )

            Text(
                text      = name.uppercase(),
                style     = MaterialTheme.typography.labelSmall,
                color     = if (isBypassed) OnSurfaceVariant else MatrixGreen,
                textAlign = TextAlign.Center,
                maxLines  = 2
            )

            Text(
                text  = if (isBypassed) "OFF" else "ON",
                style = MaterialTheme.typography.labelSmall,
                color = ledColor
            )
        }
    }
}
