package utils;

import params.Template;
import params.TemplateEmail;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateParser {

    public static HashSet<String> checkTemplate(Template template){
        Pattern pattern = Pattern.compile("<<(.+?)>+");

        Matcher matcher = pattern.matcher(template.getProps().get("description").toString());
        HashSet<String> vars = new HashSet<>();
        while (matcher.find()) {
            vars.add(template.getProps().get("description").toString().substring(matcher.start(+1), matcher.end()-2));
        }

        matcher = pattern.matcher(template.getProps().get("title").toString());
        while (matcher.find()) {
            vars.add(template.getProps().get("title").toString().substring(matcher.start(+1), matcher.end()-2));
        }

        return vars;
    }

    public static HashSet<String> checkTemplate(TemplateEmail templateEmail){
        Pattern pattern = Pattern.compile("<<(.+?)>+");

        Matcher matcher = pattern.matcher(templateEmail.getProps().get("theme").toString());
        HashSet<String> vars = new HashSet<>();
        while (matcher.find()) {
            vars.add(templateEmail.getProps().get("theme").toString().substring(matcher.start(+1), matcher.end()-2));
        }

        matcher = pattern.matcher(templateEmail.getProps().get("text").toString());
        while (matcher.find()) {
            vars.add(templateEmail.getProps().get("text").toString().substring(matcher.start(+1), matcher.end()-2));
        }

        return vars;
    }

}
