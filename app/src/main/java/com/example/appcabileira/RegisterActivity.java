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

public class RegisterActivity extends AppCompatActivity {

    private EditText editNome, editSobrenome, editEmail, editTelefone, editCpf, editSenha, editConfirmaSenha;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getClient().create(ApiService.class);

        editNome = findViewById(R.id.edit_reg_nome);
        editSobrenome = findViewById(R.id.edit_reg_sobrenome);
        editEmail = findViewById(R.id.edit_reg_email);
        editTelefone = findViewById(R.id.edit_reg_telefone);
        editCpf = findViewById(R.id.edit_reg_cpf);
        editSenha = findViewById(R.id.edit_reg_senha);
        editConfirmaSenha = findViewById(R.id.edit_reg_confirma_senha);
        Button btnRegistrar = findViewById(R.id.btn_registrar);

        btnRegistrar.setOnClickListener(v -> {
            String nome = editNome.getText().toString().trim();
            String sobrenome = editSobrenome.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String telefone = editTelefone.getText().toString().trim();
            String cpf = editCpf.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();
            String confirmaSenha = editConfirmaSenha.getText().toString().trim();

            if (nome.isEmpty() || sobrenome.isEmpty() || email.isEmpty() || telefone.isEmpty() || cpf.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!senha.equals(confirmaSenha)) {
                Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
            String tipoUsuario = prefs.getString("tipo_usuario", "cliente"); // default pra cliente caso de bypass


            Call<ApiResponse> call = apiService.registrar(nome, sobrenome, email, telefone, cpf, senha, tipoUsuario);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if ("sucesso".equals(response.body().status)) {
                            Toast.makeText(RegisterActivity.this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                            prefs.edit()
                                    .putBoolean("usuario_logado", true)
                                    .putString("nome_usuario", nome)
                                    .putString("email_usuario", email)
                                    .apply();

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erro: Email ou CPF já cadastrados.", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}