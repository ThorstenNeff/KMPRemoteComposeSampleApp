# Server-Mode gate — results (test-1, @c60de1e)

Run the local server first: `./gradlew :server:runExampleServer -PserverPort=8080` (binds 127.0.0.1).
Host is per-platform (ServerClient): Android emu → 10.0.2.2, iOS sim → 127.0.0.1 (shares host net).

## iOS (the previously-unverified runtime path) — ✅ VERIFIED both axes
- **Success** (server up): server-mode ON → tap rc_box → loads `http://127.0.0.1:8080/rc/rc_box` →
  renders (red box, canvas-var 6791, draw=1, rc-doc=rc_box). **ATS works** — the Info.plist
  `NSAppTransportSecurity → NSAllowsLocalNetworking` exception lets cleartext loopback through the
  Darwin Ktor engine. The critical unknown (ATS/cleartext) is RESOLVED.
- **Fail-soft** (server down): tap rc_box → `rc-error` UI ("Couldn't load from server" + the
  NSURLError -1004), app LIVES (no crash, rc-back → list). `.testTag("rc-error")` confirmed in
  Screens.kt (DocLoad.Failed Column).

## Verify-before-route notes (so these gates don't false-fail)
1. **Use a TOP list item** (rc_box) for the gate — a below-fold item (e.g. rc_pie) adds a
   scrollUntilVisible + fetch-timing confound that masquerades as "stays on list / no render".
2. **iOS connect-timeout ≈ 10–15s** (NSURLError -1004) → fail-soft needs `extendedWaitUntil 15s`;
   a shorter wait races the timeout and the error UI hasn't appeared yet (false "no rc-error").
3. The error IS tagged `rc-error` — an early "not visible" was timing, not a wiring gap.

## Android — ✅ VERIFIED both axes (re-confirm, after a stable emu boot)
- **Success** (server up): server-mode ON → tap rc_box → loads via `10.0.2.2:8080` (emu→host loopback)
  → renders (rc-doc=rc_box, rc-rendered, draw>0, canvas 400×400).
- **Fail-soft** (server down): tap rc_box → `rc-error` UI, app LIVES (rc-back → list).
(The emulator crashed 4× earlier this session = environment/GPU instability, not the app; once it
booted cleanly, both axes passed first try. Android connect-failure is immediate — no long wait needed.)
