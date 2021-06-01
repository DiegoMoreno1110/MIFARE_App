package com.dmr.mifare_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//NFC imports

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.Tag;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;



public class ConfiguracionActivity extends AppCompatActivity {

    private static final String TAG = "nfcinventory_simple";

    // NFC-related variables
    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadWriteTagFilters;
    private boolean mWriteMode = false;
    private boolean mAuthenticationMode = false;
    private boolean ReadUIDMode = true;
    String[][] mTechList;

    // UI elements
    EditText precio;
    TextView cantidad;
    Button recarga;
    Button pago;
    Button logout;

    //UI NFC
    EditText keyAET;
    EditText keyBET;
    EditText sectorET;
    EditText bloqueET;
    RadioGroup mRadioGroup;
    AlertDialog mTagDialog;

    //FireBase
    private DatabaseReference mDatabase;

    //private boolean bandera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // UI pago y recarga
        recarga = (Button) findViewById(R.id.recargaButton);
        precio = (EditText) findViewById(R.id.precioRecarga);
        cantidad = (TextView) findViewById(R.id.cantidadLabelRecarga);
        logout = (Button)findViewById(R.id.logout);
        pago = (Button) findViewById(R.id.pagoButton);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.configuracionActivity); // este es el HOME

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){

                    case R.id.historialActivity:
                        startActivity(new Intent(getApplicationContext(),HistorialActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.configuracionActivity:
                        return true;
                }
                return false;
            }
        });

        //Pago y Recarga
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

        recarga.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                makeRecarga();

            }

        });

        pago.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                makePago();

            }
        });

        // Boton de log out
        logout.setOnClickListener(new View.OnClickListener(){
                                      public void onClick(View v){
                                          FirebaseAuth.getInstance().signOut();
                                          startActivity(new Intent(getApplicationContext(),LoginActivity.class));            }
                                  }
        );


        //Configuracion Activity Componentes y NFC
        keyAET = ((EditText) findViewById(R.id.editTextTextKeyA));
        keyBET = ((EditText) findViewById(R.id.editTextTextKeyB));
        sectorET = ((EditText) findViewById(R.id.editTextNumberSector));
        bloqueET = ((EditText) findViewById(R.id.editTextNumberBloque));
        mRadioGroup = ((RadioGroup) findViewById(R.id.rBtnGrp));

        findViewById(R.id.buttonAutentificar).setOnClickListener(mTagAuthenticate);
        //findViewById(R.id.buttonLeerbloque).setOnClickListener(mTagRead);
        //findViewById(R.id.buttonEscribirBloque).setOnClickListener(mTagWrite);

        // get an instance of the context's cached NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // if null is returned this demo cannot run. Use this check if the
        // "required" parameter of <uses-feature> in the manifest is not set
        if (mNfcAdapter == null) {
            Toast.makeText(this, "Su dispositivo no soporta NFC. No se puede correr la aplicación.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // check if NFC is enabled
        checkNfcEnabled();

        // Handle foreground NFC scanning in this activity by creating a
        // PendingIntent with FLAG_ACTIVITY_SINGLE_TOP flag so each new scan
        // is not added to the Back Stack
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Create intent filter to handle MIFARE NFC tags detected from inside our
        // application
        IntentFilter mifareDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            mifareDetected.addDataType("application/com.e.mifarecontrol");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("No se pudo añadir un tipo MIME.", e);
        }

        // Create intent filter to detect any MIFARE NFC tag
        mReadWriteTagFilters = new IntentFilter[]{mifareDetected};

        // Setup a tech list for all NFC tags
        mTechList = new String[][]{
                new String[]{
                        MifareClassic.class.getName()
                }
        };

        resolveReadIntent(getIntent());


    }


    //Método Recarg y Pago
    public void getCurrentAmountAndUpdateTag(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        mDatabase.child("montoActual").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            double montoActual = (double) (long) dataSnapshot.getValue();

                            double precioVar = Double.parseDouble(precio.getText().toString());
                            double total = montoActual + precioVar;
                            mDatabase.child("montoActual").setValue(total);

                            Toast.makeText(getApplicationContext(), "Monto Actual: " + total, Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }

    public void makeRecarga(){

        Date currentTime = Calendar.getInstance().getTime();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        //convertir precio a double
        double precioVar = Double.parseDouble(precio.getText().toString());

        // convertir fecha a string
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(currentTime);

        getCurrentAmountAndUpdateTag();

        Historial transaccion = new Historial(strDate, "Depósito", precioVar);

        String idFire = mDatabase.push().getKey();

        mDatabase.child("tagsGuardados/transactions").child(idFire).setValue(transaccion);

        Toast.makeText(getApplicationContext(), "Se han depositado $" + precio.getText().toString(), Toast.LENGTH_SHORT).show();

        mTagWrite();


    }



    public void getCurrentAmountAndUpdateTagPago(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        mDatabase.child("montoActual").addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            double montoActual = (double) (long) dataSnapshot.getValue();

                            double precioVar = Double.parseDouble(precio.getText().toString());
                            double total = montoActual - precioVar;
                            mDatabase.child("montoActual").setValue(total);

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



    //NFC METHODS
    void resolveReadIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            if (ReadUIDMode) {
                String tipotag = "";
                String tamano = "";
                byte[] tagUID = tagFromIntent.getId();
                String hexUID = getHexString(tagUID, tagUID.length);
                Log.i(TAG, "Tag UID: " + hexUID);

                //Editable UIDField = mTagUID.getText();
                //UIDField.clear();
                //UIDField.append(hexUID);

                switch (mfc.getType()) {
                    case 0:
                        tipotag = "Mifare Classic";
                        break;
                    case 1:
                        tipotag = "Mifare Plus";
                        break;
                    case 2:
                        tipotag = "Mifare Pro";
                        break;
                    default:
                        tipotag = "Mifare Desconocido";
                        break;
                }

                switch (mfc.getSize()) {
                    case 1024:
                        tamano = " (1K Bytes)";
                        break;
                    case 2048:
                        tamano = " (2K Bytes)";
                        break;
                    case 4096:
                        tamano = " (4K Bytes)";
                        break;
                    case 320:
                        tamano = " (MINI -320 Bytes)";
                        break;
                    default:
                        tamano = " (Tamaño desconocido)";
                        break;
                }

                Log.i(TAG, "Card Type: " + tipotag + tamano);

                //Editable CardtypeField = mCardType.getText();
                //CardtypeField.clear();
                //CardtypeField.append(tipotag + tamano);

                Toast.makeText(this, "UID: " + hexUID + " Tag: " + tipotag + tamano, Toast.LENGTH_LONG).show();

                /*
                //Se crea el tag
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                TagUser tag = new TagUser(hexUID, tipotag + tamano);
                mDatabase.child("tagsGuardados").setValue(tag);

                 */


            } else {
                try {
                    mfc.connect();
                    boolean auth = false;
                    String hexkey = "";
                    int id = mRadioGroup.getCheckedRadioButtonId();
                    int sector = mfc.blockToSector(Integer.valueOf(bloqueET.getText().toString()));
                    byte[] datakey;

                    if (id == R.id.radioButtonkeyA) {
                        hexkey = keyAET.getText().toString();
                        datakey = hexStringToByteArray(hexkey);
                        auth = mfc.authenticateSectorWithKeyA(sector, datakey);
                    } else if (id == R.id.radioButtonkeyB) {
                        hexkey = keyBET.getText().toString();
                        datakey = hexStringToByteArray(hexkey);
                        auth = mfc.authenticateSectorWithKeyB(sector, datakey);
                    } else {
                        //no item selected poner toast
                        Toast.makeText(this, "°Seleccionar llave A o B!", Toast.LENGTH_LONG).show();
                        mfc.close();
                        return;
                    }



                    if (auth) {
                        int bloque = Integer.valueOf(bloqueET.getText().toString());
                        byte[] dataread = mfc.readBlock(bloque + 1);
                        Log.i("Bloques", getHexString(dataread, dataread.length));

                        dataread = mfc.readBlock(bloque);
                        String blockread = getHexString(dataread, dataread.length);
                        Log.i(TAG, "Bloque Leido: " + blockread);

                        //Editable BlockField = mDataBloque.getText();
                        //BlockField.clear();
                        //BlockField.append(blockread);
                        Toast.makeText(this, "Lectura de bloque EXITOSA: " + blockread, Toast.LENGTH_LONG).show();
                    } else {
                        // Authentication failed -Handle it
                        //Editable BlockField = mDataBloque.getText();
                        //BlockField.clear();
                        Toast.makeText(this, "Lectura de bloque FALLIDA dado autentificación fallida.", Toast.LENGTH_LONG).show();
                    }

                    mfc.close();
                    mTagDialog.cancel();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }
    }


    void resolveWriteIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            try {
                mfc.connect();
                boolean auth = false;
                String hexkey = "";
                int id = mRadioGroup.getCheckedRadioButtonId();
                int bloque = Integer.valueOf(bloqueET.getText().toString());
                int sector = mfc.blockToSector(bloque);
                byte[] datakey;

                if (id == R.id.radioButtonkeyA) {
                    hexkey = keyAET.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth = mfc.authenticateSectorWithKeyA(sector, datakey);
                } else if (id == R.id.radioButtonkeyB) {
                    hexkey = keyBET.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth = mfc.authenticateSectorWithKeyB(sector, datakey);
                } else {
                    //no item selected poner toast
                    Toast.makeText(this, "°Seleccionar llave A o B!", Toast.LENGTH_LONG).show();
                    mfc.close();
                    return;
                }


                if (auth) {

                    Toast.makeText(this, "ENTRANDO AUTH ESCRITURA." , Toast.LENGTH_LONG).show();

                    String strdata = precio.getText().toString();

                    //Convertir de int a hex
                    int precioTag = Integer.parseInt(strdata);

                    String valorConvertido = Integer.toHexString(precioTag);
                    Log.i("MESSAGE HEX", valorConvertido);


                    byte[] datatowrite = hexStringToByteArray(valorConvertido);
                    mfc.writeBlock(bloque, datatowrite);
                    Toast.makeText(this, "Escritura a bloque EXITOSA." +  valorConvertido, Toast.LENGTH_LONG).show();
                } else {
                    // Authentication failed -Handle it
                    Toast.makeText(this, "Escritura a bloque FALLIDA dado autentificación fallida.", Toast.LENGTH_LONG).show();
                }




                mfc.close();
                mTagDialog.cancel();
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }


    void resolveAuthIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            try {
                mfc.connect();
                boolean auth = false;
                String hexkey = "";
                int id = mRadioGroup.getCheckedRadioButtonId();
                int sector = Integer.valueOf(sectorET.getText().toString());
                byte[] datakey;

                if (id == R.id.radioButtonkeyA) {
                    hexkey = keyAET.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth = mfc.authenticateSectorWithKeyA(sector, datakey);
                } else if (id == R.id.radioButtonkeyB) {
                    hexkey = keyBET.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth = mfc.authenticateSectorWithKeyB(sector, datakey);
                } else {
                    //no item selected poner toast
                    Toast.makeText(this, "°Seleccionar llave A o B!", Toast.LENGTH_LONG).show();
                    mfc.close();
                    return;
                }

                if (auth) {
                    Toast.makeText(this, "Autentificación de sector EXITOSA.", Toast.LENGTH_LONG).show();


                    //bandera = true;
                    /*
                    TagUser tag = new TagUser(mTagUID.getText().toString(), mCardType.getText().toString(), sector, hexkey, Integer.parseInt(mBloque.getText().toString()));

                    //Se crea el tag
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                    mDatabase.child("tagsGuardados").setValue(tag);
                     */

                } else {
                    // Authentication failed -Handle it
                    Toast.makeText(this, "Autentificación de sector FALLIDA.", Toast.LENGTH_LONG).show();
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
        //checkNfcEnabled();

        Log.d(TAG, "onResume: " + getIntent());
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mReadWriteTagFilters, mTechList);
    }


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
            resolveWriteIntent(intent);
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


    private View.OnClickListener mTagAuthenticate = new View.OnClickListener(){
        @Override
        public void onClick(View arg0){
            enableTagAuthMode();
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguracionActivity.this)
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


    private void mTagRead(){
            enableTagReadMode();
            ReadUIDMode = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguracionActivity.this)
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

    };

    private void mTagWrite(){
            enableTagWriteMode();
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguracionActivity.this)
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

    };


    private void checkNfcEnabled(){
        Boolean nfcEnabled = mNfcAdapter.isEnabled();

        if (!nfcEnabled){
            new AlertDialog.Builder(ConfiguracionActivity.this)
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



}