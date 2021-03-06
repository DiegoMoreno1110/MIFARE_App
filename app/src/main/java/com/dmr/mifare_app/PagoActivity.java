package com.dmr.mifare_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PagoActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;

    EditText precio;
    Button pago;
    TextView cantidad;
    AlertDialog mTagDialog;

    EditText mDataBloque;



    //NFC
    private static final String TAG = "nfcinventory_simple";
    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadWriteTagFilters;
    private boolean mWriteMode = false;
    private boolean mAuthenticationMode = false;
    private boolean ReadUIDMode = true;
    String[][] mTechList;

    //Aux
    private int bloqueAux;
    private String llaveAux;
    private int sectorAux;
    private boolean bandera = false;


    public PagoActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);


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


        /*
        //NFC
        // get an instance of the context's cached NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // if null is returned this demo cannot run. Use this check if the
        // "required" parameter of <uses-feature> in the manifest is not set
        if (mNfcAdapter == null) {
            Toast.makeText(this, "Su dispositivo no soporta NFC. No se puede correr la aplicaci??n.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // check if NFC is enabled
        //checkNfcEnabled();

        // Handle foreground NFC scanning in this activity by creating a
        // PendingIntent with FLAG_ACTIVITY_SINGLE_TOP flag so each new scan
        // is not added to the Back Stack
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Create intent filter to handle MIFARE NFC tags detected from inside our
        // application
        IntentFilter mifareDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            mifareDetected.addDataType("application/com.e.mifarecontrol");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("No se pudo a??adir un tipo MIME.", e);
        }

        // Create intent filter to detect any MIFARE NFC tag
        mReadWriteTagFilters = new IntentFilter[]{mifareDetected};

        // Setup a tech list for all NFC tags
        mTechList = new String[][]{
                new String[]{
                        MifareClassic.class.getName()
                }
        };
        */


        //Obtener valores tag de FireBase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        //bloque
        mDatabase.child("tagsGuardados").child("bloque").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            bloqueAux = (int) (long) dataSnapshot.getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


        //llave
        mDatabase.child("tagsGuardados").child("llave").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            llaveAux =  (String) dataSnapshot.getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


        //sector
        mDatabase.child("tagsGuardados").child("sector").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            sectorAux =  (int) (long) dataSnapshot.getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


        //findViewById(R.id.buttonAuth).setOnClickListener(mTagAuthenticate);
        //findViewById(R.id.buttonRead).setOnClickListener(mTagRead);

        mDataBloque = ((EditText) findViewById(R.id.editTextTextPersonNameLectura));


        //resolveReadIntent(getIntent());


    }

    public void getCurrentAmountAndUpdateTagPago(){
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

        getCurrentAmountAndUpdateTagPago();

        Historial transaccion = new Historial(strDate, "Pago", precioVar);

        String idFire = mDatabase.push().getKey();

        mDatabase.child("tagsGuardados/transactions").child(idFire).setValue(transaccion);

        Toast.makeText(getApplicationContext(), "Se ha pagado $" + precio.getText().toString(), Toast.LENGTH_SHORT).show();

    }


    /*
    void resolveReadIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            if (ReadUIDMode) {
            } else {
                try {
                    mfc.connect();
                    boolean auth = false;
                    String hexkey = "";
                    //int id = mRadioGroup.getCheckedRadioButtonId();
                    int sector = mfc.blockToSector(Integer.valueOf(bloqueAux));
                    byte[] datakey;



                    //if (auth){
                    if (bandera) {
                        int bloque = Integer.valueOf(bloqueAux);
                        System.out.println("Bandera bloque"  + bloque);
                        byte[] dataread = mfc.readBlock(bloque + 1);
                        Log.i("Bloques", getHexString(dataread, dataread.length));

                        dataread = mfc.readBlock(bloque);
                        String blockread = getHexString(dataread, dataread.length);
                        Log.i(TAG, "Bloque Leido: " + blockread);

                        Editable BlockField = mDataBloque.getText();
                        BlockField.clear();
                        BlockField.append(blockread);
                        //BlockField.append(getHexString(dataread, dataread.length));
                        Toast.makeText(this, "Lectura de bloque EXITOSA: " + dataread + "block read: " + blockread, Toast.LENGTH_LONG).show();
                    } else {
                        // Authentication failed -Handle it
                        Editable BlockField = mDataBloque.getText();
                        BlockField.clear();
                        Toast.makeText(this, "Lectura de bloque FALLIDA dado autentificaci??n fallida.", Toast.LENGTH_LONG).show();
                    }

                    mfc.close();
                    mTagDialog.cancel();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }
    }

     */

    /*
    void resolveAuthIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            try {
                mfc.connect();
                boolean auth = false;
                String hexkey = "";
                //int id = mRadioGroup.getCheckedRadioButtonId();
                int sector = Integer.valueOf(sectorAux);
                byte[] datakey;




                hexkey = llaveAux;
                datakey = hexStringToByteArray(hexkey);
                auth = mfc.authenticateSectorWithKeyA(sector, datakey);

                if (auth) {
                    Toast.makeText(this, "Autentificaci??n de sector EXITOSA.", Toast.LENGTH_LONG).show();
                    bandera = true;
                } else {
                    // Authentication failed -Handle it
                    Toast.makeText(this, "Autentificaci??n de sector FALLIDA.", Toast.LENGTH_LONG).show();
                }

                mfc.close();
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }



    @Override
    public void onResume() {
        super.onResume();

        // Double check if NFC is enabled
        checkNfcEnabled();

        Log.d(TAG, "onResume: " + getIntent());
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mReadWriteTagFilters, mTechList);
    }


    /**
     * This is called for activities that set launchMode to "singleTop" or*
     * "singleTask" in their manifest package, or if a client used the*
     * FLAG_ACTIVITY_SINGLE_TOP flag when calling startActivity(Intent).
     */

    /*
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: " + intent);
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);

        if (mAuthenticationMode) {
            // Currently in tag AUTHENTICATION mode
            resolveAuthIntent(intent);
            mTagDialog.cancel();
        } else if (!mWriteMode) {
            // Currently in tag READING mode
            resolveReadIntent(intent);
        } else {
            // Currently in tag WRITING mode
            //resolveWriteIntent(intent);
        }
    }



    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause: " + getIntent());
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void enableTagWriteMode(){
        mWriteMode = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,mReadWriteTagFilters, mTechList);
    }

    private void enableTagReadMode(){
        mWriteMode = false;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,mReadWriteTagFilters, mTechList);
    }

    private void enableTagAuthMode(){
        mAuthenticationMode = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,mReadWriteTagFilters, mTechList);
    }
    */

    /*

    private View.OnClickListener mTagAuthenticate = new View.OnClickListener(){
        @Override
        public void onClick(View arg0){
            enableTagAuthMode();
            AlertDialog.Builder builder = new AlertDialog.Builder(PagoActivity.this)
                    .setTitle(getString(R.string.ready_to_authenticate))
                    .setMessage(getString(R.string.ready_to_authenticate_instructions))
                    .setCancelable(true)
                    .setNegativeButton("Cancelar",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog,int id){
                            dialog.cancel();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener(){
                        @Override
                        public void onCancel(DialogInterface dialog){
                            mAuthenticationMode = false;
                        }
                    });

            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };

    private View.OnClickListener mTagRead = new View.OnClickListener(){
        @Override
        public void onClick(View arg0){
            enableTagReadMode();
            ReadUIDMode = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(PagoActivity.this)
                    .setTitle(getString(R.string.ready_to_read))
                    .setMessage(getString(R.string.ready_to_read_instructions))
                    .setCancelable(true)
                    .setNegativeButton("Cancelar",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog,int id){
                            dialog.cancel();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener(){
                        @Override
                        public void onCancel(DialogInterface dialog){
                            enableTagReadMode();
                            ReadUIDMode = true;
                        }
                    });

            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };


    private View.OnClickListener mTagWrite = new View.OnClickListener(){
        @Override
        public void onClick(View arg0){
            enableTagWriteMode();
            AlertDialog.Builder builder = new AlertDialog.Builder(PagoActivity.this)
                    .setTitle(getString(R.string.ready_to_write))
                    .setMessage(getString(R.string.ready_to_write_instructions))
                    .setCancelable(true)
                    .setNegativeButton("Cancelar",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog,int id){
                            dialog.cancel();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener(){
                        @Override
                        public void onCancel(DialogInterface dialog){
                            enableTagReadMode();
                        }
                    });

            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };


    private void checkNfcEnabled(){
        Boolean nfcEnabled = mNfcAdapter.isEnabled();

        if (!nfcEnabled){
            new AlertDialog.Builder(PagoActivity.this)
                    .setTitle(getString(R.string.warning_nfc_is_off))
                    .setMessage(getString(R.string.turn_on_nfc))
                    .setCancelable(false)
                    .setPositiveButton("Actualizar Settings",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog,int id){
                            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    }).create().show();
        }
    }
    public static String getHexString(byte[] b, int length){
        String result = "";
        Locale loc = Locale.getDefault();

        for (int i = 0; i < length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
            result += "";
            //Poner espacio si se quiere separar de dos en dos caracteres hex
        }

        return result.toUpperCase(loc);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)+ Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

     */


}