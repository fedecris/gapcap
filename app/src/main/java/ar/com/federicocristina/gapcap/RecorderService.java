package ar.com.federicocristina.gapcap;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.widget.Toast;


public class RecorderService extends Service {

    // Tag Logcat
    private static final String TAG = "RecorderService";
    // Servicio de camara
    private static Camera mServiceCamera;
    // Grabacion de medios
    private MediaRecorder mMediaRecorder;

    private int id = 6789;

    @Override
    public void onCreate() {
        try {

            // Si se selecciono utilizar la camara frontal
            if (MainActivity.frontalCameraSwitch.isChecked())
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

            // Comentado. Se omite la inclusion del preview
            //ViewGroup.LayoutParams layoutParams = MainActivity.mFrameLayoutPreview.getLayoutParams();
            //layoutParams.height = preferredSize.height;
            //layoutParams.width = preferredSize.width;
            //MainActivity.mFrameLayoutPreview.setLayoutParams(layoutParams);

            mServiceCamera.setPreviewDisplay(MainActivity.mSurfaceHolder);
            //mServiceCamera.startPreview();
            mServiceCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);

            // AUDIO AND VIDEO SOURCE
            if (MainActivity.recordAudioSwitch.isChecked()) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // AUDIO AND VIDEO ENCODERS
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            if (MainActivity.recordAudioSwitch.isChecked()) {
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            }

            // VIDEO SIZE
            int width = getRecordingVideoSize(0);
            int height = getRecordingVideoSize(1);
            mMediaRecorder.setVideoSize(width, height);

            // ENCONDING QUALITY
            mMediaRecorder.setVideoEncodingBitRate(CamcorderProfile.get(MainActivity.lowQualitySwitch.isChecked() ? CamcorderProfile.QUALITY_LOW : CamcorderProfile.QUALITY_HIGH).videoBitRate);

            // Video frame rate
            if (!(Constants.DEFAULT.equals(MainActivity.videoFrameRateSpinner.getSelectedItem().toString()))) {
                mMediaRecorder.setVideoFrameRate(Integer.parseInt(MainActivity.videoFrameRateSpinner.getSelectedItem().toString()));

            }

            // Capture frame rate (timelapse mode)
            if (!(Constants.NO_OPTION.equals(MainActivity.captureFrameRateSpinner.getSelectedItem().toString()))) {
                mMediaRecorder.setCaptureRate(Integer.parseInt(MainActivity.captureFrameRateSpinner.getSelectedItem().toString()));
            }

            // ORIENTACION DEL DISPOSITIVO
            mMediaRecorder.setPreviewDisplay(MainActivity.mSurfaceHolder.getSurface());
            mMediaRecorder.setOrientationHint(Utils.getRotationForPreview(getBaseContext()));

            // OUTPUT FILE
            mMediaRecorder.setOutputFile(Utils.getRecordingFileName());

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

            stopForeground(true);
            MainActivity.mRecordingStatus = false;
            MainActivity.updateStartStopButtons(MainActivity.mRecordingStatus);

            if (!withError) {
                Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
            } else {
                stopSelf();
            }

        } catch (RuntimeException e2) {
            Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
        } finally {
            stopSelf();
        }

    }

    /** Retorna el tamaño de grabacion segun la seleccion del usuario */
    protected int getRecordingVideoSize(int dimension) {
        return Integer.parseInt(MainActivity.videoSizeSpinner.getSelectedItem().toString().split("x")[dimension]);
    }

}
