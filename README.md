# Tribute Auto Launch — onn. Google TV

**Developer documentation:** [Full troubleshooting history, implementation notes, tested ADB commands, cloud build instructions, and permanent-fix acceptance checklist](docs/DEVELOPER_HANDOFF.md).

## Purpose

Dedicated onn. 4K Google TV (Android TV 14) automatically opens the custom **Tribute Videos / Direct Delivery** app (`com.tukios.silk/.MainActivity`) following a cold boot or standby wake. Users must remain free to use Home/Back and other apps. **This repository contains a prototype. The user confirmed successful hard-reboot and power-button standby wake behavior on the actual Android TV 14 device on 2026-11-10.** Repeated-cycle, unplug/replug, and manual Home/Back behavior still require testing.

## Confirmed investigation

- Standard `LAUNCHER` invocation: **failed** (`No activities found`).
- Android TV `LEANBACK_LAUNCHER` invocation: **succeeded**.
- Explicit `am start -n com.tukios.silk/.MainActivity`: **succeeded**; user confirmed the target app opened.
- Cloud compilation: **succeeded**, [build run](https://github.com/ZHomeSlice/Direct-Delivery-Video-Tribute-Launcher/actions/runs/42892886407).
- APK installation: user reported **worked**.
- Hard reboot: **PASS, user-confirmed on 2026-11-10**.
- Power-button standby wake: **PASS, user-confirmed on 2026-11-10**.
- Unplug/replug power, repeated cycles, and intentional Home/Back exits: **not separately confirmed**.

## Build and install

1. Go to **Actions → Build Tribute Auto Launch APK → Run workflow**, or use the latest successful run.
2. Download and unzip the `TributeAutoLaunch-debug-apk` artifact to obtain `app-debug.apk`.
3. Enable Wireless debugging and reconnect ADB if its port has changed.
4. Install via `adb -s DEVICE_IP:PORT install -r app-debug.apk`.
5. Launch the utility and test **Launch Tribute Videos now**, then enable **Tribute wake launcher** in TV Accessibility settings.
6. Test full boot, standby wake, Home/Back exit, and logcat using the checklist in the [handoff](docs/DEVELOPER_HANDOFF.md).

Do **not** ship the debug-signed APK as a production release. A permanent fix should ideally be built into the target app, not depend on unsupported background-launch behavior.

## Open issue

Android 14 restricts background activity launching. This prototype **did work in the user's hard-reboot and power-button wake tests**, but that does not guarantee behavior on other firmware, devices, or every future restart. See the handoff for the observed behavior, risks, and required test matrix.
