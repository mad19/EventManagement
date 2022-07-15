package params;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class Event {

    private static final Logger log = LogManager.getLogger(Event.class.getName());

    private int unique_metric_id;
    private String threshold_id;
    private String alert_type;
    private String severity;
    private AlertInfo eventInfo;
    private String schedule;

    public Event (Template template, Props props, KafkaEvent kafkaEvent) throws ParseException {

        this.unique_metric_id = kafkaEvent.getUnique_metric_id();
        this.threshold_id = kafkaEvent.getThreshold_id();
        this.alert_type = kafkaEvent.getAlert_type();
        this.schedule = template.getSchedule();

        JSONObject eventInfo = new JSONObject();
        Set<String> keys = template.getProps().keySet();
        keys.forEach(key -> {
            eventInfo.put(key, replaceVars((String) template.getProps().get(key), props));
        });

        AlertInfo alertInfo = new AlertInfo(eventInfo, props, kafkaEvent);

        this.eventInfo = alertInfo;
    }

    public Event (Template template){

    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        //result.append( this.getClass().getName() );

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for ( Field field : fields  ) {
            try {
                result.append( "\"" + field.getName() + "\"" );
                result.append(": ");

                if (field.getName() == "group") {
                    result.append("\"" + field.get(this) + "\" \n}");
                } else {
                    result.append("\"" + field.get(this) + "\",");
                }
            } catch ( IllegalAccessException ex ) {
                System.out.println(ex);
            }
            result.append(newLine);
        }

        return result.toString();
    }

    private String replaceVars(String line, Props props) {
        String rx = "<<(.+?)>+";

        //StringBuilder sb = new StringBuilder();
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile(rx);
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String repString = props.getProps_list().get(matcher.group(1));
            if (repString != null) {
                matcher.appendReplacement(sb, repString);
            } else {

            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String toJson(){
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        return gson.toJson(this);

    }
}
