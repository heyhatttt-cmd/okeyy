package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlayerProfile
import com.example.ui.SlotMachineTheme
import com.example.ui.SlotUiState
import com.example.ui.ZirveViewModel
import com.example.ui.theme.BarlowCondensedDisplay
import com.example.ui.theme.CinzelDisplay
import com.example.ui.theme.ZirveDeepPurple
import com.example.ui.theme.ZirveEmerald
import com.example.ui.theme.ZirveGold
import com.example.ui.theme.ZirveGoldDeep
import com.example.ui.theme.ZirveGoldLight
import com.example.ui.theme.ZirveMagenta
import com.example.ui.theme.ZirveMagentaDark
import com.example.ui.theme.ZirveMagentaLight

@Composable
fun SlotGamesScreen(
    profile: PlayerProfile,
    slotState: SlotUiState,
    themes: List<SlotMachineTheme>,
    onSelectTheme: (Int) -> Unit,
    onSelectBet: (Long) -> Unit,
    onSpin: () -> Unit,
    onClaimFreeChips: () -> Unit,
    onOpenChat: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val activeTheme = themes.getOrElse(slotState.selectedThemeIndex) { themes.first() }
    val winGlow by animateColorAsState(
        targetValue = if (slotState.lastWinAmount > 0L) ZirveEmerald else ZirveGold,
        label = "slot_win_glow"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_lobby_scene),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC120524), Color(0xE61C0938), Color(0xEE120524))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Top Slot Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xCC2C104F),
                        border = BorderStroke(1.5.dp, ZirveGold),
                        modifier = Modifier
                            .clickable { onBack() }
                            .testTag("slot_back_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_to_lobby),
                                tint = ZirveGoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.back_to_lobby),
                                color = ZirveGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.slot_section_title),
                        fontFamily = CinzelDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ZirveGoldLight
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xCC1B0A33),
                        border = BorderStroke(1.5.dp, ZirveGold)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = ZirveGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = ZirveViewModel.formatChips(profile.chips),
                                color = Color.White,
                                fontFamily = BarlowCondensedDisplay,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = ZirveEmerald,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { onClaimFreeChips() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.cd_add_chips),
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xCC281046),
                        border = BorderStroke(1.dp, ZirveMagentaLight),
                        modifier = Modifier.clickable { onOpenChat() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubble,
                                contentDescription = stringResource(R.string.nav_chat),
                                tint = ZirveMagentaLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Sohbet",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main Split: Left Slot Machines Selector | Right Interactive 777 Reel Machine
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Machine List
                LazyColumn(
                    modifier = Modifier
                        .width(210.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    itemsIndexed(themes) { index, theme ->
                        val selected = index == slotState.selectedThemeIndex
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) Color(0xEE3F136E) else Color(0xBB1E0A38),
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) ZirveGoldLight else ZirveGold.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectTheme(index) }
                                .testTag("slot_theme_${theme.id}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = theme.title,
                                        color = if (selected) ZirveGoldLight else Color.White,
                                        fontFamily = BarlowCondensedDisplay,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(theme.accentHex).copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = theme.multiplierText,
                                            color = ZirveGoldLight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = theme.subtitle,
                                    color = Color(0xFFD1C4E9),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Right Active Slot Machine Stage
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xDD1A0832),
                    border = BorderStroke(2.5.dp, winGlow),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Machine Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Casino,
                                    contentDescription = null,
                                    tint = ZirveGoldLight,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = activeTheme.title,
                                    fontFamily = CinzelDisplay,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = ZirveGoldLight
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ZirveMagentaDark,
                                border = BorderStroke(1.dp, ZirveMagentaLight)
                            ) {
                                Text(
                                    text = activeTheme.multiplierText,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // 3 Reels Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            slotState.reels.forEachIndexed { idx, symbol ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color(0xFFFDF5E6),
                                    border = BorderStroke(
                                        3.dp,
                                        Brush.verticalGradient(listOf(ZirveGoldLight, ZirveGold, ZirveGoldDeep))
                                    ),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .size(width = 88.dp, height = 96.dp)
                                        .testTag("slot_reel_$idx")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = symbol,
                                            fontSize = 44.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Result Status Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (slotState.lastWinAmount > 0L) Color(0x6600C853) else Color(0x552E1254),
                            border = BorderStroke(1.dp, winGlow.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = slotState.lastResultMessage,
                                color = if (slotState.lastWinAmount > 0L) Color(0xFFB9F6CA) else ZirveGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Bet Controls + SPIN Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bahis:",
                                    color = Color(0xFFD1C4E9),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                listOf(50L, 100L, 250L, 500L).forEach { bet ->
                                    val isBetSelected = slotState.betAmount == bet
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isBetSelected) ZirveGold else Color(0x663B1659),
                                        border = BorderStroke(1.dp, ZirveGold),
                                        modifier = Modifier
                                            .clickable(enabled = !slotState.spinning) { onSelectBet(bet) }
                                            .testTag("slot_bet_$bet")
                                    ) {
                                        Text(
                                            text = bet.toString(),
                                            color = if (isBetSelected) ZirveDeepPurple else Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = onSpin,
                                enabled = !slotState.spinning,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZirveEmerald,
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(2.dp, ZirveGoldLight),
                                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
                                modifier = Modifier.testTag("slot_spin_button")
                            ) {
                                Text(
                                    text = if (slotState.spinning) "DÖNÜYOR…" else stringResource(R.string.slot_spin),
                                    fontFamily = BarlowCondensedDisplay,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
