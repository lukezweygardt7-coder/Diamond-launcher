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
 * Forge and Fabric have no demo mode of their own, so demo accounts get demo.jar (a small mod that applies
 * strict demo limits) copied into the instance's mods folder before every launch. The vanilla --demo flag
 * stays on as well, so removing the jar never unlocks the full game. Accounts that own the game get the
 * jar removed again so they are not limited.
 */
public final class DemoJar {
    private DemoJar() {}

    private static final String DIR = "components/demo/";
    private static final String TAG = "DemoJar";

    public static void sync(Context context, boolean demoAccount, MinecraftProfile profile, String versionId) {
        try {
            Tools.switchDemo(demoAccount);
            File modsDir = new File(Tools.getGameDirPath(profile), "mods");
            File target = new File(modsDir, "demo.jar");
            File fabricApi = new File(modsDir, "fabric-api.jar");

            boolean forge = isForge1201(versionId);
            boolean fabric = isFabric1201(versionId);

            String retro = RetroProfiles.modeOf(profile);
            File retroJar = new File(modsDir, "retro.jar");

            if (retro != null && fabric) {
                // Alpha / Beta instance: the retro-feel mod always, demo.jar and Fabric API as needed.
                if (!modsDir.isDirectory() && !modsDir.mkdirs()) return;
                copyAsset(context, DIR + retro + ".jar", retroJar);
                if (!hasFabricApi(modsDir)) copyAsset(context, DIR + "fabric-api.jar", fabricApi);
                if (demoAccount) copyAsset(context, DIR + "demo-fabric.jar", target);
                else if (target.isFile()) { //noinspection ResultOfMethodCallIgnored
                    target.delete(); }
            } else if (demoAccount && (forge || fabric)) {
                if (!modsDir.isDirectory() && !modsDir.mkdirs()) return;
                copyAsset(context, DIR + (forge ? "demo-forge.jar" : "demo-fabric.jar"), target);
                if (fabric) {
                    // Pojav's Fabric installer does not include Fabric API, which the Fabric demo mod needs.
                    if (!hasFabricApi(modsDir)) copyAsset(context, DIR + "fabric-api.jar", fabricApi);
                } else if (fabricApi.isFile()) {
                    //noinspection ResultOfMethodCallIgnored
                    fabricApi.delete();
                }
            } else {
                if (target.isFile()) {
                    //noinspection ResultOfMethodCallIgnored
                    target.delete();
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Could not sync demo.jar", t);
        }
    }

    private static boolean hasFabricApi(File modsDir) {
        File[] files = modsDir.listFiles();
        if (files == null) return false;
        for (File f : files) {
            String n = f.getName().toLowerCase(Locale.ROOT);
            if (n.startsWith("fabric-api") && n.endsWith(".jar") && f.length() > 0) return true;
        }
        return false;
    }

    private static void copyAsset(Context context, String asset, File target) throws Exception {
        try (InputStream in = context.getAssets().open(asset)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) buffer.write(chunk, 0, read);
            try (OutputStream out = new FileOutputStream(target)) {
                out.write(buffer.toByteArray());
            }
        }
    }

    /** The bundled Forge demo.jar is built for Forge on Minecraft 1.20.1. */
    public static boolean isForge1201(String versionId) {
        if (versionId == null) return false;
        String id = versionId.toLowerCase(Locale.ROOT);
        return id.contains("forge") && !id.contains("neoforge") && id.contains("1.20.1");
    }

    /** The bundled Fabric demo.jar is built for Fabric on Minecraft 1.20.1. */
    public static boolean isFabric1201(String versionId) {
        if (versionId == null) return false;
        String id = versionId.toLowerCase(Locale.ROOT);
        return id.contains("fabric") && id.contains("1.20.1");
    }
}
