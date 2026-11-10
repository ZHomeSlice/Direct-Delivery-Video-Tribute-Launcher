# Developer handoff: Tribute Videos automatic launch on onn. Google TV

Updated: 2026-11-10

## Objective and desired behavior

This **dedicated onn. 4K Google TV streaming device** is used to play tribute videos through a custom application referred to by operators as **Tribute Videos** or **Direct Delivery**. The goal is to open that app automatically **both after a full power cycle and when waking from standby**.

- On a full boot, launch the app once Android is ready.
- On a standby wake, bring the app to the foreground, regardless of what was previously displayed.
- **Do not implement kiosk-mode lock-in**: operators must still be able to press Home or Back and use other TV functions. Do not continuously relaunch after an intentional Home/Back exit; relaunch only on the next boot/wake.
- No external computer or ADB connection should be required after provisioning.
- The proper long-term solution should preferably be implemented and maintained **in the Tribute Videos application itself** or in an explicitly supported first-party companion app. This repository currently contains a **prototype companion utility**, not a verified permanent fix.

## Confirmed device and software

| Field | Value | Confidence |
| --- | --- | --- |
| Device | Walmart onn. 4K Streaming Device | Confirmed from Settings screenshot |
| Operating system | Android TV OS 14 | Confirmed from Settings screenshot |
| Build | `UKRB.260113.075.A1` | Confirmed from screenshot |
| Android security patch | 2026-06-01 | Confirmed from screenshot |
| CPU ABIs | `armeabi-v7a,armeabi` (32-bit ARM) | Confirmed via ADB |
| Target app package | `com.tukios.silk` | Confirmed in installed package list |
| Target activity | `com.tukios.silk/.MainActivity` | Confirmed via package resolver |
| Wi-Fi ADB IP at setup | `192.168.2.107` | Setup-time observation; DHCP address may change |
| ADB connection port at setup | `38501` | Ephemeral and expected to change |
| Connected ADB target | `192.168.2.107:38501` | Confirmed via `adb devices` during setup |

**Do not hardcode the Wi-Fi debugging port or treat the observed IP as permanent.**

The Roland VR-3EX video mixer's HDMI input 2 accepts **480p/576p**, while its HDMI input 4 supports higher-resolution signals but is occupied by a computer. The Google TV UI can use a higher-resolution HDMI mode than input 2 accepts. **Auto-starting Tribute Videos does not itself downscale HDMI or guarantee a valid HDMI signal on the Roland during boot**. Resolve that hardware limitation separately with a genuine HDMI-to-HDMI scaler supporting the required 480p timing if necessary.

## Investigation and verified results

1. **Normal operation:** Tribute Videos can be opened manually from the Google TV home screen.
2. **LaunchAPP:** The app was visible in LaunchAPP but would not launch.
3. **OnStartApps:** The app did not appear in its selectable list.
4. **AutoStart App Manager:** The app was also missing from its list.
5. The following ADB command **failed** to find a standard phone-style launcher activity:
   ```cmd
   adb -s 192.168.2.107:38501 shell monkey -p com.tukios.silk -c android.intent.category.LAUNCHER 1
   ```
   Returned: `** No activities found to run, monkey aborted.`
6. The Android TV launcher category **succeeded**, and the app visibly opened:
   ```cmd
   adb -s 192.168.2.107:38501 shell monkey -p com.tukios.silk -c android.intent.category.LEANBACK_LAUNCHER 1
   ```
   Returned: `Events injected: 1`.
7. The system's activity resolver identified:
   ```cmd
   adb -s 192.168.2.107:38501 shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER -p com.tukios.silk
   ```
   Returned `com.tukios.silk/.MainActivity`.
8. Explicit activity launch **succeeded**, and the operator confirmed Tribute Videos appeared on the screen:
   ```cmd
   adb -s 192.168.2.107:38501 shell am start -n com.tukios.silk/.MainActivity
   ```
   Returned `Starting: Intent { cmp=com.tukios.silk/.MainActivity }`.
9. A cloud-built **Tribute Auto Launch** prototype APK was downloaded, extracted, and the user reported the **installation/initial procedure worked**. **Cold-boot and wake trigger success have NOT been independently confirmed in the conversation**. Treat these as untested.

