# Diamond Elantra

A Minecraft: Java Edition launcher for Android that **only launches modded instances**
(Fabric, Forge, Quilt, OptiFine and modpacks), with a simplified blue home screen.

Diamond Elantra is a fork of [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)
by the PojavLauncher Team, licensed under the **GNU LGPL v3** (see `LICENSE`). Their copyright and
license notices are kept, and this fork is released under the same license.
It is not affiliated with Mojang or Microsoft.

## What is different from PojavLauncher
- Renamed to Diamond Elantra (app id `net.diamondelantra.launcher`).
- Vanilla profiles are hidden and cannot be launched. The "Create new profile" screen has no vanilla option.
- Home screen: three plain blue buttons (Controls, Logs, Folder), the profile picker and a big blue PLAY button.
  Wiki, Discord and Install .jar buttons were removed. All other buttons use the same blue style.

## Getting the APK
Every push to `main` builds an APK with GitHub Actions. Open the **Releases** page and download
`DiamondElantra.apk` from the newest build, then install it on your Android device.

## Where to change things
- `ModdedOnly.java`: decides what counts as modded
- `LauncherActivity.java`: launch guard (the toast when a non-modded profile is selected)
- `res/drawable/diamond_button_background.xml` and `res/values/colors.xml`: the blue
- `res/layout/fragment_launcher.xml` and `MainMenuFragment.java`: home screen
