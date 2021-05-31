package com.dmr.mifare_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private List<Historial> historialList;
    private HistorialAdapter historialAdapter;
    private RecyclerView recyclerView;

    private DatabaseReference database;

    public HistorialActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Componentes del menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.historialActivity); // este es el HOME

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.historialActivity:
                        return true;
                    case R.id.pagoActivity:
                        startActivity(new Intent(getApplicationContext(),PagoActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.recargaActivity:
                        startActivity(new Intent(getApplicationContext(),RecargaActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.configuracionActivity:
                        startActivity(new Intent(getApplicationContext(),ConfiguracionActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                }
                return false;
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        database = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("tagsGuardados/transactions");

        historialList = new ArrayList<Historial>();

        recyclerView = findViewById(R.id.RecyclerViewHistorial);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        historialAdapter = new HistorialAdapter(this, historialList);
        recyclerView.setAdapter(historialAdapter);


        database.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Historial historial = dataSnapshot.getValue(Historial.class);
                    historialList.add(historial);
                }

                historialAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error);
            }
        });


    }
}