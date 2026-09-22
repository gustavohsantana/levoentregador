package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LevoSuccess
import com.example.ui.theme.LevoSuccessBg
import com.example.ui.theme.LevoSurfaceHigh
import com.example.ui.theme.LevoTextPrimary
import kotlin.math.roundToInt

/**
 * Slide-to-Confirm Button.
 * Inspired by iFood Entregador & Rappi Driver.
 * Eliminates accidental clicks from rain, motorcycle vibrations, or pocket touches.
 */
@Composable
fun SlideToConfirmButton(
    text: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LevoSurfaceHigh,
    sliderColor: Color = LevoSuccess,
    sliderIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    confirmedIcon: ImageVector = Icons.Default.Check,
    heightDp: Int = 54,
    enabled: Boolean = true
) {
    val density = LocalDensity.current
    var isConfirmed by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(heightDp.dp / 2))
            .background(backgroundColor)
            .testTag("slide_to_confirm_container")
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val thumbSizeDp = (heightDp - 8).dp
        val thumbSizePx = with(density) { thumbSizeDp.toPx() }
        val maxOffset = (totalWidthPx - thumbSizePx - with(density) { 8.dp.toPx() }).coerceAtLeast(0f)

        val animatedOffsetX by animateFloatAsState(
            targetValue = if (isConfirmed) maxOffset else offsetX,
            label = "slideOffset"
        )

        val progress = if (maxOffset > 0) (animatedOffsetX / maxOffset).coerceIn(0f, 1f) else 0f

        // Fill background that expands as user slides
        val fillBrush = Brush.horizontalGradient(
            colors = listOf(
                sliderColor.copy(alpha = 0.25f),
                sliderColor.copy(alpha = 0.55f)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { (animatedOffsetX + thumbSizePx).toDp() })
                .clip(RoundedCornerShape(heightDp.dp / 2))
                .background(fillBrush)
        )

        // Center Hint Text (Fades as thumb slides across)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isConfirmed) "Confirmado!" else text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    letterSpacing = 0.3.sp
                ),
                color = if (isConfirmed) LevoSuccess else LevoTextPrimary.copy(alpha = (1f - progress * 1.3f).coerceAtLeast(0.15f))
            )
        }

        // Sliding Thumb (Target drag element)
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt() + with(density) { 4.dp.roundToPx() }, with(density) { 4.dp.roundToPx() }) }
                .size(thumbSizeDp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(if (isConfirmed) LevoSuccess else sliderColor)
                .pointerInput(enabled, maxOffset) {
                    if (!enabled || isConfirmed) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX >= maxOffset * 0.72f) {
                                isConfirmed = true
                                offsetX = maxOffset
                                onConfirmed()
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(0f, maxOffset)
                        }
                    )
                }
                .testTag("slide_thumb"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isConfirmed) confirmedIcon else sliderIcon,
                contentDescription = "Deslizar",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
