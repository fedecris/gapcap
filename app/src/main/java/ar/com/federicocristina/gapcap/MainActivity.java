package ar.com.federicocristina.gapcap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // TAG Logcat
    private static final String TAG = "Recorder";
    // Surface para la reproduccion de video
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    // Message para interactuar
    Messenger messenger = new Messenger(new ResponseHandler());

    // Start button
    public Button startButton;
    // Stop button
    public Button stopButton;
    // Path de grabacion
    public EditText filePathEditText;
    // Path de grabacion
    public EditText filePrefixEditText;
    // Timestamp del archivo
    public EditText fileDateFormatEditText;
    // Camara frontal?
    public Switch frontalCameraSwitch;
    // Usar calidad baja?
    public Switch lowQualitySwitch;
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
    public TextView status;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Habilitar icono en ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        // Iniciarlizar superficie
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Recuperar componentes generales
        startButton = findViewById(R.id.button_startService);
        stopButton = findViewById(R.id.button_StopService);
        status = findViewById(R.id.textView_status);
        runInBackgroundSwitch = findViewById(R.id.switch_toBackground);
        filePathEditText = findViewById(R.id.editText_path);
        filePrefixEditText = findViewById(R.id.editText_filePrefix);
        fileDateFormatEditText = findViewById(R.id.editText_filenameTimestamp);
        lowQualitySwitch = findViewById(R.id.switch_lowQuality);
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

        // Recuperar la configuracion de las camaras y cargar los componentes visuales
        Utils.retrieveCameraFeatures();
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
        updateComponentsStatus(RecorderService.mRecordingStatus);


    }

    public void iniciar(View v) {
        // Validar precondiciones
        String retValue = checkPreconditions();
        if (retValue!=null) {
            Toast.makeText(getBaseContext(), retValue, Toast.LENGTH_LONG).show();
            return;
        }

        updateComponentsStatus(true);

        // Iniciar el intent con el servicio de grabacion
        Intent intent = new Intent  (this, RecorderService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.MESSENGER, messenger);
        intent.putExtra(Constants.PREFERENCE_DELAY_START, Integer.parseInt(delayStartSecsEditText.getText().toString()));
        intent.putExtra(Constants.PREFERENCE_FRONT_CAMERA, frontalCameraSwitch.isChecked());
        intent.putExtra(Constants.PREFERENCE_RECORD_AUDIO, recordAudioSwitch.isChecked());
        intent.putExtra(Constants.PREFERENCE_LOW_QUALIY, lowQualitySwitch.isChecked());
        intent.putExtra(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE, customVideoFrameRateSwitch.isChecked());
        intent.putExtra(Constants.PREFERENCE_VIDEO_FRAME_RATE, Integer.parseInt(videoFrameRateSpinner.getSelectedItem().toString()));
        intent.putExtra(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, customCaptureFrameRateSwitch.isChecked());
        intent.putExtra(Constants.PREFERENCE_CAPTURE_FRAME_RATE, Integer.parseInt(captureFrameRateSpinner.getSelectedItem().toString()));
        intent.putExtra(Constants.PREFERENCE_LIMIT_SIZE, Integer.parseInt(limitSizeMBEditText.getText().toString()));
        intent.putExtra(Constants.PREFERENCE_LIMIT_TIME, Integer.parseInt(limitTimeSecsEditText.getText().toString()));
        intent.putExtra(Constants.PREFERENCE_VIDEO_SIZE, videoSizeSpinner.getSelectedItem().toString());
        intent.putExtra(Constants.PREFERENCE_FOCUS_MODE, focusModeSpinner.getSelectedItem().toString());
        intent.putExtra(Constants.PREFERENCE_FILEPATH, filePathEditText.getText().toString());
        intent.putExtra(Constants.PREFERENCE_FILEPREFIX, filePrefixEditText.getText().toString());
        intent.putExtra(Constants.PREFERENCE_FILETIMESTAMP, fileDateFormatEditText.getText().toString());
        ComponentName ret = startService(intent);
        ret.getClassName();

        // Finalizar la actividad si corresponde
        if (runInBackgroundSwitch.isChecked()) {
            finish();
        }
    }

    public void detener(View v) {
        updateComponentsStatus(false);
        stopService(new Intent(MainActivity.this, RecorderService.class));
    }

    /** Activa o desactiva los botones segun el estado de grabacion */
    public void updateComponentsStatus(boolean recording) {
        customVideoFrameRateSwitch.setEnabled(false); // Se habilita o no dependiendo del timelapse mode
        (startButton).setEnabled(!recording);
        (stopButton).setEnabled(recording);
        status.setText(recording ? R.string.RecordingStatusActive : R.string.RecordingStatusReady);
        customCaptureFrameRateChanged();
        customVideoFrameRateChanged();
    }

    public void reLoadVideoSizesAndVideoFrameRateAndFocusModes() {
        loadSupportedVideoSizes(frontalCameraSwitch.isChecked());
        loadSupportedVideoFrameRates(frontalCameraSwitch.isChecked());
        loadSupportedFocusModes(frontalCameraSwitch.isChecked());
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
        if (Utils.autofocusModes.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK)) {
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

    /** Carga de configuración */
    protected void loadSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        filePathEditText.setText(preferences.getString(Constants.PREFERENCE_FILEPATH, "Specify a path to save the files"));
        filePrefixEditText.setText(preferences.getString(Constants.PREFERENCE_FILEPREFIX, "Specify a file prefix (optional)"));
        fileDateFormatEditText.setText(preferences.getString(Constants.PREFERENCE_FILETIMESTAMP, "yyyyMMdd_HHmmss"));
        runInBackgroundSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_RUNINBACKGROUND, true));
        frontalCameraSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_FRONT_CAMERA, false));
        recordAudioSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_RECORD_AUDIO, false));
        lowQualitySwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_LOW_QUALIY, false));
        customVideoFrameRateSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE, false));
        customCaptureFrameRateSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, false));
        videoSizeSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_SIZE, 0));
        videoFrameRateSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_FRAME_RATE, 0));
        captureFrameRateSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_CAPTURE_FRAME_RATE, 0));
        limitSizeMBEditText.setText(preferences.getString(Constants.PREFERENCE_LIMIT_SIZE, "0"));
        limitTimeSecsEditText.setText(preferences.getString(Constants.PREFERENCE_LIMIT_TIME, "0"));
        delayStartSecsEditText.setText(preferences.getString(Constants.PREFERENCE_DELAY_START, "0"));
        focusModeSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_FOCUS_MODE, 0));
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
        editor.putBoolean(Constants.PREFERENCE_LOW_QUALIY, lowQualitySwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE, customVideoFrameRateSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, customCaptureFrameRateSwitch.isChecked());
        editor.putInt(Constants.PREFERENCE_VIDEO_SIZE, videoSizeSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_VIDEO_FRAME_RATE, videoFrameRateSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_CAPTURE_FRAME_RATE, captureFrameRateSpinner.getSelectedItemPosition());
        editor.putString(Constants.PREFERENCE_LIMIT_SIZE, limitSizeMBEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_LIMIT_TIME, limitTimeSecsEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_DELAY_START, delayStartSecsEditText.getText().toString());
        editor.putInt(Constants.PREFERENCE_FOCUS_MODE, focusModeSpinner.getSelectedItemPosition());
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
        if (customCaptureFrameRateSwitch.isEnabled() && Integer.parseInt(captureFrameRateSpinner.getSelectedItem().toString()) > Integer.parseInt(videoFrameRateSpinner.getSelectedItem().toString())) {
            return "Capture frame rate must be lower or equal to video frame rate";
        }
        return null;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Toast.makeText(getBaseContext(), "SURFACE CREATED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Toast.makeText(getBaseContext(), "SURFACE CHANGED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Toast.makeText(getBaseContext(), "SURFACE DESTROYED", Toast.LENGTH_SHORT).show();
    }

    // Manejador de notificaciones por parte del servicio.
    class ResponseHandler extends Handler {
        @Override public void handleMessage(Message message) {
            if (message.what==Constants.NOTIFY_ERROR) {
                updateComponentsStatus(false);
                Toast.makeText(getBaseContext(), message.obj.toString(), Toast.LENGTH_SHORT).show();
            } else if (message.what==Constants.NOTIFY_START) {
                updateComponentsStatus(true);
                Toast.makeText(getBaseContext(), R.string.ServiceStarted, Toast.LENGTH_SHORT).show();
            } else if (message.what==Constants.NOTIFY_STOP) {
                updateComponentsStatus(false);
                Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
