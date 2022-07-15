package utils;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import params.KafkaEvent;
import params.Props;
import params.Template;
import params.TemplateEmail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class VariablesUtil {

    private static final Logger log = LogManager.getLogger(VariablesUtil.class.getName());

    public static Props getVariablesOld(Set<String> vars, KafkaEvent kafkaEvent, RedisCommands<String, String> commands)
            throws Exception {

        String th_description = commands.get("TH:DESC:" + kafkaEvent.getThreshold_id());

        JSONParser parser = new JSONParser();
        JSONObject th_description_json = (JSONObject) parser.parse(th_description);

        Props props = new Props(kafkaEvent);

        // ["key":"value","key":"value"]
        vars.forEach( var -> {
            try {
                if (!props.getProps_list().containsKey(var.toString())) {
                    props.getProps_list().put(var.toString(), th_description_json.get(var.toString()).toString());
                }
            } catch (Exception e) {
                log.warn("Переменная не найдена: " + var.toString());
                props.getProps_list().put(var.toString(), String.format("<<%s>>", var.toString()));
            }
        });

        return props;
    }


    public static Props getVariables(Set<String> vars, KafkaEvent kafkaEvent, RedisCommands<String, String> commands)
            throws ParseException {

        JSONParser parser = new JSONParser();

        log.info("Getting dim info");
        JSONObject dim_th = null;
        JSONObject dim_meas = null;
        JSONObject dim_ci = null;
        try {
            dim_th = (JSONObject) parser.parse(commands.get("DIM:TH:" + kafkaEvent.getThreshold_id()));
        } catch (Exception e) {
            log.error("Getting dim_th info fails: " + e.getMessage());
        }
        try {
            dim_meas = (JSONObject) parser.parse(commands.get("DIM:MEAS:" + kafkaEvent.getUnique_metric_id()));
        } catch (Exception e) {
            log.error("Getting dim_meas info fails: " + e.getMessage());
        }
        try {
            dim_ci = (JSONObject) parser.parse(commands.get("DIM:CI:" + dim_meas.get("ci_id")));
        } catch (Exception e) {
            log.error("Getting dim_ci info fails: " + e.getMessage());
        }

        ArrayList<JSONObject> dim_rsms = new ArrayList<>();
        try {
            ArrayList rsm_list = (ArrayList) dim_ci.get("rsms");
            log.info("RSMS list: " + rsm_list.toString());
            rsm_list.forEach(rsm -> {
                String rsm_name = rsm.toString();
                JSONObject dim_rsm = null;
                log.info("Searching info in dim:rsm with rsm name: " + rsm_name);
                try {
                    dim_rsm = (JSONObject) parser.parse(commands.get("DIM:RSM:" + rsm_name));
                    log.info("Successful");
                    log.debug("DIM:RSM: " + dim_rsm.toString());
                } catch (ParseException e) {
                    log.error("Fail while getting rsm info: " + e.getMessage());
                }
                dim_rsms.add(dim_rsm);
            });
        } catch (Exception e) {
            log.error("Getting dim_rsm info fails: " + e.getMessage());
        }


        Props props = new Props(kafkaEvent);

        // ["key":"value","key":"value"]
        JSONObject finalDim_meas = dim_meas;
        JSONObject finalDim_th = dim_th;
        JSONObject finalDim_ci = dim_ci;
        vars.forEach(var -> {
            try {
                if (!props.getProps_list().containsKey(var)) {
                    String[] tmp_var = var.split("\\.");
                    switch (tmp_var[0]) {
                        case "meas":
                            props.getProps_list().put(var, finalDim_meas.get(tmp_var[1]).toString());
                            break;
                        case "th":
                            props.getProps_list().put(var, finalDim_th.get(tmp_var[1]).toString());
                            break;
                        case "rsm":
                            props.getProps_list().put(var, dim_rsms.get(0).get(tmp_var[1]).toString());
                            break;
                        case "ci":
                            props.getProps_list().put(var, finalDim_ci.get(tmp_var[1]).toString());
                            break;
                        default:
                    }
                }
            } catch (Exception e) {
                log.warn("Переменная не найдена: " + var);
                props.getProps_list().put(var, String.format("<<%s>>", var));
            }
        });

        return props;
    }


    public static HashSet<String> parseTemplate(Template template) throws Exception {

        //Получение списка необходимых переменных
        HashSet<String> vars;
        vars = TemplateParser.checkTemplate(template);
        vars.add("ci.name");
        vars.add("ci.global_id");
        vars.add("ci.type");
        vars.add("rsm.display_label");
        vars.add("rsm.name");
        vars.add("meas.name");
        log.debug("VARS: " + vars);
        return vars;
    }


    public static HashSet<String> parseTemplate(TemplateEmail template) throws Exception {

        //Получение списка необходимых переменных
        HashSet<String> vars;
        vars = TemplateParser.checkTemplate(template);
        vars.add("ci.name");
        vars.add("ci.global_id");
        vars.add("ci.type");
        vars.add("rsm.display_label");
        vars.add("rsm.name");
        vars.add("meas.name");
        log.debug("VARS: " + vars);
        return vars;
    }

}
