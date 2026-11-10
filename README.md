# Tribute Auto Launch — onn. Google TV

**Developer documentation:** [Full troubleshooting history, implementation notes, tested ADB commands, cloud build instructions, and permanent-fix acceptance checklist](docs/DEVELOPER_HANDOFF.md).

## Purpose

Dedicated onn. 4K Google TV (Android TV 14) automatically opens the custom **Tribute Videos / Direct Delivery** app (`com.tukios.silk/.MainActivity`) following a cold boot or standby wake. Users must remain free to use Home/Back and other apps. **This repository contains a prototype; Android 14 boot/wake behavior is not yet verified on the device.**

## Confirmed investigation

- Standard `LAUNCHER` invocation: **failed** (`No activities found`).
- Android TV `LEANBACK_LAUNCHER` invocation: **succeeded**.
- Explicit `am start -n com.tukios.silk/.MainActivity`: **succeeded**; user confirmed the target app opened.
- Cloud compilation: **succeeded**, [build run](https://github.com/ZHomeSlice/Direct-Delivery-Video-Tribute-Launcher/actions/runs/42892886407).
- APK installation: user reported **worked**.
- Automatic cold boot/wake and normal Home/Back exit: **not yet independently confirmed**.

## Build and install

1. Go to **Actions → Build Tribute Auto Launch APK → Run workflow**, or use the latest successful run.
2. Download and unzip the `TributeAutoLaunch-debug-apk` artifact to obtain `app-debug.apk`.
3. Enable Wireless debugging and reconnect ADB if its port has changed.
4. Install via `adb -s DEVICE_IP:PORT install -r app-debug.apk`.
5. Launch the utility and test **Launch Tribute Videos now**, then enable **Tribute wake launcher** in TV Accessibility settings.
6. Test full boot, standby wake, Home/Back exit, and logcat using the checklist in the [handoff](docs/DEVELOPER_HANDOFF.md).

Do **not** ship the debug-signed APK as a production release. A permanent fix should ideally be built into the target app, not depend on unsupported background-launch behavior.

## Open issue

Android 14 restricts background activity launching. A boot receiver or screen-on event alone does not guarantee the Tribute Videos app will be brought to the foreground. See the handoff for the observed behavior, risks, and required test matrix.
