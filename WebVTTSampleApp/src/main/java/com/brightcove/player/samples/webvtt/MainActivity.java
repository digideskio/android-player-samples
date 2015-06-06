package com.brightcove.player.samples.webvtt;

import android.net.Uri;
import android.os.Bundle;
import com.brightcove.player.captioning.BrightcoveCaptionFormat;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.view.BrightcovePlayer;
import com.brightcove.player.view.BrightcoveVideoView;

/**
 * This activity demonstrates how to play a video with closed
 * captions in multiple languages.
 */
public class MainActivity extends BrightcovePlayer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        brightcoveVideoView = (BrightcoveVideoView) findViewById(R.id.brightcove_video_view);
        brightcoveVideoView.setMediaController(new BrightcoveMediaController(brightcoveVideoView));
        super.onCreate(savedInstanceState);

        brightcoveVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sintel_trailer));

        BrightcoveCaptionFormat brightcoveCaptionFormat = BrightcoveCaptionFormat.createCaptionFormat("text/vtt", "de");
        brightcoveVideoView.addSubtitleSource(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sintel_trailer_de), brightcoveCaptionFormat);
        brightcoveCaptionFormat = BrightcoveCaptionFormat.createCaptionFormat("text/vtt", "en");
        brightcoveVideoView.addSubtitleSource(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sintel_trailer_en), brightcoveCaptionFormat);
        brightcoveCaptionFormat = BrightcoveCaptionFormat.createCaptionFormat("text/vtt", "es");
        brightcoveVideoView.addSubtitleSource(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sintel_trailer_es), brightcoveCaptionFormat);
        brightcoveCaptionFormat = BrightcoveCaptionFormat.createCaptionFormat("text/vtt", "fr");
        brightcoveVideoView.addSubtitleSource(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sintel_trailer_fr), brightcoveCaptionFormat);
        brightcoveCaptionFormat = BrightcoveCaptionFormat.createCaptionFormat("text/vtt", "it");
        brightcoveVideoView.addSubtitleSource(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sintel_trailer_it), brightcoveCaptionFormat);
        brightcoveCaptionFormat = BrightcoveCaptionFormat.createCaptionFormat("text/vtt", "nl");
        brightcoveVideoView.addSubtitleSource(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sintel_trailer_nl), brightcoveCaptionFormat);
    }
}
