package com.dmr.mifare_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PagoActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;

    EditText precio;
    Button pago;
    TextView cantidad;

    public PagoActivity() {
        // Required empty public constructor
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        // Componentes del menu inferior (NO mover)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.pagoActivity); // este es el HOME

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.recargaActivity:
                        startActivity(new Intent(getApplicationContext(),RecargaActivity.class));
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
                    case R.id.pagoActivity:
                        return true;
                }
                return false;
            }
        });

        // IU elements
        pago = (Button) findViewById(R.id.pagoButton);
        precio = (EditText) findViewById(R.id.precioPago);
        cantidad = (TextView) findViewById(R.id.cantidadLabelPago);

        precio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                cantidad.setText("$0.00");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String inputText = precio.getText().toString();
                cantidad.setText("$" + inputText);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        pago.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                makePago();

            }
        });


    }

    public void getCurrentAmountAndUpdateTag(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        mDatabase.child("tagsGuardados").child("montoActual").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            double montoActual = (double) (long) dataSnapshot.getValue();

                            double precioVar = Double.parseDouble(precio.getText().toString());
                            double total = montoActual - precioVar;
                            mDatabase.child("tagsGuardados").child("montoActual").setValue(total);

                            Toast.makeText(getApplicationContext(), "Monto Actual: " + total, Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }


    public void makePago(){

        Date currentTime = Calendar.getInstance().getTime();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        //convertir precio a double
        double precioVar = Double.parseDouble(precio.getText().toString());

        // convertir fecha a string
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(currentTime);

        getCurrentAmountAndUpdateTag();

        Historial transaccion = new Historial(strDate, "Pago", precioVar);

        String idFire = mDatabase.push().getKey();

        mDatabase.child("tagsGuardados/transactions").child(idFire).setValue(transaccion);

        Toast.makeText(getApplicationContext(), "Se ha pagado $" + precio.getText().toString(), Toast.LENGTH_SHORT).show();

    }



}