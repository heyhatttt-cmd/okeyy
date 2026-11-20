package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.GameRoom
import com.example.data.PlayerProfile
import com.example.data.UiState
import com.example.ui.ZirveViewModel
import com.example.ui.theme.BarlowCondensedDisplay
import com.example.ui.theme.CinzelDisplay
import com.example.ui.theme.ZirveCyan
import com.example.ui.theme.ZirveDeepPurple
import com.example.ui.theme.ZirveEmerald
import com.example.ui.theme.ZirveGold
import com.example.ui.theme.ZirveGoldLight
import com.example.ui.theme.ZirveMagentaLight
import com.example.ui.theme.ZirveRuby
import kotlin.random.Random

data class OkeyTile(
    val id: Int,
    val number: Int,
    val colorType: Int
)

@Composable
fun OkeyTableScreen(
    gameType: String,
    profile: PlayerProfile,
    roomsState: UiState<List<GameRoom>>,
    activeRoomId: String?,
    onCreateRoom: (gameType: String, tableName: String, minBet: Long, maxPlayers: Long) -> Unit,
    onJoinRoom: (GameRoom) -> Unit,
    onLeaveRoom: (GameRoom) -> Unit,
    onRecordOutcome: (deltaChips: Long, summary: String) -> Unit,
    onOpenChat: () -> Unit,
    onOpenFriends: () -> Unit,
    onBack: () -> Unit
) {
    val rooms = (roomsState as? UiState.Success)?.data.orEmpty()
    val currentRoom = rooms.firstOrNull { it.id == activeRoomId }
    var playingSoloOrPractice by remember { mutableStateOf(false) }

    BackHandler {
        when {
            currentRoom != null -> onLeaveRoom(currentRoom)
            playingSoloOrPractice -> playingSoloOrPractice = false
            else -> onBack()
        }
    }

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
                        listOf(Color(0xD80A2618), Color(0xEC071E12), Color(0xF00B041B))
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
            // Top Bar
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
                        color = Color(0xCC1E0B36),
                        border = BorderStroke(1.5.dp, ZirveGold),
                        modifier = Modifier
                            .clickable {
                                when {
                                    currentRoom != null -> onLeaveRoom(currentRoom)
                                    playingSoloOrPractice -> playingSoloOrPractice = false
                                    else -> onBack()
                                }
                            }
                            .testTag("table_back_button")
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
                                text = if (currentRoom != null || playingSoloOrPractice) "Masadan Ayrıl" else stringResource(R.string.back_to_lobby),
                                color = ZirveGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = currentRoom?.tableName ?: gameType,
                        fontFamily = CinzelDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
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
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = ZirveViewModel.formatChips(profile.chips),
                                color = Color.White,
                                fontFamily = BarlowCondensedDisplay,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xCC281046),
                        border = BorderStroke(1.dp, ZirveCyan),
                        modifier = Modifier.clickable { onOpenFriends() }
                    ) {
                        Text(
                            text = "Arkadaşlar",
                            color = ZirveCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
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
                                contentDescription = null,
                                tint = ZirveMagentaLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Sohbet",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (currentRoom != null || playingSoloOrPractice) {
                ActiveGameTableStage(
                    gameType = currentRoom?.gameType ?: gameType,
                    playerNames = currentRoom?.playerNames ?: listOf(profile.displayName, "Ahmet_VIP", "Elif_101", "Mert_Pro"),
                    betAmount = currentRoom?.minBet ?: 250L,
                    onRecordOutcome = onRecordOutcome
                )
            } else {
                RoomSelectionStage(
                    gameType = gameType,
                    profile = profile,
                    roomsState = roomsState,
                    onCreateRoom = onCreateRoom,
                    onJoinRoom = onJoinRoom,
                    onPlayQuickTable = { playingSoloOrPractice = true }
                )
            }
        }
    }
}

