package com.subnotway.puntos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText edtIdentificador;
    private EditText edtServidor;
    private Button btnConsultar;
    private ProgressBar progressBar;
    private TextView txtMensajeError;

    private CardView cardResultado;
    private TextView txtNombreCliente;
    private TextView txtIdentificadorCliente;
    private TextView txtPuntos;
    private TextView txtEstado;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtIdentificador = findViewById(R.id.edtIdentificador);
        edtServidor = findViewById(R.id.edtServidor);
        btnConsultar = findViewById(R.id.btnConsultar);
        progressBar = findViewById(R.id.progressBar);
        txtMensajeError = findViewById(R.id.txtMensajeError);

        cardResultado = findViewById(R.id.cardResultado);
        txtNombreCliente = findViewById(R.id.txtNombreCliente);
        txtIdentificadorCliente = findViewById(R.id.txtIdentificadorCliente);
        txtPuntos = findViewById(R.id.txtPuntos);
        txtEstado = findViewById(R.id.txtEstado);

        btnConsultar.setOnClickListener(v -> consultarPuntos());
    }

    private void consultarPuntos() {
        String identificador = edtIdentificador.getText().toString().trim();
        String servidor = edtServidor.getText().toString().trim();

        if (identificador.isEmpty()) {
            mostrarError("Por favor ingresa un identificador (NIT o DPI).");
            return;
        }

        if (servidor.isEmpty()) {
            mostrarError("Por favor ingresa la dirección del servidor.");
            return;
        }

        mostrarCargando(true);

        executor.execute(() -> {
            try {
                String base = servidor.replaceAll("/+$", "");
                String endpoint = base + "/api/clientes/puntos/" + URLEncoder.encode(identificador, "UTF-8");
                URL url = new URL(endpoint);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);
                conn.setRequestProperty("Accept", "application/json");

                int statusCode = conn.getResponseCode();
                InputStream stream = (statusCode >= 200 && statusCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONObject json = new JSONObject(response.toString());
                boolean encontrado = json.optBoolean("encontrado", false);

                if (encontrado) {
                    String nombre = json.optString("nombre", "");
                    String nit = json.optString("identificador", "");
                    int puntos = json.optInt("puntos", 0);
                    String estado = json.optString("estado", "ACTIVO");

                    mainHandler.post(() -> mostrarResultado(nombre, nit, puntos, estado));
                } else {
                    String mensaje = json.optString("mensaje", "Cliente no encontrado.");
                    mainHandler.post(() -> mostrarError(mensaje));
                }

            } catch (Exception e) {
                mainHandler.post(() -> mostrarError("No se pudo conectar con el servidor. Verifica que esté activo en " + servidor));
            } finally {
                mainHandler.post(() -> mostrarCargando(false));
            }
        });
    }

    private void mostrarResultado(String nombre, String identificador, int puntos, String estado) {
        txtMensajeError.setVisibility(View.GONE);
        txtNombreCliente.setText(nombre);
        txtIdentificadorCliente.setText("Identificador: " + identificador);
        txtPuntos.setText(String.valueOf(puntos));
        txtEstado.setText(estado);
        cardResultado.setVisibility(View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        cardResultado.setVisibility(View.GONE);
        txtMensajeError.setText(mensaje);
        txtMensajeError.setVisibility(View.VISIBLE);
    }

    private void mostrarCargando(boolean cargando) {
        if (cargando) {
            btnConsultar.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            txtMensajeError.setVisibility(View.GONE);
        } else {
            btnConsultar.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }
    }
}
