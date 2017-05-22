package truel.mathias.hexotis;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "DebbugMain";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private Button btn_son1,btn_son2,btn_son3,btn_disconnect,btn_adsr,btn_plus5,btn_plus1,btn_less1,
            btn_less5;
    private TextView txt_tempo,txt_sons,txt_control,txt_bpm_value;
    private SeekBar seekBar_tempo;
    private ToggleButton togglebtnRec;
    private EditText editTextTempo;

    private int mesureFracUp = 0;
    private int mesureFracDown = 0;
    private String fileName = " ";


    //Name of the connected device
    private String mConnectedDeviceName = null;

    //state of the recording?
    private boolean RECORDING = true;

    //string that contains all notes
    private String midiNotesContainer = new String("");

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    //bpm value
    private float bpm_value = 120.0f;

    //Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

     // Member object for the chat services
    private BluetoothService mBtService = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Initialize buttons with a listener that for click events
        txt_control = (TextView) findViewById(R.id.txt_control);

        togglebtnRec = (ToggleButton) findViewById(R.id.togglebtn_REC);
        togglebtnRec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleRecord(isChecked);
            }
        });

        txt_tempo = (TextView) findViewById(R.id.txt_tempo);
        txt_bpm_value = (TextView) findViewById(R.id.txt_bpm_value);

        seekBar_tempo = (SeekBar) findViewById(R.id.seekBar_tempo);
        seekBar_tempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm_value = 60 + 0.14f*progress;
                txt_bpm_value.setText(String.format("%.1f", bpm_value));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub


            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        editTextTempo = (EditText) findViewById(R.id.tempo_editText);
        editTextTempo.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG,"EditTxt");
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if (editTextTempo.getText().toString().equals(""))return true;
                    bpm_value = Float.parseFloat(editTextTempo.getText().toString());
                    txt_bpm_value.setText(String.format("%.1f", bpm_value));
                    editTextTempo.setText("");
                    int progress = (int) (bpm_value-60)*(1000/140);
                    seekBar_tempo.setProgress(progress);
                    handled = true;
                }
                return handled;
            }
        });


        btn_plus5 = (Button) findViewById(R.id.btn_plus5);
        btn_plus5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Btn+5");
                bpm_value+=5;
                txt_bpm_value.setText(String.format("%.1f", bpm_value));
                int progress = (int) (bpm_value-60)*(1000/140);
                seekBar_tempo.setProgress(progress);
            }
        });

        btn_plus1 = (Button) findViewById(R.id.btn_plus1);
        btn_plus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Btn+1");
                bpm_value++;
                txt_bpm_value.setText(String.format("%.1f", bpm_value));
                int progress = (int) (bpm_value-60)*(1000/140);
                seekBar_tempo.setProgress(progress);
            }
        });

        btn_less1 = (Button) findViewById(R.id.btn_less1);
        btn_less1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Btn-1");
                bpm_value--;
                txt_bpm_value.setText(String.format("%.1f", bpm_value));
                int progress = (int) (bpm_value-60)*(1000/140);
                seekBar_tempo.setProgress(progress);
            }
        });

        btn_less5 = (Button) findViewById(R.id.btn_less5);
        btn_less5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Btn-5");
                bpm_value-=5;
                txt_bpm_value.setText(String.format("%.1f", bpm_value));
                int progress = (int) (bpm_value-60)*(1000/140);
                seekBar_tempo.setProgress(progress);
            }
        });


        txt_sons = (TextView) findViewById(R.id.txt_sons);

        btn_son1 = (Button) findViewById(R.id.btn_son1);
        btn_son1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sons(1);
            }
        });

        btn_son2 = (Button) findViewById(R.id.btn_son2);
        btn_son2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sons(2);
            }
        });

        btn_son3 = (Button) findViewById(R.id.btn_son3);
        btn_son3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sons(3);
            }
        });

        btn_adsr = (Button) findViewById(R.id.btn_adsr);
        btn_adsr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adsr();
            }
        });

        btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });



        editTextTempo.setText("");

        txt_bpm_value.setText("" + bpm_value);

        txt_tempo.setText(R.string.str_tempo);

        txt_control.setText(R.string.str_control);

        txt_sons.setText(R.string.str_sons);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mBtService == null) {
            setupControl();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBtService != null) {
            mBtService.stop();
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBtService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBtService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBtService.start();
            }
        }
    }

    //Actions du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelcted");
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }



    // Called in onStart. Set up the UI and background operations for chat.
    private void setupControl() {
        Log.d(TAG, "setupControl()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBtService = new BluetoothService(MainActivity.this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    //Called in onOptionsItemSelected. Makes this device discoverable for 300 seconds (5 minutes).
    private void ensureDiscoverable() {
        Log.d(TAG, "ensureDiscoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    //Actions des bouttons
    private void toggleRecord(boolean isChecked) {
        // Check that we're actually connected before trying anythingaa
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            togglebtnRec.setChecked(false);
            return;
        } else {
            if (isChecked) {
                Log.d(TAG, "rec on");
                //togglebtnRec.setTextOff(textOn);
                RECORDING = true;
                midiNotesContainer = new String("");
                sendMessage("R");
                togglebtnRec.setChecked(true);
            } else {
                Log.d(TAG, "rec off");
                //togglebtnRec.setTextOff(textOff);
                RECORDING = false;
                togglebtnRec.setChecked(false);
            }
        }
    }

    private void Sons(int mode) {
        Log.d(TAG, "Sons");
        // Check that we're actually connected before trying anythingaa
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        switch (mode) {
            case 1:
                //son1
                sendMessage("S1");
                break;
            case 2:
                //son 2
                sendMessage("S2");
                break;
            case 3:
                //son3
                sendMessage("S3");
                break;
        }
    }

    private void adsr(){
        Log.d(TAG, "adsr");
        // Check that we're actually connected before trying anythingaa
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        //do something to change the adsr (maybe new Activity?
        sendMessage("adsr");
    }

    private void disconnect() {
        //doSomething
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        sendMessage("Disconnect");
        mBtService.stop();
    }

    private void dataHandler(String RecivedData) {
        Log.d(TAG,"dataHandler");
        //TODO define message to communicate from Arduino

    }

    private void record(String RecivedData) {
        if (RECORDING) {
            if (RecivedData != " ") {
                midiNotesContainer = midiNotesContainer + RecivedData;
            }
            if ((RecivedData.toCharArray()[RecivedData.length() - 1] == 'F')// six dernier charatere indiquent la fin de la string
                    && (RecivedData.toCharArray()[RecivedData.length() - 2] == 'F')
                    && (RecivedData.toCharArray()[RecivedData.length() - 3] == 'F')
                    && (RecivedData.toCharArray()[RecivedData.length() - 4] == 'F')
                    && (RecivedData.toCharArray()[RecivedData.length() - 5] == 'F')
                    && (RecivedData.toCharArray()[RecivedData.length() - 6] == 'F')) {
                MidiService.MidiFileCreation(mesureFracUp, mesureFracDown, bpm_value, midiNotesContainer, fileName);

            }
            Log.d(TAG, "midiNotesContainer: " + midiNotesContainer);
        }
    }


    public void sendMessage(String message) {
        Log.d(TAG,"sendMessage()");
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            Log.d(TAG,"Message: " + message);
            byte[] send = message.getBytes();
            mBtService.write(send);

            // Reset out string buffer to zero
            mOutStringBuffer.setLength(0);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupControl();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        Log.d(TAG, "Connectdevice");
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBtService.connect(device, secure);
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Handler");
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "BtState");
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.d(TAG, "3");
                            getSupportActionBar().setSubtitle(R.string.title_connected);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d(TAG, "2");
                            getSupportActionBar().setSubtitle(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Log.d(TAG, "1");
                            //break;
                        case BluetoothService.STATE_NONE:
                            Log.d(TAG, "0");
                            getSupportActionBar().setSubtitle(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.d(TAG, "MsgWrite");
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    Log.d(TAG, "MsgRead");
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String RecivedData = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG,"Returned: " + RecivedData);
                    record(RecivedData);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MsgDeviceName");
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d(TAG, "msgToast");
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
}