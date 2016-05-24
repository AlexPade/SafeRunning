package com.example.alexp.preproject;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private LocationManager locManager;
    private LocationListener locListener;
    private float velocidad;
    private TextView lblVel;
    private ImageView ledRojo;
    private ImageView ledAmarillo;
    private ImageView ledVerde;
    private TextView estado;
    private float km;
    private Thread t;
    boolean detenido;
    private TextView lblGPS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        //Leds
        ledRojo = (ImageView) findViewById(R.id.ledRojo);
        ledAmarillo = (ImageView) findViewById(R.id.ledAmarillo);
        ledVerde = (ImageView) findViewById(R.id.ledVerde);
        estado = (TextView) findViewById(R.id.estado);


        View btnInicio = findViewById(R.id.inicio);
        View btnDetener = findViewById(R.id.detener);
        lblVel = (TextView) findViewById(R.id.vel);
        lblGPS = (TextView) findViewById(R.id.gps);

        prepararGPS();

        Log.d("principio", "principio");

        btnInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comenzarActividad();
                comenzarControlDeVelocidad();

            }
        });

        btnDetener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detenido=true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar,menu);
        return true;
    }

    private void comenzarActividad() {
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                posicion(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                lblGPS.setTextColor(Color.GREEN);
            }

            @Override
            public void onProviderDisabled(String provider) {
                lblGPS.setTextColor(Color.RED);

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

    private void comenzarControlDeVelocidad(){
        t=new Thread(new Runnable() {

            @Override
            public void run() {

                int i=0;
                detenido = false;   //actividad normal
                boolean corriendo;          //falso cuando velocidad==0

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prenderColorLed("verde");
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
                                prenderColorLed("amarillo");
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
                                        prenderColorLed("verde");
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
                                    prenderColorLed("rojo");
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


    private void posicion(Location loc) {
        if(loc!=null){
            velocidad = loc.getSpeed();
            km = velocidad*3600;
            km = km/1000;
            km=roundTwoDecimals(km);
            lblVel.setText(String.valueOf(km)+"KM/H");
        }else{
        }

    }

    public void prepararGPS(){
        locManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            lblGPS.setTextColor(Color.GREEN);
        }else{
            lblGPS.setTextColor(Color.RED);
        }
    }

    private float roundTwoDecimals(float d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Float.valueOf(twoDForm.format(d));
    }

    private void prenderColorLed(String color){
        if (color.equals("rojo")){ //Prendo rojo y apago amarillo y verde
            ledRojo.setImageResource(R.drawable.rojoprendido);
            ledAmarillo.setImageResource(R.drawable.amarillo);
            ledVerde.setImageResource(R.drawable.verde);
            estado.setText("PELIGRO");
        }
        if(color.equals("amarillo")) { //Prendo amarillo
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarilloprendido);
            ledVerde.setImageResource(R.drawable.verde);
            estado.setText("Detenido");
        }
        if (color.equals("verde")){ //Prendo verde
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarillo);
            ledVerde.setImageResource(R.drawable.verdeprendido);
            estado.setText("Corriendo");
        }
        if (color.equals("nada")){ //Ningun led prendido
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarillo);
            ledVerde.setImageResource(R.drawable.verde);
            estado.setText("");
        }
    }


}