@Composable
private fun RoomSelectionStage(
    gameType: String,
    profile: PlayerProfile,
    roomsState: UiState<List<GameRoom>>,
    onCreateRoom: (gameType: String, tableName: String, minBet: Long, maxPlayers: Long) -> Unit,
    onJoinRoom: (GameRoom) -> Unit,
    onPlayQuickTable: () -> Unit
) {
    val normalizedType = if (gameType == "ÇANAK OKEY") "101 OKEY" else gameType
    var newTableName by remember { mutableStateOf("${profile.displayName} Masası") }
    var selectedBet by remember { mutableStateOf(250L) }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left: Create Online Table + Quick Play
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xDD1A0832),
            border = BorderStroke(1.5.dp, ZirveGold),
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Yeni Çevrimiçi Masa Kur",
                        color = ZirveGoldLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = newTableName,
                        onValueChange = { if (it.length <= 40) newTableName = it },
                        singleLine = true,
                        label = { Text("Masa Adı", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZirveGold,
                            unfocusedBorderColor = ZirveGold.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = ZirveGoldLight,
                            unfocusedLabelColor = Color(0xFFD1C4E9)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(100L, 250L, 500L).forEach { bet ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedBet == bet) ZirveGold else Color(0x553B1659),
                                modifier = Modifier.clickable { selectedBet = bet }
                            ) {
                                Text(
                                    text = "$bet Çip",
                                    color = if (selectedBet == bet) ZirveDeepPurple else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val maxP = if (normalizedType == "TAVLA") 2L else 4L
                            onCreateRoom(normalizedType, newTableName, selectedBet, maxP)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZirveGold, contentColor = ZirveDeepPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_room_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Masa Oluştur", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onPlayQuickTable,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZirveEmerald, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_play_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hemen Oyna", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Right: Live Active Online Rooms
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xCC160829),
            border = BorderStroke(1.5.dp, ZirveGold.copy(alpha = 0.7f)),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Açık Çevrimiçi Masalar ($gameType)",
                    color = ZirveGoldLight,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))

                when (roomsState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ZirveGold)
                        }
                    }
                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(roomsState.message, color = Color(0xFFFF8A80), fontSize = 13.sp)
                        }
                    }
                    is UiState.Success -> {
                        val filtered = roomsState.data.filter { it.gameType == normalizedType }
                        val displayRooms = if (filtered.isNotEmpty()) filtered else roomsState.data
                        if (displayRooms.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Henüz açık masa yok. Soldan ilk çevrimiçi masayı kur veya 'Hemen Oyna'ya bas!",
                                    color = Color(0xFFD1C4E9),
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(displayRooms, key = { it.id }) { room ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xAA291047),
                                        border = BorderStroke(1.dp, ZirveGold.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = room.tableName,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "${room.gameType} • Bahis: ${room.minBet} Çip • ${room.playerUids.size}/${room.maxPlayers} Oyuncu",
                                                    color = ZirveGoldLight,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Button(
                                                onClick = { onJoinRoom(room) },
                                                enabled = room.playerUids.size < room.maxPlayers || profile.userId in room.playerUids,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = ZirveEmerald)
                                            ) {
                                                Text(
                                                    text = if (profile.userId in room.playerUids) "Masaya Gir" else "Katıl",
                                                    fontWeight = FontWeight.Bold,
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
        }
    }
}

@Composable
private fun ActiveGameTableStage(
    gameType: String,
    playerNames: List<String>,
    betAmount: Long,
    onRecordOutcome: (deltaChips: Long, summary: String) -> Unit
) {
    val tiles = remember {
        mutableStateListOf<OkeyTile>().apply {
            repeat(14) { i ->
                add(OkeyTile(id = i + 1, number = Random.nextInt(1, 14), colorType = Random.nextInt(4)))
            }
        }
    }
    var nextId by remember { mutableIntStateOf(100) }
    var selectedTileId by remember { mutableStateOf<Int?>(null) }
    var centerPileCount by remember { mutableIntStateOf(38) }
    var okeyIndicator by remember { mutableIntStateOf(Random.nextInt(1, 13)) }
    var tavlaDice by remember { mutableStateOf(Pair(6, 5)) }
    var tableNotice by remember { mutableStateOf("Sıra sende! Taş çek, sırala veya El Aç.") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Seats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val seats = (0 until 4).map { idx -> playerNames.getOrNull(idx) ?: "Boş Koltuk" }
            seats.forEachIndexed { idx, name ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (idx == 0) Color(0xCC2E1052) else Color(0xAA17082C),
                    border = BorderStroke(1.5.dp, if (idx == 0) ZirveGold else Color(0x66FFFFFF))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (name == "Boş Koltuk") Color.Gray else ZirveGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = ZirveDeepPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Center Felt Table Stage
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xCC0D4728),
            border = BorderStroke(3.dp, ZirveGold),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = tableNotice,
                    color = ZirveGoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                if (gameType == "TAVLA") {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf(tavlaDice.first, tavlaDice.second).forEach { die ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(2.dp, ZirveGold),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = die.toString(),
                                        color = Color.Black,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gösterge Taşı
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Gösterge", color = Color(0xFFB9F6CA), fontSize = 10.sp)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFDF5E6),
                                border = BorderStroke(1.5.dp, ZirveGold),
                                modifier = Modifier.size(width = 40.dp, height = 54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = okeyIndicator.toString(),
                                        color = ZirveRuby,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    )
                                }
                            }
                        }
                        // Yerdeki Deste
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1B5E20),
                            border = BorderStroke(1.5.dp, ZirveGoldLight),
                            modifier = Modifier.clickable {
                                if (tiles.size < 15) {
                                    nextId++
                                    tiles.add(OkeyTile(nextId, Random.nextInt(1, 14), Random.nextInt(4)))
                                    centerPileCount = (centerPileCount - 1).coerceAtLeast(1)
                                    tableNotice = "Ortadan yeni taş çektin! Şimdi bir taş at."
                                } else {
                                    tableNotice = "Istakanda 15 taş var, önce bir taş seçip at!"
                                }
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TAŞ ÇEK", color = ZirveGoldLight, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                Text("Kalan: $centerPileCount", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Action Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (gameType == "TAVLA") {
                        Button(
                            onClick = {
                                val d1 = Random.nextInt(1, 7)
                                val d2 = Random.nextInt(1, 7)
                                tavlaDice = Pair(d1, d2)
                                if (d1 == d2) {
                                    onRecordOutcome(betAmount, "Çift $d1-$d1 geldi! +$betAmount Çip kazandın!")
                                    tableNotice = "Çift zar ($d1-$d1)! Hamle avantajı sende!"
                                } else {
                                    tableNotice = "Zar: $d1 - $d2 oynandı."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZirveGold, contentColor = ZirveDeepPurple)
                        ) {
                            Text("Zar At", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                tiles.sortWith(compareBy<OkeyTile> { it.colorType }.thenBy { it.number })
                                tableNotice = "Taşlar seri olarak sıralandı."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593))
                        ) {
                            Text("Seri Diz", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                val sel = selectedTileId
                                if (sel != null) {
                                    tiles.removeAll { it.id == sel }
                                    selectedTileId = null
                                    tableNotice = "Taş atıldı! Sıradaki oyuncu oynuyor…"
                                } else {
                                    tableNotice = "Atmak için önce ıstakadan bir taşa dokun."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                        ) {
                            Text("Taş At", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Button(
                        onClick = {
                            val reward = betAmount * 2L
                            okeyIndicator = Random.nextInt(1, 13)
                            onRecordOutcome(reward, "$gameType eli kazanıldı! +$reward Çip!")
                            tableNotice = "TEBRİKLER! +$reward Çip kazandın! Yeni el başladı."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZirveEmerald)
                    ) {
                        Text(if (gameType == "TAVLA") "Mars / Kazan" else "El Aç (+${betAmount * 2})", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Bottom Wooden Okey Rack (Istaka)
        if (gameType != "TAVLA") {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF5D3A1A),
                border = BorderStroke(2.dp, ZirveGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    itemsIndexed(tiles, key = { _, t -> t.id }) { _, tile ->
                        val isSelected = selectedTileId == tile.id
                        val textColor = when (tile.colorType) {
                            0 -> ZirveRuby
                            1 -> Color(0xFF1565C0)
                            2 -> Color(0xFF212121)
                            else -> Color(0xFFE65100)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) Color(0xFFFFF59D) else Color(0xFFFDF5E6),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) ZirveGold else Color(0xFFD7CCC8)),
                            modifier = Modifier
                                .size(width = 36.dp, height = 48.dp)
                                .clickable {
                                    selectedTileId = if (isSelected) null else tile.id
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = tile.number.toString(),
                                    color = textColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "●",
                                    color = textColor,
                                    fontSize = 7.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
