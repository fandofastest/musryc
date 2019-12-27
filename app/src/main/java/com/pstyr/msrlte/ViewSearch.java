package com.pstyr.msrlte;



import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.InterstitialAdExtendedListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.pstyr.msrlte.SplhActivity.admob_id;
public class ViewSearch extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private RelativeLayout lv_loading;
    private AVLoadingIndicatorView playprogress, playprogress2;
    public static String jsonResponse;
    public static String JUDUL;
    public static String LINK, SUB;
    InterstitialAd interstitialAd;
    com.facebook.ads.InterstitialAd interstitialFb;
    androidx.appcompat.widget.SearchView searchView;
    LinearLayout player;
    private ImageView btn_play, btn_forward, btn_backward, imgRate, img_down, btnlibrary;
    private SeekBar seekBarProgress, seekBarVolume;
    private TextView songCurrentDurationLabel, songTotalDurationLabel;
    private int seekForwardTime = 5000;
    private int seekBackwardTime = 5000;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private Utilities utils;
    private RecyclerView recyclerView;
    List<TrackView> tracklist;
    List<TrackView> tracksearchlist;
    TrackAdapterView mAdapter;
    private ProgressDialog mProgressDialog;
    ProgressBar ProgressBar;
    private RelativeLayout banner;
    public com.google.android.gms.ads.AdView adView;
    public com.facebook.ads.AdView fbView;
    String artist, query_search, songlink, imglink, tittle, usr, Ran, stream, api, duration, likes_count, idsong;
    TextView judul;
    private Handler mHandler = new Handler();
    boolean offline;
    private String DOWNLOAD_DIRECTORY="/Download";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        MobileAds.initialize(this, admob_id);

        playprogress= (AVLoadingIndicatorView) findViewById(R.id.playprogress1);
        playprogress.setIndicator("BallScaleIndicator");

        playprogress2= (AVLoadingIndicatorView) findViewById(R.id.playprogress2);
        playprogress2.setIndicator("BallScaleIndicator");

        stream = getResources().getString(R.string.stream);
        api = getResources().getString(R.string.api);

        player = (LinearLayout) findViewById(R.id.player);
        tracklist = new ArrayList<>();
        final String[] myNames = {
                SplhActivity.scd_key};
        final int rando = (int) (Math.random()*1);
        Ran = myNames[rando];


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            query_search = extras.getString("query");
            setTitle("Search Music : "+query_search);
            playprogress2.setVisibility(View.VISIBLE);
        }

        loadInter();

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
        new RecentSong().execute();
        tracksearchlist = new ArrayList<>();
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mAdapter = new TrackAdapterView(ViewSearch.this, tracksearchlist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewSearch.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(ViewSearch.this, LinearLayoutManager.HORIZONTAL));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrackView listTrack = tracksearchlist.get(position);

                if (player.getVisibility() == View.GONE) {
                    player.setVisibility(View.VISIBLE);
                    banner.setVisibility(View.VISIBLE);

                }
                btn_play.setVisibility(View.INVISIBLE);
                playprogress.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(mUpdateTimeTask);

                songlink = listTrack.getSongUri() + "/stream?client_id=" + Ran;
                tittle = listTrack.getSongTitle();
                duration = listTrack.getduration();
                imglink = listTrack.getArtworkUrl();
                usr = listTrack.getUser();
                idsong="http://fando.id/mp3ucok.php?id="+listTrack.getId()+"&key="+SplhActivity.scd_key;


