package com.example.appcabileira;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.edit_email_login);
        editSenha = findViewById(R.id.edit_senha_login);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnNovaConta = findViewById(R.id.btn_nova_conta);
        Button btnVoltar = findViewById(R.id.btn_voltar);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        btnNovaConta.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            Call<ApiResponse> call = apiService.login(email, senha);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if ("logado".equals(response.body().status)) {
                            SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
                            prefs.edit()
                                    .putBoolean("usuario_logado", true)
                                    .putString("nome_usuario", response.body().nome)
                                    .putString("tipo_usuario", response.body().tipo_usuario)
                                    .putString("email_usuario", email)
                                    .apply();

                            Toast.makeText(LoginActivity.this, "Login efetuado!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "Email ou senha incorretos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}