package mdl.sinlov.updater;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

/**
 * for updater of android application
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
public class Updater {

    public static final String KEY_VERSION_DOWNLOAD_ID = "mdl:update:key:download_id";
    private static final String TAG = "mdl_update";
    private static final String UPDATE_DOWNLOAD_PATH = "update_download";
    private static final String KEY_VERSION_DOWNLOAD_URL = "mdl:update:key:download_url";
    private static final String KEY_VERSION_CODE = "mdl:update:key:version_code";
    private static final String KEY_DOWNLOAD_VERSION_CODE =  "mdl:download:key:version_code";
    private static final String KEY_DOWNLOAD_COMPLETE_PATH = "mdl:download:key:complete:path";
    private static final String MATCHES_RES_URL = "^https?://(([a-zA-Z0-9_-])+(\\.)?)*(:\\d+)?(/((\\.)?(\\?)?=?&?[a-zA-Z0-9_-](\\?)?)*)*$";
    private static Updater instance;
    private Application application;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editorShared;
    private IUpdater iUpdater;
    private UpdaterDownloadCallBack updaterDownloadCallBack;
    private PackageManager packageManger;
    private DownloadManager downloadManager;
    private boolean isForciblyUpdate;
    private int updateVC;
    private String updatePN;
    private String updateURL;
    private boolean canUpdate;
    private boolean isMobileUpdate = false;
    private String installPath;
    private String tvTitleUI = "Update";
    private String btnUpdateUI = "Ok";

    public synchronized static Updater getInstance() {
        if (null == instance) {
            instance = new Updater();
            return instance;
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public boolean isCanUpdate() {
        return canUpdate;
    }

    public boolean isForciblyUpdate() {
        return isForciblyUpdate;
    }

    public void setForciblyUpdate(boolean forciblyUpdate) {
        isForciblyUpdate = forciblyUpdate;
    }

    public boolean isMobileUpdate() {
        return isMobileUpdate;
    }

    public void init(Application application, UpdateSetting updateSetting) {
        this.application = application;
        this.preferences = application.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        this.editorShared = preferences.edit();
        this.editorShared.apply();
        this.updaterDownloadCallBack = new UpdaterDownloadCallBack(application);
        updaterDownloadCallBack.setOnDownloadComplete(new UpdaterDownloadCallBack.OnDownloadComplete() {
            @Override
            public void filePath(String path) {
                Log.i(TAG, "OnDownloadComplete: " + path);
                installPath = path;
                //clear cache;
                editorShared.remove(KEY_DOWNLOAD_COMPLETE_PATH);
                editorShared.remove(KEY_DOWNLOAD_VERSION_CODE);
                editorShared.commit();

                editorShared.putString(KEY_DOWNLOAD_COMPLETE_PATH , path);
                editorShared.putInt(KEY_DOWNLOAD_VERSION_CODE, updateVC);
                editorShared.apply();
                installAPKByDownload();
            }
        });
        updaterDownloadCallBack.start();
        this.iUpdater = updateSetting;
        downloadManager = (DownloadManager) application.getSystemService(Context.DOWNLOAD_SERVICE);
        this.packageManger = application.getPackageManager();
        ICheckUpdate iCheckUpdate = updateSetting.getCheckUpdate();
        this.isForciblyUpdate = iCheckUpdate.isForciblyUpdate();
        String packageName = iCheckUpdate.updatePN();
        if (TextUtils.isEmpty(packageName)) {
            new IllegalArgumentException(" you are not setting package name, your update will fail ").printStackTrace();
            this.updatePN = "";
        } else {
            this.updatePN = packageName;
        }
        this.updateVC = iCheckUpdate.updateVC();
        String url = iCheckUpdate.updateURL();
        if (TextUtils.isEmpty(url)) {
            new IllegalArgumentException("you update url is error, please check! ").printStackTrace();
            this.updateURL = "";
        } else {
            this.updateURL = url;
        }
        checkUpdate();
    }

    public void setCustomUpdater(IUpdater iUpdater) {
        this.iUpdater = iUpdater;
    }

    public void setMobileUpdate(boolean mobileUpdate) {
        isMobileUpdate = mobileUpdate;
    }

    public void update(boolean isSilentUpdate) {
        if (null != iUpdater) {
            if (canUpdate) {
                if (isForciblyUpdate) {
                    iUpdater.forciblyUpdate();
                } else {
                    if (isSilentUpdate) {
                        iUpdater.silentUpdate();
                    } else {
                        iUpdater.update();
                    }
                }
            } else {
                iUpdater.noUpdate();
            }
        } else {
            new NullPointerException("your Updater is null").printStackTrace();
        }
    }

    public boolean installAPKByDownload() {
        String cacheApkPath = preferences.getString(KEY_DOWNLOAD_COMPLETE_PATH,"");
        int versionCode     = preferences.getInt(KEY_DOWNLOAD_VERSION_CODE, 0);

        if (TextUtils.isEmpty(cacheApkPath) || versionCode != updateVC || !new File(Uri.parse(cacheApkPath).getPath()).exists()) {
            new NullPointerException("your install path is empty or version not equal!").printStackTrace();
            return false;
        } else {
            DefaultPackageInstaller.installApk(application, cacheApkPath);
            return true;
        }
    }
    private PackageInfo getOnePackageInfo(PackageManager packageManager, String packageName) {
        try {
            return packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "app not found |packageName: " + e.getMessage());
            return null;
        }
    }

    private boolean checkUpdate() {
        PackageInfo ownPackageInfo = getOnePackageInfo(packageManger, application.getPackageName());
        if (null != ownPackageInfo) {
            if (TextUtils.isEmpty(updatePN)) {
                canUpdate = ownPackageInfo.versionCode < updateVC;
            } else {
                canUpdate = ownPackageInfo.packageName.equals(updatePN) && ownPackageInfo.versionCode < updateVC;
            }
        } else {
            new NullPointerException("get packageInfo error ").printStackTrace();
        }
        return canUpdate;
    }

    private void saveDownload2SharedPreferences(int versionCode, String url, long downID) {
        editorShared.putInt(KEY_VERSION_CODE, versionCode);
        editorShared.putString(KEY_VERSION_DOWNLOAD_URL, url);
        editorShared.putLong(KEY_VERSION_DOWNLOAD_ID, downID);
        editorShared.apply();
    }

    private DownloadManager.Request filterDownloadSetting(String updateURL) {
        Uri uri = Uri.parse(updateURL);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        if (isMobileUpdate) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        } else {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }
        request.setDestinationInExternalFilesDir(application, null, UPDATE_DOWNLOAD_PATH);
        return request;
    }

    private String checkUpdateArgument() {
        if (TextUtils.isEmpty(updateURL)) {
            new IllegalArgumentException("you update url is error, update cancel!\nURL: " + updateURL).printStackTrace();
            return "";
        } else {
            if (!updateURL.trim().matches(MATCHES_RES_URL)) {
                new IllegalArgumentException("you update url is not a resource url, please check !\nURL: " + updateURL).printStackTrace();
                return "";
            } else {
                return updateURL;
            }
        }
    }


    private void updateOwnSilent(boolean isVisible, int updateVC, String updatePN) {
        String url = checkUpdateArgument();
        if (!url.equals("")) {
            DownloadManager.Request request = filterDownloadSetting(url);
            request.setVisibleInDownloadsUi(isVisible);
            long id = downloadManager.enqueue(request);
            saveDownload2SharedPreferences(updateVC, updatePN, id);
        }
    }

    public void updateOwnSilent() {
        updateOwnSilent(false, updateVC, updatePN);
    }

    public void updateOwn() {
        updateOwnSilent(true, updateVC, updatePN);
    }

    /**
     * This method do not show in MIUI and some ROM
     */
    @Deprecated
    public void updateForciblyUI() {
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        final WindowManager mWindowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        wmParams.format = 1;
        wmParams.flags = 1280;
        wmParams.gravity = 49;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = -1;
        wmParams.height = -1;
        final LinearLayout infoLayout = new LinearLayout(application);
        ViewGroup.LayoutParams mmLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams mwLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoLayout.setLayoutParams(mmLayoutParams);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setBackgroundColor(Color.TRANSPARENT);
        infoLayout.setGravity(17);
        TextView tvTitle = new TextView(application);
        tvTitle.setLayoutParams(mwLayoutParams);
        tvTitle.setText(tvTitleUI);
        tvTitle.setGravity(17);
        tvTitle.setBackgroundColor(Color.BLACK);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        infoLayout.addView(tvTitle);
        Button btnUpdate = new Button(application);
        btnUpdate.setLayoutParams(mwLayoutParams);
        btnUpdate.setText(btnUpdateUI);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowManager.removeView(infoLayout);
            }
        });
        infoLayout.addView(btnUpdate);
        mWindowManager.addView(infoLayout, wmParams);
    }

    private Updater() {
    }

}
