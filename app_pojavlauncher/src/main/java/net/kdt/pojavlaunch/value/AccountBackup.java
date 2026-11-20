package net.kdt.pojavlaunch.value;

import android.os.Environment;
import android.util.Log;

import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Keeps a visible copy of every account file in Documents/DiamondElantra/accounts so it survives uninstalling the
 * app and can be copied to another Diamond Elantra install. The copy holds the same data as the private
 * accounts folder, sign-in tokens included, so other apps with storage access can read it.
 * Everything here is best effort: if the folder can't be written, the app carries on as before.
 */
public final class AccountBackup {
    private AccountBackup() {}

    private static final String TAG = "AccountBackup";

    public static File backupDir() {
        return new File(Environment.getExternalStorageDirectory(), "Documents/DiamondElantra/accounts");
    }

    /** Copy one saved account file into the visible folder. */
    public static void mirror(String username) {
        try {
            File src = new File(Tools.DIR_ACCOUNT_NEW, username + ".json");
            if (!src.isFile()) return;
            File dir = backupDir();
            if (!dir.isDirectory() && !dir.mkdirs()) return;
            copy(src, new File(dir, username + ".json"));
        } catch (Throwable t) {
            Log.w(TAG, "Could not back up account", t);
        }
    }

    /**
     * Bring accounts back from the visible folder: any account file there that the app does not have yet is
     * copied in. Covers a reinstall and moving to another Diamond Elantra.
     */
    public static void restoreMissing() {
        try {
            File[] files = backupDir().listFiles((d, n) -> n.endsWith(".json"));
            if (files == null) return;
            File accounts = new File(Tools.DIR_ACCOUNT_NEW);
            if (!accounts.isDirectory() && !accounts.mkdirs()) return;
            for (File f : files) {
                File target = new File(accounts, f.getName());
                if (!target.exists()) copy(f, target);
            }
            // Accounts that exist only inside the app (made before this backup existed) get a visible copy too.
            File[] own = accounts.listFiles((d, n) -> n.endsWith(".json"));
            if (own != null) for (File f : own) mirror(f.getName().substring(0, f.getName().length() - 5));
        } catch (Throwable t) {
            Log.w(TAG, "Could not restore accounts", t);
        }
    }

    private static void copy(File from, File to) throws Exception {
        try (InputStream in = new FileInputStream(from); OutputStream out = new FileOutputStream(to)) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }
    }
}
