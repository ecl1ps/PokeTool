package com.yuralex.poketool.updater;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.widget.Toast;

import com.yuralex.poketool.R;

import java.io.File;

public class AppUpdateDialog {
    public static void downloadAndInstallAppUpdate(final Context context, AppUpdate update) {
        try {
            final String destination = context.getExternalFilesDir(null) + "/" + "update.apk";
            final Uri uri = Uri.parse("file://" + destination);

            //Delete update file if exists
            final File file = new File(destination);
            if (file.exists()) {
                file.delete();
            }

            //set downloadmanager
            final String url = update.assetUrl;
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(context.getString(R.string.updating_to, update.version));

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    File fileDestination = new File(destination);

                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.setDataAndType(Uri.fromFile(fileDestination), manager.getMimeTypeForDownloadedFile(downloadId));
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        ctxt.startActivity(install);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(ctxt, "No application for this type of file.", Toast.LENGTH_LONG).show();
                    }

                    ctxt.unregisterReceiver(this);
                }
            };
            //register receiver for when .apk download is complete
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            System.out.println("We have an error houston");
        }
    }
}