### Why third-party launch utilities struggled

The observed difference between `LAUNCHER` and `LEANBACK_LAUNCHER` strongly suggests that the target app has a TV launcher entry, but lacks a usable `android.intent.category.LAUNCHER` entry. Confirm against the real `com.tukios.silk` AndroidManifest before making a definitive claim. Not every auto-start utility enumerates Android TV launcher entries.

### Suggested permanent application-side fix

In the app's main Android activity, keep the existing Android TV intent filter. For compatibility with utilities that enumerate standard launchable apps, **consider** adding a separate `LAUNCHER` category filter, after checking the product's launcher behavior and whether duplicative launcher icons are acceptable:

```xml
<activity android:name=".MainActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
    </intent-filter>
    <!-- Optional interoperability, validate behavior on Android TV: -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

**Adding a launcher filter alone does not implement boot or wake auto-start.** For those behaviors:

- Decide between robust first-party app support and a maintained dedicated companion app.
- Register for `RECEIVE_BOOT_COMPLETED` and consider `ACTION_BOOT_COMPLETED` / `ACTION_LOCKED_BOOT_COMPLETED` as appropriate.
- Respect **Android 14 background-activity-start limitations**; a broadcast receiver does not automatically have permission to show another activity. Do not assume a delayed `startActivity()` from `BOOT_COMPLETED` will work.
- Consider a user-enabled accessibility service **only if functionally justified and compliant with Android requirements**, or supported device-owner / dedicated-device management where available. Avoid abusing accessibility merely to bypass OS limitations; validate actual permitted behavior.
- Wake handling may use screen-on broadcasts registered from a live service, but Android TV firmware may treat standby/wake differently. Test the physical remote, HDMI-CEC, and full power restoration separately.
- Avoid launching every time an unrelated activity appears, because the operator must be allowed to leave the app intentionally.
- Add minimal logging and a visible setting to enable/disable auto-start and manually test the app's launch.

## ADB setup and troubleshooting (Windows)

Install official Android platform-tools, extract to `C:\platform-tools`:
https://dl.google.com/android/repository/platform-tools-latest-windows.zip

Check ADB:
```powershell
C:\platform-tools\adb.exe version
```

On Google TV: **Settings → System → About → Android TV OS build**, press Select 7 times if Developer options is hidden. Then **Settings → System → Developer options → Wireless debugging → Pair device with pairing code**.

Example pairing session (the pairing port/code are temporary; do not reuse):
```cmd
adb pair 192.168.2.107:PAIR_PORT
adb connect 192.168.2.107:CONNECT_PORT
adb devices
```

**Note:** `PAIR_PORT` and `CONNECT_PORT` are different values in many sessions. If `adb devices` shows both a TLS service name and the IP target, use `-s IP:PORT` to choose one.

List third-party packages:
```cmd
adb -s 192.168.2.107:CONNECT_PORT shell pm list packages -3
```
Observed:
```text
package:com.tukios.silk
package:com.iniro.onstartapps
package:com.tv.launchapp
```

Query ABIs:
```cmd
adb -s 192.168.2.107:CONNECT_PORT shell getprop ro.product.cpu.abilist
```
Observed: `armeabi-v7a,armeabi`.

Inspect and launch:
```cmd
adb -s 192.168.2.107:CONNECT_PORT shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER -p com.tukios.silk
adb -s 192.168.2.107:CONNECT_PORT shell am start -n com.tukios.silk/.MainActivity
```

If install fails with **No such file or directory**, check the actual APK path before retrying. In PowerShell:
```powershell
Get-ChildItem "$env:USERPROFILE\Downloads" -Filter '*.apk'
```

## GitHub cloud build and installation

Repository: https://github.com/ZHomeSlice/Direct-Delivery-Video-Tribute-Launcher

- GitHub Actions workflow: `.github/workflows/build-apk.yml`
- Build command: `gradle --no-daemon assembleDebug`
- Build environment: Java 17, Android SDK 35, Android Gradle Plugin 8.7.3, Gradle 8.9.
- Download the `TributeAutoLaunch-debug-apk` artifact from a successful Actions run; unzip it to obtain `app-debug.apk`.
- First confirmed **successful cloud build**: https://github.com/ZHomeSlice/Direct-Delivery-Video-Tribute-Launcher/actions/runs/42892886407
- During cloud build setup, the earlier `android-actions/setup-android@v3` configuration failed with `Failed to find package 'tools'`. Removing that action avoided the obsolete package request, but the first revision then failed because `sdkmanager` was not on `PATH`. The successful workflow finds `sdkmanager` under `$ANDROID_HOME/cmdline-tools` explicitly.
- This is a **debug-signed prototype APK**, not a production-signed release. Preserve the signing key for future release upgrades.

Example installation from an already extracted APK in the current PowerShell directory:
```powershell
C:\platform-tools\adb.exe -s 192.168.2.107:CONNECT_PORT install -r .\app-debug.apk
```
Replace `CONNECT_PORT` with the current port shown by `adb devices`.

Open companion app:
```cmd
adb -s 192.168.2.107:CONNECT_PORT shell am start -n com.tribute.autolaunch/.MainActivity
```

In the companion app, click **Launch Tribute Videos now**. For wake monitoring, open **Settings → Accessibility** and enable **Tribute wake launcher** if it appears. Installation alone does **not** prove the accessibility service is enabled or that the boot/wake hooks work.

## Test plan / acceptance criteria

Record outcomes and logs on the **actual** onn. Android TV 14 device:

| Test | Expected behavior | Status |
| --- | --- | --- |
| APK installs via ADB | Install succeeds | User reported success; capture exact output when possible |
| Manual app button | Opens Tribute Videos | Not separately confirmed for companion APK |
| Cold power loss/recovery | Automatically opens Tribute Videos | **Not tested/confirmed** |
| Settings → Restart | Automatically opens Tribute Videos | **Not tested/confirmed** |
| Remote standby then wake | Automatically opens Tribute Videos | **Not tested/confirmed** |
| Press Home / Back | Exit remains possible; no immediate relaunch | **Not tested/confirmed** |
| Stay in another app without sleeping | No forced relaunch | **Not tested/confirmed** |
| Wake while another app was last visible | Tribute Videos comes forward | **Not tested/confirmed** |
| Network briefly unavailable | Graceful app launch/retry without crash | **Not tested/confirmed** |
| Real Roland HDMI input 2 | Receives compatible 480p video throughout operation | **Hardware problem unresolved** |

To diagnose boot/wake, inspect logcat:
```cmd
adb -s 192.168.2.107:CONNECT_PORT logcat -d -s TributeAutoLaunch ActivityTaskManager AndroidRuntime
```
Watch for activity-start denial, security exceptions, app crashes, and accessibility-service connection. If the screen never emits standard screen-on events, adapt wake detection for the actual device, rather than polling and stealing focus.

## Prior safety / download pitfalls

- A previous attempt to install Google's platform-tools with `winget install --id Google.PlatformTools -e` returned `0x80190194 (404 Not Found)`; downloading from Google's official ZIP URL worked.
- An APK download page displayed misleading large ad buttons, resulting in unrelated `AceSetup.exe` downloads rather than the Android APK. **Do not execute those files.**
- The user's APK file initially appeared only after downloading GitHub Actions' ZIP artifact and extracting it, not as a directly downloadable standalone APK.
- Do not assume an installed app is compatible or authorized merely because APK sideloading returns `Success`.
- This app handles tribute content. Do not add unnecessary data collection, network access, or accessibility reading.

## Developer next steps

1. Reproduce the successful `LEANBACK_LAUNCHER` launch and confirm the target app's manifest.
2. Test whether the compiled prototype launches the app on cold boot and wake on Android TV 14; collect `logcat` failures if not.
3. Design and implement a supported application-side auto-launch strategy; explicitly preserve Home/Back freedom.
4. Add logging, configuration, and repeatable automated/manual acceptance checks.
5. Validate TV power-cycling and HDMI resolution independently. A software launcher is not a resolution converter.
6. Produce a production-signed APK with stable versioning and document upgrade/rollback procedures.
