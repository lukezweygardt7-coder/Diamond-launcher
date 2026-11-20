package net.kdt.pojavlaunch.profiles;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.modloaders.FabriclikeDownloadTask;
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.util.Map;

/**
 * The home screen's "Alpha" and "Beta" buttons. Each one is a hidden-Fabric instance on Minecraft 1.20.1 that
 * gets alpha.jar / beta.jar (a mod that gives the game the old version's feel) plus the usual demo.jar and
 * --demo limits for demo accounts. The player just sees one "Alpha Minecraft" / "Beta Minecraft" entry.
 */
public final class RetroProfiles {
    private RetroProfiles() {}

    private static final String GAME_VERSION = "1.20.1";
    private static final String LOADER_VERSION = "0.15.11";
    private static final String DIR_PREFIX = "retro-";

    /** @return "alpha" / "beta" if the profile is one of the retro instances, else null. */
    public static String modeOf(MinecraftProfile profile) {
        if (profile == null || profile.gameDir == null) return null;
        if (profile.gameDir.equals(DIR_PREFIX + "alpha")) return "alpha";
        if (profile.gameDir.equals(DIR_PREFIX + "beta")) return "beta";
        return null;
    }

    public static void select(Activity activity, String mode) {
        String label = mode.equals("alpha") ? "Alpha" : "Beta";
        Toast.makeText(activity, activity.getString(R.string.diamond_retro_installing, label), Toast.LENGTH_SHORT).show();
        ModloaderDownloadListener listener = new ModloaderDownloadListener() {
            @Override public void onDownloadFinished(File f) {
                activity.runOnUiThread(() -> {
                    String key = ensureProfile(mode, label);
                    LauncherPreferences.DEFAULT_PREF.edit().putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, key).apply();
                    ExtraCore.setValue(ExtraConstants.REFRESH_VERSION_SPINNER, key);
                    Toast.makeText(activity, activity.getString(R.string.diamond_retro_ready, label), Toast.LENGTH_LONG).show();
                });
            }
            @Override public void onDataNotAvailable() { failed(activity, label); }
            @Override public void onDownloadError(Exception e) { failed(activity, label); }
        };
        new Thread(new FabriclikeDownloadTask(listener, FabriclikeUtils.FABRIC_UTILS, GAME_VERSION, LOADER_VERSION, false)).start();
    }

    private static void failed(Activity activity, String label) {
        activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.diamond_retro_failed, label), Toast.LENGTH_LONG).show());
    }

    private static String ensureProfile(String mode, String label) {
        LauncherProfiles.load();
        for (Map.Entry<String, MinecraftProfile> e : LauncherProfiles.mainProfileJson.profiles.entrySet()) {
            if (mode.equals(modeOf(e.getValue()))) return e.getKey();
        }
        MinecraftProfile profile = new MinecraftProfile();
        profile.name = label + " Minecraft";
        profile.lastVersionId = "fabric-loader-" + LOADER_VERSION + "-" + GAME_VERSION;
        profile.gameDir = DIR_PREFIX + mode;
        String key = LauncherProfiles.getFreeProfileKey();
        LauncherProfiles.mainProfileJson.profiles.put(key, profile);
        LauncherProfiles.write();
        return key;
    }
}
