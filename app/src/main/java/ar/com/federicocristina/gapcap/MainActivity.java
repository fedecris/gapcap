package ar.com.federicocristina.gapcap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // TAG Logcat
    private static final String TAG = "Recorder";
    // Surface para la reproduccion de video
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static FrameLayout mFrameLayoutPreview;
    // Estado de grabacion
    public static boolean mRecordingStatus = false;
    // Tamaños de grabacion
    public static HashMap<Integer, List<android.hardware.Camera.Size>> cameraVideoSizes = null;
    // Tamaños de grabacion
    public static HashMap<Integer, List<Integer>> cameraFPS = null;

    // Start button
    public static Button startButton;
    // Stop button
    public static Button stopButton;
    // Path de grabacion
    public static EditText filePath;
    // Path de grabacion
    public static EditText filePrefix;
    // Camara frontal?
    public static Switch frontalCameraSwitch;
    // Usar calidad baja?
    public static Switch lowQualitySwitch;
    // Grabar audio?
    public static Switch recordAudioSwitch;
    // Video Size
    public static Spinner videoSizeSpinner;
    // Video Size
    public static Spinner fpsSpinner;
    // Estado de grabacion
    public static TextView status;
    // Ejecutar en background?
    public static Switch runInBackgroundSwitch;

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
        mFrameLayoutPreview = (FrameLayout) findViewById(R.id.frameLayout_preview);

        // Recuperar componentes generales
        startButton = findViewById(R.id.button_startService);
        stopButton = findViewById(R.id.button_StopService);
        status = findViewById(R.id.textView_status);
        runInBackgroundSwitch = findViewById(R.id.switch_toBackground);
        filePath = findViewById(R.id.editText_path);
        filePrefix = findViewById(R.id.editText_filePrefix);
        lowQualitySwitch = (Switch)findViewById(R.id.switch_lowQuality);
        recordAudioSwitch = (Switch)findViewById(R.id.switch_audio);
        frontalCameraSwitch = (Switch)findViewById(R.id.switch_frontalCamera);
        frontalCameraSwitch.setEnabled(Utils.existsFrontalCamera());
        frontalCameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                           @Override
                                                           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                               reLoadVideoSizesAndFPS();
                                                           }
                                                       });

        // Modos de captura
        loadSupportedVideoSizes(frontalCameraSwitch.isChecked());
        loadSupportedFPS(frontalCameraSwitch.isChecked());

        // Habilitacion de boton de grabacion
        updateStartStopButtons(mRecordingStatus);

        fpsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                fpsChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        loadSharedPreferences();

    }

    public void iniciar(View v) {
        // Validar precondiciones
        String retValue = checkPreconditions();
        if (retValue!=null) {
            Toast.makeText(getBaseContext(), retValue, Toast.LENGTH_LONG).show();
            return;
        }

        // Iniciar el intent con el servicio de grabacion
        Intent intent = new Intent  (this, RecorderService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName ret = startService(intent);
        ret.getClassName();

        // Finalizar la actividad si corresponde
        if (runInBackgroundSwitch.isChecked()) {
            finish();
        }
    }

    public void detener(View v) {
        stopService(new Intent(MainActivity.this, RecorderService.class));
    }

    /** Activa o desactiva los botones segun el estado de grabacion */
    public static void updateStartStopButtons(boolean recording) {
        ((Button)startButton).setEnabled(!recording);
        ((Button)stopButton).setEnabled(recording);
        status.setText(recording ? R.string.RecordingStatusActive : R.string.RecordingStatusReady);
    }

    public void reLoadVideoSizesAndFPS() {
        loadSupportedVideoSizes(frontalCameraSwitch.isChecked());
        loadSupportedFPS(frontalCameraSwitch.isChecked());
    }

    public void fpsChanged() {
        // Si se selecciono grabar audio pero el FPS no es el por defecto, entonces quitar el audio
        if (cameraFPS!=null && (!Constants.FPS_DEFAULT.equals(fpsSpinner.getSelectedItem().toString()))) {
            recordAudioSwitch.setChecked(false);
            recordAudioSwitch.setEnabled(false);
        } else {
            recordAudioSwitch.setEnabled(true);
        }
    }

    /** Carga el spinner de opciones de tamaño de grabacion */
    protected void loadSupportedVideoSizes(boolean frontalCam) {
        if (cameraVideoSizes == null) {
            cameraVideoSizes = Utils.getSupportedVideoSizes();
        }

        ArrayList<String> opciones = new ArrayList<String>();
        List<Camera.Size> sizes = cameraVideoSizes.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        for (Camera.Size size : sizes) {
            opciones.add(size.width + "x" + size.height);
        }
        videoSizeSpinner = (Spinner)findViewById(R.id.spinner_videoSize);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        videoSizeSpinner.setAdapter(adapter);
    }

    /** Carga el spinner de opciones de tamaño de grabacion */
    protected void loadSupportedFPS(boolean frontalCam) {
        if (cameraFPS == null) {
            cameraFPS = Utils.getSupportedFps();
        }

        ArrayList<String> opciones = new ArrayList<String>();
        opciones.add(Constants.FPS_DEFAULT);
        List<Integer> fpss = cameraFPS.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        for (Integer fps: fpss) {
            opciones.add(fps.toString());
        }
        fpsSpinner = (Spinner)findViewById(R.id.spinner_fps);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        fpsSpinner.setAdapter(adapter);
    }

    protected void loadSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        filePath.setText(preferences.getString(Constants.PREFERENCE_FILEPATH, "Specify a path to save the files"));
        filePrefix.setText(preferences.getString(Constants.PREFERENCE_FILEPREFIX, "Specify a file prefix (optional)"));
        runInBackgroundSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_RUNINBACKGROUND, true));
        frontalCameraSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_FRONT_CAMERA, false));
        recordAudioSwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_RECORD_AUDIO, false));
        lowQualitySwitch.setChecked(preferences.getBoolean(Constants.PREFERENCE_LOW_QUALIY, false));
        videoSizeSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_VIDEO_SIZE, 0));
        fpsSpinner.setSelection(preferences.getInt(Constants.PREFERENCE_FPS, 0));
    }

    protected void saveSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREFERENCE_FILEPATH, filePath.getText().toString());
        editor.putString(Constants.PREFERENCE_FILEPREFIX, filePrefix.getText().toString());
        editor.putBoolean(Constants.PREFERENCE_RUNINBACKGROUND, runInBackgroundSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_FRONT_CAMERA, frontalCameraSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_RECORD_AUDIO, recordAudioSwitch.isChecked());
        editor.putBoolean(Constants.PREFERENCE_LOW_QUALIY, lowQualitySwitch.isChecked());
        editor.putInt(Constants.PREFERENCE_VIDEO_SIZE, videoSizeSpinner.getSelectedItemPosition());
        editor.putInt(Constants.PREFERENCE_FPS, fpsSpinner.getSelectedItemPosition());
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPreferences();
    }

    protected String checkPreconditions() {
        if (!Utils.recordingPathExists()) {
            return "Specified path does not exist";
        }
        return null;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
