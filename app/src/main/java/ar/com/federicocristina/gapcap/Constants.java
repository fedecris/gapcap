package ar.com.federicocristina.gapcap;

class Constants {

    // Key para intercambio del messenger
    static final String MESSENGER                                  = "Messenger";
    // Notificacion de Error
    static final int NOTIFY_ERROR                                  = -1;
    // Notificacion de inicio de servicio
    static final int NOTIFY_START                                  = 1;
    // Notificacion de detencion de servicio
    static final int NOTIFY_STOP                                   = 0;
    // Max possible bit rate
    static final int ENCODING_BITRATE_STEP                          = 500000;

    // Nombre de la camara frontal
    static final String FRONT_CAM_NAME                             = "Front";
    // Nombre de la camara trasera
    static final String BACK_CAM_NAME                              = "Back";

    // Shared preferences name
    static final String SHARED_PREFERENCES_NAME                    = "prefs";

    // Focus mode auto
    static final String OPTION_FOCUS_MODE_AUTO                     = "Auto";
    // Focus mode infinity
    static final String OPTION_FOCUS_MODE_INFINITY                 = "Infinity";
    // Focus mode macro
    static final String OPTION_FOCUS_MODE_MACRO                    = "Macro";
    // Focus mode macro
    static final String OPTION_FOCUS_MODE_FIXED                    = "Fixed";

    // Shared preference: run in background
    static final String PREFERENCE_RUNINBACKGROUND                 = "RUN_IN_BACKGROUND";
    // Shared preference: front camera
    static final String PREFERENCE_FRONT_CAMERA                    = "FRONT_CAMERA";
    // Shared preference: record audio
    static final String PREFERENCE_RECORD_AUDIO                    = "RECORD_AUDIO";
    // Shared preference: low quality
    static final String PREFERENCE_QUALIY                          = "QUALITY";
    // Shared preference: file filePath
    static final String PREFERENCE_FILEPATH                        = "FILE_PATH";
    // Shared preference: file prefix
    static final String PREFERENCE_FILEPREFIX                      = "FILE_PREFIX";
    // Shared preference: file extension
    static final String PREFERENCE_FILEEXT                         = "FILE_EXT";
    // Shared preference: file prefix
    static final String PREFERENCE_FILETIMESTAMP                   = "FILE_TIMESTAMP";
    // Shared preference: video size back cam
    static final String PREFERENCE_VIDEO_SIZE_BACK                 = "VIDEO_SIZE_BACK";
    // Shared preference: video size front cam
    static final String PREFERENCE_VIDEO_SIZE_FRONT                = "VIDEO_SIZE_FRONT";
    // Shared preference: video frame rate back cam
    static final String PREFERENCE_VIDEO_FRAME_RATE_BACK           = "VIDEO_FRAME_RATE_BACK";
    // Shared preference: video frame rate front cam
    static final String PREFERENCE_VIDEO_FRAME_RATE_FRONT          = "VIDEO_FRAME_RATE_FRONT";
    // Shared preference: capture frame rate
    static final String PREFERENCE_CAPTURE_FRAME_RATE              = "CAPTURE_FRAME_RATE";
    // Shared preference: custom video frame rate back cam
    static final String PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_BACK    = "CUSTOM_VIDEO_FRAME_RATE_BACK";
    // Shared preference: custom video frame rate front cam
    static final String PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_FRONT   = "CUSTOM_VIDEO_FRAME_RATE_FRONT";
    // Shared preference: custom capture frame rate
    static final String PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE       = "CUSTOM_CAPTURE_FRAME_RATE";
    // Shared preference: limit size
    static final String PREFERENCE_LIMIT_SIZE                      = "LIMIT_SIZE";
    // Shared preference: limit time
    static final String PREFERENCE_LIMIT_TIME                      = "LIMIT_TIME";
    // Shared preference: delay start
    static final String PREFERENCE_DELAY_START                     = "DELAY_START";
    // Shared preference: focus mode back
    static final String PREFERENCE_FOCUS_MODE_BACK                 = "FOCUS_MODE_BACK";
    // Shared preference: focus mode front
    static final String PREFERENCE_FOCUS_MODE_FRONT                 = "FOCUS_MODE_FRONT";
    // Shared preference: repeat at limit
    static final String PREFERENCE_REPEAT_AT_LIMIT                 = "REPEAT_AT_LIMIT";
    // Swap cam at repeat?
    static final String PREFERENCE_SWAP_CAM_AT_REPEAT              = "SWAP_CAM_AT_REPEAT";
    // Shared preference: Force No notifications or sound
    static final String PREFERENCE_STEALTH_MODE                    = "STEALTH_MODE";
    // Shared preference: usar flash?
    static final String PREFERENCE_USE_FLASH                       = "USE_FLASH";
    // Shared preference: status text
    static final String PREFERENCE_STATUS_STATUS_TEXT              = "STATUS_STATUS_TEXT";







}
