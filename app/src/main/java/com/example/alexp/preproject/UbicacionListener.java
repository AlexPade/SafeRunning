package com.example.alexp.preproject;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Federico Jinkis on 29/5/2016.
 */
public class UbicacionListener implements android.location.LocationListener {

    private Context contextoDeApp;
    private GoogleMap mMap;
    private PolylineOptions ruta;
    private LatLng ubicacionInicial;
    private Polyline line;
    private boolean centrar = true;
    private Toast desconexion;
    private Toast conexion;

    public UbicacionListener(GoogleMap m, PolylineOptions r, LatLng u, Context c)
    {
        mMap = m;
        ruta = r;
        ubicacionInicial = u;
        contextoDeApp = c;
        desconexion = Toast.makeText(contextoDeApp, "El GPS esta apagado", Toast.LENGTH_SHORT);
        conexion = Toast.makeText(contextoDeApp, "El GPS esta encendido", Toast.LENGTH_SHORT);
    }

    public void Centrar(boolean b)
    {
        centrar = b;
    }

    public Polyline getRecorrido()
    {
        return line;
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng ubicacion = new LatLng(location.getLatitude(), location.getLongitude());

        if (centrar)
        {
            float zoom = mMap.getCameraPosition().zoom;
            float tilt = mMap.getCameraPosition().tilt;
            float bearing = mMap.getCameraPosition().bearing;
            CameraPosition nuevaPos = new CameraPosition(ubicacion, zoom, tilt, bearing);
            CameraUpdate nuevaVista = CameraUpdateFactory.newCameraPosition(nuevaPos);
            mMap.moveCamera(nuevaVista);
            centrar = true; //Para diferenciar del movimiento del usuario
        }

        ruta.add(ubicacion);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(ubicacionInicial).title("Comienzo"));
        line = mMap.addPolyline(ruta);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        conexion.show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        desconexion.show();
    }
}

