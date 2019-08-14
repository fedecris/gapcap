package ar.com.federicocristina.gapcap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import networkdcq.Host;
import networkdcq.NetworkDCQ;
import networkdcq.util.Logger;

public class MainActivity extends AppCompatActivity {

    // TAG Logcat
    private static final String TAG = "Recorder";
    // Last scheduled service
    protected static long lastScheduled = 0;
    // Schedule
    static PendingIntent pendingIntent;
    // Message para interactuar con el servicio de grabacion
    Messenger messenger = new Messenger(new ResponseHandler());
    // Consumer de notificaciones
    static NetworkConsumer networkConsumer;
    // Host remoto (ya sea el cliente o el servidor segun sea el caso)
    static Host remoteHost;

    // Start button
    public static Button startButton;
    // Stop button
    public static Button stopButton;
    // Path de grabacion
    public EditText filePathEditText;
    // Path de grabacion
    public EditText filePrefixEditText;
    // Timestamp del archivo
    public EditText fileDateFormatEditText;
    // Camara frontal?
    public Switch frontalCameraSwitch;
    // Usar calidad baja?
    public SeekBar qualitySeekBar;
    // Grabar audio?
    public Switch recordAudioSwitch;
    // Video Size
    public Spinner videoSizeSpinner;
    // Custom Video Frame Rate
    public Switch customVideoFrameRateSwitch;
    // Video Frame Rate
    public Spinner videoFrameRateSpinner;
    // Custom Capture Frame Rate
    public Switch customCaptureFrameRateSwitch;
    // Capture Frame Rate
    public Spinner captureFrameRateSpinner;
    // Estado de grabacion
    public static TextView status;
    // Ejecutar en background?
    public Switch runInBackgroundSwitch;
    // Limitar tamaño
    public EditText limitSizeMBEditText;
    // Limitar tiempo
    public EditText limitTimeSecsEditText;
    // Demorar inicio
    public EditText delayStartSecsEditText;
    // Focus
    public Spinner focusModeSpinner;
    // Repetir una vez llegado el limite?
    public Switch repeatAtLimitSwitch;
    // Stealth mode
    public Switch stealthModeSwitch;
    // Use Flash?
    public Switch flashSwitch;
    // Service mode? (soporte conexion remota para ejecutar acciones)
    public Button serverModeButton;
    // Boton para coneccion remota y controlar
    public Button connectToHostButton;
    // Boton para coneccion remota y controlar
    public Spinner connectToHostSpinner;


    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);

        // Habilitar icono en ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        // Recuperar componentes generales
        startButton = findViewById(R.id.button_startService);
        stopButton = findViewById(R.id.button_StopService);
        status = findViewById(R.id.textView_status);
        runInBackgroundSwitch = findViewById(R.id.switch_toBackground);
        filePathEditText = findViewById(R.id.editText_path);
        filePrefixEditText = findViewById(R.id.editText_filePrefix);
        fileDateFormatEditText = findViewById(R.id.editText_filenameTimestamp);
        qualitySeekBar = findViewById(R.id.seekBar_quality);
        recordAudioSwitch = findViewById(R.id.switch_audio);
        customVideoFrameRateSwitch = findViewById(R.id.switch_customVideoFrameRate);
        customCaptureFrameRateSwitch = findViewById(R.id.switch_customCaptureFrameRate);
        videoFrameRateSpinner = findViewById(R.id.spinner_videoFrameRate);
        captureFrameRateSpinner = findViewById(R.id.spinner_captureFrameRate);
        frontalCameraSwitch = findViewById(R.id.switch_frontalCamera);
        frontalCameraSwitch.setEnabled(Utils.existsFrontalCamera());
        limitSizeMBEditText = findViewById(R.id.limitSizeEditText);
        limitTimeSecsEditText = findViewById(R.id.limitTimeEditText);
        delayStartSecsEditText = findViewById(R.id.editText_delayStart);
        focusModeSpinner = findViewById(R.id.spinner_focus);
        repeatAtLimitSwitch = findViewById(R.id.switch_repeatAtLimit);
        stealthModeSwitch = findViewById(R.id.switch_stealthMode);
        flashSwitch = findViewById(R.id.switch_flash);
        serverModeButton = findViewById(R.id.button_serverMode);
        connectToHostButton = findViewById(R.id.button_connectToHost);
        connectToHostSpinner = findViewById(R.id.spinner_connectToHost);

        // Recuperar la configuracion de las camaras y cargar los componentes visuales
        Utils.retrieveCameraFeatures(getBaseContext());
        loadSupportedVideoSizes(frontalCameraSwitch.isChecked());
        loadSupportedVideoFrameRates(frontalCameraSwitch.isChecked());
        loadSupportedFocusModes(frontalCameraSwitch.isChecked());
        loadCaptureFrameRates();

        // Listener frontal camera switch
        frontalCameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reLoadVideoSizesAndVideoFrameRateAndFocusModes();
            }
        });

        // Listener custom video frame rate
        customVideoFrameRateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                customVideoFrameRateChanged();
            }
        });

        // Listener custom capture frame rate
        customCaptureFrameRateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                customCaptureFrameRateChanged();
            }
        });


        // Status actual de componentes
        loadSharedPreferences();
        updateComponentsStatus();

    }


    /** Sobrecarga */
    public void iniciar() {
        iniciar(null);
    }


    /** Iniciar la grabacion */
    public void iniciar(View v) {

        // CLIENT MODE: Enviar START al host remoto?
        if (RecorderService.appMode == Constants.APP_MODE_CLIENT && remoteHost != null) {
            NetworkData data = new NetworkData(NetworkData.ACTION_START);
            NetworkDCQ.getCommunication().sendMessage(remoteHost, data);
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
            return;
        }

        // Validar precondiciones
        String retValue = checkPreconditions();
        if (retValue!=null) {
            Toast.makeText(getBaseContext(), retValue, Toast.LENGTH_LONG).show();
            return;
        }

        // Iniciar el intent con el servicio de grabacion
        Intent alarmIntent = new Intent(this, RecorderService.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(Constants.MESSENGER, messenger);
        alarmIntent.putExtra(Constants.PREFERENCE_DELAY_START, Integer.parseInt(delayStartSecsEditText.getText().toString()));
        alarmIntent.putExtra(Constants.PREFERENCE_FRONT_CAMERA, frontalCameraSwitch.isChecked());
        alarmIntent.putExtra(Constants.PREFERENCE_RECORD_AUDIO, recordAudioSwitch.isChecked());
        alarmIntent.putExtra(Constants.PREFERENCE_QUALIY, qualitySeekBar.getProgress());
        alarmIntent.putExtra(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE, customVideoFrameRateSwitch.isChecked());
        alarmIntent.putExtra(Constants.PREFERENCE_VIDEO_FRAME_RATE, Integer.parseInt(videoFrameRateSpinner.getSelectedItem().toString()));
        alarmIntent.putExtra(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, customCaptureFrameRateSwitch.isChecked());
        alarmIntent.putExtra(Constants.PREFERENCE_CAPTURE_FRAME_RATE, Integer.parseInt(captureFrameRateSpinner.getSelectedItem().toString()));
        alarmIntent.putExtra(Constants.PREFERENCE_LIMIT_SIZE, Integer.parseInt(limitSizeMBEditText.getText().toString()));
        alarmIntent.putExtra(Constants.PREFERENCE_LIMIT_TIME, Integer.parseInt(limitTimeSecsEditText.getText().toString()));
        alarmIntent.putExtra(Constants.PREFERENCE_VIDEO_SIZE, videoSizeSpinner.getSelectedItem().toString());
        alarmIntent.putExtra(Constants.PREFERENCE_FOCUS_MODE, focusModeSpinner.getSelectedItem().toString());
        alarmIntent.putExtra(Constants.PREFERENCE_FILEPATH, filePathEditText.getText().toString());
        alarmIntent.putExtra(Constants.PREFERENCE_FILEPREFIX, filePrefixEditText.getText().toString());
        alarmIntent.putExtra(Constants.PREFERENCE_FILETIMESTAMP, fileDateFormatEditText.getText().toString());
        alarmIntent.putExtra(Constants.PREFERENCE_REPEAT_AT_LIMIT, repeatAtLimitSwitch.isChecked());
        alarmIntent.putExtra(Constants.PREFERENCE_STEALTH_MODE, stealthModeSwitch.isChecked());
        alarmIntent.putExtra(Constants.PREFERENCE_USE_FLASH, flashSwitch.isChecked());

        // Programar el inicio del servicio de grabacion, o bien iniciar inmediatamente
        if (Integer.parseInt(delayStartSecsEditText.getText().toString()) > 0) {
            lastScheduled = System.currentTimeMillis() + Integer.parseInt(delayStartSecsEditText.getText().toString()) * 1000;
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            pendingIntent = PendingIntent.getService(this, 1, alarmIntent, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, lastScheduled, pendingIntent);
            updateComponentsStatus();
        } else {
            lastScheduled = 0;
            ComponentName ret = startService(alarmIntent);
            ret.getClassName();
            updateComponentsStatus(true);
        }

        // Finalizar la actividad si corresponde
        if (runInBackgroundSwitch.isChecked()) {
            finish();
        }
    }

    /** Sobrecarga */
    public void detener() {
        detener(null);
    }

    /** Detener la grabacion */
    public void detener(View v) {

        // CLIENT MODE: Enviar STOP recording al host remoto?
        if (RecorderService.appMode == Constants.APP_MODE_CLIENT && remoteHost != null) {
            NetworkData data = new NetworkData(NetworkData.ACTION_STOP);
            NetworkDCQ.getCommunication().sendMessage(remoteHost, data);
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            return;
        }

        long current = System.currentTimeMillis();
        if (current < lastScheduled) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            lastScheduled = 0;
        } else {
            stopService(new Intent(MainActivity.this, RecorderService.class));
        }
        updateComponentsStatus(false);
    }

    /** Sobrecarga */
    public void updateComponentsStatus() {
        updateComponentsStatus(null);
    }

    /** Activa o desactiva los botones segun el estado de grabacion */
    public void updateComponentsStatus(Boolean forceRecording) {

        // Visualizacion segun modo actual
        updateLayouts();

        // Modo Cliente?
        if (RecorderService.appMode == Constants.APP_MODE_CLIENT) {
            loadAvailableServers();
            return;
        }

        // Modo Servidor?
        if (RecorderService.appMode == Constants.APP_MODE_SERVER) {
            return;
        }

        // Modo normal? Habilitar opciones
        serverModeButton.setEnabled(!RecorderService.mRecordingStatus);
        connectToHostButton.setEnabled(!RecorderService.mRecordingStatus && connectToHostSpinner.getCount()>0);
        connectToHostSpinner.setEnabled(!RecorderService.mRecordingStatus && connectToHostSpinner.getCount()>0);

        // Estado componentes sin importar el modo de grabacion
        customVideoFrameRateSwitch.setEnabled(false); // Se habilita o no dependiendo del timelapse mode

        // Existe una operacion de schedule?
        long current = System.currentTimeMillis();
        if (current < lastScheduled) {
            status.setText("Scheduled: " + Utils.getDateTimeFor(getString(R.string.DateTimeFormat), lastScheduled));
            startButton.setEnabled(false);
            stopButton.setText(R.string.Cancel);
            stopButton.setEnabled(true);
        } else {
            status.setText((forceRecording != null && forceRecording) || RecorderService.mRecordingStatus ? R.string.RecordingStatusActive : R.string.RecordingStatusReady);
            stopButton.setText(R.string.Stop);
            startButton.setEnabled(forceRecording !=null ? !forceRecording : !RecorderService.mRecordingStatus);
            stopButton.setEnabled(forceRecording != null? forceRecording : RecorderService.mRecordingStatus);
        }
        customCaptureFrameRateChanged();
        customVideoFrameRateChanged();
    }

    public void reLoadVideoSizesAndVideoFrameRateAndFocusModes() {
        loadSupportedVideoSizes(frontalCameraSwitch.isChecked());
        loadSupportedVideoFrameRates(frontalCameraSwitch.isChecked());
        loadSupportedFocusModes(frontalCameraSwitch.isChecked());
        if (!Utils.flashSupport.get(frontalCameraSwitch.isChecked() ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK)) {
            flashSwitch.setChecked(false);
            flashSwitch.setEnabled(false);
        } else {
            flashSwitch.setEnabled(true);
        }

    }

    public void customVideoFrameRateChanged() {
        // Video frame rate custom
        videoFrameRateSpinner.setEnabled(customVideoFrameRateSwitch.isChecked());
    }

    public void customCaptureFrameRateChanged() {
        // Si se especifica modo time lapse, entonces desactivar el sonido
        captureFrameRateSpinner.setEnabled(customCaptureFrameRateSwitch.isChecked());
        recordAudioSwitch.setEnabled(!customCaptureFrameRateSwitch.isChecked());
        customVideoFrameRateSwitch.setChecked(customCaptureFrameRateSwitch.isChecked());
        if (customCaptureFrameRateSwitch.isChecked()) {
            recordAudioSwitch.setChecked(false);
        }
    }

    /** Carga el spinner de opciones de tamaño de grabacion */
    protected void loadSupportedVideoSizes(boolean frontalCam) {
        ArrayList<String> opciones = new ArrayList<String>();
        List<Camera.Size> sizes = Utils.cameraVideoSizes.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        for (Camera.Size size : sizes) {
            opciones.add(size.width + "x" + size.height);
        }
        videoSizeSpinner = (Spinner)findViewById(R.id.spinner_videoSize);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        videoSizeSpinner.setAdapter(adapter);
    }

    /** Carga el spinner de opciones de tamaño de grabacion */
    protected void loadSupportedVideoFrameRates(boolean frontalCam) {
        ArrayList<String> opciones = new ArrayList<String>();
        List<Integer> fpss = Utils.videoFrameRates.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        for (Integer fps: fpss) {
            opciones.add(fps.toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        videoFrameRateSpinner.setAdapter(adapter);
    }

    /** Carga las opciones de foco */
    protected void loadSupportedFocusModes(boolean frontalCam) {
        ArrayList<String> opciones = new ArrayList<String>();
        if (Utils.autoFocusSupport.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK)) {
            opciones.add(Constants.OPTION_FOCUS_MODE_AUTO);
            opciones.add(Constants.OPTION_FOCUS_MODE_INFINITY);
            opciones.add(Constants.OPTION_FOCUS_MODE_MACRO);
        } else
            opciones.add(Constants.OPTION_FOCUS_MODE_FIXED);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        focusModeSpinner.setAdapter(adapter);
    }

    /** Carga las opciones para el modo time lapse */
    protected void loadCaptureFrameRates() {
        ArrayList<String> opciones = new ArrayList<String>();
        for (int i=1; i<=10; i++)
            opciones.add(""+i);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        captureFrameRateSpinner.setAdapter(adapter);
    }

    /** Carga los hosts disponibles para conectarse */
    public void loadAvailableServers() {
        ArrayList<String> opciones = new ArrayList<String>();
        for (String ip : NetworkDCQ.getDiscovery().otherHosts.keySet()) {
            if (NetworkDCQ.getDiscovery().otherHosts.get(ip).isOnLine())
            opciones.add(ip);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        connectToHostSpinner.setAdapter(adapter);
        if (connectToHostSpinner.getCount()==0) {
            connectToHostButton.setEnabled(false);
        }
        else {
            connectToHostButton.setEnabled(true);
            connectToHostSpinner.setEnabled(true);
            // Si ya estaba conectado a un host, pero hay otro disponible, igual dejar seleccionado el host al que esta conectado
            if (remoteHost !=null) {
                for (int i = 0; i < connectToHostSpinner.getCount(); i++) {
                    if (remoteHost.getHostIP().equals(connectToHostSpinner.getItemAtPosition(i))) {
                        connectToHostSpinner.setSelection(i);
                        break;

                    }
                }
            }
        }
    }

    public void reloadAvailableServers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadAvailableServers();
            }
        });
    }


    public void connectedToServer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Connected to: " + remoteHost.getHostIP());
            }
        });
    }

    public void serverClosed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Server closed the connection");
                remoteHost = null;
                connectToHostButton.setText(R.string.Connect);
                updateComponentsStatus();
            }
        });
    }




    public void clientConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Connected from: " + remoteHost.getHostIP());
            }
        });
    }

    public void clientDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Disconnected");
            }
        });
    }

    public void remoteRecording() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Remote recording...");
            }
        });
    }

    public void remoteStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Remote recording stopped.");
            }
        });
    }


    /** Se selecciona un item de la lista de servidores */
    public void connectToServer(View view) {
        try {
            if (remoteHost == null) {
                remoteHost = NetworkDCQ.getDiscovery().otherHosts.get((String)connectToHostSpinner.getSelectedItem());
                NetworkData data = new NetworkData(NetworkData.ACTION_CONNECT);
                NetworkDCQ.getCommunication().sendMessage(remoteHost, data);
                connectToHostButton.setText(R.string.Close);
                updateComponentsStatus(false);
                startButton.setEnabled(true);
                status.setText("Connecting to " + remoteHost.getHostIP());
            } else {
                connectToHostButton.setText(R.string.Connect);
                NetworkData data = new NetworkData(NetworkData.ACTION_DISCONNECT);
                NetworkDCQ.getCommunication().sendMessage(remoteHost, data);
                remoteHost = null;
                status.setText(R.string.RecordingStatusReady);
                updateComponentsStatus(false);
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    /** Carga de configuración */
    protected void loadSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        filePathEditText.setText(preferences.getString(Constants.PREFERENCE_FILEPATH, "Specify a path to save the files"));
        filePrefixEditText.setText(preferences.getString(Constants.PREFERENCE_FILEPREFIX, "Specify a file prefix (optional)"));
        fileDateFormatEditText.setText(preferences.getString(Constants.PREFERENCE_FILETIMESTAMP, "yyyyMMdd_HHmmss"));
        runInBackgroundSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_RUNINBACKGROUND, true));
        frontalCameraSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_FRONT_CAMERA, false));
        recordAudioSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_RECORD_AUDIO, false));
        qualitySeekBar.setProgress(preferences.getInt(Constants.PREFERENCE_QUALIY, 10));
        customVideoFrameRateSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE, false));
        customCaptureFrameRateSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, false));
        videoSizeSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_SIZE, 0));
        videoFrameRateSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_FRAME_RATE, 0));
        captureFrameRateSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_CAPTURE_FRAME_RATE, 0));
        limitSizeMBEditText.setText(preferences.getString(Constants.PREFERENCE_LIMIT_SIZE, "0"));
        limitTimeSecsEditText.setText(preferences.getString(Constants.PREFERENCE_LIMIT_TIME, "0"));
        delayStartSecsEditText.setText(preferences.getString(Constants.PREFERENCE_DELAY_START, "0"));
        focusModeSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_FOCUS_MODE, 0));
        repeatAtLimitSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_REPEAT_AT_LIMIT, false));
        stealthModeSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_STEALTH_MODE, false));
        flashSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_USE_FLASH, false));
        status.setText(preferences.getString(Constants.PREFERENCE_STATUS_STATUS_TEXT, getString(R.string.RecordingStatusReady)));
        serverModeButton.setText(preferences.getInt(Constants.PREFERENCE_STATUS_START_SERVER_BUTTON, R.string.StartServer));
        connectToHostButton.setText(preferences.getInt(Constants.PREFERENCE_STATUS_CONNECT_SERVER_BUTTON, R.string.Connect));
        Utils.setAllItems(this, connectToHostSpinner, preferences.getStringSet(Constants.PREFERENCE_STATUS_SERVERS_LIST, null));
        if (preferences.getInt(Constants.PREFERENCE_STATUS_SELECTED_SERVER, -1) > -1) {
            connectToHostSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_STATUS_SELECTED_SERVER, -1));
        }
    }

    /** Almacenamiento de configuración */
    protected void saveSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREFERENCE_FILEPATH, filePathEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_FILEPREFIX, filePrefixEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_FILETIMESTAMP, fileDateFormatEditText.getText().toString());
        editor.putBoolean(Constants.PREFERENCE_RUNINBACKGROUND, runInBackgroundSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_FRONT_CAMERA, frontalCameraSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_RECORD_AUDIO, recordAudioSwitch.isChecked());
        editor.putInt(Constants.PREFERENCE_QUALIY, qualitySeekBar.getProgress());
        editor.putBoolean(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE, customVideoFrameRateSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, customCaptureFrameRateSwitch.isChecked());
        editor.putInt(Constants.PREFERENCE_VIDEO_SIZE, videoSizeSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_VIDEO_FRAME_RATE, videoFrameRateSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_CAPTURE_FRAME_RATE, captureFrameRateSpinner.getSelectedItemPosition());
        editor.putString(Constants.PREFERENCE_LIMIT_SIZE, limitSizeMBEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_LIMIT_TIME, limitTimeSecsEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_DELAY_START, delayStartSecsEditText.getText().toString());
        editor.putInt(Constants.PREFERENCE_FOCUS_MODE, focusModeSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_FOCUS_MODE, focusModeSpinner.getSelectedItemPosition());
        editor.putBoolean(Constants.PREFERENCE_REPEAT_AT_LIMIT, repeatAtLimitSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_STEALTH_MODE, stealthModeSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_USE_FLASH, flashSwitch.isChecked());
        editor.putString(Constants.PREFERENCE_STATUS_STATUS_TEXT, status.getText().toString());
        editor.putInt(Constants.PREFERENCE_STATUS_START_SERVER_BUTTON, getString(R.string.StartServer).equals(serverModeButton.getText().toString()) ? R.string.StartServer : R.string.StopServer );
        editor.putInt(Constants.PREFERENCE_STATUS_CONNECT_SERVER_BUTTON, getString(R.string.Connect).equals(connectToHostButton.getText().toString()) ? R.string.Connect : R.string.Close);
        editor.putStringSet(Constants.PREFERENCE_STATUS_SERVERS_LIST, Utils.retrieveAllItems(connectToHostSpinner));
        editor.putInt(Constants.PREFERENCE_STATUS_SELECTED_SERVER, connectToHostSpinner.getSelectedItemPosition());
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPreferences();
    }

    /** Validaciones preliminares a la grabacion */
    protected String checkPreconditions() {
        if (!Utils.recordingPathExists(filePathEditText.getText().toString())) {
            return "Specified path does not exist";
        }
        if (limitSizeMBEditText.getText().length()==0) {
            return "Must specify size limit (or 0 for no limit)";
        }
        if (limitTimeSecsEditText.getText().length()==0) {
            return "Must specify time limit (or 0 for no limit)";
        }
        if (delayStartSecsEditText.getText().length()==0) {
            return "Must specify delay start (or 0 for no delay)";
        }
        if (customCaptureFrameRateSwitch.isChecked() && Integer.parseInt(captureFrameRateSpinner.getSelectedItem().toString()) > Integer.parseInt(videoFrameRateSpinner.getSelectedItem().toString())) {
            return "Capture frame rate must be lower or equal to video frame rate";
        }
        return null;
    }


    /** Inicia el modo server */
    public void startServerMode(View view) {

        // SERVER MODE: Se inicio el modo server?
        if (getString(R.string.StartServer).equals(serverModeButton.getText().toString())) {
            NetworkDCQ.getDiscovery().thisHost.setOnLine(true);
            serverModeButton.setText(R.string.StopServer);
            status.setText("Waiting for connection...");
            updateComponentsStatus();
        } else if (getString(R.string.StopServer).equals(serverModeButton.getText().toString())) {
            if (remoteHost!=null) {
                NetworkDCQ.getCommunication().sendMessage(remoteHost, new NetworkData(NetworkData.NOTIFY_SERVER_CLOSE));
            }
            NetworkDCQ.getDiscovery().thisHost.setOnLine(false);
            serverModeButton.setText(R.string.StartServer);
            status.setText(R.string.RecordingStatusReady);
            updateComponentsStatus();
        }
    }



    /** Inicializa los servicios de NetworDCQ */
    protected boolean startNetwork() throws Exception {
        if (networkConsumer != null)
            return true;
        networkConsumer = new NetworkConsumer(this);
        try {

            if (!NetworkDCQ.configureStartup(networkConsumer, null, null, null)) {
                throw new Exception("Network unreachable");
            }

            if (!NetworkDCQ.doStartup(true, true, false)) {
                throw new Exception("Error starting network");
            }
        } catch (Exception e) {
            stopNetwork();
            networkConsumer = null;
            throw (e);
        }

        return true;
    }

    /** Finaliza los servicios de NetworkDCQ */
    protected void stopNetwork() {
        try {
            if (networkConsumer!=null) {
                networkConsumer = null;
                NetworkDCQ.getDiscovery().stopDiscovery();
                NetworkDCQ.getCommunication().stopService();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            networkConsumer = null;
            remoteHost = null;
        }
    }


    // Manejador de notificaciones por parte del servicio.
    class ResponseHandler extends Handler {
        @Override public void handleMessage(Message message) {
            StringBuffer content = new StringBuffer();
            content.append("Error: ");
            content.append(message.obj != null ? message.obj.toString(): "unknown error.");
            if (message.what==Constants.NOTIFY_ERROR) {
                updateComponentsStatus();
                if (!stealthModeSwitch.isChecked()) {
                    Toast.makeText(getBaseContext(), content.toString(), Toast.LENGTH_SHORT).show();
                }
            } else if (message.what==Constants.NOTIFY_START) {
                updateComponentsStatus(true);
                if (!stealthModeSwitch.isChecked()) {
                    Toast.makeText(getBaseContext(), R.string.ServiceStarted, Toast.LENGTH_SHORT).show();
                }
            } else if (message.what==Constants.NOTIFY_STOP) {
                updateComponentsStatus(false);
                if (!stealthModeSwitch.isChecked()) {
                    Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == R.id.goto_local) {
                stopNetwork();
                RecorderService.appMode = Constants.APP_MODE_NORMAL;
                updateLayouts();
                updateComponentsStatus();
            } else if (item.getItemId() == R.id.goto_client) {
                startNetwork();
                NetworkDCQ.getDiscovery().thisHost.setOnLine(false);
                RecorderService.appMode = Constants.APP_MODE_CLIENT;
                serverModeButton.setText(R.string.Connect);
                updateLayouts();
                loadAvailableServers();
            } else if (item.getItemId() == R.id.goto_server) {
                startNetwork();
                NetworkDCQ.getDiscovery().thisHost.setOnLine(false);
                RecorderService.appMode = Constants.APP_MODE_SERVER;
                serverModeButton.setText(R.string.StartServer);
                updateLayouts();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /** Oculta o visualiza partes de la interfaz segun el modo */
    public void updateLayouts() {
        // NORMAL MODE
        if (RecorderService.appMode == Constants.APP_MODE_NORMAL) {
            LinearLayout layout = findViewById(R.id.layout_modifiers);
            layout.setVisibility(View.VISIBLE);
            layout = findViewById(R.id.layout_server);
            layout.setVisibility(View.GONE);
            layout = findViewById(R.id.layout_client);
            layout.setVisibility(View.GONE);
            layout = findViewById(R.id.layout_startsop);
            layout.setVisibility(View.VISIBLE);
        // CLIENT MODE
        } else if (RecorderService.appMode == Constants.APP_MODE_CLIENT) {
            if (remoteHost==null) {
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
            }
            LinearLayout layout = findViewById(R.id.layout_modifiers);
            layout.setVisibility(View.GONE);
            layout = findViewById(R.id.layout_server);
            layout.setVisibility(View.GONE);
            layout = findViewById(R.id.layout_client);
            layout.setVisibility(View.VISIBLE);
            layout = findViewById(R.id.layout_startsop);
            layout.setVisibility(View.VISIBLE);
        // NORMAL MODE SERVER MODE
        } else if (RecorderService.appMode == Constants.APP_MODE_SERVER) {
            LinearLayout layout = findViewById(R.id.layout_modifiers);
            layout.setVisibility(View.VISIBLE);
            layout = findViewById(R.id.layout_server);
            layout.setVisibility(View.VISIBLE);
            layout = findViewById(R.id.layout_client);
            layout.setVisibility(View.GONE);
            layout = findViewById(R.id.layout_startsop);
            layout.setVisibility(View.GONE);
        }
    }

}
