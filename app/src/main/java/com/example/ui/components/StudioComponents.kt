package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// 1. Studio Rotary Knob
@Composable
fun RotaryKnob(
    label: String,
    value: Float,
    minValue: Float,
    maxValue: Float,
    unit: String = "",
    accentColor: Color = AmberGlow,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    onValueChange: (Float) -> Unit
) {
    val normalizedValue = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    var dragStartY by remember { mutableStateOf(0f) }

    val angleSweep = 280f
    val startAngle = 130f
    val currentAngle = startAngle + normalizedValue * angleSweep

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> dragStartY = offset.y },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val sensitivity = (maxValue - minValue) / 200f
                            val delta = -dragAmount.y * sensitivity
                            onValueChange((value + delta).coerceIn(minValue, maxValue))
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.toPx()
                val center = Offset(canvasWidth / 2, canvasWidth / 2)
                val radius = canvasWidth / 2 - 8f

                // Track Background Arc
                drawArc(
                    color = StudioBorder,
                    startAngle = startAngle,
                    sweepAngle = angleSweep,
                    useCenter = false,
                    style = Stroke(width = 6f, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )

                // Active Value Arc
                drawArc(
                    color = accentColor,
                    startAngle = startAngle,
                    sweepAngle = normalizedValue * angleSweep,
                    useCenter = false,
                    style = Stroke(width = 8f, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )

                // Outer Knob Ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(StudioCardBg, StudioDarkMetal),
                        center = center,
                        radius = radius - 10f
                    ),
                    radius = radius - 10f,
                    center = center
                )

                // Inner Cap Shadow
                drawCircle(
                    color = StudioBlack,
                    radius = radius - 20f,
                    center = center
                )

                // Knob Marker Line
                val angleRad = Math.toRadians(currentAngle.toDouble())
                val markerStart = Offset(
                    center.x + (radius - 22f) * cos(angleRad).toFloat(),
                    center.y + (radius - 22f) * sin(angleRad).toFloat()
                )
                val markerEnd = Offset(
                    center.x + (radius - 12f) * cos(angleRad).toFloat(),
                    center.y + (radius - 12f) * sin(angleRad).toFloat()
                )

                drawLine(
                    color = accentColor,
                    start = markerStart,
                    end = markerEnd,
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        val formattedValue = when {
            unit == "ms" -> "${value.toInt()} ms"
            unit == "Hz" -> "${value.toInt()} Hz"
            unit == "%" -> "${(value * 100).toInt()}%"
            unit == "dB" -> String.format("%.1f dB", value)
            else -> String.format("%.1f", value)
        }

        Text(
            text = formattedValue,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

// 2. Footswitch Pedal Toggle Button
@Composable
fun FootswitchButton(
    isOn: Boolean,
    label: String,
    accentColor: Color = PedalGreen,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(8.dp)
    ) {
        // Glowing LED Indicator
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (isOn) accentColor else Color(0xFF333333))
                .border(
                    width = 2.dp,
                    color = if (isOn) accentColor.copy(alpha = 0.5f) else StudioBorder,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Metallic Switch Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF888888), Color(0xFF444444), Color(0xFF222222))
                    )
                )
                .border(2.dp, Color(0xFF999999), CircleShape)
                .shadow(4.dp, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            if (isOn) listOf(Color(0xFF555555), Color(0xFF222222))
                            else listOf(Color(0xFF333333), Color(0xFF111111))
                        )
                    )
                    .border(1.dp, Color(0xFF666666), CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOn) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 3. Stereo Studio Peak VU Meter
@Composable
fun VuMeter(
    level: Float, // 0.0 to 1.0
    label: String = "LEVEL",
    isClipping: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(targetValue = level, label = "vu")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        Canvas(
            modifier = Modifier
                .width(12.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(StudioBlack)
                .border(1.dp, StudioBorder, RoundedCornerShape(3.dp))
        ) {
            val totalSegments = 12
            val segmentHeight = size.height / totalSegments
            val activeSegments = (animatedLevel * totalSegments).toInt()

            for (i in 0 until totalSegments) {
                val segY = size.height - (i + 1) * segmentHeight
                val isSegmentActive = i < activeSegments

                val segmentColor = when {
                    i >= 10 -> MeterRed
                    i >= 7 -> MeterYellow
                    else -> MeterGreen
                }

                drawRect(
                    color = if (isSegmentActive) segmentColor else segmentColor.copy(alpha = 0.15f),
                    topLeft = Offset(1f, segY + 1f),
                    size = Size(size.width - 2f, segmentHeight - 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Clip LED
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isClipping) MeterRed else Color(0xFF222222))
        )
    }
}

// 4. Real-Time Oscilloscope Waveform View
@Composable
fun OscilloscopeCanvas(
    waveform: FloatArray,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonCyan
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StudioBlack)
            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        // Grid lines
        drawLine(StudioBorder.copy(alpha = 0.4f), Offset(0f, centerY), Offset(width, centerY), strokeWidth = 1f)
        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(width / 4, 0f), Offset(width / 4, height), strokeWidth = 1f)
        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(width / 2, 0f), Offset(width / 2, height), strokeWidth = 1f)
        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(3 * width / 4, 0f), Offset(3 * width / 4, height), strokeWidth = 1f)

        if (waveform.isEmpty()) return@Canvas

        val path = Path()
        val stepX = width / (waveform.size - 1)

        for (i in waveform.indices) {
            val x = i * stepX
            val y = centerY - (waveform[i] * (height / 2.2f))
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )
    }
}

