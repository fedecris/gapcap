package ar.com.federicocristina.gapcap;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class RecorderService extends Service {

    // Tag Logcat
    private static final String TAG = "RecorderService";
    // Servicio de camara
    private static Camera mServiceCamera;
    // Grabacion demedios
    private MediaRecorder mMediaRecorder;

    private int id = 6789;

    @Override
    public void onCreate() {
        try {

            // Si se selecciono utilizar la camara frontal
            if (MainActivity.useFrontal)
                mServiceCamera = Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            else
                mServiceCamera = Camera.open();

            // Adecuar segun orientacion del dispositivo
            mServiceCamera.setDisplayOrientation(Utils.getRotationForPreview(getBaseContext()));

            super.onCreate();
            if (MainActivity.mRecordingStatus == false)
                MainActivity.mRecordingStatus = startRecording();
        } catch (RuntimeException e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e2) {
            Toast.makeText(getBaseContext(), e2.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        stopRecording(false);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public boolean startRecording(){
        try {
            Camera.Parameters params = mServiceCamera.getParameters();
            Size preferredSize = params.getPreferredPreviewSizeForVideo();
            params.setPreviewSize(preferredSize.width, preferredSize.height);
            params.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            mServiceCamera.setParameters(params);

            ViewGroup.LayoutParams layoutParams = MainActivity.mFrameLayoutPreview.getLayoutParams();
            layoutParams.height = preferredSize.height;
            layoutParams.width = preferredSize.width;
            MainActivity.mFrameLayoutPreview.setLayoutParams(layoutParams);

            mServiceCamera.setPreviewDisplay(MainActivity.mSurfaceHolder);
            mServiceCamera.startPreview();
            mServiceCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mMediaRecorder.setOutputFile(Utils.getRecordingPath());
            mMediaRecorder.setVideoEncodingBitRate(6000000);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(getRecordingVideoSize(0), getRecordingVideoSize(1));
            mMediaRecorder.setPreviewDisplay(MainActivity.mSurfaceHolder.getSurface());

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            MainActivity.mRecordingStatus = true;
            MainActivity.updateStartStopButtons(MainActivity.mRecordingStatus);

            // Notificar sobre el inicio del servicio
            Toast.makeText(getBaseContext(), R.string.ServiceStarted, Toast.LENGTH_SHORT).show();
            Notification note=new Notification();
            startForeground(id, note);

            return true;
        } catch (RuntimeException e2) {
            Toast.makeText(getBaseContext(), e2.getMessage(), Toast.LENGTH_LONG).show();
            stopRecording(true);
            return false;
         } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            stopRecording(true);
            return false;
        }
    }

    public void stopRecording(boolean withError ) {
        try {
            MainActivity.mRecordingStatus = false;
            MainActivity.updateStartStopButtons(MainActivity.mRecordingStatus);
            stopForeground(true);

            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
            } catch (Exception e) { }

            try {
                mServiceCamera.reconnect();
                mServiceCamera.stopPreview();
                mServiceCamera.release();
                mServiceCamera = null;
            } catch (Exception e) { }

            if (!withError) {
                Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
            } else {
                stopSelf();
            }


        } catch (RuntimeException e2) {
            Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
        }

    }

    /** Retorna el tamaño de grabacion segun la seleccion del usuario */
    protected int getRecordingVideoSize(int dimension) {
        return Integer.parseInt(MainActivity.videoSizeSpinner.getSelectedItem().toString().split("x")[dimension]);
    }

}
