package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.LevoBorderFocus
import com.example.ui.theme.LevoBorderSubtle
import com.example.ui.theme.LevoError
import com.example.ui.theme.LevoErrorBg
import com.example.ui.theme.LevoMapsBlue
import com.example.ui.theme.LevoPrimary
import com.example.ui.theme.LevoSuccess
import com.example.ui.theme.LevoSuccessBg
import com.example.ui.theme.LevoSurfaceHigh
import com.example.ui.theme.LevoSurfaceHighlight
import com.example.ui.theme.LevoTextPrimary
import com.example.ui.theme.LevoTextSecondary
import com.example.ui.theme.LevoTextTertiary
import com.example.ui.theme.LevoWazeCyan
import com.example.util.NavigationHelper

/**
 * Premium Active Delivery Card.
 * Clean, restrained, high typographic hierarchy inspired by Google Maps / Nubank / Uber Driver.
 */
@Composable
fun ActiveStopCard(
    stop: DriverStop,
    totalStops: Int,
    routeExigeCodigo: Boolean = false,
    onStartClick: (DriverStop) -> Unit,
    onDeliveredClick: (DriverStop) -> Unit,
    onFailedClick: (DriverStop) -> Unit,
    onRetryClick: ((DriverStop) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isCodeRequired = stop.requiresConfirmationCode(routeExigeCodigo)

    val statusAccent = when {
        stop.isInProgress -> LevoSuccess
        stop.isDelivered -> LevoSuccess
        stop.isFailed -> LevoError
        else -> LevoPrimary
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_stop_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (stop.isInProgress) LevoSuccess.copy(alpha = 0.6f) else LevoBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Row 1: Status Pill & Financial Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            stop.isInProgress -> "A CAMINHO · PARADA ${stop.position} DE $totalStops"
                            stop.isDelivered -> "ENTREGA CONCLUÍDA"
                            stop.isFailed -> "NÃO ENTREGUE"
                            else -> "PRÓXIMA PARADA · ${stop.position} DE $totalStops"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        ),
                        color = statusAccent
                    )
                }

                // Clean financial tag
                if (stop.amountCents > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B382B),
                        border = BorderStroke(1.dp, LevoSuccess.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(LevoSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RECEBER: ${stop.formattedAmount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.4.sp,
                                    fontSize = 11.5.sp,
                                    color = LevoSuccess
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LevoSurfaceHigh,
                        border = BorderStroke(1.dp, LevoBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✓ PAGO NO APP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Address Headline
            Text(
                text = stop.address,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    lineHeight = 25.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = LevoTextPrimary
            )

            if (!stop.reference.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stop.reference,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LevoTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 3: Customer Module (Nubank style single-cell item)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = LevoSurfaceHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stop.customerName ?: "Cliente",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = LevoTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!stop.customerPhone.isNullOrBlank()) {
                            Text(
                                text = stop.customerPhone,
                                style = MaterialTheme.typography.bodySmall,
                                color = LevoTextTertiary
                            )
                        }
                    }

                    if (!stop.customerPhone.isNullOrBlank()) {
                        IconButton(
                            onClick = { NavigationHelper.dialCustomerPhone(context, stop.customerPhone) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LevoSurfaceHighlight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Ligar",
                                tint = LevoTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Note if present
            if (!stop.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Obs: ${stop.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = LevoTextSecondary
                )
            }

            // Code requirement status badge for this specific delivery
            if (stop.isOpen) {
                Spacer(modifier = Modifier.height(10.dp))
                if (isCodeRequired) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LevoPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, LevoPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = LevoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "CÓDIGO OBRIGATÓRIO (4 DÍGITOS)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LevoPrimary
                                )
                                Text(
                                    text = "Solicite o código do cliente (iFood/Aiqfome/Levô) ou últimos 4 dígitos do celular",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = LevoTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LevoSuccessBg,
                        border = BorderStroke(1.dp, LevoSuccess.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = LevoSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ENTREGA DIRETA (SEM CÓDIGO)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LevoSuccess
                                )
                                Text(
                                    text = "Este pedido não exige código de confirmação na entrega",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = LevoTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = LevoBorderSubtle, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Action Center: Big Tech Ergonomic Layout
            if (stop.isOpen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Google Maps Button
                    Button(
                        onClick = {
                            NavigationHelper.openGoogleMaps(
                                context = context,
                                stop = stop
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_nav_maps"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LevoSurfaceHigh,
                            contentColor = LevoTextPrimary
                        ),
                        border = BorderStroke(1.dp, LevoBorderSubtle)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = LevoMapsBlue,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Maps",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    // Waze Button
                    Button(
                        onClick = {
                            NavigationHelper.openWaze(
                                context = context,
                                stop = stop
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_nav_waze"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LevoSurfaceHigh,
                            contentColor = LevoTextPrimary
                        ),
                        border = BorderStroke(1.dp, LevoBorderSubtle)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = LevoWazeCyan,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Waze",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary CTA: Start Delivery or Finalize
                if (!stop.isInProgress) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onStartClick(stop) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("button_start_stop"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LevoPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Iniciar Deslocamento",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Allows courier to report unforeseen event or cancellation even before leaving
                        OutlinedButton(
                            onClick = { onFailedClick(stop) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("button_report_issue_before_start"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LevoError.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = LevoError
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Relatar Imprevisto / Cancelar Parada",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SlideToConfirmButton(
                            text = if (isCodeRequired) "Deslize para Inserir Código" else "Deslize para Confirmar Entrega",
                            onConfirmed = { onDeliveredClick(stop) },
                            modifier = Modifier.fillMaxWidth(),
                            sliderColor = LevoSuccess,
                            heightDp = 52
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                onClick = { onFailedClick(stop) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("button_fail_stop"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, LevoError.copy(alpha = 0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = LevoError
                                )
                            ) {
                                Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Não Consegui Entregar (Imprevisto)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            } else if (stop.isFailed) {
                // Prominent Failed / Cancelled Justification display
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LevoErrorBg,
                    border = BorderStroke(1.dp, LevoError.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = LevoError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PARADA CANCELADA / IMPREVISTO",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LevoError
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Justificativa transmitida para a Central Levô:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = LevoTextSecondary
                        )
                        Text(
                            text = stop.failureReason ?: "Imprevisto registrado pelo entregador",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = LevoTextPrimary
                            )
                        )

                        if (onRetryClick != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onRetryClick(stop) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, LevoPrimary.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LevoPrimary)
                            ) {
                                Text(
                                    text = "Reabrir Parada / Tentar Novamente",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            } else {
                // Completed status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = LevoSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Entrega finalizada com sucesso",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = LevoSuccess
                    )
                }
            }
        }
    }
}
