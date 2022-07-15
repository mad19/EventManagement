package params;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class Props {
    private Map<String, String> props_list;

    public Props (KafkaEvent kafkaEvent) {
        props_list = new HashMap<String, String>();
        props_list.put("meas.value",kafkaEvent.getMetric_value());

        int severity_code = kafkaEvent.getSeverity();
        switch (severity_code) {
            case 0:
                props_list.put("severity","critical");
                props_list.put("th.severity","CRITICAL");
                break;
            case 5:
                props_list.put("severity","major");
                props_list.put("th.severity","MAJOR");
                break;
            case 10:
                props_list.put("severity","minor");
                props_list.put("th.severity","MINOR");
                break;
            case 15:
                props_list.put("severity","warning");
                props_list.put("th.severity","WARNING");
                break;
            case 20:
                props_list.put("severity","ok");
                props_list.put("th.severity","NORMAL");
                break;
            case -1:
                props_list.put("severity","informational");
                props_list.put("th.severity","INFORMATIONAL");
                break;
            case -2:
                props_list.put("severity","unknown");
                props_list.put("th.severity","UNKNOWN");
                break;
            case -20:
                props_list.put("severity","cleared");
                props_list.put("th.severity","CLEARED");
                break;
        }
    }
}