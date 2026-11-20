package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChatMessage
import com.example.data.FriendRequest
import com.example.data.GiftItem
import com.example.data.PlayerProfile
import com.example.data.UiState
import com.example.ui.LobbyOverlay
import com.example.ui.MissionProgress
import com.example.ui.ZirveViewModel
import com.example.ui.theme.BarlowCondensedDisplay
import com.example.ui.theme.CinzelDisplay
import com.example.ui.theme.ZirveCyan
import com.example.ui.theme.ZirveDeepPurple
import com.example.ui.theme.ZirveEmerald
import com.example.ui.theme.ZirveGold
import com.example.ui.theme.ZirveGoldLight
import com.example.ui.theme.ZirveMagentaDark
import com.example.ui.theme.ZirveMagentaLight
import com.example.ui.theme.ZirveRuby

@Composable
fun OverlayHostDialog(
    overlay: LobbyOverlay,
    profile: PlayerProfile,
    onlinePlayersState: UiState<List<PlayerProfile>>,
    friendLinks: List<FriendRequest>,
    chatMessagesState: UiState<List<ChatMessage>>,
    chatChannel: String,
    giftsState: UiState<List<GiftItem>>,
    missions: List<MissionProgress>,
    dailyClaimedDay: Int,
    musicPlaying: Boolean,
    onDismiss: () -> Unit,
    onSelectChatChannel: (String) -> Unit,
    onSendChat: (String) -> Unit,
    onSendFriendRequest: (PlayerProfile) -> Unit,
    onRespondFriendRequest: (String, Boolean) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onSendGiftToFriend: (PlayerProfile) -> Unit,
    onClaimGift: (GiftItem) -> Unit,
    onClaimFreeChips: (Long) -> Unit,
    onClaimDailyReward: (Int, Long) -> Unit,
    onClaimMissionReward: (String) -> Unit,
    onUpgradeVip: (Long, Long) -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onToggleMusic: () -> Unit,
    onSignOut: () -> Unit
) {
    if (overlay == LobbyOverlay.NONE) return
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3090214))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xF51A0933),
            border = BorderStroke(2.dp, ZirveGold),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .fillMaxHeight(0.86f)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                val title = when (overlay) {
                    LobbyOverlay.FRIENDS_ONLINE -> stringResource(R.string.friends_title)
                    LobbyOverlay.CHAT -> stringResource(R.string.chat_title)
                    LobbyOverlay.SHOP -> "MAĞAZA"
                    LobbyOverlay.PACKAGES -> "ÖZEL PAKETLER"
                    LobbyOverlay.MISSIONS -> "GÖREVLER"
                    LobbyOverlay.DAILY_LOGIN -> "GÜNLÜK GİRİŞ ÖDÜLLERİ"
                    LobbyOverlay.GIFTS -> "HEDİYELER"
                    LobbyOverlay.LEADERBOARD -> "SIRALAMA"
                    LobbyOverlay.MAIL -> "GELEN KUTUSU"
                    LobbyOverlay.SETTINGS -> "AYARLAR VE PROFİL"
                    LobbyOverlay.NONE -> ""
                }

                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontFamily = CinzelDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = ZirveGoldLight
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("close_overlay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Kapat",
                            tint = ZirveGoldLight
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (overlay) {
                        LobbyOverlay.FRIENDS_ONLINE -> FriendsAndOnlineContent(
                            myUid = profile.userId,
                            playersState = onlinePlayersState,
                            friendLinks = friendLinks,
                            onSendRequest = onSendFriendRequest,
                            onRespondRequest = onRespondFriendRequest,
                            onRemoveFriend = onRemoveFriend,
                            onSendGift = onSendGiftToFriend
                        )
                        LobbyOverlay.CHAT -> ChatOverlayContent(
                            myUid = profile.userId,
                            chatChannel = chatChannel,
                            messagesState = chatMessagesState,
                            onSelectChannel = onSelectChatChannel,
                            onSendMessage = onSendChat
                        )
                        LobbyOverlay.LEADERBOARD -> LeaderboardContent(onlinePlayersState)
                        LobbyOverlay.MISSIONS -> MissionsContent(missions, onClaimMissionReward)
                        LobbyOverlay.DAILY_LOGIN -> DailyLoginContent(dailyClaimedDay, onClaimDailyReward)
                        LobbyOverlay.SHOP, LobbyOverlay.PACKAGES -> ShopAndPackagesContent(
                            profile = profile,
                            isPackages = overlay == LobbyOverlay.PACKAGES,
                            onClaimFreeChips = onClaimFreeChips,
                            onUpgradeVip = onUpgradeVip
                        )
                        LobbyOverlay.GIFTS, LobbyOverlay.MAIL -> GiftsAndMailContent(giftsState, onClaimGift)
                        LobbyOverlay.SETTINGS -> SettingsContent(
                            profile = profile,
                            musicPlaying = musicPlaying,
                            onUpdateDisplayName = onUpdateDisplayName,
                            onToggleMusic = onToggleMusic,
                            onSignOut = onSignOut
                        )
                        LobbyOverlay.NONE -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsAndOnlineContent(
    myUid: String,
    playersState: UiState<List<PlayerProfile>>,
    friendLinks: List<FriendRequest>,
    onSendRequest: (PlayerProfile) -> Unit,
    onRespondRequest: (String, Boolean) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onSendGift: (PlayerProfile) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val incomingPending = friendLinks.filter { it.toUid == myUid && it.status == "pending" }
    val acceptedLinks = friendLinks.filter { it.status == "accepted" }
    val acceptedFriendUids = acceptedLinks.map { if (it.fromUid == myUid) it.toUid else it.fromUid }.toSet()
    val pendingUids = friendLinks.filter { it.status == "pending" }
        .map { if (it.fromUid == myUid) it.toUid else it.fromUid }.toSet()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "Çevrimiçi Oyuncular",
                "Arkadaşlarım (${acceptedLinks.size})",
                "İstekler (${incomingPending.size})"
            ).forEachIndexed { idx, label ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedTab == idx) ZirveGold else Color(0x663B1659),
                    border = BorderStroke(1.dp, ZirveGold),
                    modifier = Modifier
                        .clickable { selectedTab = idx }
                        .testTag("friends_tab_$idx")
                ) {
                    Text(
                        text = label,
                        color = if (selectedTab == idx) ZirveDeepPurple else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = { Text("Oyuncu ara…", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZirveGold,
                    unfocusedBorderColor = ZirveGold.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.width(200.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        when (playersState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ZirveGold)
            }
            is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(playersState.message, color = Color(0xFFFF8A80))
            }
            is UiState.Success -> {
                val others = playersState.data.filter {
                    it.userId != myUid &&
                        (searchQuery.isBlank() || it.displayName.contains(searchQuery, ignoreCase = true))
                }
                when (selectedTab) {
                    0 -> {
                        if (others.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Şu an başka çevrimiçi oyuncu görünmüyor. Arkadaşların giriş yaptığında burada görünecek.",
                                    color = Color(0xFFD1C4E9),
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(others, key = { it.userId }) { player ->
                                    val isFriend = player.userId in acceptedFriendUids
                                    val isPending = player.userId in pendingUids
                                    PlayerRowCard(
                                        player = player,
                                        trailingContent = {
                                            when {
                                                isFriend -> {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = ZirveMagentaDark,
                                                            modifier = Modifier.clickable { onSendGift(player) }
                                                        ) {
                                                            Text(
                                                                "Hediye (+250)",
                                                                color = ZirveGoldLight,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                isPending -> {
                                                    Text("İstek Bekliyor", color = ZirveGoldLight, fontSize = 11.sp)
                                                }
                                                else -> {
                                                    Button(
                                                        onClick = { onSendRequest(player) },
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = ZirveEmerald)
                                                    ) {
                                                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Arkadaş Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (acceptedLinks.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Henüz ekli arkadaşın yok. Çevrimiçi Oyuncular sekmesinden arkadaş ekle!", color = Color(0xFFD1C4E9))
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(acceptedLinks, key = { it.id }) { link ->
                                    val friendUid = if (link.fromUid == myUid) link.toUid else link.fromUid
                                    val friendName = if (link.fromUid == myUid) link.toName else link.fromName
                                    val friendProfile = playersState.data.firstOrNull { it.userId == friendUid }
                                        ?: PlayerProfile(userId = friendUid, displayName = friendName)
                                    PlayerRowCard(
                                        player = friendProfile,
                                        trailingContent = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Button(
                                                    onClick = { onSendGift(friendProfile) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = ZirveMagentaDark)
                                                ) {
                                                    Text("Çip Hediye Et", fontSize = 11.sp, color = ZirveGoldLight)
                                                }
                                                Button(
                                                    onClick = { onRemoveFriend(link.id) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = ZirveRuby)
                                                ) {
                                                    Text("Çıkar", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        if (incomingPending.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Bekleyen arkadaşlık isteği yok.", color = Color(0xFFD1C4E9))
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(incomingPending, key = { it.id }) { req ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xAA291047),
                                        border = BorderStroke(1.dp, ZirveGold.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${req.fromName} seni arkadaş olarak eklemek istiyor",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Button(
                                                    onClick = { onRespondRequest(req.id, true) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ZirveEmerald)
                                                ) {
                                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("Kabul Et", fontSize = 11.sp)
                                                }
                                                Button(
                                                    onClick = { onRespondRequest(req.id, false) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ZirveRuby)
                                                ) {
                                                    Text("Reddet", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerRowCard(
    player: PlayerProfile,
    trailingContent: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xAA281046),
        border = BorderStroke(1.dp, ZirveGold.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when (player.onlineStatus) {
                                "online" -> ZirveEmerald
                                "playing" -> ZirveGold
                                else -> Color.Gray
                            }
                        )
                )
                Column {
                    Text(
                        text = "${player.displayName}  •  Lv.${player.level}  VIP ${player.vipLevel}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${player.currentGame} • ${ZirveViewModel.formatChips(player.chips)} Çip",
                        color = ZirveGoldLight,
                        fontSize = 11.sp
                    )
                }
            }
            trailingContent()
        }
    }
}

@Composable
private fun ChatOverlayContent(
    myUid: String,
    chatChannel: String,
    messagesState: UiState<List<ChatMessage>>,
    onSelectChannel: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Channel selector + Quick Phrases
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "lobby" to "Genel Lobi",
                "101_okey" to "101 Okey",
                "slot" to "Slot Sohbet"
            ).forEach { (id, label) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (chatChannel == id) ZirveGold else Color(0x663B1659),
                    border = BorderStroke(1.dp, ZirveGold),
                    modifier = Modifier.clickable { onSelectChannel(id) }
                ) {
                    Text(
                        text = label,
                        color = if (chatChannel == id) ZirveDeepPurple else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("Selamlar!", "Masa kurdum gelin!", "777 Slot harika!", "Tebrikler!")) { quick ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x552E1254),
                        border = BorderStroke(1.dp, ZirveCyan.copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { onSendMessage(quick) }
                    ) {
                        Text(
                            text = quick,
                            color = ZirveCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x88120524))
                .padding(8.dp)
        ) {
            when (messagesState) {
                is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ZirveGold)
                }
                is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(messagesState.message, color = Color(0xFFFF8A80))
                }
                is UiState.Success -> {
                    val msgs = messagesState.data
                    if (msgs.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Henüz mesaj yok. İlk mesajı sen yaz!", color = Color(0xFFD1C4E9))
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(msgs, key = { it.id }) { msg ->
                                val isMine = msg.senderUid == myUid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isMine) Color(0xCC4A148C) else Color(0xCC24103B),
                                        border = BorderStroke(1.dp, if (isMine) ZirveGold else ZirveMagentaLight.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
                                            Text(
                                                text = "${msg.senderName} • Lv.${msg.senderLevel} VIP ${msg.senderVip}",
                                                color = ZirveGoldLight,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = msg.text,
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { if (it.length <= 280) inputText = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.chat_placeholder), fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZirveGold,
                    unfocusedBorderColor = ZirveGold.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )
            Button(
                onClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty()) {
                        onSendMessage(trimmed)
                        inputText = ""
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZirveGold, contentColor = ZirveDeepPurple),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Gönder", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun LeaderboardContent(playersState: UiState<List<PlayerProfile>>) {
    when (playersState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ZirveGold)
        }
        is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(playersState.message, color = Color(0xFFFF8A80))
        }
        is UiState.Success -> {
            val ranked = playersState.data.sortedByDescending { it.chips }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ranked.mapIndexed { idx, p -> idx to p }, key = { it.second.userId }) { (idx, player) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (idx == 0) Color(0xCC4A2C00) else Color(0xAA261042),
                        border = BorderStroke(1.dp, if (idx < 3) ZirveGold else Color(0x44FFFFFF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    color = ZirveGoldLight,
                                    fontFamily = BarlowCondensedDisplay,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (idx == 0) ZirveGoldLight else ZirveGold.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${player.displayName} (Lv.${player.level})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "${ZirveViewModel.formatChips(player.chips)} Çip",
                                color = ZirveGoldLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionsContent(
    missions: List<MissionProgress>,
    onClaimMission: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(missions, key = { it.id }) { m ->
            val ready = m.progress >= m.target && !m.claimed
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xAA291047),
                border = BorderStroke(1.dp, if (ready) ZirveEmerald else ZirveGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(m.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "İlerleme: ${m.progress}/${m.target} • Ödül: +${m.rewardChips} Çip",
                            color = ZirveGoldLight,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { onClaimMission(m.id) },
                        enabled = ready,
                        colors = ButtonDefaults.buttonColors(containerColor = ZirveEmerald)
                    ) {
                        Text(
                            text = when {
                                m.claimed -> "Alındı"
                                ready -> "Ödülü Al"
                                else -> "Devam Ediyor"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyLoginContent(
    claimedDay: Int,
    onClaimDay: (Int, Long) -> Unit
) {
    val rewards = listOf(500L, 750L, 1000L, 1500L, 2000L, 3000L, 5000L)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        items(rewards.mapIndexed { i, amt -> (i + 1) to amt }, key = { it.first }) { (day, amount) ->
            val claimed = day <= claimedDay
            val claimable = day == claimedDay + 1
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (claimable) Color(0xEE3F136E) else Color(0xAA220E3C),
                border = BorderStroke(2.dp, if (claimable) ZirveGoldLight else ZirveGold.copy(alpha = 0.4f)),
                modifier = Modifier
                    .width(120.dp)
                    .height(160.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$day. GÜN", color = ZirveGoldLight, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Icon(
                        imageVector = Icons.Filled.CardGiftcard,
                        contentDescription = null,
                        tint = if (claimed) Color.Gray else ZirveGold,
                        modifier = Modifier.size(38.dp)
                    )
                    Text("+${ZirveViewModel.formatChips(amount)}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    Button(
                        onClick = { onClaimDay(day, amount) },
                        enabled = claimable,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZirveEmerald)
                    ) {
                        Text(if (claimed) "Alındı" else if (claimable) "Al" else "Kilitli", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopAndPackagesContent(
    profile: PlayerProfile,
    isPackages: Boolean,
    onClaimFreeChips: (Long) -> Unit,
    onUpgradeVip: (Long, Long) -> Unit
) {
    val items = if (isPackages) {
        listOf(
            Triple("VIP 1 Başlangıç Paketi", "VIP 1 rozeti + özel çerçeve", 1000L to 1L),
            Triple("VIP 2 Altın Paket", "VIP 2 rozeti + yüksek masa erişimi", 3000L to 2L),
            Triple("VIP 3 Zirve Kralı", "VIP 3 rozeti + ekstra slot bonusu", 7500L to 3L)
        )
    } else {
        listOf(
            Triple("Ücretsiz Başlangıç Çipi", "+500 Çip hemen ekle", 500L to 0L),
            Triple("Saatlik Marina Bonusu", "+1.000 Çip hediye", 1000L to 0L),
            Triple("Zirve Özel Destek", "+2.500 Çip bonus", 2500L to 0L)
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { (title, desc, pair) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xAA291047),
                border = BorderStroke(1.dp, ZirveGold.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(title, color = ZirveGoldLight, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(desc, color = Color(0xFFD1C4E9), fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (isPackages) onUpgradeVip(pair.first, pair.second) else onClaimFreeChips(pair.first)
                        },
                        enabled = !isPackages || profile.vipLevel < pair.second,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPackages) ZirveMagentaDark else ZirveEmerald)
                    ) {
                        Text(
                            text = if (isPackages) {
                                if (profile.vipLevel >= pair.second) "Aktif" else "${ZirveViewModel.formatChips(pair.first)} Çip"
                            } else {
                                "Ücretsiz Al"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftsAndMailContent(
    giftsState: UiState<List<GiftItem>>,
    onClaimGift: (GiftItem) -> Unit
) {
    when (giftsState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ZirveGold)
        }
        is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(giftsState.message, color = Color(0xFFFF8A80))
        }
        is UiState.Success -> {
            if (giftsState.data.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Gelen kutunda henüz arkadaş hediyesi yok. Arkadaşlar bölümünden hediyeleşebilirsiniz!",
                        color = Color(0xFFD1C4E9),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(giftsState.data, key = { it.id }) { gift ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xAA291047),
                            border = BorderStroke(1.dp, ZirveGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${gift.fromName} • +${gift.amount} Çip", color = ZirveGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(gift.message, color = Color.White, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { onClaimGift(gift) },
                                    enabled = !gift.claimed,
                                    colors = ButtonDefaults.buttonColors(containerColor = ZirveEmerald)
                                ) {
                                    Text(if (gift.claimed) "Alındı" else "Hediyeyi Al", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    profile: PlayerProfile,
    musicPlaying: Boolean,
    onUpdateDisplayName: (String) -> Unit,
    onToggleMusic: () -> Unit,
    onSignOut: () -> Unit
) {
    var editedName by remember(profile.displayName) { mutableStateOf(profile.displayName) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { if (it.length <= 35) editedName = it },
                singleLine = true,
                label = { Text("Oyuncu Adı") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZirveGold,
                    unfocusedBorderColor = ZirveGold.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { if (editedName.isNotBlank()) onUpdateDisplayName(editedName) },
                colors = ButtonDefaults.buttonColors(containerColor = ZirveGold, contentColor = ZirveDeepPurple)
            ) {
                Text("Adı Kaydet", fontWeight = FontWeight.ExtraBold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Arka Plan Lobi Müziği: ${if (musicPlaying) "Açık" else "Kapalı"}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onToggleMusic,
                colors = ButtonDefaults.buttonColors(containerColor = ZirveMagentaDark)
            ) {
                Text(if (musicPlaying) "Müziği Durdur" else "Müziği Başlat", color = ZirveGoldLight)
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = ZirveRuby),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Google Hesabından Çıkış Yap", fontWeight = FontWeight.Bold)
        }
    }
}
