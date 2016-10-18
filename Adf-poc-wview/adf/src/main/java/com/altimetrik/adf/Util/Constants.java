package com.altimetrik.adf.Util;

/**
 * Class used for application constants
 * Created by pigounet on 3/30/15.
 */
public class Constants {

    public static final String ATK_COLLECTION_LAYOUT_COVERFLOW = "coverFlow";
    public static final String ATK_COLLECTION_LAYOUT_GRID = "grid";
    public static final String ATK_COLLECTION_LAYOUT_LIST = "list";

    public static final String ATK_MODAL_TRANSITION_SLIDE_IN_FROM_RIGHT = "SlideInFromRight";
    public static final String ATK_MODAL_TRANSITION_FALL_DOWN = "FallDown";
    public static final String ATK_MODAL_TRANSITION_FADE_IN = "FadeIn";

    public static final String ATK_FONT_DIR = "dist/fonts/%s.ttf";

    public static final String ATK_FILTER_ATTRIBUTE_CONTAINS = "CONTAINS";
    public static final String ATK_FILTER_ATTRIBUTE_LIKE = "LIKE";

    public static final int ATK_COLLECTION_JS_DELAY = 600;
    public static final int ATK_ENHANCED_DEVICE_THRESHOLD = 9;

    public static final String ATK_ANGULAR_INVOKE_FUNCTION = "ADF.angularInvoke";

    //Data types
    public static final String ATK_DATA_TYPE_JAVASCRIPT = "javascript";
    public static final String ATK_DATA_TYPE_JSONSTRING = "jsonstring";
    public static final String ATK_DATA_TYPE_LOCAL = "local";

    //Actions
    public static final String ATK_ACTION_SELECT = "ATKActionSelect";
    public static final String ATK_ACTION_CANCEL = "ATKActionCancel";
    public static final String ATK_ACTION_DESELECT = "ATKActionDeselect";
    public static final String ATK_ACTION_MORE_DATA = "ATKActionMoreData";

    //Available commands
    public static final String ATK_COMMAND_CONTACT = "contact";
    public static final String ATK_COMMAND_WIDGET = "widget";
    public static final String ATK_COMMAND_OPEN_APP = "openapp";
    public static final String ATK_COMMAND_ACTION = "action";
    public static final String ATK_COMMAND_NOTIFICATION = "notification";
    public static final String ATK_COMMAND_PROGRESS_HUD = "progresshud";
    public static final String ATK_COMMAND_BADGE = "badge";
    public static final String ATK_COMMAND_LOCAL_NOTIFICATION = "localnotification";
    public static final String ATK_COMMAND_ANALYTICS = "analytics";

    //Analytics operations
    public static final String ATK_OPERATION_SET_ANALYTICS_INIT = "init";
    public static final String ATK_OPERATION_SET_ANALYTICS_SEND_SCREEN_VIEW = "sendscreenview";

    //Widget Operations
    public static final String ATK_OPERATION_CREATE_WIDGET = "createwidget";
    public static final String ATK_OPERATION_CREATE_ALL_WIDGETS = "createallwidgets";
    public static final String ATK_OPERATION_REMOVE_WIDGET = "removewidget";
    public static final String ATK_OPERATION_REMOVE_ALL_WIDGETS = "removeallwidgets";
    public static final String ATK_OPERATION_POST_NOTIFICATION = "postnotification";

    //Notification Operations
    public static final String ATK_OPERATION_SCHEDULE_LOCAL_NOTIFICATION = "schedule";
    public static final String ATK_OPERATION_CANCEL_LOCAL_NOTIFICATION = "cancel";

    //Open App Operations
    public static final String ATK_OPERATION_MAP = "map";
    public static final String ATK_OPERATION_PHONE = "phone";
    public static final String ATK_OPERATION_FILE = "file";

    //Contact operations
    public static final String ATK_OPERATION_CONTACT_ADD = "add";

