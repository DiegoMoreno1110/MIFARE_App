package com.dmr.mifare_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {


    //UI Components
    private Button registro;
    private Button entrar;
    private EditText correo;
    private EditText contraseña;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private User user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
       // getActionBar().hide();

        registro = (Button)findViewById(R.id.buttonRegistrarse);
        entrar = (Button)findViewById(R.id.buttonEntrar);
        correo = (EditText)findViewById(R.id.correo);
        contraseña = (EditText)findViewById(R.id.password);

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("Users");
        auth = FirebaseAuth.getInstance();

        firebaseUser= auth.getCurrentUser();

        // action to recarga fragment if firebaseUsee!= null
        /*if(firebaseUser!=null){
            Intent intent = new Intent(this, RecargaActivity.class);
            startActivity(intent);
        }*/


        // boton de registro
        registro.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (!correo.getText().toString().isEmpty() && !contraseña.getText().toString().isEmpty()){
                    user = new User(correo.getText().toString());
                    registerUser();
                }
                else {
                     failRegister(0);
                }
            }
        });

        entrar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                if (!correo.getText().toString().isEmpty() && !contraseña.getText().toString().isEmpty()){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            correo.getText().toString(),
                            contraseña.getText().toString()
                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(v.getContext(), "Bienvenido", Toast.LENGTH_SHORT).show();
                                // pasar aqui a otra pantalla (recarga fragment)
                                Intent intent = new Intent(v.getContext(), RecargaActivity.class);
                                startActivity(intent);
                            }
                            else {
                                //Log.w(TAG, "signInWithEmail:failure", task.getException());
                               Toast.makeText(v.getContext(), "Datos incorrectos, vuelva a intentarlo.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                  failRegister(0);

                }
            }
        });
    }

    public void registerUser(){
        auth.createUserWithEmailAndPassword(correo.getText().toString(),contraseña.getText().toString()).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);

                        }else{
                            failRegister(1);
                        }
                    }
                }
        );
    }

    public void updateUI(FirebaseUser currentUser){
        //String keyId = mDatabase.push().getKey();
        mDatabase.child(currentUser.getUid()).setValue(user);
        Toast.makeText(this, "Se creó un nuevo usuario con éxito", Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "Acerca su tag al celular para registrarlo con su usuario", Toast.LENGTH_SHORT).show();
        //   resolveReadIntent(this.getIntent());

    }

    public void failRegister(int opcion){
        switch (opcion){
            case 0: // no ingreso datos de registro
                Toast.makeText(this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show();
                break;
            case 1: // fallo al registrar usuario
                Toast.makeText(this, "Falló al registrar usuario", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

}



