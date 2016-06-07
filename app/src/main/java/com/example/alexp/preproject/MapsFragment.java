package com.example.alexp.preproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Iterator;

public class MapsFragment extends Fragment {


    //ATRIBUTOS
    private MapView mapView;
    private GoogleMap mMap;
    private FloatingActionButton actividadBoton;
    private FuncionesMaps listener;

    private PolylineOptions ruta;
    private boolean tengoPermisos;
    private boolean centrar;
    private boolean actualizado;


    //Interface a implementar
    public interface FuncionesMaps{
        LatLng getTarget();
        void setTarget(LatLng ll);
        boolean getCentrar();
        void setCentrar(boolean b);
        void comenzarActividad();
        void comenzarControlDeVelocidad();
        Iterator<LatLng> obtenerRuta();
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_maps, container, false);

        //Se obtienen el MapView, la veracidad de los permisos y la inicializacion de las variables
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        tengoPermisos = ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        centrar = listener.getCentrar();
        actualizado = false;

        actividadBoton = (FloatingActionButton) v.findViewById(R.id.actividadBotonFlotante);
        actividadBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.comenzarActividad();
                listener.comenzarControlDeVelocidad();
            }
        });
        actividadBoton.show();

        //Se obtiene el mapa y se lo inicia con ciertas configuraciones
        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setBuildingsEnabled(true);

                if (tengoPermisos)
                    mMap.setMyLocationEnabled(true);

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        centrar = false;
                    }
                });

                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        centrar = true;
                        return false;
                    }
                });

                //Seteo el mapa en un lugar (UNS)
                LatLng pos = listener.getTarget();
                if (pos == null)
                    pos = new LatLng(-38.701556, -62.270254);
                cambiarPosCamara(pos, (float) 19, (float) 45, mMap.getCameraPosition().bearing);
            }
        });

        return v;
    }

    public void cambioUbicacion(LatLng ubicacion) {

        if (centrar)
        {
            float zoom = mMap.getCameraPosition().zoom;
            float tilt = mMap.getCameraPosition().tilt;
            float bearing = mMap.getCameraPosition().bearing;
            cambiarPosCamara(ubicacion,zoom,tilt,bearing);
        }

        mMap.clear();
        if ((ruta != null) && (ruta.getPoints().size() > 0)) {
            agregarEtiqueta(ruta.getPoints().get(0), "Comienzo");
            ruta.add(ubicacion);
            mMap.addPolyline(ruta);
        }
    }

    public boolean estadoActualizado(){
        return actualizado;
    }

    private void agregarEtiqueta(LatLng ubicacion, String msj) {
        mMap.addMarker(new MarkerOptions().position(ubicacion).title(msj));
    }

    private void cambiarPosCamara(LatLng ubicacion, float zoom, float tilt, float bearing) {

        CameraPosition nuevaPos = new CameraPosition(ubicacion, zoom, tilt, bearing);
        CameraUpdate nuevaVista = CameraUpdateFactory.newCameraPosition(nuevaPos);
        mMap.moveCamera(nuevaVista);
    }

    //METODOS DE ESTADOS
    @Override
    public void onStart() {
        super.onStart();
        Log.d("MapsFragment","Start");

    }

    @Override
    public void onStop() {
        super.onStop();
        listener.setTarget(mMap.getCameraPosition().target);
        listener.setCentrar(centrar);
        Log.d("MapsFragment","Stop");
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();

        Log.d("MapsFragment","Resume");

        if (mMap != null) {
            mMap.clear();
            ruta = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);

            //Obtengo todas las posiciones
            Iterator<LatLng> it = listener.obtenerRuta();
            while (it.hasNext())
                ruta.add(it.next());

            if (ruta.getPoints().size() > 0) {
                //Etiqueta de comienzo
                agregarEtiqueta(ruta.getPoints().get(0), "Comienzo");

                //Creo la linea en el mapa
                mMap.addPolyline(ruta);

                //Muevo la camara al lugar del usuario
                if (centrar) {
                    int ultimo = ruta.getPoints().size() - 1;
                    cambiarPosCamara(ruta.getPoints().get(ultimo), mMap.getCameraPosition().zoom, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing);
                }
            }

            actualizado = true;
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("MapsFragment","Pause");
        actualizado = false;
        ruta = null;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        actualizado = false;
        centrar = false;
        //mapView.onDestroy();
        ruta = null;
        //mMap = null;
        //actividadBoton = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FuncionesMaps) {
            listener = (FuncionesMaps) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
