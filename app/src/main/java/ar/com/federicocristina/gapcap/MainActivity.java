package ar.com.federicocristina.gapcap;

import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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
    static public boolean mRecordingStatus = false;
    // Usar frontal o trasera?
    static public boolean useFrontal = false;
    // Enviar al background?
    public static boolean toBackground = true;
    // Tamaños de grabacion
    public static HashMap<Integer, List<android.hardware.Camera.Size>> cameraVideoSizes = null;
    // Tamaños de grabacion
    public static HashMap<Integer, List<Integer>> cameraFPS = null;

    // Start button
    public static Button startButton;
    // Stop button
    public static Button stopButton;
    // Path de grabacion
    public static EditText path;
    // Path de grabacion
    public static EditText filePrefix;
    // Ejecutar en background?
    public static Switch frontalCameraSwitch;
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

        // Gestion de seleccion de camara
        frontalCameraSwitch = (Switch)findViewById(R.id.switch_frontalCamera);
        frontalCameraSwitch.setChecked(useFrontal);
        frontalCameraSwitch.setEnabled(Utils.existsFrontalCamera());

        // Modos de captura
        loadSupportedVideoSizes(frontalCameraSwitch.isChecked());
        loadSupportedFPS(frontalCameraSwitch.isChecked());
        // Habiliacion de boton de grabacion
        updateStartStopButtons(mRecordingStatus);


    }

    public void iniciar(View v) {
        // Debe usarse la camara frontal o la trasera?
        useFrontal = frontalCameraSwitch.isChecked();

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
