# Diamond Elantra

A Minecraft: Java Edition launcher for Android with a Windows 11 style blue interface.

Diamond Elantra is a fork of [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)
by the PojavLauncher Team, licensed under the **GNU LGPL v3** (see `LICENSE`). Their copyright and
license notices are kept, and this fork is released under the same license.
It is not affiliated with Mojang or Microsoft.

## What is different from PojavLauncher
- Renamed to Diamond Elantra (app id `net.diamondelantra.launcher`).
- Windows 11 style dark theme with accent blue, Start-menu style tiles and a taskbar-style bottom bar.
- Vanilla and modded instances both work. Accounts without Java Edition get Minecraft's free demo mode
  (the game still launches with `--demo`), including Forge/Fabric instances on Minecraft 1.3 and newer.
- Local (offline) accounts count as demo accounts: the game launches with `--demo`, and demo accounts can download and run vanilla and Forge/Fabric 1.3+.
- A **Bedrock** tile opens Mojang's free Minecraft Trial (or Bedrock if installed) on Android.
- Demo accounts use the game's own `--demo` mode on every instance, including Forge and Fabric. There is no demo.jar any more.
- `retro-mod/` builds `beta.jar` and `alpha.jar` (Fabric 1.20.1, no sprinting, no attack cooldown, old title). The Alpha and Beta home-screen buttons set up a hidden Fabric 1.20.1 instance with the retro jar with the `--demo` limits for demo accounts.
- The launcher background is an animated space scene (`SpaceBackgroundView`: drifting nebulae, parallax stars, a ringed planet, shooting stars).
- The **Boost** button installs FerriteCore, ModernFix, Lithium and Entity Culling (from Modrinth) on the selected Fabric 1.20.1 instance and lowers the render distance.
- A "modded only" switch exists in `ModdedOnly.java` (`ENFORCE`, off by default).
- Wiki, Discord and Install .jar buttons were removed from the home screen.

## Getting the APK
Every push to `main` builds an APK with GitHub Actions. Open the **Releases** page and download
`DiamondElantra.apk` from the newest build, then install it on your Android device.

## Where to change things
- `ModdedOnly.java`: decides what counts as modded
- `LauncherActivity.java`: launch guard (the toast when a non-modded profile is selected)
- `res/drawable/diamond_button_background.xml` and `res/values/colors.xml`: the blue
- `res/layout/fragment_launcher.xml` and `MainMenuFragment.java`: home screen
