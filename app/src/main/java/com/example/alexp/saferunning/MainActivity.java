package com.example.alexp.saferunning;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements HomeFragment.FuncionesHome, EstoyBienFragment.estoyBienListener, MapsFragment.FuncionesMaps, SettingsFragment.FuncionesSettings{

    private LocationManager locManager;
    private LocationListener locListener;
    private ArrayList<LatLng> posiciones;
    private Timer reloj;
    private double distancia;
    private String tiempo;
    private int tiempoNro;

    private HomeFragment homeFragment;
    private String estado;
    private MapsFragment mapsFragment;
    private Uri contactData;

    private float velocidad; //En M/S
    private float km; //Velocidad en KM/H
    private LatLng target;
    private boolean centrarMapa;

    boolean detenido;
    boolean presionado;
    boolean notificationFlag;
    boolean gpsActivado;
    NotificationManager nm;
    private static final int PICK_CONTACT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FragmentTabHost mTabHost;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        posiciones = new ArrayList<LatLng>();
        centrarMapa = true;
        distancia = 0;
        tiempo = "00:00";
        tiempoNro = 0;
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("", getResources().getDrawable(R.drawable.hometab)),
                    HomeFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("mapa")
                .setIndicator("", getResources().getDrawable(R.drawable.mapatab)), MapsFragment.class, null);


        mTabHost.addTab(mTabHost.newTabSpec("ajustes")
                .setIndicator("", getResources().getDrawable(R.drawable.ajustestab)), SettingsFragment.class, null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar,menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                abrirDialogHelp();
                return true;

        }
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
                    if (posiciones.size()>2) {
                        LatLng ultimoPunto = posiciones.get(posiciones.size() - 1);
                        distancia = distancia + getDistance(ubicacion.latitude,ubicacion.longitude,ultimoPunto.latitude,ultimoPunto.longitude);
                    }
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
            Toast toast = Toast.makeText(getApplicationContext(),"El GPS esta desactivado",Toast.LENGTH_SHORT);
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
                        while ((i<60)&&(!corriendo)&&(!detenido)){       //Repito mientras velocidad==0 durante 30 segundos
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
        comenzarReloj();
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
        //estado="";
        homeFragment.noClickeableDetener();
        //if(homeFragment!=null)
          //  homeFragment.prenderColorLed("nada"); //Apago todos los leds
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
                        estado="";
                        if(homeFragment!=null)
                            homeFragment.prenderColorLed("nada"); //Apago todos los leds
                    }
                });
            }
        };
        reloj.cancel();
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
                while((!presionado)&&(i<30)) { //15 segundos de espera para apretar el boton
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

    public String calcularCalorias(){
        String toret="";

        return toret;
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
        String numero=getNumero();
        Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String coordenadas=getCoordenadas(loc);
        String direccion=getDireccion(loc);
        String mensaje="ALERTA: Necesito ayuda en "+direccion+" "+coordenadas;

        sms.sendTextMessage(numero,null,mensaje,null,null);
        Toast toast = Toast.makeText(getApplicationContext(),"MENSAJE ENVIADO",Toast.LENGTH_LONG);
        toast.show();
    }

    private void abrirDialogHelp(){
        int dialog_message=R.string.help;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void abrirDialogoAbout(){
        int dialog_message=R.string.about;
        int dialog_title=R.string.abouttitle;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialog_message);
        builder.setTitle(dialog_title);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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

    private void comenzarReloj() {
        reloj = new Timer();
        TimerTask tarea = new TimerTask() {

            int segundos = -1;
            int minutos = 0;
            int horas = 0;
            @Override
            public void run() {

                segundos++;
                if (segundos>59)
                {
                    segundos = 0;
                    tiempoNro++; //Usado para calcular calorias
                    minutos++;
                    if (minutos>59) {
                        minutos = 0;
                        horas++;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(homeFragment!=null) {
                            homeFragment.actualizarTiempo(horas,minutos,segundos);
                        }
                    }
                });
            }
        };
        reloj.schedule(tarea,0,1000);
    }

    public double getDistance(double lat1,double lon1, double lat2, double lon2){
        int Radius = 6371000; //Radio de la tierra

        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon /2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return (Radius * c);

    }


    public String cadenaContacto() {
        String temp = "";
        try {
            FileInputStream arch = openFileInput("Contactos_emergencia.txt");
            int c;
            while ((c = arch.read()) != '#') {
                if(c!='$'){
                    if(c=='/')
                        temp=temp+" ";
                    else
                        temp = temp + Character.toString((char) c);
                }
            }
            arch.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException f) {
            f.printStackTrace();
        }

        return temp;
    }

    private String getNumero(){
        String temp="";
        try {
            FileInputStream arch = openFileInput("Contactos_emergencia.txt");
            int c;
            while ((c = arch.read()) != '/');
            while ((c = arch.read()) != '#') {
                    if((c!='(')&&(c!=')')&&(c!=' ')&&(c!='-'))
                        temp = temp + Character.toString((char) c);
            }
            arch.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException f) {
            f.printStackTrace();
        }
        return temp;
    }

    private String getPeso(){  //solo debe ser llamado si calculaCalorias es true
        String toret="";
        try {int c;
            FileInputStream arch = openFileInput("Contactos_emergencia.txt");
            while ((arch.read()) != '#') ;
            while ((arch.read()) != '/') ;
            c=arch.read();
            do{
                toret+=Character.toString((char)c);
                c=arch.read();
            }while(c!= '/');

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException f) {
            f.printStackTrace();
        }
            return toret;
    }

    private String getEdad(){  //solo debe ser llamado si calculaCalorias es true
        String toret="";
        try {int c;
            FileInputStream arch = openFileInput("Contactos_emergencia.txt");
            while ((arch.read()) != '#') ;
            arch.read(); //descarto el signo '$'
            c=arch.read();
            toret+=Character.toString((char)c);
            c=arch.read();
            toret+=Character.toString((char)c);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException f) {
            f.printStackTrace();
        }
        return toret;
    }

    private String getSexo(){  //solo debe ser llamado si calculaCalorias es true
        String toret="";       //retorna m para masculino y f para femenino
        try {int c;
            FileInputStream arch = openFileInput("Contactos_emergencia.txt");
            while ((arch.read()) != '#') ;
            while ((arch.read()) != '/') ;
            arch.read(); //descarto el signo '/'
            while ((arch.read()) != '/') ;
            c=arch.read();
            toret+=Character.toString((char)c);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException f) {
            f.printStackTrace();
        }
        return toret;
    }

    public String getTiempo(){
        return tiempo;
    }

    public int getTiempoNro(){
        return tiempoNro;
    }

    public void setTiempo(String s){
        tiempo = s;
    }

    private boolean calculaCalorias(){ //metodo que retorna true si se selecciono la opcion calcular las calorias y false en caso contrario
        boolean calcula=true;
        try {
            FileInputStream arch = openFileInput("Contactos_emergencia.txt");
            while ((arch.read()) != '#') ;
            if (arch.read() != '$')
                calcula = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException f) {
            f.printStackTrace();
        }
        return calcula;
    }

    public void pickAContactNumber() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    public void ObtenerContacto() throws IOException {
        String nombre = null;
        String numero = null;
        String id = null;
        int tieneNum = 0;

        Cursor cursorNom = getContentResolver().query(contactData, null, null, null, null);

        if (cursorNom.moveToFirst()) {

            tieneNum = cursorNom.getInt(cursorNom.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (tieneNum == 1) {
                //Obtengo su nombre a partir de la query cursorNom
                nombre = cursorNom.getString(cursorNom.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Cursor cursorID = getContentResolver().query(contactData,
                        new String[]{ContactsContract.Contacts._ID},
                        null, null, null);
                if (cursorID.moveToFirst()) {

                    id = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
                }

                cursorID.close();

                Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                        new String[]{id},
                        null);

                if (cursorPhone.moveToFirst()) {
                    numero = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }

                cursorPhone.close();

                String cadena = "$" + nombre + "/" + numero + "#"; //donde / es el separador, $ es el caracter de comienzo y # el de finalizacion

                FileInputStream arch = openFileInput("Contactos_emergencia.txt");

                int c,cant=0;
                c = arch.read();
                while (c!=-1) {
                    if(cant==1)
                        cadena+=Character.toString((char) c);
                    if (c == '#')
                        cant++;
                    c = arch.read();
                }
                arch.close();

                FileOutputStream fos = openFileOutput("Contactos_emergencia.txt", MODE_PRIVATE);
                fos.write(cadena.getBytes());
                fos.close();
            }
        }
        cursorNom.close();
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == PICK_CONTACT && resultCode == RESULT_OK) {
            contactData = data.getData();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        2);
            } else {
                try {
                    ObtenerContacto();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        ObtenerContacto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {

                }
                return;
            }


        }
    }

}
