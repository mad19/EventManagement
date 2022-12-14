package params;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utils.TemplateParser;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class TemplateEmail {

    private static final Logger log = LogManager.getLogger(TemplateEmail.class.getName());

    private Integer id;
    private Integer alert_template_id;
    private String name;
    private String type;
    private String description;
    private JSONObject props;
    private String schedule;
    private Integer delay_count;
    private Integer delay_time;
    private Integer send_before;
    private Integer repeat_interval;


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
