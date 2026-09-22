package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriverRoute
import com.example.ui.theme.LevoBorderSubtle
import com.example.ui.theme.LevoError
import com.example.ui.theme.LevoPrimary
import com.example.ui.theme.LevoSuccess
import com.example.ui.theme.LevoSuccessBg
import com.example.ui.theme.LevoSurfaceHigh
import com.example.ui.theme.LevoSurfaceHighlight
import com.example.ui.theme.LevoTextPrimary
import com.example.ui.theme.LevoTextSecondary
import com.example.ui.theme.LevoTextTertiary

/**
 * Nubank / Revolut inspired Financial Dashboard for Drivers.
 * Focused on numbers, crisp typography, clean cards, no tacky gradients.
 */
@Composable
fun EarningsScreen(
    route: DriverRoute,
    feePerDeliveryCents: Int,
    onFeeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditingFee by remember { mutableStateOf(false) }
    var feeInput by remember(feePerDeliveryCents) {
        val reais = feePerDeliveryCents / 100
        val centavos = feePerDeliveryCents % 100
        mutableStateOf(if (centavos == 0) "$reais" else "$reais.${centavos.toString().padStart(2, '0')}")
    }

    val completedDeliveries = route.stops.filter { it.isDelivered }
    val failedDeliveries = route.stops.filter { it.isFailed }
    val pendingDeliveries = route.stops.filter { it.isOpen }

    val completedCount = completedDeliveries.size
    val pendingCount = pendingDeliveries.size
    val totalCount = route.stops.size

    val earnedCents = completedCount * feePerDeliveryCents
    val projectedTotalCents = totalCount * feePerDeliveryCents

    val totalCollectedCents = completedDeliveries.sumOf { it.amountCents }

    fun formatMoney(cents: Int): String {
        val r = cents / 100
        val c = cents % 100
        return "R$ $r,${c.toString().padStart(2, '0')}"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nubank-inspired Balance Display
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = LevoSurfaceHigh,
                border = BorderStroke(1.dp, LevoBorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        text = "GANHOS ACUMULADOS NA ROTA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = LevoTextTertiary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatMoney(earnedCents),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = LevoTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Projeção ao finalizar todas: ${formatMoney(projectedTotalCents)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LevoTextSecondary
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = LevoBorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Taxa configurada",
                                style = MaterialTheme.typography.bodySmall,
                                color = LevoTextTertiary
                            )
                            Text(
                                text = "${formatMoney(feePerDeliveryCents)} / entrega",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = LevoTextPrimary
                            )
                        }

                        IconButton(
                            onClick = { isEditingFee = !isEditingFee },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LevoSurfaceHighlight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Ajustar taxa",
                                tint = LevoTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (isEditingFee) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = feeInput,
                                onValueChange = { feeInput = it },
                                placeholder = { Text("Ex: 7.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_fee_val"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LevoPrimary,
                                    unfocusedBorderColor = LevoBorderSubtle
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val parsed = feeInput.replace(",", ".").toDoubleOrNull()
                                    if (parsed != null && parsed > 0) {
                                        onFeeChange((parsed * 100).toInt())
                                    }
                                    isEditingFee = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LevoPrimary)
                            ) {
                                Text("Salvar")
                            }
                        }
                    }
                }
            }
        }

        // Metrics Grid (2 Clean Metrics Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Deliveries Count Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = LevoSurfaceHigh,
                    border = BorderStroke(1.dp, LevoBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ENTREGAS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = LevoTextTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$completedCount / $totalCount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = LevoTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$pendingCount pendentes",
                            style = MaterialTheme.typography.bodySmall,
                            color = LevoTextSecondary
                        )
                    }
                }

                // Money collected at door
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = LevoSurfaceHigh,
                    border = BorderStroke(1.dp, LevoBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "COBRANÇAS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = LevoTextTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatMoney(totalCollectedCents),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = LevoSuccess
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Recebido na porta",
                            style = MaterialTheme.typography.bodySmall,
                            color = LevoTextSecondary
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Histórico de Paradas Realizadas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = LevoTextPrimary
            )
        }

        // Delivered / Failed Items List
        val completedList = route.stops.filter { it.isDelivered || it.isFailed }
        if (completedList.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = LevoSurfaceHigh,
                    border = BorderStroke(1.dp, LevoBorderSubtle)
                ) {
                    Text(
                        text = "Nenhuma entrega concluída ainda nesta rota.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LevoTextTertiary,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(completedList) { stop ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = LevoSurfaceHigh,
                    border = BorderStroke(1.dp, LevoBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (stop.isDelivered) LevoSuccessBg else LevoError.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (stop.isDelivered) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (stop.isDelivered) LevoSuccess else LevoError,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stop.address,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = LevoTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stop.customerName ?: "Cliente",
                                style = MaterialTheme.typography.bodySmall,
                                color = LevoTextSecondary
                            )
                        }

                        Text(
                            text = "+ ${formatMoney(feePerDeliveryCents)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (stop.isDelivered) LevoSuccess else LevoTextTertiary
                        )
                    }
                }
            }
        }
    }
}
