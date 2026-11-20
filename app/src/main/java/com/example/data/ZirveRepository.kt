package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ZirveRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Devam etmek için Google ile giriş yapmalısınız.")
    }

    private fun <T> Flow<T>.handleSnapshotErrors(operationType: OperationType, path: String, expectedUid: String): Flow<T> =
        this.catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val exceptionToLog = firestoreError ?: (error as? Exception)
            if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
                handleFirestoreError(exceptionToLog, operationType, path)
            }
            throw error
        }

    fun observeMyProfile(uid: String): Flow<PlayerProfile?> = flow {
        val activeUid = requireUserId()
        check(activeUid == uid) { "Oturum değişti." }
        val path = "players/$uid"
        emitAll(
            db.collection("players").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(PlayerProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .handleSnapshotErrors(OperationType.GET, path, uid)
        )
    }

    fun observeOnlinePlayers(uid: String): Flow<List<PlayerProfile>> = flow {
        val activeUid = requireUserId()
        check(activeUid == uid) { "Oturum değişti." }
        val path = "players"
        emitAll(
            db.collection("players")
                .whereIn("onlineStatus", listOf("online", "playing", "offline"))
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PlayerProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(
                            compareBy<PlayerProfile> {
                                when (it.onlineStatus) {
                                    "online" -> 0
                                    "playing" -> 1
                                    else -> 2
                                }
                            }.thenByDescending { it.chips }
                        )
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeIncomingFriendRequests(uid: String): Flow<List<FriendRequest>> = flow {
        val activeUid = requireUserId()
        check(activeUid == uid) { "Oturum değişti." }
        val path = "friend_requests"
        emitAll(
            db.collection("friend_requests")
                .whereEqualTo("toUid", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FriendRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeOutgoingFriendRequests(uid: String): Flow<List<FriendRequest>> = flow {
        val activeUid = requireUserId()
        check(activeUid == uid) { "Oturum değişti." }
        val path = "friend_requests"
        emitAll(
            db.collection("friend_requests")
                .whereEqualTo("fromUid", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FriendRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeChatMessages(uid: String, roomId: String): Flow<List<ChatMessage>> = flow {
        val activeUid = requireUserId()
        check(activeUid == uid) { "Oturum değişti." }
        val path = "chat_messages"
        emitAll(
            db.collection("chat_messages")
                .whereEqualTo("roomId", roomId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                        .takeLast(60)
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeGameRooms(uid: String): Flow<List<GameRoom>> = flow {
        val activeUid = requireUserId()
        check(activeUid == uid) { "Oturum değişti." }
        val path = "game_rooms"
        emitAll(
            db.collection("game_rooms")
                .whereIn("status", listOf("waiting", "playing"))
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(GameRoom::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeGifts(uid: String): Flow<List<GiftItem>> = flow {
        val activeUid = requireUserId()
        check(activeUid == uid) { "Oturum değişti." }
        val path = "users/$uid/gifts"
        emitAll(
            db.collection("users").document(uid).collection("gifts")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(GiftItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun ensurePlayerProfile(uid: String, rawDisplayName: String): Result<Unit> {
        val path = "players/$uid"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid) { "Oturum kullanıcı kimliği eşleşmiyor." }
            val safeName = rawDisplayName.trim().ifBlank { "Zirve Oyuncusu" }.take(40)
            val docRef = db.collection("players").document(uid)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "avatarId" to "avatar_zirve",
                    "chips" to 1000L,
                    "level" to 1L,
                    "vipLevel" to 0L,
                    "onlineStatus" to "online",
                    "currentGame" to "Lobi",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "onlineStatus" to "online",
                        "currentGame" to "Lobi",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileStats(
        uid: String,
        chips: Long? = null,
        level: Long? = null,
        vipLevel: Long? = null,
        displayName: String? = null,
        onlineStatus: String? = null,
        currentGame: String? = null,
        markDailyClaim: Boolean = false
    ): Result<Unit> {
        val path = "players/$uid"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid) { "Oturum değişti." }
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (chips != null) updates["chips"] = chips.coerceIn(0L, 1_000_000_000L)
            if (level != null) updates["level"] = level.coerceIn(1L, 999L)
            if (vipLevel != null) updates["vipLevel"] = vipLevel.coerceIn(0L, 20L)
            if (displayName != null) {
                val clean = displayName.trim().take(40)
                require(clean.isNotEmpty()) { "Oyuncu adı boş olamaz." }
                updates["displayName"] = clean
            }
            if (onlineStatus != null) {
                require(onlineStatus in listOf("online", "playing", "offline"))
                updates["onlineStatus"] = onlineStatus
            }
            if (currentGame != null) {
                updates["currentGame"] = currentGame.trim().ifEmpty { "Lobi" }.take(40)
            }
            if (markDailyClaim) {
                updates["lastDailyClaimAt"] = FieldValue.serverTimestamp()
            }
            db.collection("players").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(fromProfile: PlayerProfile, toPlayer: PlayerProfile): Result<Unit> {
        val uid = fromProfile.userId
        val docId = "${uid}_${toPlayer.userId}".take(120)
        val path = "friend_requests/$docId"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid) { "Oturum değişti." }
            require(toPlayer.userId != uid) { "Kendinize arkadaşlık isteği gönderemezsiniz." }
            val payload = mapOf(
                "fromUid" to uid,
                "fromName" to fromProfile.displayName.take(40),
                "toUid" to toPlayer.userId,
                "toName" to toPlayer.displayName.take(40),
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("friend_requests").document(docId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun respondFriendRequest(requestId: String, accept: Boolean): Result<Unit> {
        val path = "friend_requests/$requestId"
        return try {
            requireUserId()
            require(requestId.isNotBlank()) { "Geçersiz istek kimliği." }
            val newStatus = if (accept) "accepted" else "declined"
            db.collection("friend_requests").document(requestId).update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun removeFriendship(requestId: String): Result<Unit> {
        val path = "friend_requests/$requestId"
        return try {
            requireUserId()
            require(requestId.isNotBlank()) { "Geçersiz arkadaşlık kaydı." }
            db.collection("friend_requests").document(requestId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(roomId: String, sender: PlayerProfile, text: String): Result<Unit> {
        val docRef = db.collection("chat_messages").document()
        val path = docRef.path
        return try {
            val activeUid = requireUserId()
            check(activeUid == sender.userId) { "Oturum değişti." }
            val cleanText = text.trim().take(300)
            require(cleanText.isNotEmpty()) { "Mesaj boş olamaz." }
            val payload = mapOf(
                "roomId" to roomId.take(64),
                "senderUid" to activeUid,
                "senderName" to sender.displayName.take(40),
                "senderVip" to sender.vipLevel.coerceIn(0L, 20L),
                "senderLevel" to sender.level.coerceIn(1L, 999L),
                "text" to cleanText,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun createGameRoom(
        host: PlayerProfile,
        gameType: String,
        tableName: String,
        minBet: Long,
        maxPlayers: Long
    ): Result<String> {
        val docRef = db.collection("game_rooms").document()
        val path = docRef.path
        return try {
            val activeUid = requireUserId()
            check(activeUid == host.userId) { "Oturum değişti." }
            require(gameType in listOf("101 OKEY", "BATAK", "TAVLA", "DÜZ OKEY"))
            val safeTableName = tableName.trim().ifBlank { "${host.displayName} Masası" }.take(50)
            val payload = mapOf(
                "hostUid" to activeUid,
                "hostName" to host.displayName.take(40),
                "gameType" to gameType,
                "tableName" to safeTableName,
                "minBet" to minBet.coerceIn(50L, 1_000_000L),
                "maxPlayers" to if (maxPlayers == 2L) 2L else 4L,
                "playerUids" to listOf(activeUid),
                "playerNames" to listOf(host.displayName.take(40)),
                "status" to "waiting",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun joinGameRoom(room: GameRoom, uid: String, displayName: String): Result<Unit> {
        val path = "game_rooms/${room.id}"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid) { "Oturum değişti." }
            if (activeUid in room.playerUids) return Result.success(Unit)
            require(room.playerUids.size < room.maxPlayers) { "Masa dolu." }
            val updatedUids = room.playerUids + activeUid
            val updatedNames = room.playerNames + displayName.take(40)
            db.collection("game_rooms").document(room.id).update(
                mapOf(
                    "playerUids" to updatedUids,
                    "playerNames" to updatedNames,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun leaveGameRoom(room: GameRoom, uid: String): Result<Unit> {
        val path = "game_rooms/${room.id}"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid) { "Oturum değişti." }
            if (room.hostUid == activeUid) {
                db.collection("game_rooms").document(room.id).delete().await()
            } else {
                val idx = room.playerUids.indexOf(activeUid)
                if (idx >= 0) {
                    val newUids = room.playerUids.toMutableList().apply { removeAt(idx) }
                    val newNames = room.playerNames.toMutableList().apply {
                        if (idx < size) removeAt(idx)
                    }
                    db.collection("game_rooms").document(room.id).update(
                        mapOf(
                            "playerUids" to newUids,
                            "playerNames" to newNames,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun sendGift(from: PlayerProfile, toUid: String, amount: Long, message: String): Result<Unit> {
        val docRef = db.collection("users").document(toUid).collection("gifts").document()
        val path = docRef.path
        return try {
            val activeUid = requireUserId()
            check(activeUid == from.userId) { "Oturum değişti." }
            val safeAmount = amount.coerceIn(10L, 5000L)
            val safeMsg = message.trim().ifBlank { "İyi şanslar!" }.take(120)
            val payload = mapOf(
                "fromUid" to activeUid,
                "fromName" to from.displayName.take(40),
                "toUid" to toUid,
                "amount" to safeAmount,
                "message" to safeMsg,
                "claimed" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun claimGift(uid: String, giftId: String): Result<Unit> {
        val path = "users/$uid/gifts/$giftId"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid) { "Oturum değişti." }
            db.collection("users").document(uid).collection("gifts").document(giftId).update(
                mapOf(
                    "claimed" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
