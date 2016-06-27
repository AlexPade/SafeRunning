package com.example.alexp.saferunning;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements HomeFragment.FuncionesHome, EstoyBienFragment.estoyBienListener, MapsFragment.FuncionesMaps {

    private LocationManager locManager;
    private LocationListener locListener;
    private ArrayList<LatLng> posiciones;

    private HomeFragment homeFragment;
    private String estado;
    private MapsFragment mapsFragment;

    private float velocidad; //En M/S
    private float km; //Velocidad en KM/H
    private LatLng target;
    private boolean centrarMapa;
    private Chronometer cronometro;

    boolean detenido;
    boolean presionado;
    boolean notificationFlag;
    boolean gpsActivado;
    NotificationManager nm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FragmentTabHost mTabHost;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        posiciones = new ArrayList<LatLng>();
        centrarMapa = true;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("", getResources().getDrawable(R.drawable.hometab)),
                    HomeFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("mapa")
                .setIndicator("", getResources().getDrawable(R.drawable.mapatab)), MapsFragment.class, null);


        mTabHost.addTab(mTabHost.newTabSpec("ajustes")
                .setIndicator("", getResources().getDrawable(R.drawable.ajustestab)), HomeFragment.class, null);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar,menu);
        return true;
    }

    public void comenzarActividad() {

        if(gpsActivado) {

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
        }else{
            Toast toast = Toast.makeText(getApplicationContext(),"El GPS esta desactivado",Toast.LENGTH_LONG);
            toast.show();
        }

    }

    public void comenzarControlDeVelocidad(){
        Thread t;
        t=new Thread(new Runnable() {


            @Override
            public void run() {

                homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
                int i=0;
                int j=0;
                detenido = false;   //actividad normal
                boolean corriendo; //falso cuando velocidad==0
                boolean valido = false;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(homeFragment!=null) {
                            estado = "corriendo";
                            homeFragment.prenderColorLed("verde");
                        }
                    }
                });

                while((!detenido)&&(j<10)) {//Espero 10 segundos antes de empezar
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(j==9){
                        valido=true;
                    }
                    j++;
                }


                while((!detenido)&&(valido)){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (velocidad==0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            if(homeFragment!=null) {
                                estado = "detenido";
                                homeFragment.prenderColorLed("amarillo");
                            }
                            }
                        });
                        corriendo=false;
                        while ((i<20)&&(!corriendo)&&(!detenido)){       //Repito mientras velocidad==0 durante 10 segundos
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(velocidad==0) {
                                i++;

                            }else {
                                corriendo = true;
                                i=0;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(homeFragment!=null) {
                                            estado = "corriendo";
                                            homeFragment.prenderColorLed("verde");
                                        }
                                    }
                                });                            }
                        }
                        if(!corriendo&&(!detenido)){        //Si salio se salio del while sin presionar detener y la velocidad ==0 durante 10 segundos mando la notificacion
                            detenido=true;
                            //Mando notificacion
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(homeFragment!=null) {
                                        estado = "peligro";
                                        homeFragment.prenderColorLed("rojo");
                                    }
                                    mandarNotificacion();
                                }
                            });
                        }
                    }
                }
            }
        });
        t.start();
    }

    private void mandarNotificacion(){
        int notId = 1;

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("notificacionFrag", "notificacion");
        i.putExtra("NotID",notId);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        i,
                        0
                );
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.corredor);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle("ALERTA")
                .setContentText("Presione ESTOY BIEN")
                .setSmallIcon(R.drawable.alerta)
                .setAutoCancel(true)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Presione ESTOY BIEN para continuar, de lo contrario se enviara un SMS de alerta"))
                //.addAction(R.drawable.estoyok, "ESTOY BIEN", null)
                .setVibrate(new long[]{100, 250, 100, 500});

        nm.notify(notId,builder.build());
        notificationFlag=true;

        conteoNotificacion();
    }

    public float getKM(){
        return km;
    }

    public void setKm(float km) {
        this.km = km;
    }

    public String getEstado(){
        return estado;
    }


    private void posicion(Location loc) {
        if(loc!=null){
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
            setVelocidad(loc.getSpeed());
            //setKm(roundTwoDecimals((velocidad*3600)/1000));
            if(homeFragment!=null)
                homeFragment.actualizarVelocidad(getKM());
        }

    }

    public String getDireccion(Location loc) { //Obtener la direccion de la calle a partir de la latitud y la longitud
        String toReturn="";
        if (loc!=null) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<android.location.Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    android.location.Address address = list.get(0);
                    toReturn= address.getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return toReturn;
    }

    private void setVelocidad(float v){
        velocidad=v;
        setKm(roundTwoDecimals((velocidad*3600)/1000));
    }

    public float getVelocidad() {
        return velocidad;
    }

    @Override
    public void detenerActividad() {
        detenido=true;
        estado="";
        homeFragment.noClickeableDetener();
        if(homeFragment!=null)
            homeFragment.prenderColorLed("nada"); //Apago todos los leds
        locManager.removeUpdates(locListener);
        setVelocidad(0);
        homeFragment.actualizarVelocidad(getKM());

        Toast toast = Toast.makeText(getApplicationContext(),"Deteniendo Actividad...",Toast.LENGTH_SHORT);
        toast.show();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        homeFragment.cambiarBoton();
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(task,2000);
    }

    public void conteoNotificacion(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                presionado=false;
                int i = 0;
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                while((!presionado)&&(i<20)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(i%2==0)
                        v.vibrate(500);
                    i++;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!presionado){
                            //Mandar mensaje
                            Toast toast = Toast.makeText(getApplicationContext(),"mandar mensaje..",Toast.LENGTH_LONG);
                            toast.show();
                            notificacionSMS();
                            mandarSMS();
                        }else{
                            Toast toast = Toast.makeText(getApplicationContext(),"boton presionado..",Toast.LENGTH_LONG);
                            toast.show();
                        }
                        detenerActividad();  //Si se detecto el peligro, se detiene la actividad
                    }
                });

            }
        }).start();


    }

    private void notificacionSMS() {
        nm.cancel(1); //Cancelo la notificacion de alerta, para enviar una notificacion de mensaje enviado.
        int notId= 2;

        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.corredor);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("ALERTA")
                .setContentText("Se envió un SMS de alerta.")
                .setSmallIcon(R.drawable.alerta)
                .setAutoCancel(true)
                .setLargeIcon(bitmap)
                .setVibrate(new long[]{100, 250, 100, 500});

        nm.notify(notId,builder.build());

    }

    public void mandarSMS(){
        SmsManager sms = SmsManager.getDefault();
        String numero="";
        Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String coordenadas=getCoordenadas(loc);
        String direccion=getDireccion(loc);
        String mensaje="ALERTA: Necesito ayuda en "+direccion+" "+coordenadas;

        sms.sendTextMessage(numero,null,mensaje,null,null);
        Toast toast = Toast.makeText(getApplicationContext(),"MENSAJE ENVIADO",Toast.LENGTH_LONG);
        toast.show();
    }


    private float roundTwoDecimals(float d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Float.valueOf(twoDForm.format(d));
    }

    @Override
    public void botonPresionado() {
        presionado=true;

    }

    public void comenzarControlGPS() {
        locManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            homeFragment.cambiarLblGps(true);
                            gpsActivado=true;
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            homeFragment.cambiarLblGps(false);
                            gpsActivado=false;
                        }
                    });
                }
            }
        },0,1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notificationFlag) {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            EstoyBienFragment estoyBienFragment = new EstoyBienFragment();
            ft.replace(android.R.id.content, estoyBienFragment).commit();
            notificationFlag=false;
        }
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

    public boolean getGpsActivado (){
        return gpsActivado;
    }

    public String getCoordenadas(Location loc) {
        String toReturn="";
        if(loc!=null){
            float lat = roundTwoDecimals((float) loc.getLatitude());
            float lon = roundTwoDecimals((float) loc.getLongitude());
            toReturn = "Lat: "+lat+"Lon: "+lon;
        }

        return toReturn;
    }

    public boolean getDetenido() {
        return detenido;
    }
}
