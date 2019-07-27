package ar.com.federicocristina.gapcap;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {


    public static String getDateTime() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    }

    public static String getRecordingPath() {
        String retValue = Environment.getExternalStorageDirectory().getPath() + File.separator + MainActivity.path.getText() + File.separator +  "cap_" + Utils.getDateTime() + ".mp4";
        // Quitar dobles slashes
        retValue = retValue.replace("//", "/");
        return retValue;
    }
}
