package com.example.alexp.saferunning;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsFragment extends Fragment {

    private FuncionesSettings mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_settings, container, false);

        TextView textoClick= (TextView) view.findViewById(R.id.textoClick);
        final TextView campoContacto=(TextView) view.findViewById(R.id.campoContacto);
        final CheckBox opcionPro=(CheckBox) view.findViewById(R.id.opcionPro);
        final TextView about= (TextView) view.findViewById(R.id.about);

        campoContacto.setText(mListener.cadenaContacto());
        textoClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.pickAContactNumber();
                //campoContacto.setText(mListener.cadenaContacto());
            }
        });
        opcionPro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(SettingsFragment.this.getContext(),"Disponible en versiones futuras.", Toast.LENGTH_SHORT).show();
                opcionPro.setChecked(false);
            }
        }
        );

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.abrirDialogoAbout();
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FuncionesSettings) {
            mListener = (FuncionesSettings) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FuncionesSettings");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface FuncionesSettings {
        void pickAContactNumber();
        String cadenaContacto();
        void abrirDialogoAbout();
    }
}
