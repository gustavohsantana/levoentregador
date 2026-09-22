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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
fun OutcomeDialog(
    stop: DriverStop,
    outcomeType: String, // "DELIVERED" or "FAILED"
    exigeCodigo: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (code: String?, reason: String?) -> Unit
) {
    val context = LocalContext.current
    val isCodeRequired = stop.requiresConfirmationCode(exigeCodigo)

    var deliveryCode by remember { mutableStateOf("") }
    var allowWithoutCodeException by remember { mutableStateOf(false) }
    var exceptionReason by remember { mutableStateOf("") }

    var selectedReason by remember { mutableStateOf("Cliente ausente / Não atende") }
    var customReason by remember { mutableStateOf("") }

    val failureReasons = listOf(
        "Cliente ausente / Não atende interfone ou ligações",
        "Endereço não localizado / Área de risco",
        "Problema mecânico / Pneu furado / Acidente",
        "Cancelado ou recusado pelo cliente no local",
        "Mercadoria avariada / Embalagem violada",
        "Estabelecimento solicitou retorno do pedido",
        "Outro imprevisto (descrever abaixo)"
    )

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("outcome_dialog"),
        shape = RoundedCornerShape(20.dp),
        containerColor = LevoSurfaceHigh,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (outcomeType == "DELIVERED") Icons.Default.Check else Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = if (outcomeType == "DELIVERED") LevoSuccess else LevoError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (outcomeType == "DELIVERED") "Confirmar Entrega" else "Relatar Imprevisto / Cancelar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (outcomeType == "DELIVERED") LevoSuccess else LevoError
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Address & Customer summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LevoSurfaceHighlight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Parada ${stop.position}: ${stop.customerName ?: "Cliente"}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = LevoTextPrimary
                            )
                            Text(
                                text = stop.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = LevoTextSecondary,
                                maxLines = 1
                            )
                        }

                        if (!stop.customerPhone.isNullOrBlank()) {
                            IconButton(
                                onClick = { NavigationHelper.dialCustomerPhone(context, stop.customerPhone) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LevoSurfaceHigh)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Ligar para cliente",
                                    tint = LevoTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (stop.amountCents > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LevoSuccessBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Atenção: Cobrar ${stop.formattedAmount} do cliente",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = LevoSuccess
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (outcomeType == "DELIVERED") {
                    // Smart handling of confirmation code vs no code
                    if (isCodeRequired) {
                        // Badge indicating code is required for this specific order
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LevoPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, LevoPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = LevoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Código de Confirmação Obrigatório",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = LevoPrimary
                                    )
                                    Text(
                                        text = "Solicite o código de 4 dígitos informado no app do cliente (iFood/Aiqfome/Levô) ou os últimos 4 dígitos do celular.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = LevoTextSecondary
                                    )
                                }
                            }
                        }

                        if (!allowWithoutCodeException) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Digite os dígitos do código:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = LevoTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = deliveryCode,
                                onValueChange = { if (it.length <= 6) deliveryCode = it },
                                placeholder = { Text("Ex: 1234", color = LevoTextTertiary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_delivery_code"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LevoSuccess,
                                    unfocusedBorderColor = LevoBorderSubtle,
                                    focusedTextColor = LevoTextPrimary,
                                    unfocusedTextColor = LevoTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Exception affordance for real-world scenarios (phone out of battery, customer without app)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { allowWithoutCodeException = true }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = LevoTextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cliente não tem o código? (Liberar com justificativa)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = LevoPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        } else {
                            // Exception justification mode
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = LevoErrorBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Liberação de Entrega sem Código (Exceção)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = LevoError
                                    )
                                    Text(
                                        text = "Informe o motivo para registro de auditoria na Levô:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = LevoTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = exceptionReason,
                                        onValueChange = { exceptionReason = it },
                                        placeholder = { Text("Ex: Celular do cliente descarregou, conferido documento", color = LevoTextTertiary, fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LevoError,
                                            unfocusedBorderColor = LevoBorderSubtle
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Voltar e digitar código",
                                        style = MaterialTheme.typography.bodySmall.copy(color = LevoPrimary),
                                        modifier = Modifier.clickable { allowWithoutCodeException = false }
                                    )
                                }
                            }
                        }
                    } else {
                        // Order DOES NOT require confirmation code
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LevoSuccessBg,
                            border = BorderStroke(1.dp, LevoSuccess.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = LevoSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Entrega Direta (Sem Código)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = LevoSuccess
                                    )
                                    Text(
                                        text = "Este pedido não exige código de confirmação. Confirme a entrega para avançar o trajeto.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = LevoTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Código de confirmação (opcional):",
                            style = MaterialTheme.typography.bodySmall,
                            color = LevoTextTertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = deliveryCode,
                            onValueChange = { if (it.length <= 6) deliveryCode = it },
                            placeholder = { Text("Opcional (se fornecido)", color = LevoTextTertiary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LevoSuccess,
                                unfocusedBorderColor = LevoBorderSubtle
                            )
                        )
                    }
                } else {
                    // Outcome is FAILED or CANCELLED (Unforeseen event / Justification)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LevoErrorBg,
                        border = BorderStroke(1.dp, LevoError.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = LevoError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "A justificativa será registrada no sistema Levô e transmitida à central e ao restaurante.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = LevoTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Selecione o motivo do imprevisto / cancelamento:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = LevoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    failureReasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = LevoError,
                                    unselectedColor = LevoTextTertiary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = LevoTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Observação / Justificativa detalhada (opcional):",
                        style = MaterialTheme.typography.bodySmall,
                        color = LevoTextTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("Ex: Aguardei 15 minutos, interfone sem resposta...", color = LevoTextTertiary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LevoError,
                            unfocusedBorderColor = LevoBorderSubtle
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (outcomeType == "DELIVERED") {
                        val finalCode = deliveryCode.trim().ifBlank { null }
                        val finalReason = if (allowWithoutCodeException && exceptionReason.isNotBlank()) {
                            "Entrega liberada sem código: ${exceptionReason.trim()}"
                        } else null

                        onConfirm(finalCode, finalReason)
                    } else {
                        val baseReason = selectedReason
                        val finalReason = if (customReason.isNotBlank()) {
                            "$baseReason - ${customReason.trim()}"
                        } else {
                            baseReason
                        }
                        onConfirm(null, finalReason)
                    }
                },
                enabled = if (outcomeType == "DELIVERED" && isCodeRequired && !allowWithoutCodeException) {
                    deliveryCode.trim().length >= 4
                } else true,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (outcomeType == "DELIVERED") LevoSuccess else LevoError,
                    contentColor = if (outcomeType == "DELIVERED") Color.Black else Color.White
                ),
                modifier = Modifier.testTag("confirm_outcome_button")
            ) {
                Text(
                    text = if (outcomeType == "DELIVERED") "Confirmar Entrega" else "Enviar Justificativa para Levô",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LevoBorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LevoTextSecondary)
            ) {
                Text("Voltar")
            }
        }
    )
}
