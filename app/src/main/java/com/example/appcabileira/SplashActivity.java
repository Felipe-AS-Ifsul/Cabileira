package com.example.appcabileira;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);

        boolean primeiraVez = prefs.getBoolean("primeira_vez", true);
        boolean usuarioLogado = prefs.getBoolean("usuario_logado", false);

        Intent intent;

        if (primeiraVez) {
            intent = new Intent(this, SelectionActivity.class);
        } else if (usuarioLogado) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}