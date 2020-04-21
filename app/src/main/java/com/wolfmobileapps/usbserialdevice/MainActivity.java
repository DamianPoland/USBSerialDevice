package com.wolfmobileapps.usbserialdevice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SerialInputOutputManager.Listener {

    private static final String TAG = "MainActivity";

    //views
    private Button buttonConnect;
    private Button buttonSendData;
    private Button buttonClear;
    private TextView textViewDataFromUSB;
    private TextView textViewConnection;
    private EditText editTextDataToSend;

    // vars
    private UsbSerialDriver driver;
    private UsbDeviceConnection connection;
    private UsbSerialPort port;

    // constans
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private static final String CONNECTED = "Connected";
    private static final String DISCONNECTED = "Disconnected";
    private static final String BUTTON_CONNECTED = "Connect";
    private static final String BUTTON_DISCONNECTED = "Disconnect";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //views
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonSendData = findViewById(R.id.buttonSendData);
        buttonClear = findViewById(R.id.buttonClear);
        textViewDataFromUSB = findViewById(R.id.textViewDataFromUSB);
        textViewConnection = findViewById(R.id.textViewConnection);
        editTextDataToSend = findViewById(R.id.editTextDataToSend);

        // set view disconnected
        textViewConnection.setText(DISCONNECTED);
        textViewConnection.setTextColor(getResources().getColor(R.color.Red));
        buttonConnect.setText(BUTTON_CONNECTED);

        // btn connect
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (textViewConnection.getText().toString().equals(DISCONNECTED)) {
                    // start connection
                    startConnection();
                } else {

                    try {
                        port.close(); // close connection - port

                        // set view disconnected
                        setViewDisconnected();

                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Exception: " + e, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // btn sent data
        buttonSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (textViewConnection.getText().toString().equals(DISCONNECTED)) {
                    Toast.makeText(MainActivity.this, "No connection!", Toast.LENGTH_SHORT).show();
                } else {
                    // send data
                    sendDataToUsbDevice();
                }
            }
        });

        // btn clear
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewDataFromUSB.setText("");
            }
        });
    }

    // start connection
    public void startConnection() {

        // Find all available drivers from attached devices.
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

        // if no drivers return
        if (availableDrivers.isEmpty()) {
            Toast.makeText(this, "No available drivers!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open a connection to the first available driver - always only one.
        driver = availableDrivers.get(0);
        connection = usbManager.openDevice(driver.getDevice());

        //ask for permission if not granted
        if (!usbManager.hasPermission(driver.getDevice())) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            driver = null;
            connection = null;
            return;
        }

        // open port and set parametters
        try {
            port = driver.getPorts().get(0);
            port.open(connection);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE); //default baudRate: 115200

            // set listener onNewData work
            SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, this);
            Executors.newSingleThreadExecutor().submit(usbIoManager);

            // set view connected
            textViewConnection.setText(CONNECTED);
            textViewConnection.setTextColor(getResources().getColor(R.color.Green));
            buttonConnect.setText(BUTTON_DISCONNECTED);

        } catch (final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Exception in open connection: \n" + e, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void sendDataToUsbDevice() {

        try {
            // string to send
            String writeData = editTextDataToSend.getText().toString();
            if (writeData.equals("")) {
                Toast.makeText(this, "Empty text!", Toast.LENGTH_SHORT).show();
                return;
            }
            writeData = writeData + "\n"; //add end line
            //byte[] b = writeData.getBytes(StandardCharsets.UTF_8);
            byte[] b = writeData.getBytes();

            // write string to usb
            port.write(b, 1000);

        } catch (IOException e) {
            Toast.makeText(this, "Exception to sent data: \n" + e, Toast.LENGTH_SHORT).show();
        }
    }

    // implements from  implements SerialInputOutputManager.Listener
    @Override
    public void onNewData(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Convert byte[] to String
                //String stringData = Base64.getEncoder().encodeToString(data); // min API 26, now is 21
                String stringData = new String(data);

                // add data to text View
                textViewDataFromUSB.append(stringData);
            }
        });
    }

    // implements from  implements SerialInputOutputManager.Listener
    @Override
    public void onRunError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, DISCONNECTED, Toast.LENGTH_SHORT).show();
                setViewDisconnected();

            }
        });
    }

    // set view disconnected
    public void setViewDisconnected() {
        textViewConnection.setText(DISCONNECTED);
        textViewConnection.setTextColor(getResources().getColor(R.color.Red));
        buttonConnect.setText(BUTTON_CONNECTED);
    }

    // do górnego menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_info:
                startActivity(new Intent(MainActivity.this, ActivityInfo.class));
                break;
        }
        return super.onOptionsItemSelected(item);//nie usuwać bo up button nie działa
    }
}
