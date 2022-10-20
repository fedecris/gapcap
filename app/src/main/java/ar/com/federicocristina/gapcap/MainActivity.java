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
import java.util.List;


public class MainActivity extends AppCompatActivity {

    // TAG Logcat
    private static final String TAG = "Recorder";
    // Last scheduled service
    protected static long lastScheduled = 0;
    // Schedule
    static PendingIntent pendingIntent;
    // Message para interactuar con el servicio de grabacion
    Messenger messenger = new Messenger(new ResponseHandler());

    // Estado de grabacion
    public static TextView status;
    // Start button
    public static Button startButton;
    // Stop button
    public static Button stopButton;
    // Path de grabacion
    public EditText filePathEditText;
    // Path de grabacion
    public EditText filePrefixEditText;
    // Extension del archivo
    public EditText fileExtEditText;
    // Timestamp del archivo
    public EditText fileDateFormatEditText;
    // Camara frontal?
    public Switch frontalCameraSwitch;
    // Usar calidad baja?
    public SeekBar qualitySeekBar;
    // Grabar audio?
    public Switch recordAudioSwitch;
    // Video Size back cam
    public Spinner videoSizeBackSpinner;
    // Video Size fron cam
    public Spinner videoSizeFrontSpinner;
    // Custom Video Frame Rate Back
    public Switch customVideoFrameRateBackSwitch;
    // Custom Video Frame Rate Front
    public Switch customVideoFrameRateFrontSwitch;
    // Video Frame Rate Back cam
    public Spinner videoFrameRateBackSpinner;
    // Video Frame Rate Front cam
    public Spinner videoFrameRateFrontSpinner;
    // Custom Capture Frame Rate
    public Switch customCaptureFrameRateSwitch;
    // Capture Frame Rate
    public Spinner captureFrameRateSpinner;
    // Ejecutar en background?
    public Switch runInBackgroundSwitch;
    // Limitar tamaño
    public EditText limitSizeMBEditText;
    // Limitar tiempo
    public EditText limitTimeSecsEditText;
    // Demorar inicio
    public EditText delayStartSecsEditText;
    // Focus mode back cam
    public Spinner focusModeBackSpinner;
    // Focus mode front cam
    public Spinner focusModeFrontSpinner;
    // Repetir una vez llegado el limite?
    public Switch repeatAtLimitSwitch;
    // Alternar camara al repetir?
    public Switch swapCamAtRepeatSwitch;
    // Stealth mode
    public Switch stealthModeSwitch;
    // Use Flash?
    public Switch flashSwitch;


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
        fileExtEditText = findViewById(R.id.editText_fileExt);
        fileDateFormatEditText = findViewById(R.id.editText_filenameTimestamp);
        qualitySeekBar = findViewById(R.id.seekBar_quality);
        recordAudioSwitch = findViewById(R.id.switch_audio);
        customVideoFrameRateBackSwitch = findViewById(R.id.switch_customVideoFrameRateBack);
        customVideoFrameRateFrontSwitch = findViewById(R.id.switch_customVideoFrameRateFront);
        customCaptureFrameRateSwitch = findViewById(R.id.switch_customCaptureFrameRate);
        videoFrameRateBackSpinner = findViewById(R.id.spinner_videoFrameRateBack);
        videoFrameRateFrontSpinner = findViewById(R.id.spinner_videoFrameRateFront);
        captureFrameRateSpinner = findViewById(R.id.spinner_captureFrameRate);
        frontalCameraSwitch = findViewById(R.id.switch_frontalCamera);
        frontalCameraSwitch.setEnabled(Utils.existsFrontalCamera());
        limitSizeMBEditText = findViewById(R.id.limitSizeEditText);
        limitTimeSecsEditText = findViewById(R.id.limitTimeEditText);
        delayStartSecsEditText = findViewById(R.id.editText_delayStart);
        focusModeBackSpinner = findViewById(R.id.spinner_focusBack);
        focusModeFrontSpinner = findViewById(R.id.spinner_focusFront);
        repeatAtLimitSwitch = findViewById(R.id.switch_repeatAtLimit);
        swapCamAtRepeatSwitch = findViewById(R.id.Switch_swapCamAtRepeat);
        stealthModeSwitch = findViewById(R.id.switch_stealthMode);
        flashSwitch = findViewById(R.id.switch_flash);
        videoSizeBackSpinner = (Spinner)findViewById(R.id.spinner_videoSizeBack);
        videoSizeFrontSpinner = (Spinner)findViewById(R.id.spinner_videoSizeFront);
        focusModeBackSpinner = (Spinner)findViewById(R.id.spinner_focusBack);
        focusModeFrontSpinner = (Spinner)findViewById(R.id.spinner_focusFront);

