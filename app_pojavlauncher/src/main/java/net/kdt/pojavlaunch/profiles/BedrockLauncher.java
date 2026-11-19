package net.kdt.pojavlaunch.profiles;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import net.kdt.pojavlaunch.R;

/**
 * Starts Minecraft Bedrock for Android. Bedrock cannot run inside this launcher, so this opens Mojang's own
 * app: the free "Minecraft Trial" for demo/local accounts, the full game for accounts that own it.
 * If neither is installed it opens the Trial's Google Play page.
 */
public final class BedrockLauncher {
    private BedrockLauncher() {}

    public static final String FULL = "com.mojang.minecraftpe";
    public static final String TRIAL = "com.mojang.minecrafttrialpe";

    public static void launch(Context context, boolean preferTrial) {
        String[] order = preferTrial ? new String[]{TRIAL, FULL} : new String[]{FULL, TRIAL};
        PackageManager pm = context.getPackageManager();
        for (String pkg : order) {
            Intent intent = pm.getLaunchIntentForPackage(pkg);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
        }
        Toast.makeText(context, R.string.diamond_bedrock_missing, Toast.LENGTH_LONG).show();
        openStore(context, TRIAL);
    }

    private static void openStore(Context context, String pkg) {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(market);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkg))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
