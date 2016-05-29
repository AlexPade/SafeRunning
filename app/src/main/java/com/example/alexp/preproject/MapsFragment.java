package com.example.alexp.preproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsFragment extends Fragment {

    private MapView mapView;
    private GoogleMap mMap;
    private LocationManager locManager;
    private UbicacionListener locListener;
    private PolylineOptions ruta;
    private LatLng ubicacionInicial;
    private boolean tengoPermisos;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_maps, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMap = mapView.getMap();
        onMapReady(mMap);
        return v;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setBuildingsEnabled(true);

        boolean tengoPermisos = utilizarGPS();

        /*
        if (tengoPermisos) {
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    locListener.Centrar(false);
                }
            });
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    locListener.Centrar(true);
                    return false;
                }
            });
        }
        */
    }

    private boolean utilizarGPS() {

        boolean resultado = ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (resultado) {
            mMap.setMyLocationEnabled(true);

            locManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
            ruta = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);

            //Se pide la ubicacion inicial
            Location l = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            ubicacionInicial = new LatLng(l.getLatitude(), l.getLongitude());
            mMap.addMarker(new MarkerOptions().position(ubicacionInicial).title("Comienzo"));
            ruta.add(ubicacionInicial);

            //Llevo la camara a la posicion inicial
            CameraPosition nuevaPos = new CameraPosition(ubicacionInicial, (float) 18.0, (float) 45.0, l.getBearing());
            CameraUpdate nuevaVista = CameraUpdateFactory.newCameraPosition(nuevaPos);
            mMap.moveCamera(nuevaVista);

            locListener = new UbicacionListener(mMap,ruta,ubicacionInicial,this.getActivity().getApplicationContext());

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        }
        return resultado;
    }

    private void apagarMapa()
    {
        if (tengoPermisos) {
            locManager.removeUpdates(locListener);
            Polyline recorrido = locListener.getRecorrido();
            if (recorrido != null) {
                //TODO agregar a la base de datos para el historial
                for (LatLng l : recorrido.getPoints()) {
                    //TODO se calcula la distancia recorrida
                }
            }
        }
    }


    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
