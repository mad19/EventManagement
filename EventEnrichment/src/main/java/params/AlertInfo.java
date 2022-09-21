package params;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Data
@AllArgsConstructor
public class AlertInfo {

    private JSONObject attributes;
    private String environment;
    private String createTime;
    private String value;
    private String severity;
    private String event;
    private String resource;
    private String rawData;
    private ArrayList<String> service;
    private String text;
    private String group;


    public AlertInfo(JSONObject eventInfo, Props props, KafkaEvent kafkaEvent) throws ParseException {
        this.environment = "Sys";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date tm = formatter.parse(kafkaEvent.getTm());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        this.createTime = dateFormat.format(tm);

        this.value = kafkaEvent.getMetric_value();
        this.severity = props.getProps_list().get("severity");
        this.event = props.getProps_list().get("meas.name");
        this.text = eventInfo.get("title").toString();
        this.resource = props.getProps_list().get("ci.name");
        this.service = new ArrayList<>(Arrays.asList(props.getProps_list().get("rsm.display_label")));
        this.group = props.getProps_list().get("ci.type");
        this.rawData = eventInfo.get("description").toString();

        JSONObject attrs = new JSONObject();
        attrs.put("region", "RU");
        attrs.put("notification_id", kafkaEvent.getThreshold_id());
        attrs.put("metric_id", kafkaEvent.getUnique_metric_id());
        attrs.put("ci_id", props.getProps_list().get("ci.global_id"));
        attrs.put("rsm_id", props.getProps_list().get("rsm.name"));
        attrs.put("description", eventInfo.get("description").toString());
        attrs.put("solution", eventInfo.get("solution").toString());

        this.attributes = attrs;

    }
}
