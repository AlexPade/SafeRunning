package com.example.alexp.saferunning;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;


public class FirstTimeFragment extends Fragment {

    private FuncionesFirstTime mListener;
    private TextView textView;

    public FirstTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SplashActivity activity =mListener.getActividad();
        RelativeLayout fondo = (RelativeLayout)activity.findViewById(R.id.fondo);
        fondo.setVisibility(View.INVISIBLE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first_time, container, false);
        final boolean[] checked = new boolean[1];
        checked[0]=false;
        final View pickAContact= view.findViewById(R.id.pick);
        final View listo=view.findViewById(R.id.listo);
        final EditText edit1=(EditText)view.findViewById(R.id.edit1);
        final EditText edit2=(EditText)view.findViewById(R.id.edit2);
        final View tv5=view.findViewById(R.id.tv5);
        final RadioButton rbMasc=(RadioButton)view.findViewById(R.id.rbMasc);
        final RadioButton rbFem=(RadioButton)view.findViewById(R.id.rbFem);
        final View tv1=view.findViewById(R.id.tv1);
        final View tv2=view.findViewById(R.id.tv2);
        final View tv3=view.findViewById(R.id.tv3);
        final View tv4=view.findViewById(R.id.tv4);
        final View textError=view.findViewById(R.id.textError);
        pickAContact.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mListener.pickAContactNumber();
                pickAContact.setEnabled(false);
                listo.setEnabled(true);
            }
        });
        View check = view.findViewById(R.id.check);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked[0] = ((CheckBox) v).isChecked();
                if (checked[0])
                {
                    edit1.setEnabled(true);
                    edit2.setEnabled(true);
                    tv5.setEnabled(true);
                    rbMasc.setEnabled(true);
                    rbFem.setEnabled(true);
                    tv1.setEnabled(true);
                    tv2.setEnabled(true);
                    tv3.setEnabled(true);
                    tv4.setEnabled(true);
                }
                else{
                    edit1.setEnabled(false);
                    edit2.setEnabled(false);
                    tv5.setEnabled(false);
                    rbMasc.setEnabled(false);
                    rbFem.setEnabled(false);
                    tv1.setEnabled(false);
                    tv2.setEnabled(false);
                    tv3.setEnabled(false);
                    tv4.setEnabled(false);
                }
            }
        });
        listo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (checked[0]) {
                    String campoEdad = edit1.getText().toString();
                    String campoPeso = edit2.getText().toString();
                    boolean correcto = true;
                    String sexo=null;

                    if (rbFem.isChecked())
                        sexo="f";
                    else
                    if (rbMasc.isChecked())
                        sexo="m";

                    if (campoEdad.matches("") || campoPeso.matches(""))
                        correcto = false;
                    else
                        correcto = mListener.controlCampos(campoEdad, campoPeso);

                    if ((correcto) && (sexo!=null)){
                        mListener.datosCaloriasEnArchivo(campoEdad, campoPeso, sexo);
                        mListener.nuevaAct();
                    }
                    else {
                        textError.setVisibility(View.VISIBLE);
                    }
                }
                else
                    mListener.nuevaAct();
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
        if (context instanceof FuncionesFirstTime) {
            mListener = (FuncionesFirstTime) context;
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

    public interface FuncionesFirstTime{
        void ObtenerContacto() throws IOException;
        void pickAContactNumber();
        void nuevaAct();
        boolean controlCampos(String campoEdad, String campoPeso);
        void datosCaloriasEnArchivo(String edad, String peso, String sexo);
        SplashActivity getActividad();
    }
}
