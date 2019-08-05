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
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // TAG Logcat
    private static final String TAG = "Recorder";
    // Last scheduled service
    protected static long lastScheduled = 0;
    // Schedule
    static PendingIntent pendingIntent;
    // Message para interactuar
    Messenger messenger = new Messenger(new ResponseHandler());

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
        updateComponentsStatus();


    }

    public void iniciar(View v) {
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


    public void detener(View v) {
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
        // Estado componentes sin importar el modo de grabacion
        customVideoFrameRateSwitch.setEnabled(false); // Se habilita o no dependiendo del timelapse mode

        // Existe una operacion de schedule?
        long current = System.currentTimeMillis();
        if (current < lastScheduled) {
            status.setText("Scheduled: " + Utils.getDateTimeFor("yyyy-MM-dd HH:mm:ss", lastScheduled));
            startButton.setEnabled(false);
            stopButton.setText("CANCEL");
            stopButton.setEnabled(true);
        } else {
            status.setText((forceRecording != null && forceRecording) || RecorderService.mRecordingStatus ? R.string.RecordingStatusActive : R.string.RecordingStatusReady);
            stopButton.setText("STOP");
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
            StringBuffer content = new StringBuffer();
            content.append("Error: ");
            content.append(message.obj != null ? message.obj.toString(): "unknown error.");
            if (message.what==Constants.NOTIFY_ERROR) {
                updateComponentsStatus();
                Toast.makeText(getBaseContext(), content.toString(), Toast.LENGTH_SHORT).show();
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
