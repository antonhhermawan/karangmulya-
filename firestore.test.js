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

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowTs() {
  return new Date(Date.now() - 1000);
}

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

test("Unauthenticated user: cannot read citizen_records or letter_requests", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("citizen_records").get());
  await assertFails(unauthDb.collection("letter_requests").get());
});

test("Authenticated user: can create and read their own citizen_record with valid 16-digit NIK/KK", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("citizen_records").doc("alice_profile");
  await assertSucceeds(
    docRef.set({
      userId: ALICE_UID,
      fullName: "Siti Aminah",
      nik: "3205012345670001",
      kkNumber: "3205019876540001",
      rtRw: "RT 02 / RW 01",
      dusun: "Dusun Krajan",
      address: "Jl. Desa Karangmulya No. 12",
      phone: "081234567890",
      occupation: "Wiraswasta",
      directoryGroup: "WARGA",
      verificationStatus: "MENUNGGU_VERIFIKASI",
      isSelfProfile: true,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(docRef.get());
});

test("Authenticated user: cannot read another user's citizen_record (PII Isolation)", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("citizen_records").doc("bob_profile").set({
      userId: BOB_UID,
      fullName: "Budi Santoso",
      nik: "3205012345670002",
      kkNumber: "3205019876540002",
      rtRw: "RT 01 / RW 02",
      dusun: "Dusun Mekarsari",
      address: "Jl. Karangmulya Timur No. 5",
      phone: "081298765432",
      occupation: "Petani",
      directoryGroup: "WARGA",
      verificationStatus: "TERVERIFIKASI",
      isSelfProfile: true,
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("citizen_records").doc("bob_profile").get());
});

test("Authenticated user: can create, query with userId filter, and update status of letter_request", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const letterRef = aliceDb.collection("letter_requests").doc("letter_1");
  await assertSucceeds(
    letterRef.set({
      userId: ALICE_UID,
      regNumber: "SRT-2026-1001",
      applicantName: "Siti Aminah",
      applicantNik: "3205012345670001",
      letterType: "Surat Keterangan Usaha (SKU)",
      purpose: "Persyaratan pengajuan KUR UMKM Desa Karangmulya",
      attachmentName: "ktp_kk_usaha.pdf",
      status: "DIAJUKAN",
      staffNote: "Menunggu verifikasi Kaur Umum",
      qrCodeData: "KARANGMULYA-SRT-2026-1001",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("letter_requests").where("userId", "==", ALICE_UID).get()
  );

  await assertSucceeds(
    letterRef.update({
      status: "SELESAI",
      staffNote: "Surat telah ditandatangani Kepala Desa",
      updatedAt: nowTs(),
    })
  );
});

test("Authenticated user: fails unfiltered query on letter_requests (Query Enforcer)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("letter_requests").get());
});

test("Audit logs: allow create for owner, strictly forbid update and delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logRef = aliceDb.collection("audit_logs").doc("log_1");
  await assertSucceeds(
    logRef.set({
      userId: ALICE_UID,
      actorName: "Siti Aminah",
      actorRole: "WARGA",
      actionType: "CREATE_SURAT",
      targetRef: "SRT-2026-1001",
      description: "Mengajukan Surat Keterangan Usaha (SKU)",
      createdAt: nowTs(),
    })
  );
  await assertFails(logRef.update({ description: "Modified" }));
  await assertFails(logRef.delete());
});
