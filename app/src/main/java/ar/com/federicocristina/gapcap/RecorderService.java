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
        MainActivity.mRecordingStatus = false;

        // Si se selecciono utilizar la camara frontal
        if (MainActivity.useFrontal)
            mServiceCamera = Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        else
            mServiceCamera = Camera.open();

        mServiceCamera.setDisplayOrientation(Utils.getRotationForPreview(getBaseContext()));

        super.onCreate();
        if (MainActivity.mRecordingStatus == false)
            startRecording();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        MainActivity.mRecordingStatus = false;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public boolean startRecording(){
        try {
            // Notificar sobre el inicio del servicio
            Toast.makeText(getBaseContext(), R.string.ServiceStarted, Toast.LENGTH_SHORT).show();
            Notification note=new Notification();
            startForeground(id, note);

            Camera.Parameters params = mServiceCamera.getParameters();
            Size preferredSize = params.getPreferredPreviewSizeForVideo();
            params.setPreviewSize(preferredSize.width, preferredSize.height);
            params.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            mServiceCamera.setParameters(params);

            try {
                mServiceCamera.setPreviewDisplay(MainActivity.mSurfaceHolder);
                mServiceCamera.startPreview();
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            mServiceCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mMediaRecorder.setVideoEncodingBitRate(6000000);
            mMediaRecorder.setOutputFile(Utils.getRecordingPath());
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(MainActivity.useFrontal?640:720, 480); // mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
            mMediaRecorder.setPreviewDisplay(MainActivity.mSurfaceHolder.getSurface());

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            MainActivity.mRecordingStatus = true;

            return true;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void stopRecording() {
        Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
        stopForeground(true);
        try {
            mServiceCamera.reconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        mServiceCamera.stopPreview();
        mMediaRecorder.release();

        mServiceCamera.release();
        mServiceCamera = null;
    }

}
