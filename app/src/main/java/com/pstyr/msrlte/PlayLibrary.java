package com.pstyr.msrlte;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class PlayLibrary extends AppCompatActivity {

    ProgressDialog mProgressDialog;
//    RelativeLayout banner;
    ImageView songImg, playerstate;
    TextView songTitle, songUsr, songCurrent, songTotal;
    SeekBar seekBar;
    ProgressBar playerProgress;
    MediaPlayer mediaPlayer;
    Utilities utilities;
    private Handler mHandler = new Handler();
    public AudioManager audioManager;
    boolean repeat_status = false;

    String trackTitle, trackUrl, trackImg, trackArtist, src;
    ImageView rew, ff, share, imgRate;
//    private com.facebook.ads.AdView fbView;
//    private com.google.android.gms.ads.AdView adView;
    private com.google.android.gms.ads.InterstitialAd interstitialAd;
    private com.facebook.ads.InterstitialAd interstitialFb;

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerlibrary);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        songImg = findViewById(R.id.player_Img);
        playerstate = findViewById(R.id.play_btn);
        songTitle = findViewById(R.id.player_Title);
        seekBar = findViewById(R.id.seekBar);
        songCurrent = findViewById(R.id.song_current);
        songTotal = findViewById(R.id.song_total);

        share = (ImageView) findViewById(R.id.img_share);
        imgRate = (ImageView) findViewById(R.id.img_rate);
        rew = (ImageView) findViewById(R.id.btn_forward);
        ff = (ImageView) findViewById(R.id.bottombar_next);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trackTitle = extras.getString("songtitle");
            trackImg = extras.getString("songimg");
            trackArtist = extras.getString("songartist");
            trackUrl = extras.getString("songurl");
            src = extras.getString("source");
        }

        songTitle.setText(trackTitle);
        songTitle.setSelected(true);
        songTitle.setSingleLine(true);
        songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);

//        if (trackArtist != null) {
//            songUsr.setText(trackArtist);
//        } else {
//            songUsr.setVisibility(View.GONE);
//        }

//        Glide.with(this).load(trackImg).error(R.drawable.icon).into(songImg);

        mediaPlayer = new MediaPlayer();
        utilities = new Utilities();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // song lifecycle
        if (mediaPlayer.isPlaying() || mediaPlayer.isLooping() || mediaPlayer != null) {

            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        try {
            mediaPlayer.reset();
            if (src.equals("online")) {
//                loadNewInter();
                mediaPlayer.setDataSource(trackUrl + "?client_id=" + SplhActivity.sc);
                mediaPlayer.prepareAsync();
            } else {
                mediaPlayer.setDataSource(trackUrl);
                mediaPlayer.prepare();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mplayer) {
                updateProgressBar();
                playpausebutton();

            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                songTitle.setText("Sorry. We can't play this song. Please select another song to play.");
                playerstate.setVisibility(View.INVISIBLE);
                return true;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mplayer) {
                if (repeat_status) {
                    completingplay();
                } else {
                    playerstate.setBackgroundResource(R.drawable.ic_play_circle);
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);

                }
            }
        });

        playerstate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playpausebutton();
            }
        });

        ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition + seekForwardTime <= mediaPlayer.getDuration()) {
                    mediaPlayer.seekTo(currentPosition + seekForwardTime);
                } else {
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                }
            }
        });

        rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition - seekBackwardTime >= 0) {
                    mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                } else {
                    mediaPlayer.seekTo(0);
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                final String appName = getPackageName();
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                share.putExtra(Intent.EXTRA_SUBJECT, "Download and enjoy this good application");
                share.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id="
                        + appName);

                startActivity(Intent.createChooser(share, "Share and invite your friends to View this Apps !!"));
            }
        });
    }

    public void completingplay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        } else {
            playpausebutton();
            updateProgressBar();
        }
    }

    public void playpausebutton() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playerstate.setBackgroundResource(R.drawable.ic_play_circle);
        } else {
            mediaPlayer.start();
            playerstate.setBackgroundResource(R.drawable.ic_pause_circle);
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            songTotal.setText("" + utilities.milliSecondsToTimer(totalDuration));
            songCurrent.setText("" + utilities.milliSecondsToTimer(currentDuration));

            int progress = (int) (utilities.getProgressPercentage(currentDuration, totalDuration));
            seekBar.setProgress(progress);

            mHandler.postDelayed(this, 100);
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (playerProgress != null) {
            playerProgress = null;
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isLooping() || mediaPlayer.isPlaying()) {

                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }
}