// 5. Real-Time Spectrum Analyzer Canvas
@Composable
fun SpectrumAnalyzerCanvas(
    spectrumBands: FloatArray,
    modifier: Modifier = Modifier,
    barColor: Color = AmberGlow
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StudioBlack)
            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
    ) {
        val width = size.width
        val height = size.height
        val numBars = spectrumBands.size
        if (numBars == 0) return@Canvas

        val barWidth = (width / numBars) - 2f

        for (i in 0 until numBars) {
            val barHeight = (spectrumBands[i] * height).coerceIn(2f, height)
            val x = i * (barWidth + 2f) + 1f
            val y = height - barHeight

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(MeterRed, AmberGlow, MeterGreen),
                    startY = y,
                    endY = height
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

// 6. Analog Chromatic Tuner Canvas
@Composable
fun NeedleTunerCanvas(
    noteName: String,
    centsOffset: Float, // -50 to +50
    isInTune: Boolean,
    stringNumber: Int,
    targetFreq: Float,
    detectedFreq: Float,
    modifier: Modifier = Modifier
) {
    val animatedCents by animateFloatAsState(targetValue = centsOffset, label = "tuner")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioDarkMetal)
            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PRECISION CHROMATIC TUNER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Analog Dial
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerX = width / 2
                val pivotY = height + 30f

                // Dial Arc Scale
                drawArc(
                    color = StudioBorder,
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(width = 4f),
                    size = Size(width - 40f, height * 2f),
                    topLeft = Offset(20f, 10f)
                )

                // Tick Marks (-50 to +50 cents)
                for (c in -50..50 step 10) {
                    val tickAngle = 270f + (c / 50f) * 60f
                    val rad = Math.toRadians(tickAngle.toDouble())
                    val innerR = height - 10f
                    val outerR = height + (if (c == 0) 10f else 0f)

                    val startX = centerX + innerR * cos(rad).toFloat()
                    val startY = pivotY + innerR * sin(rad).toFloat()
                    val endX = centerX + outerR * cos(rad).toFloat()
                    val endY = pivotY + outerR * sin(rad).toFloat()

                    val markColor = when {
                        c == 0 -> MeterGreen
                        abs(c) <= 10 -> MeterYellow
                        else -> StudioBorder
                    }

                    drawLine(
                        color = markColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (c == 0) 4f else 2f
                    )
                }

                // Tuning Needle
                val needleAngle = 270f + (animatedCents / 50f) * 60f
                val needleRad = Math.toRadians(needleAngle.toDouble())
                val needleLen = height + 5f

                val needleEndX = centerX + needleLen * cos(needleRad).toFloat()
                val needleEndY = pivotY + needleLen * sin(needleRad).toFloat()

                val needleColor = if (isInTune) MeterGreen else DistortionRed

                drawLine(
                    color = needleColor,
                    start = Offset(centerX, pivotY),
                    end = Offset(needleEndX, needleEndY),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )

                drawCircle(
                    color = StudioBlack,
                    radius = 16f,
                    center = Offset(centerX, pivotY)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Note Display Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isInTune) MeterGreen.copy(alpha = 0.2f) else DistortionRed.copy(alpha = 0.15f)
                        )
                        .border(
                            width = 3.dp,
                            color = if (isInTune) MeterGreen else DistortionRed,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = noteName,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isInTune) MeterGreen else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "STRING #$stringNumber",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlow
                    )
                    Text(
                        text = String.format("Detect: %.1f Hz", detectedFreq),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format("Target: %.1f Hz", targetFreq),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%+.1f cents", centsOffset),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInTune) MeterGreen else MeterYellow
                    )
                }
            }
        }
    }
}