//                Glide.with(getApplicationContext()).load(imglink).error(R.drawable.item).into(img_player);
                judul.setText(tittle);

                if (mediaPlayer!=null) {
                    if (mediaPlayer.isLooping() || mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        btn_play.setImageResource(R.drawable.ic_play_circle);
                    }
                }

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(idsong);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        setTitle(usr);
        judul = (TextView) findViewById(R.id.lirikjalan);
        judul.setSelected(true);
        judul.setSingleLine();
        judul.setEllipsize(TextUtils.TruncateAt.MARQUEE);

        btn_play = (ImageView) findViewById(R.id.btn_play);
        img_down = (ImageView) findViewById(R.id.img_down);
        imgRate = (ImageView) findViewById(R.id.img_rate);
        img_down = (ImageView) findViewById(R.id.img_down);
        imgRate = (ImageView) findViewById(R.id.img_rate);
        if (SplhActivity.st_down.equals("d")) {
            img_down = (ImageView) findViewById(R.id.img_down);
            img_down.setVisibility(View.VISIBLE);
        } else {
            imgRate = (ImageView) findViewById(R.id.img_rate);
            imgRate.setVisibility(View.VISIBLE);
        }
        btnlibrary = (ImageView) findViewById(R.id.img_library);
        btn_backward = (ImageView) findViewById(R.id.btn_forward);
        btn_forward = (ImageView) findViewById(R.id.bottombar_next);
        seekBarProgress = (SeekBar) findViewById(R.id.audio_progress_control);
        seekBarProgress.setProgress(100);
        seekBarProgress.setMax(100);

        songCurrentDurationLabel = (TextView) findViewById(R.id.slidepanel_time_progress);
        songTotalDurationLabel = (TextView) findViewById(R.id.slidepanel_time_total);

        mediaPlayer = new MediaPlayer();
        utils = new Utilities();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        imgRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                if (mediaPlayer!=null) {
                    if (mediaPlayer.isLooping() || mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();

                    }
                }
                btn_rate();
            }
        });
        // button play
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnplaystatement();
            }
        });

        // button forward
        btn_backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition - seekBackwardTime <= mediaPlayer.getDuration()) {
                    mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                } else {
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                }
            }
        });
        btn_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition + seekForwardTime <= mediaPlayer.getDuration()) {
                    mediaPlayer.seekTo(currentPosition + seekForwardTime);
                } else {
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                }
            }
        });
        btnlibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                if (mediaPlayer!=null) {
                    if (mediaPlayer.isLooping() || mediaPlayer.isPlaying()) {
                        mediaPlayer.reset();

                    }
                }
                Intent i = new Intent(ViewSearch.this, MainLibrary.class);
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                    mediaPlayer.reset();
                }
                startActivity(i);
            }
        });
        img_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (offline) {
                    Toast.makeText(ViewSearch.this, "This offline is downloaded.", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressDialog = new ProgressDialog(ViewSearch.this);
                    mProgressDialog.setMessage("Prepare for saving, please wait!");
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setCancelable(true);

                    // execute this when the downloader must be fired
                    final ViewSearch.DownloadTask downloadTask = new ViewSearch.DownloadTask(ViewSearch.this);



//                    downloadTask.execute(idsong);


                    String cutTitle =tittle;
                    cutTitle = cutTitle.replace(" ", "-").replace(".", "-") + ".mp3";
                    DownloadManager  downloadManager = (DownloadManager) getApplication().getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(idsong));
                    request.setTitle(tittle);
                    request.setDescription("Downloading");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(DOWNLOAD_DIRECTORY, cutTitle);
                    request.allowScanningByMediaScanner();
                    long downloadID = downloadManager.enqueue(request);

                    Toast.makeText(getApplicationContext(), "Downloading Start", Toast.LENGTH_SHORT).show();






                    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            downloadTask.cancel(true);
                        }
                    });
                }
            }
        });
        // offline lifecycle
        if (mediaPlayer.isPlaying() || mediaPlayer.isLooping() || mediaPlayer != null) {

            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        if (songlink!=null) {
            player.setVisibility(View.VISIBLE);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(songlink);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            player.setVisibility(View.GONE);
        }
//        Glide.with(this).load(imglink).error(R.drawable.item).into(img_player);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mplayer) {
                player.setVisibility(View.VISIBLE);
                playprogress.setVisibility(View.GONE);
                updateProgressBar();
                btnplaystatement();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mplayer) {
                btn_play.setImageResource(R.drawable.ic_play_circle);
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);

            }
        });
        // Listeners
        seekBarProgress.setOnSeekBarChangeListener(this); // Important

    }

    private void btnplaystatement() {
        btn_play.setVisibility(View.VISIBLE);
        if (mediaPlayer.isPlaying()) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                btn_play.setImageResource(R.drawable.ic_play_circle);
            }
        } else {
            // Resume offline
            if (mediaPlayer != null) {
                mediaPlayer.start();
                btn_play.setImageResource(R.drawable.ic_pause_circle);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();
            songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
            songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            seekBarProgress.setProgress(progress);
            mHandler.postDelayed(this, 100);
        }
    };

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
        mediaPlayer.seekTo(currentPosition);
        updateProgressBar();
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String folder;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();

                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                //String filename = Jud +".mp3";
                folder = "sdcard/Download/";

                File file = new File(folder);
                if (!file.exists()) {
                    file.mkdirs();
                }

                String filename = tittle+".mp3";
                File saveFile = new File(file , filename);

                input = connection.getInputStream();
                output = new FileOutputStream(saveFile);

