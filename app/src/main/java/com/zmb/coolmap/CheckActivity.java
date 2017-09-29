package com.zmb.coolmap;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Process;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;

public class CheckActivity extends AppCompatActivity {
    private final String TAG = CheckActivity.class.getSimpleName();
    private BaiduSDKREceiver baiduSDKREceiver;
    private int isFirst = 0;
    private Context context = null;
    ConnReceiver connRev = null;

    private class ConnReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            checkNetworkState();
        }
    }
    private class BaiduSDKREceiver extends BroadcastReceiver{

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String s = intent.getAction();
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Log.d(TAG, "onReceive: ak error");
                AlertDialog.Builder builder = new AlertDialog.Builder(CheckActivity.this);
                builder.setPositiveButton(R.string.key_check_pos_str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String AKcode = null;
                        ApplicationInfo applicationInfo = null;
                        try {
                            applicationInfo = getPackageManager().getApplicationInfo(CheckActivity.this.context.getPackageName(),PackageManager.GET_META_DATA);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        if(applicationInfo != null){
                            AKcode = applicationInfo.metaData.getCharSequence("com.baidu.lbsapi.API_KEY","no ak config").toString();
                        }
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT,"AK code error:"+AKcode);
                        startActivity(sendIntent);
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.key_check_neg_str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent1 = new Intent(CheckActivity.this, CoolMapActivity.class);
                        startActivity(intent1);
                        finish();
                    }
                });
                builder.setTitle(R.string.key_check_title_str);
                builder.setMessage(R.string.ak_check_msg);
                AlertDialog dialog = builder.create();
                dialog.show();

            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Log.d(TAG, "onReceive: ");
                Intent intent1 = new Intent(CheckActivity.this, CoolMapActivity.class);
                startActivity(intent1);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        this.context = this.getBaseContext();
        IntentFilter netFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connRev = new ConnReceiver();
        registerReceiver(connRev,netFilter);
        SharedPreferences sp = getSharedPreferences(getPackageName(),MODE_APPEND);
        int lastpid = sp.getInt(Config.LASTPID,0);
        int currentpid = Process.myPid();
        SharedPreferences.Editor e = sp.edit();
        e.clear().commit();
        e.putInt(Config.PREPID,lastpid);
        e.putInt(Config.LASTPID,currentpid);
        e.commit();
        checkNetworkState();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isFirst == 1)
            unregisterReceiver(baiduSDKREceiver);
            unregisterReceiver(connRev);

    }
    private void getStart()
    {
        SharedPreferences sp = getSharedPreferences(getPackageName() , MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        int prepid,currentpid;
        prepid = sp.getInt(Config.PREPID,0);
        currentpid = sp.getInt(Config.LASTPID,0);
        Log.d(TAG, "getStart: "+currentpid+":"+prepid);
        if( prepid != currentpid){

            isFirst = 1;
            baiduSDKREceiver = new BaiduSDKREceiver();
            IntentFilter intentFilter  = new IntentFilter();
            intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
            intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
            intentFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
            registerReceiver(baiduSDKREceiver,intentFilter);
            e.clear().commit();
            e.putInt("versioncode",Process.myPid()).commit();

        }else {
            Intent intent1 = new Intent(CheckActivity.this, CoolMapActivity.class);
            startActivity(intent1);
            finish();
        }
    }

    private void checkNetworkState()
    {
        ConnectivityManager connectionService = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = connectionService.getActiveNetworkInfo();
        if(nInfo == null){
            Log.d(TAG, "checkNetworkState: nInfo null");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setPositiveButton(R.string.internet_check_pos_str, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent connintent = new Intent();
                    connintent.setAction(Settings.ACTION_SETTINGS);
                    startActivity(connintent);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.internet_check_net_str, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent1 = new Intent(CheckActivity.this, CoolMapActivity.class);
                    startActivity(intent1);
                    finish();
                }
            });
            builder.setTitle(R.string.key_check_title_str);
            builder.setMessage(R.string.internet_check_msg);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
           getStart();}

    }
}
