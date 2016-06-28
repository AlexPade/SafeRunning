package com.example.alexp.saferunning;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private View btnInicio;
    private View btnDetener;
    private TextView lblVel;
    private ImageView ledRojo;
    private ImageView ledAmarillo;
    private ImageView ledVerde;
    private TextView estado;
    private ImageView lblGPS;
    private TextView lblCro;
    private String ledPrendido = "apagados";
    private Chronometer cronometro;

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

        btnInicio = view.findViewById(R.id.inicio);
        btnDetener = view.findViewById(R.id.detener);
        lblVel = (TextView) view.findViewById(R.id.vel);
        lblGPS = (ImageView) view.findViewById(R.id.gps);
        lblCro = (TextView) view.findViewById(R.id.cro);
        if (lblCro.getText().length()<1)
            lblCro.setText(mListener.getTiempo());

        btnInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.comenzarActividad();
                if (mListener.getGpsActivado()) {
                    mListener.comenzarControlDeVelocidad();
                    btnInicio.setVisibility(View.INVISIBLE);
                    btnDetener.setVisibility(View.VISIBLE);
                }

            }
        });

        btnDetener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.detenerActividad();

            }
        });

        mListener.comenzarControlGPS();


        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("flag", 1);
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
        String act = mListener.getEstado();
        if (act != null) {
            btnInicio.setVisibility(View.INVISIBLE);
            btnDetener.setVisibility(View.VISIBLE);
            if (act.equals("peligro")) {
                ledRojo.setImageResource(R.drawable.rojoprendido);
                estado.setText(R.string.danger);
            } else if (act.equals("detenido")) {
                ledAmarillo.setImageResource(R.drawable.amarilloprendido);
                estado.setText(R.string.stopped);
            } else if (act.equals("corriendo")) {
                ledVerde.setImageResource(R.drawable.verdeprendido);
                estado.setText(R.string.moving);
            } else {
                btnDetener.setVisibility(View.INVISIBLE);
                btnInicio.setVisibility(View.VISIBLE);
            }
        }

        lblVel.setText(String.valueOf(mListener.getKM()) + "KM/H");


    }

    @Override
    public void onPause() {
        Log.d("Pause", "Pause");
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Start", "Start");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("stop", "stop");

    }

    public void cambiarBoton() {
        btnInicio.setVisibility(View.VISIBLE);
        btnDetener.setVisibility(View.INVISIBLE);
        btnDetener.setEnabled(true);

    }

    public void noClickeableDetener() {
        btnDetener.setEnabled(false);
    }

    public void cambiarLblGps(boolean prendido) {
        if (prendido)
            lblGPS.setImageResource(R.drawable.gpson);
        else
            lblGPS.setImageResource(R.drawable.gpsoff);
    }

    public void actualizarVelocidad(float vel) {
        lblVel.setText(String.valueOf(vel) + "KM/H");
    }

    public void actualizarTiempo(int horas, int minutos, int segundos) {
        String hs;
        String min;
        String seg;

        //Parseo HS
        if (horas<10)
            hs="0"+horas;
        else
            hs=""+horas;
        //Parseo MIN
        if (minutos<10)
            min = "0"+minutos;
        else
            min = ""+minutos;
        //Parseo SEG
        if (segundos<10)
            seg = "0"+segundos;
        else
            seg = ""+segundos;

        String tiempo;
        if (horas<1)
            tiempo = ""+min+":"+seg;
        else
            tiempo = ""+hs+":"+min+":"+seg;

        lblCro.setText(tiempo);
        mListener.setTiempo(tiempo);
    }

    protected void prenderColorLed(String color) {
        if (color.equals("rojo")) { //Prendo rojo y apago amarillo y verde
            ledRojo.setImageResource(R.drawable.rojoprendido);
            ledAmarillo.setImageResource(R.drawable.amarillo);
            ledVerde.setImageResource(R.drawable.verde);
            estado.setText(R.string.danger);
            ledPrendido = "rojo";
        }
        if (color.equals("amarillo")) { //Prendo amarillo
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarilloprendido);
            ledVerde.setImageResource(R.drawable.verde);
            estado.setText(R.string.stopped);
            ledPrendido = "amarillo";
        }
        if (color.equals("verde")) { //Prendo verde
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarillo);
            ledVerde.setImageResource(R.drawable.verdeprendido);
            estado.setText(R.string.moving);
            ledPrendido = "verde";
        }
        if (color.equals("nada")) { //Ningun led prendido
            ledRojo.setImageResource(R.drawable.rojo);
            ledAmarillo.setImageResource(R.drawable.amarillo);
            ledVerde.setImageResource(R.drawable.verde);
            estado.setText("");
            ledPrendido = "apagados";
        }
    }

    public interface FuncionesHome {
        void comenzarActividad();

        void comenzarControlDeVelocidad();

        void comenzarControlGPS();

        void detenerActividad();

        float getKM();

        String getEstado();

        boolean getGpsActivado();

        String calcularCalorias();

        boolean getDetenido();

        void setTiempo(String s);

        String getTiempo();
    }
}

