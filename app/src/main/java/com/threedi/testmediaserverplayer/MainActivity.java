package com.threedi.testmediaserverplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.VideoView;

public class MainActivity extends Activity {

    int currentIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final VideoView videoView = findViewById(R.id.videoview);
        final String path="/sdcard/testplay/";
        videoView.setVideoPath(path+"0.mp4");
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                currentIndex++;
                String videoUrl = path+currentIndex+".mp4";
                videoView.setVideoPath(videoUrl);
            }
        });

    }
}
