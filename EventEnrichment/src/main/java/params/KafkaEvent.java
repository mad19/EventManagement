package params;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class KafkaEvent {
    private int unique_metric_id;
    private String threshold_id;
    private Integer alert_id;
    private String alert_type;
    private int severity;
    private String tm;
    private String metric_value;
    private String source;


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);

        Field[] fields = this.getClass().getDeclaredFields();

        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                result.append( field.get(this) );
            } catch ( IllegalAccessException ex ) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }

}
