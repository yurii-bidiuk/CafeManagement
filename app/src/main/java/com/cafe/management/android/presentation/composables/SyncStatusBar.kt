package com.cafe.management.android.presentation.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SyncStatusBar(
    isOnline: Boolean,
    pendingTransactionsCount: Int,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isOnline || pendingTransactionsCount > 0 || isSyncing,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = when {
                isSyncing -> MaterialTheme.colorScheme.primaryContainer
                !isOnline -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with animation
                if (isSyncing) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    Icon(
                        Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        imageVector = when {
                            !isOnline -> Icons.Default.CloudOff
                            pendingTransactionsCount > 0 -> Icons.Default.CloudQueue
                            else -> Icons.Default.CloudDone
                        },
                        contentDescription = null,
                        tint = when {
                            !isOnline -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            isSyncing -> "Синхронізація..."
                            !isOnline -> "Офлайн режим"
                            pendingTransactionsCount > 0 -> "Очікує синхронізації"
                            else -> "Синхронізовано"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            !isOnline -> MaterialTheme.colorScheme.onErrorContainer
                            isSyncing -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )

                    if (pendingTransactionsCount > 0 && !isSyncing) {
                        Text(
                            text = "Транзакцій: $pendingTransactionsCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                !isOnline -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onTertiaryContainer
                            }
                        )
                    }
                }

                // Sync button when online but have pending
                if (isOnline && pendingTransactionsCount > 0 && !isSyncing) {
                    TextButton(
                        onClick = { /* TODO: Trigger manual sync */ },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text("Синхронізувати")
                    }
                }
            }
        }
    }
}
