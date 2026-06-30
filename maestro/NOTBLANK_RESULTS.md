# RemoteComposeExample — Render Acceptance Gate results (test-1, @2e24383)

Maestro nav+render gate (render_gate_{android,ios}.yaml): GREEN on Android (2× stable) + iOS — all 16
docs reachable; per doc rc-doc==id + rc-rendered + rc-draw-count>0 (+ rc-back nav).

⚠️ rc-draw-count>0 is NOT a sufficient "not-blank" oracle (a 1-op draw of an empty/off-canvas body =
draw=1 but visually blank). Supplemented with an out-of-Maestro **canvas pixel-variance** pass (true
visible-pixel check). Blank floor ≈ 400; sparse < 1500; VISIBLE ≥ 1500.

## Area B — "bundled_rc" (10, all real) — ALL NON-BLANK ✓
rc_box 7878 · rc_text 3547 · rc_gradient 7407 · rc_moon 4008 · rc_clock 3730 · rc_confetti 4358 ·
rc_click 5513 · rc_pie 3590 = VISIBLE ;  rc_scroll 1201 · rc_compass 1026 = sparse (non-blank, content present).

## Area A — "creation_dsl" (6)
dsl_oval 10602 VISIBLE [real ✓] · dsl_gradient 5398 VISIBLE · dsl_tap 5844 VISIBLE ·
dsl_compose_card 4425 VISIBLE · dsl_text 601 sparse · **dsl_clock 218 = BLANK ⚠️**

## Verdict
- **11 real docs (bundled_rc ×10 + dsl_oval): ACCEPTED** — all render non-blank on-device (Android+iOS).
- **5 DSL placeholders: PENDING-real** (dev-1 builds real bodies). Among them **dsl_clock renders BLANK**
  (var 218, ~blank) — passes the draw-count gate but shows no pixels → flag to dev-1 (even a placeholder
  should not be blank). dsl_text sparse.
- Gate hardening: a transition-race flake (rc-back over-popped once) was fixed with waitForAnimationToEnd
  settle-waits → 2× stable. For a durable not-blank gate, recommend dev-1 expose a not-blank signal, or
  keep this variance pass as the acceptance oracle.
