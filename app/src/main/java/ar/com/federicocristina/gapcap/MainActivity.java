package ar.com.federicocristina.gapcap;

import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

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
    // Usar frontal o trasera?
    public static boolean useFrontal = false;
    // Usar low quality?
    public static boolean lowQuality = false;
    // Record audio?
    public static boolean recordAudio = true;
    // Enviar al background?
    public static boolean toBackground = true;
    // Tamaños de grabacion
    public static HashMap<Integer, List<android.hardware.Camera.Size>> cameraVideoSizes = null;
    // Tamaños de grabacion
    public static HashMap<Integer, List<Integer>> cameraFPS = null;
    // Current option videoSize
    public static int currentVideoSize = 0;
    // Current option fps
    public static int currentFPS = 0;


    // Start button
    public static Button startButton;
    // Stop button
    public static Button stopButton;
    // Path de grabacion
    public static EditText path;
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
    Switch runInBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Habilitar icono en ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        // Botones start stop
        startButton = findViewById(R.id.button_startService);
        stopButton = findViewById(R.id.button_StopService);

        // Iniciarlizar superficie
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mFrameLayoutPreview = (FrameLayout) findViewById(R.id.frameLayout_preview);

        // Recuperar componentes generales
        status = findViewById(R.id.textView_status);
        runInBackground = findViewById(R.id.switch_toBackground);
        runInBackground.setChecked(toBackground);
        path = findViewById(R.id.editText_path);
        filePrefix = findViewById(R.id.editText_filePrefix);

        // Calidad
        lowQualitySwitch = (Switch)findViewById(R.id.switch_lowQuality);
        lowQualitySwitch.setChecked(lowQuality);

        recordAudioSwitch = (Switch)findViewById(R.id.switch_audio);
        recordAudioSwitch.setChecked(recordAudio);

        // Gestion de seleccion de camara
        frontalCameraSwitch = (Switch)findViewById(R.id.switch_frontalCamera);
        frontalCameraSwitch.setChecked(useFrontal);
        frontalCameraSwitch.setEnabled(Utils.existsFrontalCamera());

        // Modos de captura
        loadSupportedVideoSizes(frontalCameraSwitch.isChecked());
        loadSupportedFPS(frontalCameraSwitch.isChecked());
        // Habiliacion de boton de grabacion
        updateStartStopButtons(mRecordingStatus);

        // Spinners
        videoSizeSpinner.setSelection(currentVideoSize);
        fpsSpinner.setSelection(currentFPS);
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

    }

    public void iniciar(View v) {
        // Debe usarse la camara frontal o la trasera?
        useFrontal = frontalCameraSwitch.isChecked();

        // Low quality?
        lowQuality = lowQualitySwitch.isChecked();

        // Record audio?
        recordAudio = recordAudioSwitch.isChecked();

        // Iniciar el intent con el servicio de grabacion
        Intent intent = new Intent  (this, RecorderService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName ret = startService(intent);
        ret.getClassName();

        // Finalizar la actividad si corresponde
        toBackground = runInBackground.isChecked();

        if (toBackground) {
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

    public void reLoadVideoSizes(View view) {
        loadSupportedVideoSizes(frontalCameraSwitch.isChecked());
        loadSupportedFPS(frontalCameraSwitch.isChecked());
    }

    public void fpsChanged() {
        // Si se selecciono grabar audio pero el FPS no es el por defecto, entonces quitar el audio
        if (cameraFPS!=null && (!"<Default>".equals(fpsSpinner.getSelectedItem().toString()))) {
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
        opciones.add("<Default>");
        List<Integer> fpss = cameraFPS.get(frontalCam ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        for (Integer fps: fpss) {
            opciones.add(fps.toString());
        }
        fpsSpinner = (Spinner)findViewById(R.id.spinner_fps);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.spinner_item_custom, opciones);
        fpsSpinner.setAdapter(adapter);
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
