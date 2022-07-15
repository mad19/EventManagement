package adapters;

import java.util.ArrayList;

public interface WebServiceProcessor {
    //Web service server
    public static String SERVER = "http://192.168.200.6/mc/";

    //Threshold's alerts
    public static String TH_ALERT = "TH:ALERTS:";

    //Alerts description
    public static String ALERT = "ALERT:";

    //Additional information of threshold
    public static String TH_DESC = "TH:DESC:";

    static String getEndByType(String type) {
        if (type == "ALERT") {
            return WebServiceProcessor.ALERT;
        }
        if (type == "TH_ALERT") {
            return WebServiceProcessor.TH_ALERT;
        }
        if (type == "TH_DESC") {
            return WebServiceProcessor.TH_DESC;
        }
        return "";
    }
}
