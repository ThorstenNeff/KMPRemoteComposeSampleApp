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

## Android — re-confirm formality (dev-1 already verified server-mode)
Pending a stable local emulator (the emulator crashed 4× this session = environment/GPU instability,
NOT the app — iOS ran clean throughout). Flows `server_gate_android_{success,failsoft}.yaml` are ready;
the per-platform host (10.0.2.2) is wired in ServerClient.android.kt. Re-run when the emu is stable.
