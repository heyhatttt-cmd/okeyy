package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthScreen
import com.example.auth.signOut
import com.example.data.PlayerProfile
import com.example.data.UiState
import com.example.data.ZirveRepository
import com.example.ui.AppDestination
import com.example.ui.LobbyOverlay
import com.example.ui.ZirveViewModel
import com.example.ui.components.ZirveLobbyScreen
import com.example.ui.screens.OkeyTableScreen
import com.example.ui.screens.OverlayHostDialog
import com.example.ui.screens.SlotGamesScreen
import com.example.ui.theme.ZirveOkeyTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZirveOkeyTheme {
                ZirveRootApp()
            }
        }
    }
}

@Composable
fun ZirveRootApp() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
    } else {
        val uid = user.uid
        val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "Zirve Oyuncusu"
        key(uid) {
            ZirveAuthenticatedShell(
                currentUserId = uid,
                initialDisplayName = displayName
            )
        }
    }
}

@Composable
private fun ZirveAuthenticatedShell(
    currentUserId: String,
    initialDisplayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: ZirveViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ZirveViewModel(
                    repository = ZirveRepository(db),
                    currentUserId = currentUserId,
                    initialDisplayName = initialDisplayName
                )
            }
        }
    )

    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val activeGameType by viewModel.activeGameType.collectAsStateWithLifecycle()
    val activeRoomId by viewModel.activeRoomId.collectAsStateWithLifecycle()
    val overlay by viewModel.overlay.collectAsStateWithLifecycle()
    val chatChannel by viewModel.chatChannel.collectAsStateWithLifecycle()
    val musicPlaying by viewModel.musicPlaying.collectAsStateWithLifecycle()
    val musicTrackIndex by viewModel.musicTrackIndex.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()
    val slotState by viewModel.slotState.collectAsStateWithLifecycle()
    val missions by viewModel.missions.collectAsStateWithLifecycle()
    val dailyClaimedDay by viewModel.dailyClaimedDay.collectAsStateWithLifecycle()

    val myProfileState by viewModel.myProfileState.collectAsStateWithLifecycle()
    val onlinePlayersState by viewModel.onlinePlayersState.collectAsStateWithLifecycle()
    val incomingRequestsState by viewModel.incomingRequestsState.collectAsStateWithLifecycle()
    val friendLinks by viewModel.allFriendLinks.collectAsStateWithLifecycle()
    val chatMessagesState by viewModel.chatMessagesState.collectAsStateWithLifecycle()
    val gameRoomsState by viewModel.gameRoomsState.collectAsStateWithLifecycle()
    val giftsState by viewModel.giftsState.collectAsStateWithLifecycle()

    LaunchedEffect(bannerMessage) {
        if (bannerMessage != null) {
            delay(3200L)
            viewModel.clearBanner()
        }
    }

    val profile = (myProfileState as? UiState.Success)?.data ?: PlayerProfile(
        id = currentUserId,
        userId = currentUserId,
        displayName = initialDisplayName,
        chips = 1000L,
        level = 1L,
        vipLevel = 0L
    )

    val onlineCount = (onlinePlayersState as? UiState.Success)?.data
        ?.count { it.onlineStatus == "online" || it.onlineStatus == "playing" }
        ?.coerceAtLeast(1) ?: 1

    val pendingReqCount = (incomingRequestsState as? UiState.Success)?.data
        ?.count { it.status == "pending" } ?: 0

    val unclaimedGiftCount = (giftsState as? UiState.Success)?.data
        ?.count { !it.claimed } ?: 0

    val activeRoomCount = (gameRoomsState as? UiState.Success)?.data?.size ?: 0

    Box(modifier = Modifier.fillMaxSize()) {
        when (destination) {
            AppDestination.LOBBY -> {
                ZirveLobbyScreen(
                    profile = profile,
                    onlineCount = onlineCount,
                    pendingRequestCount = pendingReqCount,
                    unclaimedGiftCount = unclaimedGiftCount,
                    activeRoomCount = activeRoomCount,
                    musicPlaying = musicPlaying,
                    musicTrackName = viewModel.musicTracks[musicTrackIndex % viewModel.musicTracks.size],
                    bannerMessage = bannerMessage,
                    onDismissBanner = { viewModel.clearBanner() },
                    onToggleMusic = { viewModel.toggleMusic() },
                    onNextMusic = { viewModel.nextMusicTrack() },
                    onOpenGame = { card ->
                        if (card.isSlot) {
                            viewModel.openSlotSection()
                        } else {
                            viewModel.openGameSection(card.title)
                        }
                    },
                    onOpenOverlay = { target -> viewModel.openOverlay(target) },
                    onClaimFreeChips = { viewModel.claimFreeChips(500L) }
                )
            }
            AppDestination.SLOT_GAMES -> {
                SlotGamesScreen(
                    profile = profile,
                    slotState = slotState,
                    themes = viewModel.slotThemes,
                    onSelectTheme = { viewModel.selectSlotTheme(it) },
                    onSelectBet = { viewModel.selectSlotBet(it) },
                    onSpin = { viewModel.spinSlotMachine() },
                    onClaimFreeChips = { viewModel.claimFreeChips(500L) },
                    onOpenChat = {
                        viewModel.selectChatChannel("slot")
                        viewModel.openOverlay(LobbyOverlay.CHAT)
                    },
                    onBack = { viewModel.returnToLobby() }
                )
            }
            AppDestination.GAME_TABLE -> {
                OkeyTableScreen(
                    gameType = activeGameType,
                    profile = profile,
                    roomsState = gameRoomsState,
                    activeRoomId = activeRoomId,
                    onCreateRoom = { type, name, bet, maxP ->
                        viewModel.createGameRoom(type, name, bet, maxP)
                    },
                    onJoinRoom = { room -> viewModel.joinRoom(room) },
                    onLeaveRoom = { room -> viewModel.leaveActiveRoom(room) },
                    onRecordOutcome = { delta, summary ->
                        viewModel.recordTableOutcome(delta, summary)
                    },
                    onOpenChat = {
                        viewModel.selectChatChannel("101_okey")
                        viewModel.openOverlay(LobbyOverlay.CHAT)
                    },
                    onOpenFriends = { viewModel.openOverlay(LobbyOverlay.FRIENDS_ONLINE) },
                    onBack = { viewModel.returnToLobby() }
                )
            }
        }

        OverlayHostDialog(
            overlay = overlay,
            profile = profile,
            onlinePlayersState = onlinePlayersState,
            friendLinks = friendLinks,
            chatMessagesState = chatMessagesState,
            chatChannel = chatChannel,
            giftsState = giftsState,
            missions = missions,
            dailyClaimedDay = dailyClaimedDay,
            musicPlaying = musicPlaying,
            onDismiss = { viewModel.closeOverlay() },
            onSelectChatChannel = { viewModel.selectChatChannel(it) },
            onSendChat = { viewModel.sendChatMessage(it) },
            onSendFriendRequest = { viewModel.sendFriendRequest(it) },
            onRespondFriendRequest = { reqId, accept ->
                viewModel.respondFriendRequest(reqId, accept)
            },
            onRemoveFriend = { viewModel.removeFriend(it) },
            onSendGiftToFriend = { viewModel.sendGiftToFriend(it) },
            onClaimGift = { viewModel.claimGift(it) },
            onClaimFreeChips = { viewModel.claimFreeChips(it) },
            onClaimDailyReward = { day, amt -> viewModel.claimDailyReward(day, amt) },
            onClaimMissionReward = { viewModel.claimMissionReward(it) },
            onUpgradeVip = { cost, vip -> viewModel.upgradeVipDemo(cost, vip) },
            onUpdateDisplayName = { viewModel.updateDisplayName(it) },
            onToggleMusic = { viewModel.toggleMusic() },
            onSignOut = {
                viewModel.closeOverlay()
                signOut(credentialManager, scope)
            }
        )
    }
}
