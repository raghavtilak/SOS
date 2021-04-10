package com.raghav.sos.ShakeServices;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.raghav.sos.Contacts.ContactModel;
import com.raghav.sos.Contacts.DbHelper;
import com.raghav.sos.R;

import java.util.List;

public class SensorService extends Service {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private OpenCellID openCellID;
    GsmCellLocation loc;

    public SensorService(){
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onShake(int count) {
                //Location loc=getLocation();
                if(count==3) {
                    Log.d("Check:","Count=3");
                    vibrate();

/* WORKING
                    LocationRequest locationRequest=new LocationRequest();
                    locationRequest.setInterval(10000);
                    locationRequest.setFastestInterval(5000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    FusedLocationProviderClient fused=LocationServices.getFusedLocationProviderClient(SensorService.this);
                    fused.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            Location location=locationResult.getLastLocation();
                            if(location!=null){
                                Log.d("Check: ",String.valueOf(location.getLatitude()));
                            }else{
                                Log.d("Check: ","Locationnull");
                            }
                        }
                    },null);
*/

                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                    // WORKING WITH GPS ON (IN LOCK SCREEN TOO)
                    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                        @Override
                        public boolean isCancellationRequested() {
                            Log.d("Check:","Cancel req");
                            return false;
                        }

                        @NonNull
                        @Override
                        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                            Log.d("Check:","onCanceled");
                           // getOpenCellLocation();
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location!=null){
                                Log.d("Check:","Lat:"+location.getLatitude());


                                SmsManager smsManager = SmsManager.getDefault();

                                DbHelper db=new DbHelper(SensorService.this);
                                List<ContactModel> list=db.getAllContacts();
                                for(ContactModel c: list){
                                    String message = "Hey, "+c.getName()+"I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.\n "+"http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                                }
                                //  smsManager.sendTextMessage("+917877047794", null, "Can't Fetch Location.", null, null);
                                Log.d("Check:","Message Sent");
                            }else{
                                Log.d("Check:","Location null");
                                String message= "I am in DANGER, i need help. Please urgently reach me out.\n"+"GPS was turned off.Couldn't find location. Call your nearest Police Station.";
                                SmsManager smsManager = SmsManager.getDefault();
                                DbHelper db=new DbHelper(SensorService.this);
                                List<ContactModel> list=db.getAllContacts();
                                for(ContactModel c: list){
                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Check: ","OnFailure");
                            String message= "I am in DANGER, i need help. Please urgently reach me out.\n"+"GPS was turned off.Couldn't find location. Call your nearest Police Station.";
                            SmsManager smsManager = SmsManager.getDefault();
                            DbHelper db=new DbHelper(SensorService.this);
                            List<ContactModel> list=db.getAllContacts();
                            for(ContactModel c: list){
                                smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                            } }
                    });


//GIVING NULL EVEN WHEN GPS ON
                 /*
                   fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d("Check: ","OnSuccess");
                            if (location != null) {
                                String message = "http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();

                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage("+918619146262", null, message, null, null);
                                smsManager.sendTextMessage("+917877047794", null, "Can't Fetch Location.", null, null);
                                Log.d("Check:","Message Sent");
                            }else{
                                Log.d("Check: ","Location Null");

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                                Log.d("Check: ","OnFailure");
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage("+918619146262", null, "Can't Fetch Location.", null, null);
                            smsManager.sendTextMessage("+917877047794", null, "Can't Fetch Location.", null, null);
                                Toast.makeText(getApplicationContext(), "SMS SENT",
                                        Toast.LENGTH_LONG).show();
                        }
                    });
*/

                }
            }
        });
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void vibrate(){
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibEff;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibEff=VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
            vibrator.cancel();
            vibrator.vibrate(vibEff);
        }else{
            vibrator.vibrate(500);
        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("You are protected.")
                .setContentText("We are there for you")

                //this is important, otherwise the notification will show the way
                //you want i.e. it will show some default notification
                .setSmallIcon(R.drawable.ic_launcher_foreground)

                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Override
    public void onDestroy() {
        //mSensorManager.unregisterListener(mShakeDetector);

        Log.d("Check: ","Destroyed");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ReactivateService.class);
        this.sendBroadcast(broadcastIntent);


        super.onDestroy();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Log.d("Check: ","Removed");
        super.onTaskRemoved(rootIntent);

    }


    public void getOpenCellLocation(){
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        loc = (GsmCellLocation) tm.getCellLocation();
        String networkOperator = tm.getNetworkOperator();
        String mcc = networkOperator.substring(0, 3);
        String mnc = networkOperator.substring(3);

        openCellID = new OpenCellID(this);
        openCellID.setMcc(mcc);
        openCellID.setMnc(mnc);
        int cellid = loc.getCid();
        int lac = loc.getLac();
        openCellID.setCallID(cellid);
        openCellID.setCallLac(lac);

            openCellID.GetOpenCellID();

        SmsManager smsManager = SmsManager.getDefault();

        DbHelper db=new DbHelper(SensorService.this);
        List<ContactModel> list=db.getAllContacts();
        for(ContactModel c: list){
            String message = "Hey, "+c.getName()+"I am in DANGER, i need help. Please urgently reach me out. Here are my nearest mobile tower coordinates.\n "+"http://maps.google.com/?q=" + openCellID.latitude + "," + openCellID.longitude;
            smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
        }
        //  smsManager.sendTextMessage("+917877047794", null, "Can't Fetch Location.", null, null);
        Log.d("Check:","Message Sent via OpenCell");

    }

}