package ar.com.federicocristina.gapcap;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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
    // Camara de grabacion
    public static Camera mCamera ;
    // Enviar al background?
    public static boolean toBackground = true;

    // Path de grabacion
    public static EditText path;
    // Estado de grabacion
    TextView status;
    // Ejecutar en background?
    CheckBox runInBackground;
    // Usar camara frontal?
    CheckBox useFrontalCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Iniciarlizar superficie
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Recuperar componentes status
        status = findViewById(R.id.textView_status);
        status.setText(mRecordingStatus ? "Activo" : "Inactivo");
        runInBackground = findViewById(R.id.checkBox_background);
        runInBackground.setChecked(toBackground);
        useFrontalCam = findViewById(R.id.checkBox_frontal);
        useFrontalCam.setChecked(useFrontal);
        path = findViewById(R.id.editText_path);
    }

    public void iniciar(View v) {
        // Debe usarse la camara frontal o la trasera?
        useFrontal = useFrontalCam.isChecked();

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
