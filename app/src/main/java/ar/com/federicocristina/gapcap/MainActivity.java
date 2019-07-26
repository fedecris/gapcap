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
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "Recorder";
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static Camera mCamera ;
    public static boolean mPreviewRunning;
    public static boolean toBackground = true;

    TextView status;
    CheckBox runInBackground;
    CheckBox useFrontalCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        status = findViewById(R.id.textView_status);
        status.setText(RecorderService.mRecordingStatus ? "Activo" : "Inactivo");

        runInBackground = findViewById(R.id.checkBox_background);
        runInBackground.setChecked(toBackground);

        useFrontalCam = findViewById(R.id.checkBox_frontal);
        useFrontalCam.setChecked(RecorderService.useFrontal);
    }

    public void iniciar(View v) {

        RecorderService.useFrontal = useFrontalCam.isChecked();

        Intent intent = new Intent  (this, RecorderService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName ret = startService(intent);
        ret.getClassName();
        toBackground = runInBackground.isChecked();
        if (toBackground) {
            finish();
        }
        status.setText("Activo");
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
