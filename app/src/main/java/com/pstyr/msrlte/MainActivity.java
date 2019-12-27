package com.pstyr.msrlte;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;
import java.io.InputStream;

import static com.pstyr.msrlte.SplhActivity.admob_id;
import static com.pstyr.msrlte.SplhActivity.url_move;

public class MainActivity extends AppCompatActivity {

    SearchView searchview;
    ImageView btn_search;
    RelativeLayout banner;
    CardView cardHome;
    Button viwRate;
    Button viwPravacy;
    Button viwShare;
    String search_query;
    SharedPreference sharedPreference;
    com.google.android.gms.ads.AdView adView;
    com.facebook.ads.AdView fbView;

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
        setContentView(R.layout.activity_main);

        searchview = findViewById(R.id.searchView);
//        cardHome = findViewById(R.id.cardHome);
        MobileAds.initialize(this, admob_id);
        banner = (RelativeLayout)findViewById(R.id.adViewContainer);
        sharedPreference = new SharedPreference(this);

        viwRate = (Button) findViewById( R.id.rate_id );
        viwPravacy = (Button) findViewById( R.id.pravacy_id );
        viwShare = (Button) findViewById( R.id.share_id );

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchview.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchview.setMaxWidth(Integer.MAX_VALUE);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_query = query;
                Intent i = new Intent(MainActivity.this, ViewSearch.class);
                i.putExtra("query", search_query);
                startActivity(i);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                search_query = query;
                return false;
            }
        });
        searchview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchview.setIconified(false);
            }
        });
        if (SplhActivity.active_ads.equals("fb")) {
            fbView = new com.facebook.ads.AdView(this, SplhActivity.id_banner, AdSize.BANNER_HEIGHT_50);
            AdSettings.addTestDevice("936f0142-9449-42bb-aaa0-0c8ef62f0d2b");
            banner.addView(fbView);
            fbView.loadAd();

        } else {
            adView = new com.google.android.gms.ads.AdView(this);
            adView.setAdUnitId(SplhActivity.id_banner);
            adView.setAdSize(com.google.android.gms.ads.AdSize.SMART_BANNER);
            banner.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
        if (SplhActivity.st_popup.equals("a")) {
            setPop_up();
        }
//        cardHome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchview.setIconified(false);
//            }
//        });

        viwRate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateApps();
            }
        } );
        viwPravacy.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPolicy();
            }
        } );
        viwShare.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareApp();
            }
        } );
    }

    public void showPolicy(){
        String txt;
        try {
            InputStream is = getAssets().open("pravacy.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            txt = new String(buffer);
        } catch (IOException ex) {
            return;
        }

        AlertDialog alertDialogPolicy = new AlertDialog.Builder(MainActivity.this).create();
        alertDialogPolicy.setTitle("Privacy Policy");
        alertDialogPolicy.setIcon(R.mipmap.ic_launcher);
        alertDialogPolicy.setMessage(txt);
        alertDialogPolicy.setButton(AlertDialog.BUTTON_POSITIVE, "Accept",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sharedPreference.setApp_runFirst("NO");
                        dialog.dismiss();

                    }
                });
        alertDialogPolicy.setButton(AlertDialog.BUTTON_NEGATIVE, "Decline",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        warningpolicy();

                    }
                });
        alertDialogPolicy.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                warningpolicy();
            }
        });
        alertDialogPolicy.show();
    }

    public void warningpolicy() {
        if (sharedPreference.getApp_runFirst().equals("FIRST")) {
            AlertDialog alertDialogPolicy = new AlertDialog.Builder(MainActivity.this).create();
            alertDialogPolicy.setTitle("Policy Warning !");
            alertDialogPolicy.setIcon(R.mipmap.ic_launcher);
            alertDialogPolicy.setMessage("Please accept our policy before use this App.\nThank You.");
            alertDialogPolicy.setButton(AlertDialog.BUTTON_POSITIVE, "Restart",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent i = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    });
            alertDialogPolicy.setButton(AlertDialog.BUTTON_NEGATIVE, "Quit",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            System.exit(1);
                            finish();
                        }
                    });
            alertDialogPolicy.show();
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
    private void ShareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());
        startActivity(Intent.createChooser(share, "Share App Via"));
    }
    @Override
    public void onBackPressed() {
        ExitApp();
    }

    private void ExitApp() {
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.exitdialog, null);
        dialogBuilder.setView(dialogView);
        ImageView imgExit = (ImageView) dialogView.findViewById(R.id.img_exit);
        ImageButton imgRare = (ImageButton) dialogView.findViewById(R.id.img_rate);
        final android.app.AlertDialog playDialog = dialogBuilder.create();
        imgRare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + getPackageName())));

//                        playDialog.dismiss();
                }
            }
        });
        imgExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(1);
                finish();
            }
        });
        playDialog.show();
    }

    private void setPop_up() {
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.popuap, null);
        dialogBuilder.setView(dialogView);
        ImageView icon_up = (ImageView) dialogView.findViewById(R.id.ic_icon);
        Glide.with(this).load(SplhActivity.dt_icon).into(icon_up);
        ImageView icon_banner = (ImageView) dialogView.findViewById(R.id.imgbanner);
        Glide.with(this).load(SplhActivity.dt_banner).into(icon_banner);
        TextView title_up = (TextView) dialogView.findViewById(R.id.txt_title);
        title_up.setText(SplhActivity.dt_title);
        TextView desc_up = (TextView) dialogView.findViewById(R.id.textMassage);
        desc_up.setText(SplhActivity.dt_desc);
        TextView imgInstal = (TextView) dialogView.findViewById(R.id.text_innstal);
        final android.app.AlertDialog playDialog = dialogBuilder.create();
        imgInstal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent rate = new Intent(Intent.ACTION_VIEW);
                rate.setData(Uri.parse("market://details?id=" + url_move));
                startActivity(rate);
                playDialog.dismiss();
            }
        });
        playDialog.show();
    }
}