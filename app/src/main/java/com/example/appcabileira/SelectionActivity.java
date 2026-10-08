package com.example.appcabileira;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        Button btnProfissional = findViewById(R.id.btn_sou_profissional);
        Button btnCliente = findViewById(R.id.btn_sou_cliente);

        btnProfissional.setOnClickListener(v -> irParaLogin("profissional"));
        btnCliente.setOnClickListener(v -> irParaLogin("cliente"));
    }

    private void irParaLogin(String tipoUsuario) {
        SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("tipo_usuario", tipoUsuario);
        editor.putBoolean("primeira_vez", false);
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}