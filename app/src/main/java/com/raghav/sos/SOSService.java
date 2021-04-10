package com.raghav.sos;

import android.app.Service;
import android.content.Intent;
import android.media.VolumeProvider;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SOSService extends Service {
    int count=0;
    private MediaSession mediaSession;
    public SOSService() {
        Log.d("Count: ",String.valueOf(count));

        // Toast.makeText(SOSService.this, "open", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSession(this, "PlayerService");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                .build());
        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        VolumeProvider myVolumeProvider =
                new VolumeProvider(VolumeProvider.VOLUME_CONTROL_RELATIVE, /*max volume*/100, /*initial volume level*/50) {
                    @Override
                    public void onAdjustVolume(int direction) {
                         count++;

                        if(count==5){
                            Log.d("Count: ",String.valueOf(count));
                            count=0;
                            Toast.makeText(SOSService.this, "Pressed", Toast.LENGTH_SHORT).show();
                        }
                /*
                -1 -- volume down
                1 -- volume up
                0 -- volume button released
                 */
                    }
                };
        mediaSession.setPlaybackToRemote(myVolumeProvider);

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}