package net.kdt.pojavlaunch.profiles;

import android.app.Activity;
import android.widget.Toast;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * The home screen's "Boost" button. On the selected Fabric 1.20.1 instance it downloads FerriteCore, ModernFix,
 * Lithium and Entity Culling from Modrinth into the mods folder and lowers the render distance, so the game
 * uses less RAM. Sodium is left out on purpose: it needs a newer OpenGL than this launcher's renderers give.
 */
public final class PerformanceBoost {
    private PerformanceBoost() {}

    private static final String[] SLUGS = {"ferrite-core", "modernfix", "lithium", "entityculling"};

    public static void run(Activity activity) {
        MinecraftProfile profile;
        try {
            profile = LauncherProfiles.getCurrentProfile();
        } catch (Throwable t) {
            toast(activity, "Pick an instance first.");
            return;
        }
        String id = profile.lastVersionId == null ? "" : profile.lastVersionId.toLowerCase(Locale.ROOT);
        if (!id.contains("fabric") || !id.contains("1.20.1")) {
            toast(activity, "Boost works on Fabric 1.20.1 instances. Select one first (Alpha and Beta count).");
            return;
        }
        File gameDir = Tools.getGameDirPath(profile);
        toast(activity, "Installing performance mods…");
        new Thread(() -> {
            int ok = 0;
            File mods = new File(gameDir, "mods");
            //noinspection ResultOfMethodCallIgnored
            mods.mkdirs();
            for (String slug : SLUGS) {
                try {
                    install(slug, mods);
                    ok++;
                } catch (Throwable t) {
                    android.util.Log.w("PerformanceBoost", "Failed " + slug, t);
                }
            }
            lowerDistances(new File(gameDir, "options.txt"));
            String msg = ok == SLUGS.length ? "Boost installed. Render distance lowered to 8."
                    : "Boost installed " + ok + " of " + SLUGS.length + " mods. Check your connection and try again.";
            toast(activity, msg);
        }).start();
    }

    private static void install(String slug, File mods) throws Exception {
        String url = "https://api.modrinth.com/v2/project/" + slug + "/version"
                + "?game_versions=%5B%221.20.1%22%5D&loaders=%5B%22fabric%22%5D";
        JSONArray versions = new JSONArray(DownloadUtils.downloadString(url));
        if (versions.length() == 0) throw new Exception("no version for " + slug);
        JSONArray files = versions.getJSONObject(0).getJSONArray("files");
        JSONObject file = files.getJSONObject(0);
        for (int i = 0; i < files.length(); i++) {
            if (files.getJSONObject(i).optBoolean("primary")) { file = files.getJSONObject(i); break; }
        }
        File target = new File(mods, "boost-" + slug + ".jar");
        File temp = new File(mods, "boost-" + slug + ".part");
        DownloadUtils.downloadFile(file.getString("url"), temp);
        if (target.exists()) //noinspection ResultOfMethodCallIgnored
            target.delete();
        if (!temp.renameTo(target)) throw new Exception("rename failed");
    }

    /** Set renderDistance and simulationDistance to 8 in options.txt, keeping every other setting. */
    private static void lowerDistances(File options) {
        try {
            StringBuilder out = new StringBuilder();
            boolean rd = false, sd = false;
            if (options.isFile()) {
                for (String line : Tools.read(options.getAbsolutePath()).split("\n")) {
                    if (line.startsWith("renderDistance:")) { line = "renderDistance:8"; rd = true; }
                    else if (line.startsWith("simulationDistance:")) { line = "simulationDistance:8"; sd = true; }
                    if (!line.isEmpty()) out.append(line).append('\n');
                }
            }
            if (!rd) out.append("renderDistance:8\n");
            if (!sd) out.append("simulationDistance:8\n");
            try (OutputStream os = new FileOutputStream(options)) {
                os.write(out.toString().getBytes("UTF-8"));
            }
        } catch (Throwable t) {
            android.util.Log.w("PerformanceBoost", "options.txt", t);
        }
    }

    private static void toast(Activity activity, String text) {
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_LONG).show());
    }
}
