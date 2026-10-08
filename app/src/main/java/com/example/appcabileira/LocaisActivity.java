package com.example.appcabileira;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocaisActivity extends AppCompatActivity {

    private MapView mapCrud;
    private Marker markerEdicao;
    private MapEventsOverlay mapEventsOverlay;
    private EditText editNomeLocal, editEndereco;
    private Button btnSalvar, btnExcluir, btnCancelar;
    private ScrollView scrollView;
    private LinearLayout containerLista;
    private ApiService apiService;
    private String emailLogado;
    private int idEdicaoAtual = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locais);

        apiService = ApiClient.getClient().create(ApiService.class);
        SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
        emailLogado = prefs.getString("email_usuario", "");

        scrollView = findViewById(R.id.scroll_view_locais);
        mapCrud = findViewById(R.id.map_crud);
        editNomeLocal = findViewById(R.id.edit_nome_local);
        editEndereco = findViewById(R.id.edit_endereco);
        btnSalvar = findViewById(R.id.btn_salvar_local);
        btnExcluir = findViewById(R.id.btn_excluir_local);
        btnCancelar = findViewById(R.id.btn_cancelar_edicao);
        containerLista = findViewById(R.id.container_lista_locais);

        configurarMapa();
        carregarLocais();

        btnSalvar.setOnClickListener(v -> salvarOuAtualizarLocal());
        btnCancelar.setOnClickListener(v -> limparModoEdicao());
        btnExcluir.setOnClickListener(v -> excluirLocalAtual());
    }

    private void configurarMapa() {
        mapCrud.setMultiTouchControls(true);
        GeoPoint startPoint = new GeoPoint(-23.5505, -46.6333);
        mapCrud.getController().setZoom(14.0);
        mapCrud.getController().setCenter(startPoint);

        mapCrud.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                scrollView.requestDisallowInterceptTouchEvent(true);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                scrollView.requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        markerEdicao = new Marker(mapCrud);
        markerEdicao.setPosition(startPoint);
        markerEdicao.setTitle("Novo Local (Toque no mapa para mover)");

        Drawable iconeEdicao = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default);
        iconeEdicao.setTint(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        markerEdicao.setIcon(iconeEdicao);

        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                markerEdicao.setPosition(p);
                mapCrud.invalidate();
                return true;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        };
        mapEventsOverlay = new MapEventsOverlay(receiver);

        mapCrud.getOverlays().add(mapEventsOverlay);
        mapCrud.getOverlays().add(markerEdicao);
    }

    private void carregarLocais() {
        apiService.listarLocais(emailLogado).enqueue(new Callback<List<LocalServico>>() {
            @Override
            public void onResponse(Call<List<LocalServico>> call, Response<List<LocalServico>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    montarListaEMarcadores(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<LocalServico>> call, Throwable t) {}
        });
    }

    private void montarListaEMarcadores(List<LocalServico> locais) {
        containerLista.removeAllViews();

        mapCrud.getOverlays().clear();
        mapCrud.getOverlays().add(mapEventsOverlay);
        mapCrud.getOverlays().add(markerEdicao);

        LayoutInflater inflater = LayoutInflater.from(this);
        Drawable fotoPlaceholder = ContextCompat.getDrawable(this, R.drawable.place_holder);

        for (LocalServico local : locais) {
            Marker m = new Marker(mapCrud);
            m.setPosition(new GeoPoint(local.latitude, local.longitude));
            m.setTitle(local.nome_local);
            m.setSnippet("Profissional: " + local.nome + " " + local.sobrenome + "\nEndereço: " + local.endereco);
            if (fotoPlaceholder != null) {
                m.setImage(fotoPlaceholder);
            }
            mapCrud.getOverlays().add(m);

            View viewItem = inflater.inflate(R.layout.item_local_salvo, containerLista, false);
            TextView txtNome = viewItem.findViewById(R.id.txt_item_nome);
            TextView txtEndereco = viewItem.findViewById(R.id.txt_item_endereco);

            txtNome.setText(local.nome_local);
            txtEndereco.setText(local.endereco);

            viewItem.setOnClickListener(v -> {
                idEdicaoAtual = local.id;
                editNomeLocal.setText(local.nome_local);
                editEndereco.setText(local.endereco);

                GeoPoint geo = new GeoPoint(local.latitude, local.longitude);
                markerEdicao.setPosition(geo);
                mapCrud.getController().animateTo(geo);

                btnSalvar.setText("Atualizar Local");
                btnExcluir.setVisibility(View.VISIBLE);
                btnCancelar.setVisibility(View.VISIBLE);
                scrollView.smoothScrollTo(0, 0);
            });

            containerLista.addView(viewItem);
        }
        mapCrud.invalidate();
    }

    private void salvarOuAtualizarLocal() {
        String nome = editNomeLocal.getText().toString().trim();
        String endereco = editEndereco.getText().toString().trim();
        double lat = markerEdicao.getPosition().getLatitude();
        double lng = markerEdicao.getPosition().getLongitude();

        if (nome.isEmpty() || endereco.isEmpty()) {
            Toast.makeText(this, "Preencha os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idEdicaoAtual == -1) {
            apiService.salvarLocal(nome, endereco, lat, lng, emailLogado).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    Toast.makeText(LocaisActivity.this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    limparModoEdicao();
                    carregarLocais();
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {}
            });
        } else {
            apiService.atualizarLocal(idEdicaoAtual, nome, endereco, lat, lng).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    Toast.makeText(LocaisActivity.this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    limparModoEdicao();
                    carregarLocais();
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {}
            });
        }
    }

    private void excluirLocalAtual() {
        if (idEdicaoAtual == -1) return;
        apiService.excluirLocal(idEdicaoAtual).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Toast.makeText(LocaisActivity.this, "Local Excluído!", Toast.LENGTH_SHORT).show();
                limparModoEdicao();
                carregarLocais();
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {}
        });
    }

    private void limparModoEdicao() {
        idEdicaoAtual = -1;
        editNomeLocal.setText("");
        editEndereco.setText("");
        btnSalvar.setText("Salvar Novo");
        btnExcluir.setVisibility(View.GONE);
        btnCancelar.setVisibility(View.GONE);
        editNomeLocal.requestFocus();
    }

    @Override
    protected void onResume() { super.onResume(); if (mapCrud != null) mapCrud.onResume(); }
    @Override
    protected void onPause() { super.onPause(); if (mapCrud != null) mapCrud.onPause(); }
}