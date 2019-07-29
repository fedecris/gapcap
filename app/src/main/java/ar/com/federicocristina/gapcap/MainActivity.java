package ar.com.federicocristina.gapcap;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // TAG Logcat
    private static final String TAG = "Recorder";
    // Surface para la reproduccion de video
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    // Estado de grabacion
    static public boolean mRecordingStatus = false;
    // Usar frontal o trasera?
    static public boolean useFrontal = false;
    // Enviar al background?
    public static boolean toBackground = true;
    // Camara de grabacion
    public static Camera mCamera;


    // Path de grabacion
    public static EditText path;
    // Ejecutar en background?
    public static Switch frontalCameraSwitch;
    // Estado de grabacion
    TextView status;
    // Ejecutar en background?
    Switch runInBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Iniciarlizar superficie
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Recuperar componentes generales
        status = findViewById(R.id.textView_status);
        status.setText(mRecordingStatus ? "Activo" : "Inactivo");
        runInBackground = findViewById(R.id.switch_toBackground);
        runInBackground.setChecked(toBackground);
        path = findViewById(R.id.editText_path);

        // Gestion de seleccion de camara
        frontalCameraSwitch = (Switch)findViewById(R.id.switch_frontalCamera);
        frontalCameraSwitch.setChecked(useFrontal);
        frontalCameraSwitch.setEnabled(Utils.existsFrontalCamera());
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
        status.setText("Activo");
        if (toBackground) {
            finish();
        }
    }

    public void detener(View v) {
        stopService(new Intent(MainActivity.this, RecorderService.class));
        status.setText("Inactivo");
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
