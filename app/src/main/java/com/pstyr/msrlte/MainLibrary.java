package com.pstyr.msrlte;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static com.pstyr.msrlte.SplhActivity.PERCODE;
import static com.pstyr.msrlte.SplhActivity.admob_id;

public class MainLibrary extends AppCompatActivity {
    RecyclerView recView;
    RelativeLayout banner;
    AdapterLibrary mAdapterOffline;
    List<Object> listOffline;
    ProgressDialog mProgressDialog;
    TextView noSong;

    com.google.android.gms.ads.AdView adView;
    com.facebook.ads.AdView fbView;

    String search_query, cat;
    public static String DB_PATH = Environment.getExternalStorageDirectory() + File.separator + "Download";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        MobileAds.initialize(this, admob_id);

        recView = findViewById(R.id.recView);
        noSong = findViewById(R.id.noSong);
        setTitle("Downloaded Songs");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 1214);
            }
        }
        banner = (RelativeLayout)findViewById(R.id.adViewContainer);
        if (SplhActivity.active_content.equals("fb")) {
            fbView = new com.facebook.ads.AdView(this, SplhActivity.id_banner_content, AdSize.BANNER_HEIGHT_50);
            banner.addView(fbView);
            fbView.loadAd();

        } else {
            adView = new com.google.android.gms.ads.AdView(this);
            adView.setAdUnitId(SplhActivity.id_banner_content);
            adView.setAdSize(com.google.android.gms.ads.AdSize.SMART_BANNER);
            banner.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }

        LinearLayoutManager linlayout = new LinearLayoutManager(this);
        linlayout.setOrientation(RecyclerView.VERTICAL);
        recView.setLayoutManager(linlayout);
        recView.setHasFixedSize(true);
        recView.setItemAnimator(new DefaultItemAnimator());

        listOffline = new ArrayList<>();
        mAdapterOffline = new AdapterLibrary(this, listOffline);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                }, PERCODE);

            } else {
                new LoadDownloadedData().execute();
            }
        } else {
            new LoadDownloadedData().execute();
        }

        recView.setAdapter(mAdapterOffline);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERCODE) {
            new LoadDownloadedData().execute();
        }
    }
    @SuppressLint("StaticFieldLeak")
    public class LoadDownloadedData extends AsyncTask<Void, Void, Void> {
        TrackLibrary trackOffline;
        String card = SplhActivity.folder;
        ArrayList<String> files = GetFiles(card);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            listOffline.clear();

            if (files != null) {
                for (int x = 0; x < files.size(); x++) {
                    String judullagu = files.get(x);
                    String url_song = card + "/" + files.get(x);
                    trackOffline = new TrackLibrary(judullagu, url_song);
                    listOffline.add(trackOffline);
                    Log.d("Title Songs", url_song);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (listOffline.size() < 1) {
                listOffline.clear();
                noSong.setVisibility(View.VISIBLE);
            } else {
                noSong.setVisibility(View.GONE);
                mAdapterOffline.notifyDataSetChanged();
            }
        }

    }

    public ArrayList<String> GetFiles(String directorypath) {
        ArrayList<String> Myfiles = new ArrayList<String>();
        File f = new File(directorypath);

        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {

            String filename = files[i].getName();
            String ext = filename.substring(filename.lastIndexOf('.') + 1, filename.length());
            if (ext.equals("MP3") || ext.equals("mp3")) {
                Myfiles.add(files[i].getName());
            }
        }

        return Myfiles;
    }

    public void Exit() {
        new AlertDialog.Builder(MainLibrary.this)
                .setTitle("Close Now")
                .setMessage("Are you sure ?")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.exit(1);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }
}
