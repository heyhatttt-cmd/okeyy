package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class PlayerProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Oyuncu",
    val avatarId: String = "avatar_zirve",
    val chips: Long = 1000L,
    val level: Long = 1L,
    val vipLevel: Long = 0L,
    val onlineStatus: String = "online",
    val currentGame: String = "Lobi",
    val lastDailyClaimAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FriendRequest(
    @DocumentId val id: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val toUid: String = "",
    val toName: String = "",
    val status: String = "pending",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val roomId: String = "lobby",
    val senderUid: String = "",
    val senderName: String = "",
    val senderVip: Long = 0L,
    val senderLevel: Long = 1L,
    val text: String = "",
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GameRoom(
    @DocumentId val id: String = "",
    val hostUid: String = "",
    val hostName: String = "",
    val gameType: String = "101 OKEY",
    val tableName: String = "",
    val minBet: Long = 100L,
    val maxPlayers: Long = 4L,
    val playerUids: List<String> = emptyList(),
    val playerNames: List<String> = emptyList(),
    val status: String = "waiting",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GiftItem(
    @DocumentId val id: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val toUid: String = "",
    val amount: Long = 100L,
    val message: String = "",
    val claimed: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
