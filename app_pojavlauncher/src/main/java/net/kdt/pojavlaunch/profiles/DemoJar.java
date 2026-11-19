package net.kdt.pojavlaunch.profiles;

import android.content.Context;
import android.util.Log;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Forge has no demo mode of its own, so demo accounts get demo.jar (a small Forge mod that applies the
 * demo limits) copied into the instance's mods folder before every launch. Accounts that own the game
 * get it removed again so they are not limited.
 */
public final class DemoJar {
    private DemoJar() {}

    private static final String ASSET = "components/demo/demo.jar";
    private static final String TAG = "DemoJar";

    public static void sync(Context context, boolean demoAccount, MinecraftProfile profile, String versionId) {
        try {
            Tools.switchDemo(demoAccount);
            File modsDir = new File(Tools.getGameDirPath(profile), "mods");
            File target = new File(modsDir, "demo.jar");

            if (demoAccount && isForge1201(versionId)) {
                if (!modsDir.isDirectory() && !modsDir.mkdirs()) return;
                try (InputStream in = context.getAssets().open(ASSET)) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] chunk = new byte[8192];
                    int read;
                    while ((read = in.read(chunk)) != -1) buffer.write(chunk, 0, read);
                    byte[] data = buffer.toByteArray();
                    if (!target.isFile() || target.length() != data.length) {
                        try (OutputStream out = new FileOutputStream(target)) {
                            out.write(data);
                        }
                    }
                }
            } else if (target.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                target.delete();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Could not sync demo.jar", t);
        }
    }

    /** The bundled demo.jar is built for Forge on Minecraft 1.20.1. */
    public static boolean isForge1201(String versionId) {
        if (versionId == null) return false;
        String id = versionId.toLowerCase(Locale.ROOT);
        return id.contains("forge") && !id.contains("neoforge") && id.contains("1.20.1");
    }
}