    //Action operations
    public static final String ATK_OPERATION_EXECUTE_ACTION = "excecuteaction";
    public static final String ATK_OPERATION_CLOSE_APP = "closeapp";
    public static final String ATK_OPERATION_PREPARE_FOR_TRANSITION = "preparefortransition";
    public static final String ATK_OPERATION_COMMIT_TRANSITION = "committransition";
    public static final String ATK_OPERATION_ROTATE = "rotate";

    //Badge operations
    public static final String ATK_OPERATION_SET_BADGE_TEXT = "setbadgetext";

    //Progress HUD operations
    public static final String ATK_OPERATION_PROGRESS_HUD_SHOW = "show";
    public static final String ATK_OPERATION_PROGRESS_HUD_HIDE = "hide";
    public static final String ATK_OPERATION_PROGRESS_HUD_SET_PROGRESS = "setprogress";

    //Progress HUD types
    public static final String ATK_PROGRESS_HUD_INDETERMINATE = "indeterminate";
    public static final String ATK_PROGRESS_HUD_DETERMINATE = "determinate";
    public static final String ATK_PROGRESS_BAR_DETERMINATE_HORIZONTAL_BAR = "determinatehorizontalbar";

    //Badge View alignments
    public static final String ATK_BADGE_VIEW_ALIGNMENT_TOP_RIGHT = "topright";
    public static final String ATK_BADGE_VIEW_ALIGNMENT_TOP_LEFT = "topleft";
    public static final String ATK_BADGE_VIEW_ALIGNMENT_TOP_CENTER = "topcenter";
    public static final String ATK_BADGE_VIEW_ALIGNMENT_BOTTOM_RIGHT = "bottomright";
    public static final String ATK_BADGE_VIEW_ALIGNMENT_BOTTOM_LEFT = "bottomleft";
    public static final String ATK_BADGE_VIEW_ALIGNMENT_BOTTOM_CENTER = "bottomcenter";
    public static final String ATK_BADGE_VIEW_ALIGNMENT_CENTER = "center";
    public static final String ATK_BADGE_VIEW_ALIGNMENT_CENTER_LEFT = "centerleft";
    public static final String ATK_BADGE_VIEW_ALIGNMENT_CENTER_RIGHT = "centerright";

    //Action supported from web
    public static final String ATK_ACTION_SCROLL_TO_PAGE = "scrolltopage";
    public static final String ATK_ACTION_SET_SELECTED = "setselected";
    public static final String ATK_ACTION_JAVASCRIPT = "javascript";

    //Action Sync Content
    public static final String ATK_ACTION_SYNC_CONTENT = "synchronizecontent";

    //Image scales
    public static final String ATK_IMAGE_SCALE_TO_FILL = "scaletofill";
    public static final String ATK_IMAGE_SCALE_ASPECT_FIT = "scaleaspectfit";
    public static final String ATK_IMAGE_SCALE_ASPECT_FILL = "scaleaspectfill";

    //Transition tags
    public static final String ATK_TRANSITION_SCREEN_SHOT = "screenshot";
    public static final String ATK_SPLASH_SCREEN = "splashscreen";

    //Post Notification Options
    public static final String ATK_NOTIFICATION_REMOVE_SPLASH_SCREEN = "RemoveLaunchScreen";
    public static final String ATK_NOTIFICATION_GET_USER_LOCATION = "ATKGetUserLocation";
    public static final String ATK_NOTIFICATION_STOP_UPDATING_USER_LOCATION = "ATKStopUpdatingUserLocation";
    public static final String ATK_NOTIFICATION_ANIMATION = "ATKAnimationNotification";
    public static final String ATK_NOTIFICATION_EXECUTE_ACTION = "ATKExecuteActionNotification";
    public static final String ATK_NOTIFICATION_SHOW_SPLASH_SCREEN = "ShowLaunchScreen";
    public static final String ATK_NOTIFICATION_RESTART_APP = "RestartApp";
    public static final String ATK_NOTIFICATION_NAVIGATION = "ATKNavigationNotification";
    public static final String ATK_NOTIFICATION_CONFIGURE_REMOTE_NOTIFICATIONS = "ConfigureRemoteNotifications";
    public static final String ATK_NOTIFICATION_DEVICE_LANGUAGE = "GetDeviceLanguage";
    public static final String ATK_NOTIFICATION_CLEAR_CACHE_AND_RELOAD_APP = "ClearAndReloadApp";
    public static final String ATK_NOTIFICATION_REMOVE_LIST_ITEM = "ATKRemoveItemNotification";

