const { onDocumentCreated, onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * Trigger: When a hardware or intake event is written under users/{userId}/devices/{deviceId}/events/{eventId}
 * Purpose: Detects MISSED dose events reported by hardware or firmware timeout, sending immediate high-priority FCM push.
 */
exports.onDeviceEventCreated = onDocumentCreated(
  "users/{userId}/devices/{deviceId}/events/{eventId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return null;

    const eventData = snap.data();
    const { userId, deviceId } = event.params;

    // Detect missed dose event from ESP32 or audit engine
    if (eventData.eventType === "MISSED_DOSE" || eventData.status === "MISSED") {
      const medicineName = eventData.medicineName || "Prescription Medicine";
      const dosage = eventData.dosage || "Scheduled dose";
      const scheduleId = eventData.scheduleId || event.params.eventId;

      const payload = {
        data: {
          type: "MISSED_DOSE",
          scheduleId: String(scheduleId),
          medicineName: String(medicineName),
          dosage: String(dosage),
          deviceId: String(deviceId),
          timestamp: String(Date.now()),
          deepLink: "smartmedbox://history"
        }
      };

      return sendMulticastNotification(userId, payload);
    }

    return null;
  }
);

/**
 * Trigger: When telemetry latest document is updated under users/{userId}/devices/{deviceId}/telemetry/latest
 * Purpose: Monitors chamber DHT22 sensor readings and alerts user if temperature/humidity breach safe boundaries.
 */
exports.onTelemetryUpdated = onDocumentWritten(
  "users/{userId}/devices/{deviceId}/telemetry/latest",
  async (event) => {
    const snap = event.data?.after;
    if (!snap || !snap.exists) return null;

    const telemetry = snap.data();
    const { userId, deviceId } = event.params;

    const temp = telemetry.temp || 0;
    const humidity = telemetry.humidity || 0;

    let alertTitle = null;
    let alertMessage = null;

    if (temp > 30.0 || temp < 10.0) {
      alertTitle = "Storage Temperature Warning";
      alertMessage = `Chamber temperature (${temp}°C) breached safe medication threshold (10°C - 30°C).`;
    } else if (humidity > 70.0) {
      alertTitle = "Chamber Moisture Alert";
      alertMessage = `Chamber humidity (${Math.round(humidity)}%) breached dry-seal threshold (70%).`;
    }

    if (alertTitle && alertMessage) {
      const payload = {
        data: {
          type: "ENVIRONMENTAL_ALERT",
          title: alertTitle,
          message: alertMessage,
          deviceId: String(deviceId),
          temp: String(temp),
          humidity: String(humidity),
          deepLink: "smartmedbox://device"
        }
      };

      return sendMulticastNotification(userId, payload);
    }

    return null;
  }
);

/**
 * Scheduled Trigger: Runs every 15 minutes to verify device heartbeats.
 * Purpose: Alerts patient/caregiver if the Smart Medicine Box disconnects for > 30 minutes.
 */
exports.checkDeviceHeartbeat = onSchedule("every 15 minutes", async (event) => {
  const thirtyMinutesAgo = new Date(Date.now() - 30 * 60 * 1000);

  // Query users collection group for devices
  const usersSnap = await db.collection("users").get();

  for (const userDoc of usersSnap.docs) {
    const userId = userDoc.id;
    const devicesSnap = await db.collection("users").doc(userId).collection("devices").get();

    for (const deviceDoc of devicesSnap.docs) {
      const device = deviceDoc.data();
      const lastSeen = device.lastSeen ? device.lastSeen.toDate() : null;

      if (lastSeen && lastSeen < thirtyMinutesAgo && device.isOnline !== false) {
        // Mark device offline in Firestore
        await deviceDoc.ref.update({ isOnline: false });

        const payload = {
          data: {
            type: "DEVICE_OFFLINE",
            deviceId: deviceDoc.id,
            offlineMinutes: "30",
            title: "Smart Box Offline",
            message: `Smart Medicine Box ${device.modelName || deviceDoc.id} has been unreachable for > 30 minutes.`,
            deepLink: "smartmedbox://device"
          }
        };

        await sendMulticastNotification(userId, payload);
      }
    }
  }
});

/**
 * Helper: Sends an FCM multicast message to all registered device tokens of a user,
 * automatically purging invalid/expired tokens from Firestore.
 */
async function sendMulticastNotification(userId, payload) {
  const tokensSnap = await db.collection("users").doc(userId).collection("fcmTokens").get();
  if (tokensSnap.empty) return;

  const tokens = tokensSnap.docs.map((doc) => doc.id);
  const response = await admin.messaging().sendEachForMulticast({
    tokens,
    data: payload.data,
    android: {
      priority: "high"
    }
  });

  // Prune dead/unregistered tokens
  const tokensToDelete = [];
  response.responses.forEach((res, idx) => {
    if (!res.success) {
      const errCode = res.error?.code;
      if (
        errCode === "messaging/invalid-registration-token" ||
        errCode === "messaging/registration-token-not-registered"
      ) {
        tokensToDelete.push(tokensSnap.docs[idx].ref.delete());
      }
    }
  });

  if (tokensToDelete.length > 0) {
    await Promise.all(tokensToDelete);
  }
}

