package com.momoclips.serverlocal;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

import com.momoclips.android.R;
import com.momoclips.util.JsonUtils;
@OptIn(markerClass = UnstableApi.class)
public class PipServerActivity extends AppCompatActivity {

    private ExoPlayer player;
    PlayerView playerView;
    private DataSource.Factory mediaDataSourceFactory;
    private ProgressBar progressBar;
    String id, title;
    JsonUtils jsonUtils;
    private final PictureInPictureParams.Builder pictureInPictureParamsBuilder =
            new PictureInPictureParams.Builder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(PipServerActivity.this,R.color.player_bg));
        setContentView(R.layout.activity_server_no_video);

        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        Intent i = getIntent();
        id = i.getStringExtra("id");
        title = i.getStringExtra("title");

        progressBar = findViewById(R.id.progressBar);
        playerView=findViewById(R.id.exoPlayerView);
        player = new ExoPlayer.Builder(PipServerActivity.this).setSeekForwardIncrementMs(10000).setSeekBackIncrementMs(10000).build();
        mediaDataSourceFactory = buildDataSourceFactory();
        playerView.setPlayer(player);
        playerView.setUseController(true);
        playerView.requestFocus();
        loadUrl();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                Log.e("Ad", "Finished");

            }
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                player.stop();
                errorDialog();
            }
        });

    }
    private void loadUrl() {
        Uri uri = Uri.parse(id);
        MediaSource mediaSource = buildMediaSource(uri);
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        switch (type) {
            case C.CONTENT_TYPE_SS:
                return new SsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_DASH:
                return new DashMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItem);
            case C.CONTENT_TYPE_RTSP:
                return new RtspMediaSource.Factory().createMediaSource(mediaItem);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }

        }
    }

    private DataSource.Factory buildDataSourceFactory() {
        return new DefaultHttpDataSource.Factory().setUserAgent(Util.getUserAgent(PipServerActivity.this, "ExoPlayerDemo"));
    }

    private void pictureInPictureMode() {
        Rational aspectRatio = new Rational(3, 4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
        }
    }

    @Override
    public void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode()) {
                Rational aspectRatio = new Rational(3, 4);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
                }
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
                                              Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        }
        if (isInPictureInPictureMode) {

        } else {

        }
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        updateVideoView(i);
    }
    private void updateVideoView(Intent i) {
        id = i.getStringExtra("id");
        title = i.getStringExtra("title");

        progressBar = findViewById(R.id.progressBar);
        playerView=findViewById(R.id.exoPlayerView);
        player = new ExoPlayer.Builder(PipServerActivity.this).setSeekForwardIncrementMs(10000).setSeekBackIncrementMs(10000).build();
        mediaDataSourceFactory = buildDataSourceFactory();
        playerView.setPlayer(player);
        playerView.setUseController(true);
        playerView.requestFocus();
        loadUrl();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                Log.e("Ad", "Finished");

            }
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                player.stop();
                errorDialog();
            }
        });
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.O)
    public void onBackPressed() {
        //super.onBackPressed();
         pictureInPictureMode();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    public void errorDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getResources().getString(R.string.exo_msg_oops))
                .setCancelable(false)
                .setMessage(getResources().getString(R.string.exo_msg_failed))
                .setPositiveButton(getResources().getString(R.string.exo_option_retry), (dialog, which) -> retryLoad())
                .setNegativeButton(getResources().getString(R.string.exo_option_no), (dialogInterface, i) -> finish())
                .show();
    }
    public void retryLoad() {
        Uri uri = Uri.parse(id);
        MediaSource mediaSource = buildMediaSource(uri);
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
    }
}
