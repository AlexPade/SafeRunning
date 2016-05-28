package com.example.alexp.preproject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class HomeFragment extends Fragment {

    private TextView lblVel;
    private ImageView ledRojo;
    private ImageView ledAmarillo;
    private ImageView ledVerde;
    private TextView estado;
    private ImageView lblGPS;

    private FuncionesHome mListener;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Leds
        ledRojo = (ImageView) view.findViewById(R.id.ledRojo);
        ledAmarillo = (ImageView) view.findViewById(R.id.ledAmarillo);
        ledVerde = (ImageView) view.findViewById(R.id.ledVerde);
        estado = (TextView) view.findViewById(R.id.estado);


        View btnInicio = view.findViewById(R.id.inicio);
        lblVel = (TextView) view.findViewById(R.id.vel);
        lblGPS = (ImageView) view.findViewById(R.id.gps);

        mListener.prepararGPS();

        btnInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.comenzarActividad();
                mListener.comenzarControlDeVelocidad();

            }
        });

        return view;
    }

    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FuncionesHome) {
            mListener = (FuncionesHome) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void cambiarLblGps(boolean prendido){
        if(prendido)
            lblGPS.setImageResource(R.drawable.gpson);
        else
            lblGPS.setImageResource(R.drawable.gpsoff);
    }

    public void actualizarVelocidad(float vel){
        lblVel.setText(String.valueOf(vel)+"KM/H");
    }

    protected void prenderColorLed(String color){
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

    public interface FuncionesHome{
        void comenzarActividad();
        void comenzarControlDeVelocidad();
        void prepararGPS();
    }
}