    //Navigation notifications
    public static final String ATK_NOTIFICATION_NAVIGATION_TYPE_CHANGE_BACKGROUND_COLOR = "changeBackgroundColor";
    public static final String ATK_NOTIFICATION_NAVIGATION_TYPE_CHANGE_TITLE = "changeTitle";
    public static final String ATK_NOTIFICATION_NAVIGATION_TYPE_PUSH_BUNDLE = "pushBundle";
    public static final String ATK_NOTIFICATION_NAVIGATION_TYPE_PUSH_COMPONENT = "pushComponent";
    public static final String ATK_NOTIFICATION_NAVIGATION_TYPE_POP = "pop";

    //Execute Action
    public static final String ATK_NOTIFICATION_EXECUTE_ACTION_SET_ENABLED = "setEnabled";
    public static final String ATK_NOTIFICATION_EXECUTE_ACTION_DISMISS_POPUP = "dismissPopover";

    public static final String ATK_REDIRECT_URL_START = "vfiosbridge://";

    //Charts
    public static final String ATK_CHART_BAR = "coreplot-barchart";
    public static final String ATK_CHART_GROUPED_BAR = "groupedbarchart";
    public static final String ATK_CHART_SCATTER = "scatterchart";
    public static final String ATK_CHART_PIE = "piechart";
    public static final String ATK_CHART_CIRCLE = "circlechart";
    public static final String ATK_CHART_STACKED_BAR = "stackedbarchart";

    //DateTimePicker Types
    public static final String ATK_DATE_TIME_PICKER_TYPE_DATE = "date";
    public static final String ATK_DATE_TIME_PICKER_TYPE_TIME = "time";
    public static final String ATK_DATE_TIME_PICKER_TYPE_DATETIME = "datetime";

    //DateTimePicker Default Values
    public static final String ATK_DATE_TIME_PICKER_DATE_DEFAULT_FORMAT = "MM-dd-yyyy";
    public static final String ATK_DATE_TIME_PICKER_TIME_DEFAULT_FORMAT = "HH:mm";
    public static final String ATK_DATE_TIME_PICKER_DATETIME_DEFAULT_FORMAT = "MM-dd-yyyy HH:mm";

    //Barcode Scanner Supported Types
    public static final String ATK_BARCODE_SCANNER_TYPE_QR = "QR";
    public static final String ATK_BARCODE_SCANNER_TYPE_UPCE = "UPCE";
    public static final String ATK_BARCODE_SCANNER_TYPE_CODE_39 = "Code39";
    public static final String ATK_BARCODE_SCANNER_TYPE_CODE_93 = "Code93";
    public static final String ATK_BARCODE_SCANNER_TYPE_CODE_128 = "Code128";
    public static final String ATK_BARCODE_SCANNER_TYPE_EAN_8 = "EAN8";
    public static final String ATK_BARCODE_SCANNER_TYPE_EAN_13 = "EAN13";
    public static final String ATK_BARCODE_SCANNER_TYPE_PDF_417 = "PDF417";
    public static final String ATK_BARCODE_SCANNER_TYPE_ITF_14 = "ITF14";
    public static final String ATK_BARCODE_SCANNER_TYPE_DATA_MATRIX = "DataMatrix";

    //Notification Device Types
    public static final String ATK_ANDROID_PHONE_TAG = "Android Phone";
    public static final String ATK_ANDROID_TABLET_TAG = "Android Tablet";

    public static final String ATK_NOTIFICATION_ACTION = "adf.intent.action.NOTIFICATION";

}
