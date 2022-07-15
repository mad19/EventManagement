package params;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.simple.JSONObject;

import java.text.ParseException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class EmailEvent {
    private int unique_metric_id;
    private String threshold_id;
    private String metric_name;
    private String ci_name;
    private String alert_type;
    private String severity;
    private JSONObject emailInfo;
    private String schedule;


    public EmailEvent(TemplateEmail templateEmail, Props props, KafkaEvent kafkaEvent) throws ParseException {
        this.unique_metric_id = kafkaEvent.getUnique_metric_id();
        this.threshold_id = kafkaEvent.getThreshold_id();
        this.alert_type = kafkaEvent.getAlert_type();
        this.schedule = templateEmail.getSchedule();
        this.metric_name = props.getProps_list().get("meas.name");
        this.ci_name = props.getProps_list().get("ci.name");

        JSONObject emailInfo = new JSONObject();
        Set<String> keys = templateEmail.getProps().keySet();
        keys.forEach(key -> {
            emailInfo.put(key, replaceVars((String) templateEmail.getProps().get(key), props));
        });

        this.emailInfo = emailInfo;

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
