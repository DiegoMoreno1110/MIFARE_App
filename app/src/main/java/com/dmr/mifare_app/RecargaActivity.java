package com.dmr.mifare_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RecargaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.recargaActivity); // este es el HOME

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.pagoActivity:
                        startActivity(new Intent(getApplicationContext(),PagoActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.historialActivity:
                        startActivity(new Intent(getApplicationContext(),HistorialActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.configuracionActivity:
                        startActivity(new Intent(getApplicationContext(),ConfiguracionActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.recargaActivity:
                        return true;
                }
                return false;
            }
        });


    }
}