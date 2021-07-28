package com.lab240.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager {
    public String getCommand() {
        return command;
    }

    private final String command;
    public CommandManager(String command){
        this.command = command;
    }

    private static final String REGEX_ALL = "^(?:(?:[^\\{\\}\\\\]|(?:\\\\\\\\)|\\\\\\{|\\\\\\})*\\{(?:[^\\{\\}\\\\]|(?:\\\\\\\\)|\\\\\\{|\\\\\\})++\\})*+(?:[^\\{\\}\\\\]|(?:\\\\\\\\)|\\\\\\{|\\\\\\})*$";
    private static final String REGEX_PARAMETER = "\\{((?:[^\\{\\}\\\\]|(?:\\\\\\\\)|\\\\\\{|\\\\\\})++)\\}";

    public List<String> getParameters(){
        if(!command.matches(REGEX_ALL))
            return Collections.emptyList();

        Pattern pattern = Pattern.compile(REGEX_PARAMETER);
        Matcher matcher = pattern.matcher(command);
        List<String> pars = new ArrayList<>();
        while (matcher.find()){
            pars.add(matcher.group(1));
        }
        return pars;
    }

    public String getResult(List<String> values){
        String command = this.command;
        if(command.matches(REGEX_ALL)) {
            for(String value : values)
                command = command.replaceFirst(REGEX_PARAMETER, value);
            }
        return command
                .replace("\\{", "{")
                .replace("\\}", "}")
                .replace("\\\\","\\");
    }

    public String getTemplate(){
        return command
                .replace("\\{", "{")
                .replace("\\}", "}")
                .replace("\\\\","\\");
    }

    public String getResult(){
        return getResult("");
    }

    public String getResult(String value){
        String command = this.command;
        if(command.matches(REGEX_ALL))
            command = command.replaceAll(REGEX_PARAMETER, value);
        return command
                .replace("\\{", "{")
                .replace("\\}", "}")
                .replace("\\\\","\\");
    }
}
