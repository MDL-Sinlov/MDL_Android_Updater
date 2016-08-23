package mdl.sinlov.updater;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;


/**
 * <pre>
 *     sinlov
 *
 *     /\__/\
 *    /`    '\
 *  ≈≈≈ 0  0 ≈≈≈ Hello world!
 *    \  --  /
 *   /        \
 *  /          \
 * |            |
 *  \  ||  ||  /
 *   \_oo__oo_/≡≡≡≡≡≡≡≡o
 *
 * </pre>
 * Created by "sinlov" on 16/6/20.
 */
public class UpdaterDownloadCallBack {
    /**
     * default of priority set is 1000
     */
    public static final int DEFAULT_PRIORITY = 1000;
    private static final String DEFAULT_DOWNLOAD_CLICK_STRING = "Downloading...";
    private Context context = null;
    private UpdateDownloadReceiver updateDownloadReceiver;
    private final IntentFilter downloadIntentFilter;
    private OnDownloadComplete onDownloadComplete;
    private static OnNotificationClicked onNotificationClicked;
    private static String downloadNotificationClickStr = DEFAULT_DOWNLOAD_CLICK_STRING;

    public static void setDownloadNotificationClickStr(String downloadNotificationClickStr) {
        UpdaterDownloadCallBack.downloadNotificationClickStr = downloadNotificationClickStr;
    }

    public static void setOnNotificationClicked(OnNotificationClicked onNotificationClicked) {
        UpdaterDownloadCallBack.onNotificationClicked = onNotificationClicked;
    }

    public void setOnDownloadComplete(OnDownloadComplete onDownloadComplete) {
        this.onDownloadComplete = onDownloadComplete;
    }

    public void start() {
        context.registerReceiver(updateDownloadReceiver, downloadIntentFilter);
    }

    public void stop() {
        context.unregisterReceiver(updateDownloadReceiver);
    }

    public UpdaterDownloadCallBack(Context context) {
        this.context = context.getApplicationContext();
        downloadIntentFilter = new IntentFilter();
        downloadIntentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadIntentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        downloadIntentFilter.setPriority(DEFAULT_PRIORITY);
        this.updateDownloadReceiver = new UpdateDownloadReceiver();
    }

    public class UpdateDownloadReceiver extends BroadcastReceiver {

        private DownloadManager downloadManager;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                String path = filterByDownloadID(context, intent);
                String withOutSDPath = filterWithOutSDCard(context, path);
                if (null != onDownloadComplete) {
                    if (!TextUtils.isEmpty(path)) {
                        onDownloadComplete.filePath(path);
                    } else {
                        if (!TextUtils.isEmpty(withOutSDPath)) {
                            onDownloadComplete.filePath(withOutSDPath);
                        } else {
                            new IllegalArgumentException("install path not founding, are you delete it ?").printStackTrace();
                        }
                    }
                }
            } else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                if (null == onNotificationClicked) {
                    Toast.makeText(context, downloadNotificationClickStr, Toast.LENGTH_SHORT).show();
                } else {
                    onNotificationClicked.onClick(context, intent);
                }
            }
        }

        private String filterWithOutSDCard(Context context, String path) {
            if (null == path) {
                return "";
            }
            String result = null;
            Cursor cursor;
            int columnCount;
            cursor = context.getContentResolver().query(Uri.parse(path), null, null, null, null);
            if (null != cursor) {
                columnCount = cursor.getColumnCount();
                while (cursor.moveToNext()) {
                    for (int j = 0; j < columnCount; j++) {
                        String columnName = cursor.getColumnName(j);
                        String string = cursor.getString(j);
                        if (string != null) {
                            result = columnName + ": " + string;
                        } else {
//                            ALog.d("result null");
                        }
                    }
                }
                cursor.close();
            }
            return result;
        }

        private String filterByDownloadID(Context context, Intent intent) {
            long id = Updater.getInstance().getPreferences().getLong(Updater.KEY_VERSION_DOWNLOAD_ID, 0);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cursor = downloadManager.query(query);
            int columnCount = cursor.getColumnCount();
            String path = null;
            while (cursor.moveToNext()) {
                for (int j = 0; j < columnCount; j++) {
                    String columnName = cursor.getColumnName(j);
                    String string = cursor.getString(j);
                    if (columnName.equals("local_uri")) {
                        path = string;
                    }
                    if (string == null) {
//                        ALog.d(columnName + ": null");
                    }
                }
            }
            cursor.close();
            return path;
        }
    }

    public interface OnDownloadComplete {
        void filePath(String path);
    }

    public interface OnNotificationClicked {
        void onClick(Context context, Intent intent);
    }
}
