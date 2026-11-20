package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.LobbyMusicPlayer
import com.example.data.ChatMessage
import com.example.data.FriendRequest
import com.example.data.GameRoom
import com.example.data.GiftItem
import com.example.data.PlayerProfile
import com.example.data.UiState
import com.example.data.ZirveRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppDestination {
    LOBBY,
    SLOT_GAMES,
    GAME_TABLE
}

enum class LobbyOverlay {
    NONE,
    FRIENDS_ONLINE,
    CHAT,
    SHOP,
    PACKAGES,
    MISSIONS,
    DAILY_LOGIN,
    GIFTS,
    LEADERBOARD,
    MAIL,
    SETTINGS
}

data class SlotMachineTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val multiplierText: String,
    val accentHex: Long
)

data class SlotUiState(
    val selectedThemeIndex: Int = 0,
    val betAmount: Long = 100L,
    val reels: List<String> = listOf("7️⃣", "7️⃣", "7️⃣"),
    val spinning: Boolean = false,
    val lastWinAmount: Long = 0L,
    val lastResultMessage: String = "Şansını dene, 777 ile büyük ödülü kap!"
)

data class MissionProgress(
    val id: String,
    val title: String,
    val rewardChips: Long,
    val progress: Int,
    val target: Int,
    val claimed: Boolean
)

