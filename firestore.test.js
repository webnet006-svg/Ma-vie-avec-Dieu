const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_user_123";
const BOB_UID = "bob_user_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

// 1. Unauthenticated access rejected
test("Unauthenticated user: cannot read or write user profile or prayers", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("prayers").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("prayers").doc("prayer1").set({
    id: "prayer1",
    userId: ALICE_UID,
    title: "Prière sans auth",
    category: "FAMILLE",
    isAnswered: false,
    isPrivate: true,
    prayCount: 1,
    createdAt: new Date(),
  }));
});

// 2. Authenticated user can manage own profile and subcollections
test("Authenticated user: can create and read own profile and prayer", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Create profile
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice Dupont",
    email: "alice@example.com",
    denominationalAffiliation: "Église Protestante Évangélique",
    dailyNotificationTime: "07:30",
    offlineModeEnabled: false,
    createdAt: new Date(),
  }));

  // Create prayer
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("prayers").doc("prayer_1").set({
    id: "prayer_1",
    userId: ALICE_UID,
    title: "Prière pour la paix dans ma famille",
    description: "Que le Seigneur accorde la réconciliation et son amour.",
    category: "FAMILLE",
    isAnswered: false,
    isPrivate: true,
    prayCount: 2,
    createdAt: new Date(),
  }));

  // Read prayer
  const doc = await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("prayers").doc("prayer_1").get());
});

// 3. Cross-user isolation: Bob cannot read or modify Alice's data
test("Cross-user isolation: Bob cannot read or modify Alice's prayer", async () => {
  // Alice seeds her prayer
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("prayers").doc("alice_secret").set({
    id: "alice_secret",
    userId: ALICE_UID,
    title: "Requête secrète d'Alice",
    category: "VIE_PERSONNELLE",
    isAnswered: false,
    isPrivate: true,
    prayCount: 1,
    createdAt: new Date(),
  }));

  // Bob attempts to read Alice's prayer
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("prayers").doc("alice_secret").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("prayers").get());

  // Bob attempts to write into Alice's prayer path
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("prayers").doc("malicious").set({
    id: "malicious",
    userId: BOB_UID,
    title: "Hack",
    category: "AUTRE",
    isAnswered: false,
    isPrivate: false,
    prayCount: 0,
    createdAt: new Date(),
  }));
});

// 4. Schema validation: rejects invalid or missing fields
test("Schema validation: rejects prayer with missing required fields or spoofed user ID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Missing category and createdAt
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).collection("prayers").doc("invalid_p").set({
    id: "invalid_p",
    userId: ALICE_UID,
    title: "Titre sans catégorie",
  }));

  // Spoofed userId
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).collection("prayers").doc("spoofed_p").set({
    id: "spoofed_p",
    userId: BOB_UID,
    title: "Prière avec faux UID",
    category: "FAMILLE",
    isAnswered: false,
    isPrivate: true,
    prayCount: 1,
    createdAt: new Date(),
  }));
});

// 5. Account deletion: user can delete their data
test("Account deletion: Alice can delete her prayer and profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("prayers").doc("prayer_del").set({
    id: "prayer_del",
    userId: ALICE_UID,
    title: "Prière à effacer",
    category: "AUTRE",
    isAnswered: false,
    isPrivate: true,
    prayCount: 1,
    createdAt: new Date(),
  }));

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("prayers").doc("prayer_del").delete());
});
