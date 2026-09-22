package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LevoBorderFocus
import com.example.ui.theme.LevoBorderSubtle
import com.example.ui.theme.LevoError
import com.example.ui.theme.LevoPrimary
import com.example.ui.theme.LevoPrimaryContainer
import com.example.ui.theme.LevoSuccess
import com.example.ui.theme.LevoSuccessBg
import com.example.ui.theme.LevoSurfaceHigh
import com.example.ui.theme.LevoSurfaceHighlight
import com.example.ui.theme.LevoTextPrimary
import com.example.ui.theme.LevoTextSecondary
import com.example.ui.theme.LevoTextTertiary

/**
 * Screen designed with Google Material You & Nubank tier finish:
 * - Clean elevation layers
 * - Muted 1dp precision border geometry
 * - Prominent typography hierarchy
 * - Human-centric ergonomics
 */
@Composable
fun LoginRouteScreen(
    isLoading: Boolean,
    errorMessage: String?,
    serverUrl: String,
    isDriverLoggedIn: Boolean,
    savedCourierName: String?,
    savedPhoneOrId: String?,
    onDriverLogin: (phoneOrId: String, pin: String, name: String?) -> Unit,
    onOpenRoute: (token: String, isDemo: Boolean) -> Unit,
    onCheckAssignedRoute: () -> Unit,
    onOpenSettings: () -> Unit,
    onFullLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneInput by remember { mutableStateOf(savedPhoneOrId ?: "") }
    var pinInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf(savedCourierName ?: "") }

    var isManualLinkOpen by remember { mutableStateOf(false) }
    var manualTokenInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        // Top Bar: Clean, Minimalist action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LevoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "levô",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp
                    ),
                    color = LevoTextPrimary
                )
                Text(
                    text = " · driver",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = LevoTextTertiary
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LevoSurfaceHigh)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Preferências",
                    tint = LevoTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Center Content Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (isDriverLoggedIn) {
                // ==========================================
                // 1. STATE: DRIVER LOGGED IN (Awaiting Route)
                // ==========================================
                Text(
                    text = "Olá, ${savedCourierName?.split(" ")?.firstOrNull() ?: "Entregador"}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = LevoTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Conectado ao sistema de despacho",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LevoTextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Nubank-inspired Status Pill Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = LevoSurfaceHigh,
                    border = BorderStroke(1.dp, LevoBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(LevoSuccess)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DISPONÍVEL PARA ROTAS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = LevoSuccess
                                )
                            }

                            Text(
                                text = "ID: ${savedPhoneOrId ?: "Ativo"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LevoTextTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Aguardando novo despacho da loja. Assim que uma rota for atribuída, os pedidos carregarão na sua tela.",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                            color = LevoTextSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onCheckAssignedRoute,
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("button_check_route"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LevoPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sincronizar Pedidos",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onFullLogout,
                        modifier = Modifier.testTag("button_switch_account")
                    ) {
                        Text(
                            text = "Entrar com outro motoboy",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = LevoTextTertiary
                            )
                        )
                    }
                }

            } else {
                // ==========================================
                // 2. STATE: FIRST TIME LOGIN (Single Sign-on)
                // ==========================================
                Text(
                    text = "Acesso do Entregador",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp
                    ),
                    color = LevoTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Faça login uma vez para receber suas rotas da central.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LevoTextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = LevoSurfaceHigh,
                    border = BorderStroke(1.dp, LevoBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "SEU NOME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = LevoTextTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text("Nome ou apelido", color = LevoTextTertiary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_driver_name"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LevoSurfaceHighlight,
                                unfocusedContainerColor = LevoSurfaceHighlight,
                                focusedBorderColor = LevoPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = LevoTextPrimary,
                                unfocusedTextColor = LevoTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "TELEFONE OU IDENTIFICADOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = LevoTextTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            placeholder = { Text("(11) 98765-4321 ou CPF", color = LevoTextTertiary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_driver_phone"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LevoSurfaceHighlight,
                                unfocusedContainerColor = LevoSurfaceHighlight,
                                focusedBorderColor = LevoPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = LevoTextPrimary,
                                unfocusedTextColor = LevoTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "SENHA OU PIN DO SISTEMA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = LevoTextTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it },
                            placeholder = { Text("PIN cadastrado na central", color = LevoTextTertiary) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_driver_pin"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LevoSurfaceHighlight,
                                unfocusedContainerColor = LevoSurfaceHighlight,
                                focusedBorderColor = LevoPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = LevoTextPrimary,
                                unfocusedTextColor = LevoTextPrimary
                            )
                        )

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = LevoError
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = {
                                if (phoneInput.isNotBlank()) {
                                    onDriverLogin(phoneInput.trim(), pinInput.trim(), nameInput.trim())
                                }
                            },
                            enabled = !isLoading && phoneInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("button_login_driver"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LevoPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Entrar no App",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Fast Direct Demo or Contingency Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onDriverLogin("11999998888", "1234", "Carlos Motoboy")
                        }
                    ) {
                        Text(
                            text = "Entrar como Demonstração",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = LevoPrimary
                            )
                        )
                    }

                    TextButton(
                        onClick = { isManualLinkOpen = !isManualLinkOpen }
                    ) {
                        Text(
                            text = if (isManualLinkOpen) "Fechar link manual" else "Inserir link da rota",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LevoTextTertiary
                            )
                        )
                    }
                }

                if (isManualLinkOpen) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = LevoSurfaceHigh,
                        border = BorderStroke(1.dp, LevoBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualTokenInput,
                                onValueChange = { manualTokenInput = it },
                                placeholder = { Text("levo.delivery/m/...", fontSize = 13.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_route_token"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LevoPrimary,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (manualTokenInput.isNotBlank()) {
                                        onOpenRoute(manualTokenInput.trim(), false)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LevoPrimary)
                            ) {
                                Text("Abrir")
                            }
                        }
                    }
                }
            }
        }
    }
}
