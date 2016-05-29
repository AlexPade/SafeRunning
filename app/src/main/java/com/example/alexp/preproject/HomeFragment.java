package com.example.alexp.preproject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
    private String ledPrendido="apagados";

    private FuncionesHome mListener;

    private MainActivity activity;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        activity = (MainActivity) getActivity();
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


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("asd","ASDSAD");
        outState.putInt("flag",1);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Resume","Resume");
        if(ledPrendido.equals("rojo")) {
            ledRojo.setImageResource(R.drawable.rojoprendido);
            estado.setText("PELIGRO");
        }
        if(ledPrendido.equals("amarillo")) {
            ledAmarillo.setImageResource(R.drawable.amarilloprendido);
            estado.setText("Detenido");
        }
        if(ledPrendido.equals("verde")) {
            ledVerde.setImageResource(R.drawable.verdeprendido);
            estado.setText("Corriendo");
        }
        float kms;
        kms = activity.getKM();
        lblVel.setText(String.valueOf(kms)+"KM/H");


    }

    @Override
    public void onPause() {
        Log.d("Pause","Pause");
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Start","Start");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("stop","stop");

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
            ledPrendido = "rojo";
        }
        if(color.equals("amarillo")) { //Prendo amarillo
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarilloprendido);
            ledVerde.setImageResource(R.drawable.verde);
            estado.setText("Detenido");
            ledPrendido = "amarillo";
        }
        if (color.equals("verde")){ //Prendo verde
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarillo);
            ledVerde.setImageResource(R.drawable.verdeprendido);
            estado.setText("Corriendo");
            ledPrendido = "verde";
        }
        if (color.equals("nada")){ //Ningun led prendido
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarillo);
            ledVerde.setImageResource(R.drawable.verde);
            estado.setText("");
            ledPrendido = "apagados";
        }
    }

    public interface FuncionesHome{
        void comenzarActividad();
        void comenzarControlDeVelocidad();
        void prepararGPS();
    }
}
