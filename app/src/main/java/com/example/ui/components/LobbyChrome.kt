package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlayerProfile
import com.example.ui.LobbyOverlay
import com.example.ui.ZirveViewModel
import com.example.ui.theme.BarlowCondensedDisplay
import com.example.ui.theme.CinzelDisplay
import com.example.ui.theme.ZirveAmber
import com.example.ui.theme.ZirveCyan
import com.example.ui.theme.ZirveDeepPurple
import com.example.ui.theme.ZirveEmerald
import com.example.ui.theme.ZirveEmeraldDark
import com.example.ui.theme.ZirveGold
import com.example.ui.theme.ZirveGoldDeep
import com.example.ui.theme.ZirveGoldLight
import com.example.ui.theme.ZirveMagenta
import com.example.ui.theme.ZirveMagentaDark
import com.example.ui.theme.ZirveMagentaLight
import com.example.ui.theme.ZirveRuby
import kotlinx.coroutines.launch

data class LobbyGameCardItem(
    val id: String,
    val title: String,
    val badgeText: String,
    @DrawableRes val imageRes: Int?,
    val gradientColors: List<Color>,
    val isSlot: Boolean = false
)

@Composable
fun ZirveLobbyScreen(
    profile: PlayerProfile,
    onlineCount: Int,
    pendingRequestCount: Int,
    unclaimedGiftCount: Int,
    activeRoomCount: Int,
    musicPlaying: Boolean,
    musicTrackName: String,
    bannerMessage: String?,
    onDismissBanner: () -> Unit,
    onToggleMusic: () -> Unit,
    onNextMusic: () -> Unit,
    onOpenGame: (LobbyGameCardItem) -> Unit,
    onOpenOverlay: (LobbyOverlay) -> Unit,
    onClaimFreeChips: () -> Unit
) {
    val gameCards = listOf(
        LobbyGameCardItem(
            id = "101_okey",
            title = "101 OKEY",
            badgeText = "$activeRoomCount Masa",
            imageRes = R.drawable.card_101_okey,
            gradientColors = listOf(Color(0xFFB71C1C), Color(0xFF4A001E))
        ),
        LobbyGameCardItem(
            id = "batak",
            title = "BATAK",
            badgeText = "İhaleli / Koz",
            imageRes = R.drawable.card_batak,
            gradientColors = listOf(Color(0xFF0D47A1), Color(0xFF0A1B47))
        ),
        LobbyGameCardItem(
            id = "tavla",
            title = "TAVLA",
            badgeText = "1v1 Düello",
            imageRes = R.drawable.card_tavla,
            gradientColors = listOf(Color(0xFF8D4E00), Color(0xFF3E1C00))
        ),
        LobbyGameCardItem(
            id = "slot",
            title = "SLOT OYUNLARI",
            badgeText = "777 Jackpot",
            imageRes = R.drawable.card_slot_oyunlari,
            gradientColors = listOf(Color(0xFF8E24AA), Color(0xFF38006B)),
            isSlot = true
        ),
        LobbyGameCardItem(
            id = "duz_okey",
            title = "DÜZ OKEY",
            badgeText = "Klasik Masa",
            imageRes = null,
            gradientColors = listOf(Color(0xFF00796B), Color(0xFF00332C))
        ),
        LobbyGameCardItem(
            id = "canak_okey",
            title = "ÇANAK OKEY",
            badgeText = "Büyük Ödül",
            imageRes = null,
            gradientColors = listOf(Color(0xFFC62828), Color(0xFF4E0714))
        )
    )

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Exact visual scene from the provided design
        Image(
            painter = painterResource(R.drawable.bg_lobby_scene),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle edge readability scrims without hiding the sunset marina artwork
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x55120524),
                        0.18f to Color.Transparent,
                        0.76f to Color.Transparent,
                        1.0f to Color(0x99120524)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // --- TOP BAR ---
            LobbyTopBar(
                profile = profile,
                onlineCount = onlineCount,
                pendingRequestCount = pendingRequestCount,
                unclaimedGiftCount = unclaimedGiftCount,
                musicPlaying = musicPlaying,
                musicTrackName = musicTrackName,
                onAddChipsClick = onClaimFreeChips,
                onShopClick = { onOpenOverlay(LobbyOverlay.SHOP) },
                onPackageClick = { onOpenOverlay(LobbyOverlay.PACKAGES) },
                onMailClick = { onOpenOverlay(LobbyOverlay.MAIL) },
                onFriendsClick = { onOpenOverlay(LobbyOverlay.FRIENDS_ONLINE) },
                onSettingsClick = { onOpenOverlay(LobbyOverlay.SETTINGS) },
                onToggleMusic = onToggleMusic,
                onNextMusic = onNextMusic
            )

            // --- CENTER: LEFT SIDEBAR + HORIZONTALLY SCROLLABLE GAME CARDS ---
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                val cardHeight = (maxHeight * 0.86f).coerceIn(135.dp, 260.dp)
                val cardWidth = (cardHeight * 0.74f).coerceIn(108.dp, 194.dp)

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Vertical Navigation Dock (Home, Heart, Friends, Diamond, Crown)
                    LeftVerticalDock(
                        onHomeClick = {
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        },
                        onHeartClick = { onOpenOverlay(LobbyOverlay.DAILY_LOGIN) },
                        onFriendsClick = { onOpenOverlay(LobbyOverlay.FRIENDS_ONLINE) },
                        onDiamondClick = {
                            val slotIndex = gameCards.indexOfFirst { it.isSlot }
                            if (slotIndex >= 0) onOpenGame(gameCards[slotIndex])
                        },
                        onCrownClick = { onOpenOverlay(LobbyOverlay.LEADERBOARD) }
                    )

                    Spacer(Modifier.width(6.dp))

                    // Left Scroll Arrow
                    CarouselArrowButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.cd_scroll_left),
                        testTag = "scroll_games_left",
                        onClick = {
                            coroutineScope.launch {
                                val target = (listState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                listState.animateScrollToItem(target)
                            }
                        }
                    )

                    // Horizontally Scrollable Game Cards
                    LazyRow(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("games_horizontal_list"),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(gameCards, key = { it.id }) { card ->
                            LobbyGameCard(
                                card = card,
                                width = cardWidth,
                                height = cardHeight,
                                onClick = { onOpenGame(card) }
                            )
                        }
                    }

                    // Right Scroll Arrow
                    CarouselArrowButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.cd_scroll_right),
                        testTag = "scroll_games_right",
                        onClick = {
                            coroutineScope.launch {
                                val target = (listState.firstVisibleItemIndex + 1)
                                    .coerceAtMost(gameCards.lastIndex)
                                listState.animateScrollToItem(target)
                            }
                        }
                    )
                }
            }

            // --- BOTTOM NAVIGATION BAR (7 ITEMS MATCHING SCREENSHOT) ---
            LobbyBottomDock(
                pendingRequestCount = pendingRequestCount,
                unclaimedGiftCount = unclaimedGiftCount,
                onFreeChips = onClaimFreeChips,
                onMissions = { onOpenOverlay(LobbyOverlay.MISSIONS) },
                onDailyLogin = { onOpenOverlay(LobbyOverlay.DAILY_LOGIN) },
                onFriends = { onOpenOverlay(LobbyOverlay.FRIENDS_ONLINE) },
                onGifts = { onOpenOverlay(LobbyOverlay.GIFTS) },
                onLeaderboard = { onOpenOverlay(LobbyOverlay.LEADERBOARD) },
                onChat = { onOpenOverlay(LobbyOverlay.CHAT) }
            )
        }

        // Toast/Status Banner
        AnimatedVisibility(
            visible = bannerMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            if (bannerMessage != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xE6220B3D),
                    border = BorderStroke(1.5.dp, ZirveGold),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clickable { onDismissBanner() }
                ) {
                    Text(
                        text = bannerMessage,
                        color = ZirveGoldLight,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LobbyTopBar(
    profile: PlayerProfile,
    onlineCount: Int,
    pendingRequestCount: Int,
    unclaimedGiftCount: Int,
    musicPlaying: Boolean,
    musicTrackName: String,
    onAddChipsClick: () -> Unit,
    onShopClick: () -> Unit,
    onPackageClick: () -> Unit,
    onMailClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onToggleMusic: () -> Unit,
    onNextMusic: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar + ZİRVE OKEY + VIP 0
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { onSettingsClick() }
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar_zirve),
                        contentDescription = stringResource(R.string.cd_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(
                                2.5.dp,
                                Brush.linearGradient(listOf(ZirveGoldLight, ZirveGold, ZirveGoldDeep)),
                                CircleShape
                            )
                    )
                    Surface(
                        shape = CircleShape,
                        color = ZirveRuby,
                        border = BorderStroke(1.dp, ZirveGoldLight),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 3.dp)
                            .size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = profile.level.toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.brand_title),
                        fontFamily = CinzelDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ZirveGoldLight,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ZirveMagentaDark,
                            border = BorderStroke(1.dp, ZirveMagentaLight)
                        ) {
                            Text(
                                text = stringResource(R.string.vip_prefix, profile.vipLevel),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xAA102A18),
                            border = BorderStroke(1.dp, ZirveEmerald)
                        ) {
                            Text(
                                text = "● $onlineCount Çevrimiçi",
                                color = Color(0xFF69F0AE),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onFriendsClick() }
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Center-Left: Chips Counter + MAĞAZA + PAKET + Lv.1
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Chip Balance Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xCC180A2E),
                    border = BorderStroke(1.5.dp, ZirveGold),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 5.dp, end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = ZirveGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = ZirveViewModel.formatChips(profile.chips),
                            color = Color.White,
                            fontFamily = BarlowCondensedDisplay,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = ZirveEmerald,
                            border = BorderStroke(1.dp, Color.White),
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { onAddChipsClick() }
                                .testTag("add_chips_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(R.string.cd_add_chips),
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // MAĞAZA Glossy Green Button
                GlossyTopActionButton(
                    label = stringResource(R.string.btn_shop),
                    icon = Icons.Filled.ShoppingCart,
                    gradient = listOf(Color(0xFF43E97B), ZirveEmeraldDark),
                    borderColor = Color(0xFFB9F6CA),
                    testTag = "shop_button",
                    onClick = onShopClick
                )

                // PAKET Glossy Purple Button
                GlossyTopActionButton(
                    label = stringResource(R.string.btn_package),
                    icon = Icons.Filled.Redeem,
                    gradient = listOf(ZirveMagentaLight, ZirveMagentaDark),
                    borderColor = Color(0xFFF3E5F5),
                    testTag = "package_button",
                    onClick = onPackageClick
                )

                // Lv.1 Star Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = ZirveGoldLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xCC241038),
                        border = BorderStroke(1.dp, ZirveGold)
                    ) {
                        Text(
                            text = stringResource(R.string.level_prefix, profile.level),
                            color = ZirveGoldLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Right: Mail, Friends, Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TopIconBadgeButton(
                    icon = Icons.Filled.Email,
                    contentDescription = stringResource(R.string.cd_mail),
                    badgeCount = unclaimedGiftCount,
                    tint = ZirveGoldLight,
                    testTag = "top_mail_button",
                    onClick = onMailClick
                )
                TopIconBadgeButton(
                    icon = Icons.Filled.Groups,
                    contentDescription = stringResource(R.string.cd_friends),
                    badgeCount = pendingRequestCount,
                    tint = ZirveGoldLight,
                    testTag = "top_friends_button",
                    onClick = onFriendsClick
                )
                TopIconBadgeButton(
                    icon = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    badgeCount = 0,
                    tint = ZirveGoldLight,
                    testTag = "top_settings_button",
                    onClick = onSettingsClick
                )
            }
        }

        // Second Row Right: Zirve Okey Müzik Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xCC120624),
                border = BorderStroke(1.dp, ZirveGold.copy(alpha = 0.75f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = ZirveGoldLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = musicTrackName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ZirveGold)
                            .clickable { onToggleMusic() }
                            .testTag("music_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (musicPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.cd_music_toggle),
                            tint = ZirveDeepPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0x55FFFFFF))
                            .clickable { onNextMusic() }
                            .testTag("music_next_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = stringResource(R.string.cd_music_next),
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlossyTopActionButton(
    label: String,
    icon: ImageVector,
    gradient: List<Color>,
    borderColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.5.dp, borderColor),
        color = Color.Transparent,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ZirveGoldLight,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontFamily = BarlowCondensedDisplay,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TopIconBadgeButton(
    icon: ImageVector,
    contentDescription: String,
    badgeCount: Int,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .testTag(testTag)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xCC220E3C),
            border = BorderStroke(1.5.dp, ZirveGold),
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        if (badgeCount > 0) {
            Surface(
                shape = CircleShape,
                color = ZirveRuby,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(15.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badgeCount.coerceAtMost(9).toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LeftVerticalDock(
    onHomeClick: () -> Unit,
    onHeartClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onDiamondClick: () -> Unit,
    onCrownClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xCC1E0C2E),
        border = BorderStroke(1.5.dp, ZirveGold)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LeftSidebarItem(Icons.Filled.Home, "Ana Lobi", ZirveAmber, "side_home", onHomeClick)
            LeftSidebarItem(Icons.Filled.Favorite, "Günlük Ödül", Color(0xFFFF4081), "side_heart", onHeartClick)
            LeftSidebarItem(Icons.Filled.Groups, "Oyuncular", ZirveGold, "side_friends", onFriendsClick)
            LeftSidebarItem(Icons.Filled.Diamond, "Slot VIP", ZirveCyan, "side_diamond", onDiamondClick)
            LeftSidebarItem(Icons.Filled.EmojiEvents, "Sıralama", ZirveGoldLight, "side_crown", onCrownClick)
        }
    }
}

@Composable
private fun LeftSidebarItem(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x553B1659))
            .border(1.dp, ZirveGold.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CarouselArrowButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color(0xB319082E),
        border = BorderStroke(1.5.dp, ZirveGold),
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = ZirveGoldLight,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun LobbyGameCard(
    card: LobbyGameCardItem,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            2.5.dp,
            Brush.linearGradient(listOf(ZirveGoldLight, ZirveGold, ZirveGoldDeep))
        ),
        shadowElevation = 12.dp,
        modifier = Modifier
            .width(width)
            .height(height)
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("game_card_${card.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(card.gradientColors))
        ) {
            if (card.imageRes != null) {
                Image(
                    painter = painterResource(card.imageRes),
                    contentDescription = card.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.06f)
                )
            } else {
                // Extra scrollable Okey cards styled to match the royal gold aesthetic
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = ZirveGoldLight,
                        modifier = Modifier.size(30.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("7", "8", "9").forEach { num ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFDF5E6),
                                border = BorderStroke(1.dp, ZirveGold),
                                modifier = Modifier.size(width = 28.dp, height = 38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = num,
                                        color = if (num == "8") ZirveRuby else Color(0xFF1565C0),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ZirveRuby,
                        border = BorderStroke(1.5.dp, ZirveGoldLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = card.title,
                            color = ZirveGoldLight,
                            fontFamily = BarlowCondensedDisplay,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
            }

            // Live table / badge pill at top-right corner of each card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xCC120524),
                border = BorderStroke(1.dp, ZirveGold),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = card.badgeText,
                    color = ZirveGoldLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun LobbyBottomDock(
    pendingRequestCount: Int,
    unclaimedGiftCount: Int,
    onFreeChips: () -> Unit,
    onMissions: () -> Unit,
    onDailyLogin: () -> Unit,
    onFriends: () -> Unit,
    onGifts: () -> Unit,
    onLeaderboard: () -> Unit,
    onChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color(0xE01A0833),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(ZirveGoldDeep, ZirveGoldLight, ZirveGoldDeep))),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomDockItem(
                label = stringResource(R.string.nav_free_chips),
                icon = Icons.Filled.MonetizationOn,
                iconTint = ZirveGoldLight,
                badge = 0,
                testTag = "bottom_free_chips",
                onClick = onFreeChips
            )
            BottomDockItem(
                label = stringResource(R.string.nav_missions),
                icon = Icons.Filled.AssignmentTurnedIn,
                iconTint = ZirveEmerald,
                badge = 0,
                testTag = "bottom_missions",
                onClick = onMissions
            )
            BottomDockItem(
                label = stringResource(R.string.nav_daily_login),
                icon = Icons.Filled.CardGiftcard,
                iconTint = Color(0xFFFF5252),
                badge = 0,
                testTag = "bottom_daily_login",
                onClick = onDailyLogin
            )
            BottomDockItem(
                label = stringResource(R.string.nav_friends),
                icon = Icons.Filled.Groups,
                iconTint = ZirveCyan,
                badge = pendingRequestCount,
                testTag = "bottom_friends",
                onClick = onFriends
            )
            BottomDockItem(
                label = stringResource(R.string.nav_gifts),
                icon = Icons.Filled.Redeem,
                iconTint = ZirveMagentaLight,
                badge = unclaimedGiftCount,
                testTag = "bottom_gifts",
                onClick = onGifts
            )
            BottomDockItem(
                label = stringResource(R.string.nav_leaderboard),
                icon = Icons.Filled.EmojiEvents,
                iconTint = ZirveGold,
                badge = 0,
                testTag = "bottom_leaderboard",
                onClick = onLeaderboard
            )
            BottomDockItem(
                label = stringResource(R.string.nav_chat),
                icon = Icons.Filled.ChatBubble,
                iconTint = Color(0xFF448AFF),
                badge = 0,
                testTag = "bottom_chat",
                onClick = onChat
            )
        }
    }
}

@Composable
private fun BottomDockItem(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    badge: Int,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x552E1254))
            .border(1.dp, ZirveMagenta.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
                if (badge > 0) {
                    Surface(
                        shape = CircleShape,
                        color = ZirveRuby,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 5.dp, y = (-3).dp)
                            .size(13.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = badge.coerceAtMost(9).toString(),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Text(
                text = label,
                color = ZirveGoldLight,
                fontFamily = BarlowCondensedDisplay,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
