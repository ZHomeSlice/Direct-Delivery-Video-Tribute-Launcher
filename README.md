# Tribute Auto Launch

Prototype Android TV 14 app for an onn. 4K Google TV that opens Tribute Videos (`com.tukios.silk/.MainActivity`) on boot and wake. This project needs device testing because Android 14 may restrict background activity starts.

## Build APK

Open **Actions → Build Tribute Auto Launch APK → Run workflow**. After a successful run, download the **TributeAutoLaunch-debug-apk** artifact. Install its `app-debug.apk` with ADB.

## On the TV

Open **Tribute Auto Launch**, test **Launch Tribute Videos now**, then enable **Tribute wake launcher** in **Settings → Accessibility**. Test cold boot, standby wake, and Home/Back manually.

This app doesn't read Accessibility screen contents or lock users into Tribute Videos.

## Current status

First automated cloud build configured. Boot and wake behavior must be tested on the actual Android TV 14 device.
