package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriverStop
import com.example.ui.theme.LevoBorderSubtle
import com.example.ui.theme.LevoError
import com.example.ui.theme.LevoErrorBg
import com.example.ui.theme.LevoPrimary
import com.example.ui.theme.LevoSuccess
import com.example.ui.theme.LevoSuccessBg
import com.example.ui.theme.LevoSurfaceHigh
import com.example.ui.theme.LevoSurfaceHighlight
import com.example.ui.theme.LevoTextPrimary
import com.example.ui.theme.LevoTextSecondary
import com.example.ui.theme.LevoTextTertiary
import com.example.util.NavigationHelper

/**
 * Premium Delivery Stop Card for "Sequência de Entregas".
 * Designed for high legibility, full address readability, clear badges and quick GPS access.
 */
@Composable
fun StopListItem(
    stop: DriverStop,
    isActive: Boolean,
    preferredNav: String = "MAPS",
    routeExigeCodigo: Boolean = false,
    onClick: () -> Unit,
    onStartClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isCodeRequired = stop.requiresConfirmationCode(routeExigeCodigo)

    val backgroundColor = when {
        stop.isInProgress -> LevoSurfaceHighlight
        isActive -> LevoSurfaceHighlight
        else -> LevoSurfaceHigh
    }

    val borderColor = when {
        stop.isInProgress -> LevoSuccess.copy(alpha = 0.8f)
        isActive -> LevoPrimary
        else -> LevoBorderSubtle
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("stop_item_${stop.id}"),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(if (isActive || stop.isInProgress) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Stop Number, Status Tag & Navigation Shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left badge & Status text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    stop.isDelivered -> LevoSuccessBg
                                    stop.isFailed -> LevoErrorBg
                                    stop.isInProgress -> LevoSuccess
                                    isActive -> LevoPrimary
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            stop.isDelivered -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = LevoSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            stop.isFailed -> {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = LevoError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            stop.isInProgress -> {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = "${stop.position}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isActive) Color.White else LevoTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = when {
                            stop.isInProgress -> "EM ANDAMENTO"
                            stop.isDelivered -> "ENTREGA CONCLUÍDA"
                            stop.isFailed -> "IMPREVISTO"
                            isActive -> "PARADA EM FOCO"
                            else -> "PARADA ${stop.position}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = when {
                            stop.isInProgress -> LevoSuccess
                            stop.isDelivered -> LevoSuccess
                            stop.isFailed -> LevoError
                            isActive -> LevoPrimary
                            else -> LevoTextTertiary
                        }
                    )
                }

                // Right Action / Status Badge
                if (stop.isOpen) {
                    Surface(
                        onClick = { NavigationHelper.openPreferredNavigation(context, stop, preferredNav) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("btn_quick_nav_${stop.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Navegar até parada",
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Navegar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = Color(0xFF93C5FD)
                            )
                        }
                    }
                } else if (stop.isDelivered) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LevoSuccessBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "CONCLUÍDO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = LevoSuccess
                        )
                    }
                } else if (stop.isFailed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LevoErrorBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "NÃO ENTREGUE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = LevoError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Full Address (Spacious, 2 lines max, no premature truncation!)
            Text(
                text = stop.address,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = LevoTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!stop.reference.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stop.reference,
                    style = MaterialTheme.typography.bodySmall,
                    color = LevoTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Customer Name, Payment Pill & Code Requirement Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Customer Name
                Text(
                    text = stop.customerName ?: "Cliente",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp
                    ),
                    color = LevoTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodySmall,
                    color = LevoTextTertiary
                )

                // Financial Badge
                if (stop.amountCents > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF143323),
                        border = BorderStroke(0.8.dp, LevoSuccess.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cobrar ${stop.formattedAmount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = LevoSuccess
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LevoSurfaceHighlight,
                        border = BorderStroke(0.6.dp, LevoBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✓ Pago online",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            )
                        }
                    }
                }

                // Code Requirement Badge
                if (stop.isOpen) {
                    if (isCodeRequired) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LevoPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = LevoPrimary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Código",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = LevoPrimary
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LevoSurfaceHighlight)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = LevoTextTertiary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Sem código",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.5.sp
                                    ),
                                    color = LevoTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Failure justification if stop failed
            if (stop.isFailed) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LevoErrorBg,
                    border = BorderStroke(1.dp, LevoError.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = LevoError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Motivo: ${stop.failureReason ?: "Imprevisto registrado"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = LevoError,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
