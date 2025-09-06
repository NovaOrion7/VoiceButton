package com.novaorion.volumecontrol

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AquariumUnlockDialog(
    adsWatched: Int,
    adsRemaining: Int,
    isAdReady: Boolean,
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Dialog açıldığında reklam yüklemeyi tetikle
    LaunchedEffect(Unit) {
        AdMobHelper.loadRewardedAd(context)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🐠",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = context.getString(R.string.aquarium_unlock_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = context.getString(R.string.aquarium_unlock_description),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Progress göstergesi
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = context.getString(R.string.unlock_progress),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$adsWatched/3",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = { adsWatched / 3f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (adsRemaining > 0) {
                            Text(
                                text = context.getString(R.string.ads_remaining, adsRemaining),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = context.getString(R.string.theme_unlocked),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                if (adsRemaining > 0 && !isAdReady) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = context.getString(R.string.ads_loading),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (adsRemaining > 0) {
                Button(
                    onClick = onWatchAd,
                    enabled = isAdReady,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAdReady) context.getString(R.string.watch_ad) else context.getString(R.string.loading_ad),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = context.getString(R.string.use_theme),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.close))
            }
        }
    )
}
