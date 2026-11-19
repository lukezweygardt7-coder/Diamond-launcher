package net.kdt.pojavlaunch.profiles;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.util.Locale;

/** Diamond Elantra only launches modded instances. This decides what counts as "modded". */
public final class ModdedOnly {
    private ModdedOnly() {}

    private static final String[] LOADER_HINTS = {
            "fabric", "forge", "quilt", "optifine", "neoforge", "liteloader", "babric", "bta"
    };

    public static boolean isModdedProfile(MinecraftProfile profile) {
        return profile != null && isModdedVersion(profile.lastVersionId);
    }

    public static boolean isModdedVersion(String versionId) {
        if (versionId == null) return false;
        if (MinecraftProfile.LATEST_RELEASE.equalsIgnoreCase(versionId)
                || MinecraftProfile.LATEST_SNAPSHOT.equalsIgnoreCase(versionId)) return false;

        String lower = versionId.toLowerCase(Locale.ROOT);
        for (String hint : LOADER_HINTS) {
            if (lower.contains(hint)) return true;
        }
        // Anything else that extends another version (inheritsFrom) is a mod loader / modpack version.
        try {
            JMinecraftVersionList.Version info = Tools.getVersionInfo(versionId, true);
            return info != null && info.inheritsFrom != null;
        } catch (Throwable t) {
            return false;
        }
    }
}
