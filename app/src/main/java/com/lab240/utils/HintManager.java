package com.lab240.utils;

import com.lab240.devices.Hint;
import com.lab240.devices.HintGroup;
import com.lab240.devices.SingleHint;

import java.util.ArrayList;
import java.util.List;

public class HintManager {

    public static final Hint[] DEFAULT_HINTS = unite(getItselfs("zeroalarm", "resethour1", "resethour2", "clear", "reset", "save", "setdefault", "r11", "r10", "r21", "r20"),
            getGettersAndSetters("aofs", "a2ofs", "aofs2", "lschm", "schm", "schm1", "l2schm", "lschm2", "schm2", "schma", "schma1", "schma2", "ccheck", "strton", "aofh", "onoffschm1", "onoffschm2", "clevel1", "clevel2", "clevel3", "clevel4", "notifyer", "notifyonoff", "notifyhours", "ltempnot", "htempnot", "anotify1", "anotify2", "cnotify1", "cnotify2", "cnotify3", "cnotify4", "dtempl", "ntempl", "ctempl", "tdelta", "email", "hotter", "cooler", "baselog", "tariff", "onoffv1", "onoffv2", "tmatrix", "bbschm1", "bbschm2", "testbool", "testint", "tz", "resetucounter", "cschm1", "cschm2", "om1", "om2"),
            getGetters("tlevels", "tls", "hot", "synced", "time", "salt", "cschm1", "cschm2", "bschm", "net", "ip", "pinset", "sendalive", "cmpl", "r1", "r2"));



    private static List<Hint> getGettersAndSetters(String... pars){
        List<Hint> res = new ArrayList<>();
        for(String str : pars){
            res.add(new HintGroup(str.toUpperCase(), "sh "+str, "set "+str+"={value}"));
        }
        return res;
    }

    private static List<Hint> getSetters(String... pars){
        List<Hint> res = new ArrayList<>();
        for(String str : pars){
            res.add(new HintGroup(str.toUpperCase(), "set "+str+"={value}"));
        }
        return res;
    }

    private static List<Hint> getGetters(String... pars){
        List<Hint> res = new ArrayList<>();
        for(String str : pars){
            res.add(new HintGroup(str.toUpperCase(), "sh "+str));
        }
        return res;
    }

    private static List<Hint> getItselfs(String... pars){
        List<Hint> res = new ArrayList<>();
        for(String str : pars){
            CommandManager cm = new CommandManager(str);
            res.add(new SingleHint(cm.getTemplate(), str));
        }
        return res;
    }

    @SafeVarargs
    private static Hint[] unite(List<Hint>... pars){
        List<Hint> res = new ArrayList<>();
        for(List<Hint> i : pars){
            res.addAll(i);
        }
        return res.toArray(new Hint[0]);
    }
}
