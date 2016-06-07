package com.example.alexp.preproject;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.Chronometer;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements HomeFragment.FuncionesHome, MapsFragment.FuncionesMaps{

    private LocationManager locManager;
    private LocationListener locListener;
    private ArrayList<LatLng> posiciones;

    private FragmentTabHost mTabHost;
    private HomeFragment homeFragment;
    private MapsFragment mapsFragment;

    private float velocidad; //En M/S
    private float km; //Velocidad en KM/H
    private Thread t;
    private LatLng target;
    private boolean centrarMapa;
    private Chronometer cronometro;

    boolean detenido;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        posiciones = new ArrayList<LatLng>();
        centrarMapa = true;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("",getResources().getDrawable(R.drawable.hometab)),
                HomeFragment.class, null); //HomeFragment.class es el fragment atado a la pestaña "home"
                                            //Puse en todas las pestañas homefragment para probar, pero hay que cambiarlo por el
                                            //que corresponda cuando se creen los otros fragments

        mTabHost.addTab(mTabHost.newTabSpec("mapa")
                .setIndicator("",getResources().getDrawable(R.drawable.mapatab)), MapsFragment.class, null); //Cambiar homefragment


        mTabHost.addTab(mTabHost.newTabSpec("ajustes")
                .setIndicator("",getResources().getDrawable(R.drawable.ajustestab)), HomeFragment.class, null); //Cambiar homefragment


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar,menu);
        return true;
    }

    public void comenzarActividad() {

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng ubicacion = new LatLng(location.getLatitude(), location.getLongitude());
                posiciones.add(ubicacion);
                posicion(location);

                mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag("mapa");
                if ((mapsFragment != null) && (mapsFragment.estadoActualizado()))
                    mapsFragment.cambioUbicacion(ubicacion);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
                homeFragment.cambiarLblGps(true);
            }

            @Override
            public void onProviderDisabled(String provider) {
                homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
                homeFragment.cambiarLblGps(false);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);

    }

    public void comenzarControlDeVelocidad(){
        t=new Thread(new Runnable() {


            @Override
            public void run() {

                homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
                int i=0;
                detenido = false;   //actividad normal
                boolean corriendo;          //falso cuando velocidad==0

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        homeFragment.prenderColorLed("verde");
                    }
                });
                Log.d("uno","uno");
                try {
                    Thread.sleep(10000);    //Espero 10 segundos antes de empezar
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                Log.d("dos","dos");
                while(!detenido){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("tres","tres");
                    if (velocidad==0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                homeFragment.prenderColorLed("amarillo");
                            }
                        });
                        corriendo=false;
                        while ((i<10)&&(!corriendo)){       //Repito mientras velocidad==0 durante 10 segundos
                            Log.d("cuatro","cuatro");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.d("cinco","cinco");
                            if(velocidad==0) {
                                i++;

                            }else {
                                corriendo = true;
                                i=0;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        homeFragment.prenderColorLed("verde");
                                    }
                                });                            }
                        }
                        Log.d("siete","siete");
                        if(!corriendo){                     //Si salio se salio del while y la velocidad ==0 durante 10 segundos mando la notificacion
                            detenido=true;
                            //Mando notificacion
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    homeFragment.prenderColorLed("rojo");
                                    mandarNotificacion();
                                }
                            });
                            Log.d("ocho","ocho");
                        }
                    }
                }
            }
        });
        t.start();
    }

    private void mandarNotificacion(){
        int notId = 1;
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("NotID",notId);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        i,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.corredor);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle("ALERTA")
                .setContentText("Presione ESTOY BIEN")
                .setSmallIcon(R.drawable.alerta)
                .setAutoCancel(true)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Presione ESTOY BIEN para continuar, de lo contrario se enviara un sms de alerta"))
                .addAction(R.drawable.estoyok, "ESTOY BIEN", null)
                .setVibrate(new long[]{100, 250, 100, 500});

        //builder.setFullScreenIntent(pendingIntent,true);
        nm.notify(notId,builder.build());
    }

    public float getKM(){
        return km;
    }

    private void posicion(Location loc) {
        if(loc!=null){

            velocidad = loc.getSpeed();
            km = velocidad*3600;
            km = km/1000;
            km=roundTwoDecimals(km);
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
            if (homeFragment != null)
                homeFragment.actualizarVelocidad(km);
        }

    }

    public void prepararGPS(){
        locManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            homeFragment.cambiarLblGps(true);
        }else{
            homeFragment.cambiarLblGps(false);
        }
    }

    private float roundTwoDecimals(float d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Float.valueOf(twoDForm.format(d));
    }

    public Iterator<LatLng> obtenerRuta(){
        return posiciones.iterator();
    }

    public LatLng getTarget(){
        return target;
    }

    public void setTarget(LatLng ll){
        target = ll;
    }

    public boolean getCentrar(){
        return centrarMapa;
    }

    public void setCentrar(boolean b){
        centrarMapa = b;
    }
}
