package com.example.appcabileira;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.lang.reflect.Field;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MapView map;

    private void carregarTodosOsLocaisNoMapa() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.buscarTodosLocais().enqueue(new Callback<List<LocalServico>>() {
            @Override
            public void onResponse(Call<List<LocalServico>> call, Response<List<LocalServico>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LocalServico> locais = response.body();

                    Drawable fotoPlaceholder = ContextCompat.getDrawable(MainActivity.this, R.drawable.place_holder);

                    for (LocalServico local : locais) {
                        Marker marcador = new Marker(map);
                        marcador.setPosition(new GeoPoint(local.latitude, local.longitude));
                        marcador.setTitle(local.nome_local);

                        marcador.setSnippet("Profissional: " + local.nome + " " + local.sobrenome + "\nEndereço: " + local.endereco);


                        if (fotoPlaceholder != null) {
                            marcador.setImage(fotoPlaceholder);
                        }

                        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        map.getOverlays().add(marcador);
                    }
                    map.invalidate();
                }
            }

            @Override
            public void onFailure(Call<List<LocalServico>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erro ao carregar mapa de profissionais.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    Toast.makeText(MainActivity.this, "Lido: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        map = findViewById(R.id.map_view);


        SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
        String tipoUsuario = prefs.getString("tipo_usuario", "cliente");
        String nomeUsuario = prefs.getString("nome_usuario", "Usuário");


        View headerView = navigationView.getHeaderView(0);
        TextView txtUserName = headerView.findViewById(R.id.txt_user_name);
        txtUserName.setText(nomeUsuario);

        Menu menu = navigationView.getMenu();

        MenuItem navEditLocais = menu.findItem(R.id.nav_edit_locais);
        navEditLocais.setVisible(tipoUsuario.equals("profissional"));

        MenuItem navLogout = menu.findItem(R.id.nav_logout);
        SpannableString spanString = new SpannableString(navLogout.getTitle());
        spanString.setSpan(new ForegroundColorSpan(Color.RED), 0, spanString.length(), 0);
        navLogout.setTitle(spanString);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_qr_code) {
                ScanOptions options = new ScanOptions();
                options.setPrompt("Aponte para o QR Code do Profissional");
                barcodeLauncher.launch(options);
            } else if (id == R.id.nav_edit_locais) {
                startActivity(new Intent(MainActivity.this, LocaisActivity.class));
            } else if (id == R.id.nav_logout) {
                prefs.edit().putBoolean("usuario_logado", false).apply();
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // configuração inicial do mapa no meio de são paulo pq sim
        map.setMultiTouchControls(true);
        GeoPoint startPoint = new GeoPoint(-23.5505, -46.6333);
        map.getController().setZoom(14.0);
        map.getController().setCenter(startPoint);

        carregarTodosOsLocaisNoMapa();

    }

    @Override
    public void onResume() { super.onResume(); if (map != null) map.onResume(); }

    @Override
    public void onPause() { super.onPause(); if (map != null) map.onPause(); }
}