        // Recuperar la configuracion de las camaras y cargar los componentes visuales
        Utils.retrieveCameraFeatures(getBaseContext());
        reLoadVideoSizesAndVideoFrameRateAndFocusModes();
        loadCaptureFrameRates();

        // Listener frontal camera switch
        frontalCameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reLoadVideoSizesAndVideoFrameRateAndFocusModes();
            }
        });

        // Listener custom video frame rate back
        customVideoFrameRateBackSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                customVideoFrameRateChanged( videoFrameRateBackSpinner, customVideoFrameRateBackSwitch);
            }
        });

        // Listener custom video frame rate front
        customVideoFrameRateFrontSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                customVideoFrameRateChanged( videoFrameRateFrontSpinner, customVideoFrameRateFrontSwitch);
            }
        });

        // Listener custom capture frame rate
        customCaptureFrameRateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                customCaptureFrameRateChanged();
            }
        });

        // Listener limit
        repeatAtLimitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateComponentsStatus();
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
        try { alarmIntent.putExtra(Constants.PREFERENCE_DELAY_START, Integer.parseInt(delayStartSecsEditText.getText().toString())); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_FRONT_CAMERA, frontalCameraSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_RECORD_AUDIO, recordAudioSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_QUALIY, qualitySeekBar.getProgress()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_BACK, customVideoFrameRateBackSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_FRONT, customVideoFrameRateFrontSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_VIDEO_FRAME_RATE_BACK, Integer.parseInt(videoFrameRateBackSpinner.getSelectedItem().toString())); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_VIDEO_FRAME_RATE_FRONT, Integer.parseInt(videoFrameRateFrontSpinner.getSelectedItem().toString())); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, customCaptureFrameRateSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_CAPTURE_FRAME_RATE, Integer.parseInt(captureFrameRateSpinner.getSelectedItem().toString())); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_LIMIT_SIZE, Integer.parseInt(limitSizeMBEditText.getText().toString())); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_LIMIT_TIME, Integer.parseInt(limitTimeSecsEditText.getText().toString())); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_VIDEO_SIZE_BACK, videoSizeBackSpinner.getSelectedItem().toString()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_VIDEO_SIZE_FRONT, videoSizeFrontSpinner.getSelectedItem().toString()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_FOCUS_MODE_BACK, focusModeBackSpinner.getSelectedItem().toString()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_FOCUS_MODE_FRONT, focusModeFrontSpinner.getSelectedItem().toString()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_FILEPATH, filePathEditText.getText().toString()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_FILEPREFIX, filePrefixEditText.getText().toString()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_FILEEXT, fileExtEditText.getText().toString()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_FILETIMESTAMP, fileDateFormatEditText.getText().toString()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_REPEAT_AT_LIMIT, repeatAtLimitSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_SWAP_CAM_AT_REPEAT, swapCamAtRepeatSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_STEALTH_MODE, stealthModeSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }
        try { alarmIntent.putExtra(Constants.PREFERENCE_USE_FLASH, flashSwitch.isChecked()); } catch (Exception e) { /* Ignore */ }


        // Programar el inicio del servicio de grabacion, o bien iniciar de inmediato
        // Se quita la posibilidad de iniciar en background para minimizar las posibilidades de kill,
        // pasando siempre el inicio del servicio por el AlarmManager
        int delayStart;
        try {
            delayStart = Integer.parseInt(delayStartSecsEditText.getText().toString());
        } catch (Exception e) {
            delayStart = 1;
        }

        lastScheduled = System.currentTimeMillis() + delayStart * 1000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getService(this, 1, alarmIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, lastScheduled, pendingIntent);
        updateComponentsStatus();
    }

    /** Sobrecarga */
    public void detener() {
        detener(null);
    }

    /** Detener la grabacion */
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
        customVideoFrameRateBackSwitch.setEnabled(false); // Se habilita o no dependiendo del timelapse mode
        customVideoFrameRateFrontSwitch.setEnabled(false); // Se habilita o no dependiendo del timelapse mode

        // Swap de camaras habilitado solo si es posible
        if ((!frontalCameraSwitch.isChecked() && !Utils.existsFrontalCamera()) || (frontalCameraSwitch.isChecked() && !Utils.existsBackCamera())) {
            swapCamAtRepeatSwitch.setEnabled(false);
            swapCamAtRepeatSwitch.setChecked(false);
        } else {
            swapCamAtRepeatSwitch.setEnabled(repeatAtLimitSwitch.isChecked());
        }

        // Habilitar frontal o back components segun existencia
        focusModeBackSpinner.setEnabled(Utils.existsBackCamera());
        focusModeFrontSpinner.setEnabled(Utils.existsFrontalCamera());
        videoFrameRateBackSpinner.setEnabled(Utils.existsBackCamera());
        videoFrameRateFrontSpinner.setEnabled(Utils.existsFrontalCamera());
        customVideoFrameRateBackSwitch.setEnabled(Utils.existsBackCamera());
        customVideoFrameRateFrontSwitch.setEnabled(Utils.existsFrontalCamera());
        videoSizeBackSpinner.setEnabled(Utils.existsBackCamera());
        videoSizeFrontSpinner.setEnabled(Utils.existsFrontalCamera());

        // Existe una operacion de schedule?
        long current = System.currentTimeMillis();
        if (current < lastScheduled) {
            status.setText("Scheduled: " + Utils.getDateTimeFor(getString(R.string.DateTimeFormat), lastScheduled));
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            stopButton.setText(R.string.Cancel);
        } else {
            status.setText((forceRecording != null && forceRecording) || RecorderService.mRecordingStatus ? R.string.RecordingStatusActive : R.string.RecordingStatusReady);
            stopButton.setText(R.string.Stop);
            startButton.setEnabled(forceRecording !=null ? !forceRecording : !RecorderService.mRecordingStatus);
            stopButton.setEnabled(forceRecording != null? forceRecording : RecorderService.mRecordingStatus);
        }
        // Durante la grabacion no deberia poder modificarse la configuracion de grabacion
        LinearLayout layout = findViewById(R.id.layout_modifiers);
        layout.setVisibility(stopButton.isEnabled() ? View.GONE : View.VISIBLE);

        customCaptureFrameRateChanged();
        customVideoFrameRateChanged(videoFrameRateBackSpinner, customVideoFrameRateBackSwitch);
        customVideoFrameRateChanged(videoFrameRateFrontSpinner, customVideoFrameRateFrontSwitch);
    }

    public void reLoadVideoSizesAndVideoFrameRateAndFocusModes() {
        if (Utils.existsBackCamera()) {
            loadSupportedVideoSizes(false, videoSizeBackSpinner);
            loadSupportedFocusModes(false, focusModeBackSpinner);
            loadSupportedVideoFrameRates(false, videoFrameRateBackSpinner);
        }

        if (Utils.existsFrontalCamera()) {
            loadSupportedVideoSizes(true, videoSizeFrontSpinner);
            loadSupportedFocusModes(true, focusModeFrontSpinner);
            loadSupportedVideoFrameRates(true, videoFrameRateFrontSpinner);
        }

        if (!Utils.flashSupport.get(frontalCameraSwitch.isChecked() ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK)) {
            flashSwitch.setChecked(false);
            flashSwitch.setEnabled(false);
        } else {
            flashSwitch.setEnabled(true);
        }

    }

    public void customVideoFrameRateChanged(Spinner aSpinner, Switch aSwitch) {
        // Video frame rate custom
        aSpinner.setEnabled(aSwitch.isChecked());
    }

    public void customCaptureFrameRateChanged() {
        // Si se especifica modo time lapse, entonces desactivar el sonido
        captureFrameRateSpinner.setEnabled(customCaptureFrameRateSwitch.isChecked());
        recordAudioSwitch.setEnabled(!customCaptureFrameRateSwitch.isChecked());
        customVideoFrameRateBackSwitch.setChecked(customCaptureFrameRateSwitch.isChecked());
        customVideoFrameRateFrontSwitch.setChecked(customCaptureFrameRateSwitch.isChecked());
        if (customCaptureFrameRateSwitch.isChecked()) {
            recordAudioSwitch.setChecked(false);
        }
    }

    /** Carga el spinner de opciones de tamaño de grabacion */
    protected void loadSupportedVideoSizes(boolean frontalCam, Spinner aSpinner) {
        ArrayList<String> opciones = new ArrayList<String>();
        List<Camera.Size> sizes = Utils.cameraVideoSizes.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        for (Camera.Size size : sizes) {
            opciones.add(size.width + "x" + size.height);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        aSpinner.setAdapter(adapter);


    }

    /** Carga el spinner de opciones de tamaño de grabacion */
    protected void loadSupportedVideoFrameRates(boolean frontalCam, Spinner aSpinner) {
        ArrayList<String> opciones = new ArrayList<String>();
        List<Integer> fpss = Utils.videoFrameRates.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        for (Integer fps: fpss) {
            opciones.add(fps.toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        aSpinner.setAdapter(adapter);
    }

    /** Carga las opciones de foco */
    protected void loadSupportedFocusModes(boolean frontalCam, Spinner aSpinner) {
        ArrayList<String> opciones = new ArrayList<String>();
        if (Utils.autoFocusSupport.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK)) {
            opciones.add(Constants.OPTION_FOCUS_MODE_AUTO);
            opciones.add(Constants.OPTION_FOCUS_MODE_INFINITY);
            opciones.add(Constants.OPTION_FOCUS_MODE_MACRO);
        } else
            opciones.add(Constants.OPTION_FOCUS_MODE_FIXED);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        aSpinner.setAdapter(adapter);
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
        filePrefixEditText.setText(preferences.getString(Constants.PREFERENCE_FILEPREFIX, "pref_"));
        fileExtEditText.setText(preferences.getString(Constants.PREFERENCE_FILEEXT, ".mp4"));
        fileDateFormatEditText.setText(preferences.getString(Constants.PREFERENCE_FILETIMESTAMP, "yyyyMMdd_HHmmss"));
        runInBackgroundSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_RUNINBACKGROUND, true));
        frontalCameraSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_FRONT_CAMERA, false));
        recordAudioSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_RECORD_AUDIO, false));
        qualitySeekBar.setProgress(preferences.getInt(Constants.PREFERENCE_QUALIY, 10));
        customVideoFrameRateBackSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_BACK, false));
        customVideoFrameRateFrontSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_FRONT, false));
        customCaptureFrameRateSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, false));
        videoSizeBackSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_SIZE_BACK, 0));
        videoSizeFrontSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_SIZE_FRONT, 0));
        videoFrameRateBackSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_FRAME_RATE_BACK, 0));
        videoFrameRateFrontSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_FRAME_RATE_FRONT, 0));
        captureFrameRateSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_CAPTURE_FRAME_RATE, 0));
        limitSizeMBEditText.setText(preferences.getString(Constants.PREFERENCE_LIMIT_SIZE, "0"));
        limitTimeSecsEditText.setText(preferences.getString(Constants.PREFERENCE_LIMIT_TIME, "0"));
        delayStartSecsEditText.setText(preferences.getString(Constants.PREFERENCE_DELAY_START, "0"));
        focusModeBackSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_FOCUS_MODE_BACK, 0));
        focusModeFrontSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_FOCUS_MODE_FRONT, 0));
        repeatAtLimitSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_REPEAT_AT_LIMIT, false));
        swapCamAtRepeatSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_SWAP_CAM_AT_REPEAT, false));
        stealthModeSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_STEALTH_MODE, false));
        flashSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_USE_FLASH, false));
        status.setText(preferences.getString(Constants.PREFERENCE_STATUS_STATUS_TEXT, getString(R.string.RecordingStatusReady)));
    }

    /** Almacenamiento de configuración */
    protected void saveSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREFERENCE_FILEPATH, filePathEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_FILEPREFIX, filePrefixEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_FILEEXT, fileExtEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_FILETIMESTAMP, fileDateFormatEditText.getText().toString());
        editor.putBoolean(Constants.PREFERENCE_RUNINBACKGROUND, runInBackgroundSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_FRONT_CAMERA, frontalCameraSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_RECORD_AUDIO, recordAudioSwitch.isChecked());
        editor.putInt(Constants.PREFERENCE_QUALIY, qualitySeekBar.getProgress());
        editor.putBoolean(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_BACK, customVideoFrameRateBackSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_FRONT, customVideoFrameRateFrontSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE, customCaptureFrameRateSwitch.isChecked());
        editor.putInt(Constants.PREFERENCE_VIDEO_SIZE_BACK, videoSizeBackSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_VIDEO_SIZE_FRONT, videoSizeFrontSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_VIDEO_FRAME_RATE_BACK, videoFrameRateBackSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_VIDEO_FRAME_RATE_FRONT, videoFrameRateFrontSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_CAPTURE_FRAME_RATE, captureFrameRateSpinner.getSelectedItemPosition());
        editor.putString(Constants.PREFERENCE_LIMIT_SIZE, limitSizeMBEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_LIMIT_TIME, limitTimeSecsEditText.getText().toString());
        editor.putString(Constants.PREFERENCE_DELAY_START, delayStartSecsEditText.getText().toString());
        editor.putInt(Constants.PREFERENCE_FOCUS_MODE_BACK, focusModeBackSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_FOCUS_MODE_FRONT, focusModeFrontSpinner.getSelectedItemPosition());
        editor.putBoolean(Constants.PREFERENCE_REPEAT_AT_LIMIT, repeatAtLimitSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_SWAP_CAM_AT_REPEAT, swapCamAtRepeatSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_STEALTH_MODE, stealthModeSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_USE_FLASH, flashSwitch.isChecked());
        editor.putString(Constants.PREFERENCE_STATUS_STATUS_TEXT, status.getText().toString());
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
        if (customCaptureFrameRateSwitch.isChecked() &&
                (Integer.parseInt(captureFrameRateSpinner.getSelectedItem().toString()) > Integer.parseInt(videoFrameRateBackSpinner.getSelectedItem().toString()) ||
                 Integer.parseInt(captureFrameRateSpinner.getSelectedItem().toString()) > Integer.parseInt(videoFrameRateFrontSpinner.getSelectedItem().toString())))
        {
            return "Capture frame rate must be lower or equal to video frame rate";
        }
        return null;
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


}
