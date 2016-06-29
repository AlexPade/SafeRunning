package com.example.alexp.saferunning;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity implements FirstTimeFragment.FuncionesFirstTime{

    private static final long SPLASH_SCREEN_DELAY = 3000;
    private Uri contactData;
    private static final int PICK_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);


        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                boolean existe = fileExists(getApplicationContext(), "Contactos_emergencia.txt");

                if(existe) {
                   nuevaAct();
                }
                else{
                //Paso 2: Crear una nueva transacción
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                //Paso 3: Crear un nuevo fragmento y añadirlo
                FirstTimeFragment firstTimeFragment = new FirstTimeFragment();
                transaction.add(R.id.contenedor, firstTimeFragment);
                //Paso 4: Confirmar el cambio
                transaction.commit();
                }
            }
        };

        Timer timer = new Timer();
        // Simulate a long loading process on application startup.
        timer.schedule(task, SPLASH_SCREEN_DELAY);

    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    public void ObtenerContacto() throws IOException {
        String nombre = null;
        String numero = null;
        String id = null;
        int tieneNum = 0;

        // querying contact data store
        Cursor cursorNom = getContentResolver().query(contactData, null, null, null, null);

        if (cursorNom.moveToFirst()) {
            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.
            tieneNum = cursorNom.getInt(cursorNom.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (tieneNum == 1) {
                //Obtengo su nombre a partir de la query cursorNom
                nombre = cursorNom.getString(cursorNom.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // getting contacts ID
                Cursor cursorID = getContentResolver().query(contactData,
                        new String[]{ContactsContract.Contacts._ID},
                        null, null, null);
                if (cursorID.moveToFirst()) {

                    id = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
                }

                cursorID.close();

                // Using the contact ID now we will get contact phone number
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
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        ObtenerContacto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void pickAContactNumber() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    public void nuevaAct(){
        Intent mainIntent;
        // Start the next activity
        mainIntent = new Intent().setClass(
                SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        // Close the activity so the user won't able to go back this
        // activity pressing Back button
        finish();
    }

    public boolean controlCampos(String campoEdad, String campoPeso){
        boolean toret=false;
        int campoE,campoP;
        campoE=Integer.parseInt(campoEdad);
        campoP=Integer.parseInt(campoPeso);
        if(campoE>5 && campoE<99 && campoP<300 && campoP >20)
            toret=true;
        return toret;
    }

   public void datosCaloriasEnArchivo(String edad, String peso, String sexo){
       FileOutputStream fos = null;
       try {

           fos = openFileOutput("Contactos_emergencia.txt", MODE_APPEND);       //el modo append evita crear nuevamente el archivo en caso de que exista
       String cadena = "$" + edad + "/" + peso + "/" + sexo + "#"; //donde / es el separador, $ es el caracter de comienzo y # el de finalizacion
           fos.write(cadena.getBytes());
           fos.close();

       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
        catch (IOException e) {
           e.printStackTrace();
       }
   }

    public SplashActivity getActividad(){
        return this;
    }
    }
