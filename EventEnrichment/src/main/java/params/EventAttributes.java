package params;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventAttributes {

    private String region;
    private String Notification_id;
    private String Notification_status;
    private String Description;

}
