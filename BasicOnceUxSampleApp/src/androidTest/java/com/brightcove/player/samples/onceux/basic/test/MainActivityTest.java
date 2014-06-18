
package com.brightcove.player.samples.onceux.basic.test;

import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.content.Context;
import android.media.MediaPlayer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.event.Event;
import com.brightcove.player.samples.onceux.basic.MainActivity;
import com.brightcove.player.samples.onceux.basic.R;
import com.brightcove.player.view.BrightcoveVideoView;
import com.brightcove.plugin.onceux.event.OnceUxEventType;
import com.brightcove.player.display.VideoDisplayComponent;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private final String TAG = this.getClass().getSimpleName();

    private BrightcoveVideoView brightcoveVideoView;

    private EventEmitter eventEmitter;
    private MainActivity mainActivity;
    private String adUrl = "http://onceux.unicornmedia.com/now/ads/vmap/od/auto/95ea75e1-dd2a-4aea-851a-28f46f8e8195/43f54cc0-aa6b-4b2c-b4de-63d707167bf9/9b118b95-38df-4b99-bb50-8f53d62f6ef8??umtp=0";
    private String contentUrl = "http://cdn5.unicornmedia.com/now/stitched/mp4/95ea75e1-dd2a-4aea-851a-28f46f8e8195/00000000-0000-0000-0000-000000000000/3a41c6e4-93a3-4108-8995-64ffca7b9106/9b118b95-38df-4b99-bb50-8f53d62f6ef8/0/0/105/1438852996/content.mp4";
    private VideoDisplayComponent videoDisplay;
    private int playheadPosition;
    private int progress;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mainActivity = getActivity();
        brightcoveVideoView = (BrightcoveVideoView) mainActivity.findViewById(R.id.brightcove_video_view);
        eventEmitter = brightcoveVideoView.getEventEmitter();

        eventEmitter.once(EventType.DID_SET_VIDEO, new EventListener() {
                @Override
                public void processEvent(Event event) {
                    brightcoveVideoView.start();
                }
            });
        eventEmitter.on(EventType.PROGRESS, new EventListener() {
                @Override
                public void processEvent(Event event) {
                    progress = event.getIntegerProperty(Event.PLAYHEAD_POSITION);
                    Log.v(TAG, "position at: " + progress);
                }
            });

    }

    public void setWifi(final boolean state) {
        WifiManager wifiManager = (WifiManager) this.getActivity().getSystemService(Context.WIFI_SERVICE);
        boolean wifiResult = wifiManager.setWifiEnabled(state);
        Log.v(TAG, "Wifi change successful: " + wifiResult);
        //setWifiEnabled will return a true if the operation succeeds, not necessarily if the Wifi state is changed to enabled.
    }

    public void seekTo(int msec) {
        Log.d(TAG, "Seeking to " + msec);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(Event.SEEK_POSITION, msec);
        playheadPosition = msec;
        eventEmitter.emit(EventType.SEEK_TO, properties);
    }

    public void testNoAdDataEventDoesNotTrigger() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Log.v(TAG, "Checking for Ad Data Url");
        eventEmitter.once(OnceUxEventType.NO_AD_DATA_URL, new EventListener() {
                @Override
                public void processEvent(Event event) {
                    Log.v(TAG, "This should not have happened; an Ad Data URL was supplied.");
                    latch.countDown();
                }
            });
        mainActivity.getOnceUxPlugin().processVideo(adUrl, contentUrl);
        eventEmitter.emit(EventType.PLAY);
        assertFalse("Test Failed.", latch.await(15, TimeUnit.SECONDS));
        brightcoveVideoView.stopPlayback();
    }

    public void testAdDataReadyEvent() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);
        setWifi(false);
        Log.v(TAG, "Wifi should be off.");
        // Turning off Wifi to trigger an error in the AD_DATA_READY event.
        // Currently disabled due to a bug that makes the test fail. Bug SDK-156
        eventEmitter.on(OnceUxEventType.AD_DATA_READY, new EventListener() {
                @Override
                public void processEvent(Event event) {
                    Object errorMessage = event.properties.get(OnceUxEventType.VMAP_ERRORS);
                    Object responseMessage = event.properties.get(OnceUxEventType.VMAP_RESPONSE);
                    Log.v(TAG, "AD_DATA_READY Error: " + errorMessage);
                    Log.v(TAG, "AD_DATA_READY Response: " + responseMessage);
                    if (responseMessage == null || responseMessage.equals("")) {
                        if(errorMessage == null || errorMessage.equals("")) {
                            // both are empty
                            Log.v(TAG, "Error: AD_DATA_READY is empty");
                            latch.countDown();
                            setWifi(true);
                            latch.countDown();
                        } else {
                            // response is empty, error is not
                            Log.v(TAG, "Error: AD_DATA_READY has at least one error");
                            latch.countDown();
                            setWifi(true);
                            latch.countDown();
                        }
                    } else {
                        if (errorMessage == null || errorMessage.equals("")) {
                            // response not empty, error empty
                            Log.v(TAG, "This should not happen. AD_DATA_READY is ready.");
                            setWifi(true);
                        } else {
                            // both are not empty
                            Log.v(TAG, "Error: AD_DATA_READY is too full");
                            latch.countDown();
                            setWifi(true);
                            latch.countDown();
                        }
                    }
                };
            });

        mainActivity.getOnceUxPlugin().processVideo(adUrl, contentUrl);
        eventEmitter.emit(EventType.PLAY);
        assertTrue("Test Failed", latch.await(30, TimeUnit.SECONDS));
        brightcoveVideoView.stopPlayback();
    }

    public void testSeekControlsPostAdBreak() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);
        eventEmitter.on(OnceUxEventType.END_AD_BREAK, new EventListener() {
                @Override
                public void processEvent(Event event) {
                    Log.v(TAG, "END_AD_BREAK Emitted. Seeking from: " + progress);
                    seekTo(40000);
                    //The Seek we are testing.
                    latch.countDown();
                }
            });
        eventEmitter.on(EventType.PROGRESS, new EventListener() {
                @Override
                public void processEvent(Event event) {
                    if (progress > 39500) {
                        if (progress < 41500) {
                            //Due to the asynchronous nature of the request and how android handles HLS, the seek usually lands about 1.2 seconds late.
                            Log.v(TAG, "Successful seek at: " + progress);
                            latch.countDown();
                        } else {
                            Log.v(TAG, "Too far.");
                        }
                    } else {
                        Log.v(TAG, "Not far enough. ");
                    }
                }
            });

        mainActivity.getOnceUxPlugin().processVideo(adUrl, contentUrl);
        eventEmitter.emit(EventType.PLAY);
        assertTrue("Timeout occurred.", latch.await(2, TimeUnit.MINUTES));
        brightcoveVideoView.stopPlayback();
    }

    public void testHideSeekControls() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(6);
        eventEmitter.on(EventType.HIDE_SEEK_CONTROLS, new EventListener(){
                @Override
                public void processEvent(Event event) {
                    Log.v(TAG, "Seek controls hidden at: " + progress);
                    latch.countDown();
                }
            });
        eventEmitter.on(OnceUxEventType.END_AD_BREAK, new EventListener(){
                @Override
                public void processEvent(Event event) {
                    latch.countDown();
                    if (latch.getCount() == 4) {
                        seekTo(59500);
                    }
                    if (latch.getCount() == 2) {
                        seekTo(166500);
                    }
                }
            });
        mainActivity.getOnceUxPlugin().processVideo(adUrl, contentUrl);
        eventEmitter.emit(EventType.PLAY);
        assertTrue("Timeout occurred.", latch.await(4, TimeUnit.MINUTES));
        brightcoveVideoView.stopPlayback();
    }

}
