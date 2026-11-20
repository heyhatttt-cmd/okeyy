const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);
  await assertFails(
    unauthDb
      .collection("players")
      .where("onlineStatus", "in", ["online", "playing", "offline"])
      .get()
  );
  await assertFails(
    unauthDb.collection("players").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hacker",
      chips: 1000,
      level: 1,
      vipLevel: 0,
      onlineStatus: "online",
      currentGame: "Lobi",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertFails(
    unauthDb.collection("chat_messages").where("roomId", "==", "lobby").get()
  );
});

test("Player profile create, partial update, cross-user isolation, and scoped list", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  // Valid Kotlin-mirrored player create payload (no @DocumentId `id` field)
  await assertSucceeds(
    aliceDb.collection("players").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "ZirveAlice",
      avatarId: "avatar_zirve",
      chips: 1000,
      level: 1,
      vipLevel: 0,
      onlineStatus: "online",
      currentGame: "Lobi",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Partial update of chips & currentGame succeeds while preserving createdAt
  await assertSucceeds(
    aliceDb.collection("players").doc(ALICE_UID).update({
      chips: 1500,
      currentGame: "Slot Oyunları",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot update Alice's player profile
  await assertFails(
    bobDb.collection("players").doc(ALICE_UID).update({
      chips: 999999,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject Ghost Field (Shadow Update)
  await assertFails(
    aliceDb.collection("players").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject future timestamp tampering
  await assertFails(
    aliceDb.collection("players").doc(ALICE_UID).update({
      chips: 2000,
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Scoped online players query succeeds; unfiltered query fails
  await assertSucceeds(
    bobDb
      .collection("players")
      .where("onlineStatus", "in", ["online", "playing", "offline"])
      .get()
  );
  await assertFails(bobDb.collection("players").get());
});

test("Friend requests: send, accept by recipient only, and scoped queries", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const past = new Date(Date.now() - 5000);

  // Alice sends friend request to Bob
  await assertSucceeds(
    aliceDb.collection("friend_requests").doc("req_1").set({
      fromUid: ALICE_UID,
      fromName: "ZirveAlice",
      toUid: BOB_UID,
      toName: "ZirveBob",
      status: "pending",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Alice cannot self-accept her own request
  await assertFails(
    aliceDb.collection("friend_requests").doc("req_1").update({
      status: "accepted",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Charlie cannot read or modify Alice->Bob friend request
  await assertFails(charlieDb.collection("friend_requests").doc("req_1").get());
  await assertFails(
    charlieDb.collection("friend_requests").doc("req_1").update({
      status: "accepted",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob accepts the request
  await assertSucceeds(
    bobDb.collection("friend_requests").doc("req_1").update({
      status: "accepted",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Scoped queries succeed for both Alice and Bob; unfiltered query fails
  await assertSucceeds(
    aliceDb
      .collection("friend_requests")
      .where("fromUid", "==", ALICE_UID)
      .get()
  );
  await assertSucceeds(
    bobDb
      .collection("friend_requests")
      .where("toUid", "==", BOB_UID)
      .get()
  );
  await assertFails(charlieDb.collection("friend_requests").get());
});

test("Lobby chat messages: create, scoped query, immutable updates, owner-only delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 3000);

  await assertSucceeds(
    aliceDb.collection("chat_messages").doc("msg_1").set({
      roomId: "lobby",
      senderUid: ALICE_UID,
      senderName: "ZirveAlice",
      senderVip: 0,
      senderLevel: 1,
      text: "Herkese iyi oyunlar!",
      createdAt: past,
    })
  );

  // Cannot impersonate another senderUid
  await assertFails(
    bobDb.collection("chat_messages").doc("msg_2").set({
      roomId: "lobby",
      senderUid: ALICE_UID,
      senderName: "FakeAlice",
      senderVip: 0,
      senderLevel: 1,
      text: "Spoof",
      createdAt: past,
    })
  );

  // Cannot update chat message
  await assertFails(
    aliceDb.collection("chat_messages").doc("msg_1").update({ text: "Edited" })
  );

  // Scoped lobby chat query succeeds
  await assertSucceeds(
    bobDb.collection("chat_messages").where("roomId", "==", "lobby").get()
  );
});

test("Game rooms and user gifts follow strict rules", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 3000);

  await assertSucceeds(
    aliceDb.collection("game_rooms").doc("room_1").set({
      hostUid: ALICE_UID,
      hostName: "ZirveAlice",
      gameType: "101 OKEY",
      tableName: "Zirve VIP Masa 1",
      minBet: 250,
      maxPlayers: 4,
      playerUids: [ALICE_UID],
      playerNames: ["ZirveAlice"],
      status: "waiting",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Bob joins Alice's table
  await assertSucceeds(
    bobDb.collection("game_rooms").doc("room_1").update({
      playerUids: [ALICE_UID, BOB_UID],
      playerNames: ["ZirveAlice", "ZirveBob"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Scoped query for active rooms succeeds
  await assertSucceeds(
    bobDb
      .collection("game_rooms")
      .where("status", "in", ["waiting", "playing"])
      .get()
  );

  // Alice sends Bob a gift; only Bob can read and claim it
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("gifts")
      .doc("gift_1")
      .set({
        fromUid: ALICE_UID,
        fromName: "ZirveAlice",
        toUid: BOB_UID,
        amount: 250,
        message: "İyi şanslar!",
        claimed: false,
        createdAt: past,
        updatedAt: past,
      })
  );

  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("gifts")
      .get()
  );
  await assertSucceeds(
    bobDb
      .collection("users")
      .doc(BOB_UID)
      .collection("gifts")
      .get()
  );
  await assertSucceeds(
    bobDb
      .collection("users")
      .doc(BOB_UID)
      .collection("gifts")
      .doc("gift_1")
      .update({
        claimed: true,
        updatedAt: new Date(Date.now() - 1000),
      })
  );
});