//                byte data[] = new byte[4096];
                byte data[] = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setMessage("Saving music in progress!");
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(ViewSearch.this)) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1111);
                        Toast.makeText(context, "Please allow app to access the storage file", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Download is not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Download is not available", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(context, "Download music succsesfully", Toast.LENGTH_SHORT).show();
                showInterstitial();
            }

        }

    }

    private class RecentSong extends AsyncTask<Void, Void, Void> {
        TrackView track;
        String data = "";
        String songtitle, songuri, artworkurl, username,id;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(ViewSearch.this);
//            mProgressDialog.setIndeterminate(false);
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.setMessage("Please wait...");
            playprogress2.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            tracksearchlist.clear();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(api + "/search/tracks?q="+query_search+"&client_id="+Ran+"&limit=100");
                HttpURLConnection httpURLConnection2 = (HttpURLConnection) url.openConnection();
                InputStream inputStream2 = httpURLConnection2.getInputStream();
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                String line = "";
                while (line != null) {
                    line = bufferedReader2.readLine();
                    data = data + line;
                }
                try {


                    JSONObject json = new JSONObject(data);

                    JSONArray getParam = json.getJSONArray("collection");
                    for (int i = 0; i < getParam.length(); i++) {

                        songtitle = getParam.getJSONObject(i).getString("title");
                        artworkurl = getParam.getJSONObject(i).getString("artwork_url");
                        songuri = getParam.getJSONObject(i).getString("uri");
                        id = getParam.getJSONObject(i).getString("id");


                        duration = getParam.getJSONObject(i).getString("full_duration");
                        likes_count = getParam.getJSONObject(i).getString("likes_count");

                        JSONObject getValueUser = getParam.getJSONObject(i).getJSONObject("user");
                        username = getValueUser.getString("username");


                        track = new TrackView(songtitle, songuri, artworkurl, username, duration, likes_count,id);
                        tracksearchlist.add(track);

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
            mAdapter.notifyDataSetChanged();
//            mProgressDialog.dismiss();
            playprogress2.hide();
        }
    }

    public void loadInter(){
        if (SplhActivity.active_content.equals("fb")) {

            interstitialFb = new com.facebook.ads.InterstitialAd(this, SplhActivity.id_inter_content);
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

                }

                @Override
                public void onAdLoaded(Ad ad) {

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

                }

                @Override
                public void onInterstitialActivityDestroyed() {

                }
            });
            interstitialFb.loadAd();
        } else {

            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId(SplhActivity.id_inter_content);
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    public void showInterstitial() {
        if (SplhActivity.active_content.equals("fb")) {
            if (interstitialFb.isAdLoaded()) {
                interstitialFb.show();
            }
        } else {
            if(interstitialAd.isLoaded())
                interstitialAd.show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setTitle("Search Music "+query);
                query = query.replaceAll(" ","+");
                new RecentSong().execute();
                playprogress2.setVisibility(View.VISIBLE);
                query_search = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed

                return false;
            }
        });

        if (query_search!=null) {
            setTitle(""+query_search);
//            new RecentSong().execute();
        } else {
            setTitle("");
        }

        return true;
    }
    public void btn_rate(){
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
    public void onBackPressed() {
        super.onBackPressed();
        if (this.mediaPlayer != null && this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.stop();
        }
    }
}
