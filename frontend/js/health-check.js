
/* ===============================================
   GLOBAL BACKEND HEALTH CHECK (no /health needed)
   Uses /movies as a ping instead.
   =============================================== */

const API_BASE = "http://localhost:8080/api";
const HEALTH_CHECK_INTERVAL = 4000; // 4 seconds

async function checkBackendStatus() {
  const onDownPage = window.location.pathname.includes("system-down.html");

  try {
    // Try ANY endpoint that always exists
    const res = await fetch(`${API_BASE}/movies`, { method: "GET" });

    if (res.ok) {
      // Backend is alive
      if (onDownPage) {
        window.location.href = "index.html";
      }
      return;
    }

    // If 404 / 500 → backend considered down
    if (!onDownPage) window.location.href = "system-down.html";

  } catch (err) {
    // Network error → backend down
    if (!onDownPage) window.location.href = "system-down.html";
  }
}

// Run immediately
checkBackendStatus();

// Then check every few seconds
setInterval(checkBackendStatus, HEALTH_CHECK_INTERVAL);

