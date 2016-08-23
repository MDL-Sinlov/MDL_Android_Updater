package mdl.sinlov.android.updater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mdl.sinlov.updater.CheckUpdate;
import mdl.sinlov.updater.ICheckUpdate;
import mdl.sinlov.updater.UpdateSetting;
import mdl.sinlov.updater.Updater;
import mdl.sinlov.updater.UpdaterDownloadCallBack;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_main_result)
    TextView tvMainResult;
    @BindView(R.id.btn_check_update)
    Button btnCheckUpdate;
    @BindView(R.id.btn_update)
    Button btnUpdate;
    @BindView(R.id.btn_update_silent)
    Button btnUpdateSilent;
    @BindView(R.id.btn_update_forcibly)
    Button btnUpdateForcibly;
    @BindView(R.id.btn_change_download_click_string)
    Button btnChangeDownloadClickString;
    @BindView(R.id.btn_change_download_click_call_back)
    Button btnChangeDownloadClickCallBack;
    @BindView(R.id.btn_update_forcibly_ui)
    Button btnUpdateForciblyUi;

    private AlertDialog forciblyUI;
    private UpdateCheckSetting updateSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        ALog.initTag();
        updateSetting = new UpdateCheckSetting(
                new CheckUpdate(false, 2, "mdl.sinlov.android.updater", "http://192.168.1.107:8082/app-debug.apk"));
        Updater.getInstance().init(this.getApplication(), updateSetting);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    @OnClick({R.id.btn_check_update, R.id.btn_update, R.id.btn_update_silent, R.id.btn_update_forcibly,
            R.id.btn_update_forcibly_ui, R.id.btn_change_download_click_string, R.id.btn_change_download_click_call_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_check_update:
                if (Updater.getInstance().isCanUpdate()) {
                    Toast.makeText(MainActivity.this, "Need update", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No need to update", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_update:
                Updater.getInstance().update(false);
                break;
            case R.id.btn_update_silent:
                Updater.getInstance().update(true);
                break;
            case R.id.btn_update_forcibly:
                Updater.getInstance().setForciblyUpdate(true);
                Updater.getInstance().update(false);
                break;
            case R.id.btn_update_forcibly_ui:
                //TODO forcibly ui
                break;
            case R.id.btn_change_download_click_string:
                UpdaterDownloadCallBack.setDownloadNotificationClickStr("My click words");
                break;
            case R.id.btn_change_download_click_call_back:
                UpdaterDownloadCallBack.setOnNotificationClicked(new UpdaterDownloadCallBack.OnNotificationClicked() {
                    @Override
                    public void onClick(Context context, Intent intent) {
                        Toast.makeText(context, "My New Call Back", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    private void showForciblyUpdateUI() {
        if (null == forciblyUI) {
            forciblyUI = new AlertDialog.Builder(this)
                    .setTitle("Application need update")
                    .setMessage("please wait update download!")
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            canCloseDialog(dialog, false);
                            // installAPKByDownload return true is download success
                            if (!Updater.getInstance().installAPKByDownload()) {
                                Updater.getInstance().updateOwn();
                            }
                        }
                    })
                    .setCancelable(false)
                    .create();
            forciblyUI.setCanceledOnTouchOutside(false);
            forciblyUI.show();
        }
    }


    private class UpdateCheckSetting extends UpdateSetting {

        public UpdateCheckSetting(ICheckUpdate checkUpdate) {
            super(checkUpdate);
        }

        @Override
        public void update() {
            // base update
            Updater.getInstance().updateOwn();
        }

        @Override
        public void forciblyUpdate() {
            // you can show forcibly update UI at here
            showForciblyUpdateUI();
            Updater.getInstance().updateOwn();
        }

        @Override
        public void silentUpdate() {
            // you can show silent update UI at here
            Updater.getInstance().updateOwnSilent();
        }

        @Override
        public void noUpdate() {
            // no update you want do
            Toast.makeText(MainActivity.this, "No Update", Toast.LENGTH_SHORT).show();
        }
    }

    private static void canCloseDialog(DialogInterface dialogInterface, boolean close) {
        try {
            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialogInterface, close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