class ZirveViewModel(
    private val repository: ZirveRepository,
    val currentUserId: String,
    initialDisplayName: String
) : ViewModel() {

    private val musicPlayer = LobbyMusicPlayer(viewModelScope)

    private val _destination = MutableStateFlow(AppDestination.LOBBY)
    val destination: StateFlow<AppDestination> = _destination.asStateFlow()

    private val _activeGameType = MutableStateFlow("101 OKEY")
    val activeGameType: StateFlow<String> = _activeGameType.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    private val _overlay = MutableStateFlow(LobbyOverlay.NONE)
    val overlay: StateFlow<LobbyOverlay> = _overlay.asStateFlow()

    private val _chatChannel = MutableStateFlow("lobby")
    val chatChannel: StateFlow<String> = _chatChannel.asStateFlow()

    private val _musicPlaying = MutableStateFlow(false)
    val musicPlaying: StateFlow<Boolean> = _musicPlaying.asStateFlow()

    private val _musicTrackIndex = MutableStateFlow(0)
    val musicTrackIndex: StateFlow<Int> = _musicTrackIndex.asStateFlow()

    val musicTracks: List<String> get() = musicPlayer.tracks

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _slotState = MutableStateFlow(SlotUiState())
    val slotState: StateFlow<SlotUiState> = _slotState.asStateFlow()

    val slotThemes = listOf(
        SlotMachineTheme("classic777", "777 ZİRVE KLASİK", "Klasik 3 Makara Jackpot", "x25 ÖDÜL", 0xFFE040FB),
        SlotMachineTheme("royal_crown", "ALTIN TAÇ VIP", "VIP Elmas & Taç Makinesi", "x40 ÖDÜL", 0xFFFFC107),
        SlotMachineTheme("fruit_feast", "MEYVE ŞÖLENİ", "Kiraz & Çan Hızlı Kazanç", "x15 ÖDÜL", 0xFFFF5252),
        SlotMachineTheme("diamond_vault", "ELMAS HAZİNE", "Mega Çip Kasası", "x50 ÖDÜL", 0xFF00E5FF)
    )

    private val _missions = MutableStateFlow(
        listOf(
            MissionProgress("m_chat", "Sohbette 1 mesaj gönder", 250L, 0, 1, false),
            MissionProgress("m_slot", "Slot Oyunlarında 1 kez çevir", 400L, 0, 1, false),
            MissionProgress("m_friend", "Çevrimiçi bir oyuncuya arkadaşlık isteği at", 500L, 0, 1, false),
            MissionProgress("m_table", "101 Okey veya masa oyununa katıl", 350L, 0, 1, false)
        )
    )
    val missions: StateFlow<List<MissionProgress>> = _missions.asStateFlow()

    private val _dailyClaimedDay = MutableStateFlow(0)
    val dailyClaimedDay: StateFlow<Int> = _dailyClaimedDay.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensurePlayerProfile(currentUserId, initialDisplayName)
                .onFailure { _bannerMessage.value = "Profil senkronizasyonu başarısız: ${it.localizedMessage}" }
        }
    }

    private fun <T> Flow<T>.toUiState(): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                emit(UiState.Error(rootError.localizedMessage ?: "Veri akışı hatası"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val myProfileState: StateFlow<UiState<PlayerProfile?>> =
        repository.observeMyProfile(currentUserId).toUiState()

    val onlinePlayersState: StateFlow<UiState<List<PlayerProfile>>> =
        repository.observeOnlinePlayers(currentUserId).toUiState()

    val incomingRequestsState: StateFlow<UiState<List<FriendRequest>>> =
        repository.observeIncomingFriendRequests(currentUserId).toUiState()

    val outgoingRequestsState: StateFlow<UiState<List<FriendRequest>>> =
        repository.observeOutgoingFriendRequests(currentUserId).toUiState()

    val allFriendLinks: StateFlow<List<FriendRequest>> =
        combine(
            repository.observeIncomingFriendRequests(currentUserId).catch { emit(emptyList()) },
            repository.observeOutgoingFriendRequests(currentUserId).catch { emit(emptyList()) }
        ) { incoming, outgoing ->
            (incoming + outgoing).distinctBy { it.id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chatMessagesState: StateFlow<UiState<List<ChatMessage>>> =
        _chatChannel
            .flatMapLatest { channel -> repository.observeChatMessages(currentUserId, channel) }
            .toUiState()

    val gameRoomsState: StateFlow<UiState<List<GameRoom>>> =
        repository.observeGameRooms(currentUserId).toUiState()

    val giftsState: StateFlow<UiState<List<GiftItem>>> =
        repository.observeGifts(currentUserId).toUiState()

    fun currentProfileOrDefault(): PlayerProfile =
        (myProfileState.value as? UiState.Success)?.data ?: PlayerProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = "Oyuncu",
            chips = 1000L,
            level = 1L,
            vipLevel = 0L
        )

    fun openOverlay(overlay: LobbyOverlay) {
        _overlay.value = overlay
    }

    fun closeOverlay() {
        _overlay.value = LobbyOverlay.NONE
    }

    fun selectChatChannel(channel: String) {
        _chatChannel.value = channel
    }

    fun openSlotSection() {
        _destination.value = AppDestination.SLOT_GAMES
        _overlay.value = LobbyOverlay.NONE
        viewModelScope.launch {
            repository.updateProfileStats(
                uid = currentUserId,
                onlineStatus = "playing",
                currentGame = "Slot Oyunları"
            )
        }
    }

    fun openGameSection(gameType: String) {
        _activeGameType.value = gameType
        _destination.value = AppDestination.GAME_TABLE
        _overlay.value = LobbyOverlay.NONE
        incrementMission("m_table")
        viewModelScope.launch {
            repository.updateProfileStats(
                uid = currentUserId,
                onlineStatus = "playing",
                currentGame = gameType
            )
        }
    }

    fun returnToLobby() {
        _destination.value = AppDestination.LOBBY
        _activeRoomId.value = null
        viewModelScope.launch {
            repository.updateProfileStats(
                uid = currentUserId,
                onlineStatus = "online",
                currentGame = "Lobi"
            )
        }
    }

    fun toggleMusic() {
        val nextState = !_musicPlaying.value
        _musicPlaying.value = nextState
        if (nextState) {
            musicPlayer.start(_musicTrackIndex.value)
        } else {
            musicPlayer.stop()
        }
    }

    fun nextMusicTrack() {
        val nextIdx = (_musicTrackIndex.value + 1) % musicPlayer.tracks.size
        _musicTrackIndex.value = nextIdx
        if (_musicPlaying.value) {
            musicPlayer.start(nextIdx)
        }
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun showBanner(msg: String) {
        _bannerMessage.value = msg
    }

    fun claimFreeChips(amount: Long = 500L) {
        val profile = currentProfileOrDefault()
        val newChips = profile.chips + amount
        viewModelScope.launch {
            repository.updateProfileStats(uid = currentUserId, chips = newChips)
                .onSuccess { _bannerMessage.value = "+${formatChips(amount)} Ücretsiz Çip hesabına eklendi!" }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "Çip eklenemedi." }
        }
    }

    fun claimDailyReward(dayIndex: Int, amount: Long) {
        if (_dailyClaimedDay.value >= dayIndex) return
        val profile = currentProfileOrDefault()
        viewModelScope.launch {
            repository.updateProfileStats(
                uid = currentUserId,
                chips = profile.chips + amount,
                markDailyClaim = true
            ).onSuccess {
                _dailyClaimedDay.value = dayIndex
                _bannerMessage.value = "Günlük Giriş Ödülü: +${formatChips(amount)} Çip!"
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Ödül alınamadı."
            }
        }
    }

    fun claimMissionReward(missionId: String) {
        val target = _missions.value.firstOrNull { it.id == missionId } ?: return
        if (target.claimed || target.progress < target.target) return
        val profile = currentProfileOrDefault()
        val newLevel = if (profile.chips + target.rewardChips >= profile.level * 2500L) profile.level + 1L else profile.level
        viewModelScope.launch {
            repository.updateProfileStats(
                uid = currentUserId,
                chips = profile.chips + target.rewardChips,
                level = newLevel
            ).onSuccess {
                _missions.update { list ->
                    list.map { if (it.id == missionId) it.copy(claimed = true) else it }
                }
                _bannerMessage.value = "Görev ödülü alındı: +${formatChips(target.rewardChips)} Çip!"
            }
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            repository.updateProfileStats(uid = currentUserId, displayName = newName)
                .onSuccess { _bannerMessage.value = "Oyuncu adın güncellendi." }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "Ad güncellenemedi." }
        }
    }

    fun upgradeVipDemo(cost: Long, targetVip: Long) {
        val profile = currentProfileOrDefault()
        if (profile.chips < cost) {
            _bannerMessage.value = "Yetersiz çip! Önce ücretsiz çip alabilir veya slot oynayabilirsin."
            return
        }
        viewModelScope.launch {
            repository.updateProfileStats(
                uid = currentUserId,
                chips = profile.chips - cost,
                vipLevel = maxOf(profile.vipLevel, targetVip)
            ).onSuccess {
                _bannerMessage.value = "Tebrikler! VIP $targetVip paketi aktif edildi."
            }
        }
    }

    fun sendFriendRequest(target: PlayerProfile) {
        val me = currentProfileOrDefault()
        viewModelScope.launch {
            repository.sendFriendRequest(me, target)
                .onSuccess {
                    incrementMission("m_friend")
                    _bannerMessage.value = "${target.displayName} oyuncusuna arkadaşlık isteği gönderildi."
                }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "İstek gönderilemedi." }
        }
    }

    fun respondFriendRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            repository.respondFriendRequest(requestId, accept)
                .onSuccess {
                    _bannerMessage.value = if (accept) "Arkadaş eklendi!" else "İstek reddedildi."
                }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "İşlem başarısız." }
        }
    }

    fun removeFriend(requestId: String) {
        viewModelScope.launch {
            repository.removeFriendship(requestId)
                .onSuccess { _bannerMessage.value = "Arkadaşlıktan çıkarıldı." }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "Silinemedi." }
        }
    }

    fun sendChatMessage(text: String, channelOverride: String? = null) {
        val me = currentProfileOrDefault()
        val channel = channelOverride ?: _chatChannel.value
        viewModelScope.launch {
            repository.sendChatMessage(channel, me, text)
                .onSuccess { incrementMission("m_chat") }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "Mesaj gönderilemedi." }
        }
    }

    fun createGameRoom(gameType: String, tableName: String, minBet: Long, maxPlayers: Long) {
        val me = currentProfileOrDefault()
        viewModelScope.launch {
            repository.createGameRoom(me, gameType, tableName, minBet, maxPlayers)
                .onSuccess { roomId ->
                    _activeRoomId.value = roomId
                    _bannerMessage.value = "$tableName masası açıldı!"
                }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "Masa oluşturulamadı." }
        }
    }

    fun joinRoom(room: GameRoom) {
        val me = currentProfileOrDefault()
        viewModelScope.launch {
            repository.joinGameRoom(room, currentUserId, me.displayName)
                .onSuccess { _activeRoomId.value = room.id }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "Masaya katılınamadı." }
        }
    }

    fun leaveActiveRoom(room: GameRoom) {
        viewModelScope.launch {
            repository.leaveGameRoom(room, currentUserId)
                .onSuccess { _activeRoomId.value = null }
        }
    }

    fun recordTableOutcome(deltaChips: Long, summary: String) {
        val profile = currentProfileOrDefault()
        val updated = (profile.chips + deltaChips).coerceAtLeast(0L)
        val nextLevel = if (deltaChips > 0 && updated >= profile.level * 2500L) profile.level + 1L else profile.level
        viewModelScope.launch {
            repository.updateProfileStats(uid = currentUserId, chips = updated, level = nextLevel)
                .onSuccess { _bannerMessage.value = summary }
        }
    }

    fun sendGiftToFriend(toPlayer: PlayerProfile, amount: Long = 250L) {
        val me = currentProfileOrDefault()
        if (me.chips < amount) {
            _bannerMessage.value = "Hediye göndermek için yeterli çip yok."
            return
        }
        viewModelScope.launch {
            val sendRes = repository.sendGift(me, toPlayer.userId, amount, "${me.displayName} sana çip hediye etti!")
            if (sendRes.isSuccess) {
                repository.updateProfileStats(uid = currentUserId, chips = me.chips - amount)
                _bannerMessage.value = "${toPlayer.displayName} oyuncusuna +${formatChips(amount)} çip gönderildi!"
            } else {
                _bannerMessage.value = "Hediye gönderilemedi."
            }
        }
    }

    fun claimGift(gift: GiftItem) {
        if (gift.claimed) return
        val me = currentProfileOrDefault()
        viewModelScope.launch {
            val res = repository.claimGift(currentUserId, gift.id)
            if (res.isSuccess) {
                repository.updateProfileStats(uid = currentUserId, chips = me.chips + gift.amount)
                _bannerMessage.value = "+${formatChips(gift.amount)} Çip hediye kutundan alındı!"
            }
        }
    }

    // --- Slot Machine Logic ---
    fun selectSlotTheme(index: Int) {
        _slotState.update { it.copy(selectedThemeIndex = index.coerceIn(0, slotThemes.lastIndex)) }
    }

    fun selectSlotBet(bet: Long) {
        _slotState.update { it.copy(betAmount = bet) }
    }

    fun spinSlotMachine() {
        val state = _slotState.value
        if (state.spinning) return
        val profile = currentProfileOrDefault()
        if (profile.chips < state.betAmount) {
            _slotState.update {
                it.copy(lastResultMessage = "Çip yetersiz! Ücretsiz Çip butonundan çip alabilirsin.")
            }
            return
        }

        val symbols = when (state.selectedThemeIndex) {
            1 -> listOf("👑", "💎", "7️⃣", "🏆", "⭐")
            2 -> listOf("🍒", "🍋", "🍉", "🔔", "7️⃣")
            3 -> listOf("💎", "💠", "👑", "🍀", "7️⃣")
            else -> listOf("7️⃣", "👑", "🍒", "💎", "🔔", "🍀")
        }

        val multiplierBase = when (state.selectedThemeIndex) {
            1 -> 40L
            2 -> 15L
            3 -> 50L
            else -> 25L
        }

        val bet = state.betAmount
        _slotState.update {
            it.copy(spinning = true, lastWinAmount = 0L, lastResultMessage = "Makaralar dönüyor…")
        }
        incrementMission("m_slot")

        viewModelScope.launch {
            val afterBetChips = profile.chips - bet
            repository.updateProfileStats(uid = currentUserId, chips = afterBetChips)

            repeat(9) { step ->
                delay(75L)
                _slotState.update { current ->
                    val r0 = if (step < 5) symbols.random() else current.reels[0]
                    val r1 = if (step < 7) symbols.random() else current.reels[1]
                    val r2 = symbols.random()
                    current.copy(reels = listOf(r0, r1, r2))
                }
            }

            // Weighted outcome so gameplay feels lively & rewarding
            val roll = Random.nextInt(100)
            val finalReels = when {
                roll < 8 -> {
                    val s = symbols.first()
                    listOf(s, s, s)
                }
                roll < 20 -> {
                    val s = symbols[Random.nextInt(symbols.size)]
                    listOf(s, s, s)
                }
                roll < 52 -> {
                    val s = symbols[Random.nextInt(symbols.size)]
                    val other = symbols.filter { it != s }.random()
                    listOf(s, s, other).shuffled()
                }
                else -> symbols.shuffled().take(3)
            }

            val winAmount = when {
                finalReels[0] == finalReels[1] && finalReels[1] == finalReels[2] -> {
                    if (finalReels[0] == "7️⃣" || finalReels[0] == "👑" || finalReels[0] == "💎") {
                        bet * multiplierBase
                    } else {
                        bet * 8L
                    }
                }
                finalReels[0] == finalReels[1] || finalReels[1] == finalReels[2] || finalReels[0] == finalReels[2] -> {
                    bet * 2L
                }
                else -> 0L
            }

            if (winAmount > 0L) {
                val latestChips = currentProfileOrDefault().chips
                val newTotal = latestChips + winAmount
                val newLevel = if (newTotal >= profile.level * 2500L) profile.level + 1L else profile.level
                repository.updateProfileStats(uid = currentUserId, chips = newTotal, level = newLevel)
            }

            val msg = when {
                winAmount >= bet * 10 -> "MEGA JACKPOT! +${formatChips(winAmount)} Çip Kazandın!"
                winAmount > 0L -> "TEBRİKLER! +${formatChips(winAmount)} Çip Kazandın!"
                else -> "Bu turda çıkmadı, tekrar çevir!"
            }

            _slotState.update {
                it.copy(
                    spinning = false,
                    reels = finalReels,
                    lastWinAmount = winAmount,
                    lastResultMessage = msg
                )
            }
        }
    }

    private fun incrementMission(missionId: String) {
        _missions.update { list ->
            list.map {
                if (it.id == missionId && it.progress < it.target) {
                    it.copy(progress = it.progress + 1)
                } else it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayer.stop()
    }

    companion object {
        fun formatChips(value: Long): String {
            val s = value.toString()
            val sb = StringBuilder()
            var count = 0
            for (i in s.length - 1 downTo 0) {
                sb.append(s[i])
                count++
                if (count % 3 == 0 && i != 0) sb.append('.')
            }
            return sb.reverse().toString()
        }
    }
}
