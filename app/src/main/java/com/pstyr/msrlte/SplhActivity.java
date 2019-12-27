package com.pstyr.msrlte;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAdExtendedListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.onesignal.OneSignal;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplhActivity extends Activity {
    ImageView viw_image;
    Button viw_rate, viw_play;
    static String PACKAGE_NAME;
    public static String id_inter;
    public static String id_banner;
    public static String id_banner_content;
    public static String id_inter_content;
    public static String active_ads = "";
    public static String active_content = "";
    public static String admob_id;

    public static String st_move;
    public static String scd_key;
    public static String st_down;
    public static String sc = "";
    static String url_move;
    static String dtjson = "";
    public static String dt_icon;
    public static String dt_banner;
    public static String dt_title;
    public static String dt_desc;
    public static String st_popup;
    public InterstitialAd mInterstitialAd;
    public com.facebook.ads.InterstitialAd interstitialFb;
    public static int PERCODE = 1214;
    public static String folder;
    ShimmerFrameLayout shimmerFrameLayout;
    private AVLoadingIndicatorView playprogress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.splahsmain);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 1214);
            }
        }
        folder = Environment.getExternalStorageDirectory() + File.separator + "Download";

        MobileAds.initialize(this, admob_id);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        dtjson = getResources().getString(R.string.dtjson);

        shimmerFrameLayout=findViewById(R.id.shimmer_view_container);

        viw_image = (ImageView) findViewById(R.id.icon_id);
        viw_play = findViewById(R.id.play_id);
        viw_rate = findViewById(R.id.rate_id);

        viw_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateApps();
            }
        });

        if (CheckConnection()) {
            new FetchData().execute();
            SystemClock.sleep(1500);
            startApp();
        }
    }

    public void loadInter() {
        final ProgressDialog progressInter = new ProgressDialog(this);
//        progressInter.setIndeterminate(false);
//        progressInter.setCancelable(false);
//        progressInter.setMessage("Loading...");
        shimmerFrameLayout.startShimmer();

        if (active_ads.equals("fb")) {
            AudienceNetworkAds.initialize(this);
            interstitialFb = new com.facebook.ads.InterstitialAd(this, id_inter);
            interstitialFb.setAdListener(new InterstitialAdExtendedListener() {
                @Override
                public void onRewardedAdCompleted() {

                }

                @Override
                public void onRewardedAdServerSucceeded() {

                }

                @Override
                public void onRewardedAdServerFailed() {

                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    shimmerFrameLayout.stopShimmer();
                    Intent i = new Intent(SplhActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    interstitialFb.isAdLoaded();
                    shimmerFrameLayout.stopShimmer();
                    interstitialFb.show();
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }

                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    Intent i = new Intent(SplhActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onInterstitialActivityDestroyed() {

                }

            });
            interstitialFb.loadAd();
        } else {
            mInterstitialAd = new InterstitialAd(getApplicationContext());
            mInterstitialAd.setAdUnitId(SplhActivity.id_inter);
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    shimmerFrameLayout.stopShimmer();
                    mInterstitialAd.show();

                }

                @Override
                public void onAdFailedToLoad(int errorCode){
                    shimmerFrameLayout.stopShimmer();
                    Intent i = new Intent(SplhActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when the ad is displayed.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    Intent i = new Intent(SplhActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchData extends AsyncTask<Void, Void, Void> {

        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(dtjson);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while(line != null){
                    line = bufferedReader.readLine();
                    data = data + line;
                }
                try {
                    JSONObject json = new JSONObject(data);

                    admob_id = json.getString("admob_id");

                    st_move = json.getString("st_move");
                    url_move = json.getString("url_move");
                    st_popup = json.getString("st_popup");

                    JSONArray getParam = json.getJSONArray("dtjson");
                    for(int i = 0; i < getParam.length(); i++) {
                        if (PACKAGE_NAME.equals(getParam.getJSONObject(i).getString("package"))) {

                            st_down = getParam.getJSONObject(i).getString("st_down");
                            active_ads = getParam.getJSONObject(i).getString("ads_home");
                            active_content = getParam.getJSONObject(i).getString("ads_content");

                            scd_key = getParam.getJSONObject(i).getString("scd_key");

                            dt_title = getParam.getJSONObject(i).getString("dt_title");
                            dt_desc = getParam.getJSONObject(i).getString("dt_desc");
                            dt_icon = getParam.getJSONObject(i).getString("dt_icon");
                            dt_banner = getParam.getJSONObject(i).getString("dt_banner");

                            if (active_ads.equals("fb")) {
                                id_inter = getParam.getJSONObject(i).getString("fb_inter");
                                id_banner = getParam.getJSONObject(i).getString("fb_banner");
                            } else {
                                id_inter = getParam.getJSONObject(i).getString("ad_inter");
                                id_banner = getParam.getJSONObject(i).getString("ad_banner");
                            }

                            if (active_content.equals("fb")) {
                                id_inter_content = getParam.getJSONObject(i).getString("fb_inter");
                                id_banner_content = getParam.getJSONObject(i).getString("fb_banner");
                            } else {
                                id_inter_content = getParam.getJSONObject(i).getString("ad_inter");
                                id_banner_content = getParam.getJSONObject(i).getString("ad_banner");
                            }
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    public void startApp(){
        if (CheckConnection()){
            if (isOnline()) {
                if (st_move.equals("y")) {
                    shimmerFrameLayout.setVisibility(View.INVISIBLE);
                    viw_image.setVisibility(View.VISIBLE);
                    viw_play.setVisibility(View.VISIBLE);
                    viw_rate.setVisibility(View.VISIBLE);
                    viw_play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            shimmerFrameLayout.setVisibility(View.VISIBLE);
                            viw_image.setVisibility(View.INVISIBLE);
                            viw_play.setVisibility(View.INVISIBLE);
                            viw_rate.setVisibility(View.INVISIBLE);
                            loadInter();
                        }
                    });

                } else {
                    MoveApps();
                }
            }
        } else {
            WarningBox();
        }
    }
    public boolean CheckConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean isOnline() {

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            HttpURLConnection urlc = (HttpURLConnection) (new URL("https://www.google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(500); //choose your own timeframe
            urlc.setReadTimeout(500); //choose your own timeframe
            urlc.connect();
            int networkcode2 = urlc.getResponseCode();
            return (networkcode2 == 200);
        } catch (IOException e) {
            return false;  //connectivity exists, but no internet.
        }

    }
    public void rateApps(){
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + getPackageName())));
        }
    }
    public void MoveApps() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(SplhActivity.this)
                        .setTitle("Apps Maintenance")
                        .setMessage("This App is on maintenance,\nPlease go to our new apps with new feature and new experience.")
                        .setIcon(R.mipmap.ic_launcher)
                        .setCancelable( false )
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id="
                                                + url_move)));
                                finish();
                            }
                        })
                        .show();
            }
        });
    }

    public void WarningBox() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(SplhActivity.this)
                        .setTitle("No Connection")
                        .setMessage("Please check your internet connection\nThis app running well with good connection")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(1);
                                finish();
                            }
                        })
                        .show();
            }
        });
    }
}
