package com.lab240.lab240;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.lab240.adapters.HintAdapter;
import com.lab240.lab240.adapters.TerminalAdapter;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.CommandManager;
import com.lab240.utils.Lab240;
import com.lab240.utils.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TerminalActivity extends AppCompatActivity {

    public static class OutLine{
        public enum Type{
            IN, OUT, LOG;
        }

        public OutLine(String value, Type type) {
            this.type = type;
            this.value = value;
        }

        public final Type type;
        public final String value;
    }

    public static final String HINTS = "Hints";
    public static final String IN = "In";
    public static final String OUT = "Out";
    public static final String LOG = "Log";
    public static final String DEVICE = "Device";
    public static final String IN_DATE_FORMAT = "'IN'(dd.MM HH:mm):  ";
    public static final String OUT_DATE_FORMAT = "'OUT'(dd.MM HH:mm):  ";
    public static final String LOG_DATE_FORMAT = "'LOG'(dd.MM HH:mm):  ";
    String out;
    String in;
    String log;
    TerminalAdapter ta;
    static final int KEY = 1;

    MQTT.MessageCallback inCallback, outCallback, logCallback;

    List<OutLine> outs = new ArrayList<>();
    RecyclerView outsView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        Bundle extras = getIntent().getExtras();
        if(!extras.containsKey(IN) || !extras.containsKey(OUT) || !extras.containsKey(DEVICE) || !extras.containsKey(HINTS)) {
            finish();
            return;
        }
        String[] hintsArray = getIntent().getStringArrayExtra(HINTS);
        in = getIntent().getStringExtra(IN);
        out = getIntent().getStringExtra(OUT);
        log = getIntent().getStringExtra(LOG);
        String device = getIntent().getStringExtra(DEVICE);

        outsView = findViewById(R.id.outs);
        RecyclerView hints = findViewById(R.id.hints);
        Button send = findViewById(R.id.send);
        EditText cmd = findViewById(R.id.cmd);

        ta = new TerminalAdapter();
        outsView.setAdapter(ta);

        send.setOnClickListener(view -> {
            String c = cmd.getText().toString();
            cmd.setText("");
            send(c);
        });

        HintAdapter hintAdapter = new HintAdapter(str -> {
            CommandManager cm = new CommandManager(str);
            List<String> parameters = cm.getParameters();
            if(parameters.isEmpty()) {
                send(cm.getResult());
            }else {
                AlertSheetDialog asd = new AlertSheetDialog(this);
                TextView textView = asd.addText(cm.getTemplate());
                textView.setTypeface(ResourcesCompat.getFont(this, R.font.allerta));
                List<EditText> fields = new ArrayList<>();
                for(String par : parameters) {
                    EditText e = asd.addTextInput(par);
                    e.setSingleLine(true);
                    fields.add(e);
                }
                asd.addButton("Отправить", ()->{
                    List<String> pars = new ArrayList<>();
                    for(EditText e : fields){
                        pars.add(e.getText().toString());
                    }
                    send(cm.getResult(pars));
                }, AlertSheetDialog.ButtonType.DEFAULT);
                asd.show();
            }
        });
        hintAdapter.setData(Arrays.asList(hintsArray));
        hints.setAdapter(hintAdapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(device);

        lcc = cause -> {
            handleNoConnection();
        };
        prepareTopics();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Lab240.getMqtt().removeListener(out, outCallback);
        Lab240.getMqtt().unsubscribe(out, KEY);
        Lab240.getMqtt().removeListener(in, inCallback);
        Lab240.getMqtt().unsubscribe(in, KEY);
        Lab240.getMqtt().removeListener(log, logCallback);
        Lab240.getMqtt().unsubscribe(log, KEY);
    }

    public void send(String value){
        if(Lab240.getMqtt().isConnected())
            Lab240.getMqtt().send(in, value, 0);
        else {
            handleNoConnection();
            return;
        }
        ta.setData(this.outs);
    }

    private static String clearMsg(String str){
        return str.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
    }

    public void handleNoConnection(){
        AlertSheetDialog asd = new AlertSheetDialog(this);
        asd.setCancelable(false);
        asd.setCloseOnAction(false);
        asd.addText(getResources().getString(R.string.no_connection));
        asd.addButton("Подключиться", ()->{
            Lab240.getMqtt().connect(this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    prepareTopics();
                    asd.dismiss();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {}
            });
        }, AlertSheetDialog.ButtonType.DEFAULT);
        asd.addButton("Выйти", ()->{
            Lab240.exit(this);
            System.exit(0);
        }, AlertSheetDialog.ButtonType.DESTROY);
        asd.show();
    }

    private MQTT.LostConnectionCallback lcc = null;

    @Override
    protected void onPause() {
        super.onPause();
        if(lcc != null)
            Lab240.getMqtt().removeOnConnectionLostCallback(lcc);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(lcc != null)
            Lab240.getMqtt().addOnConnectionLostCallback(lcc);
    }

    public void prepareTopics(){
        if(!Lab240.getMqtt().isConnected()){
            return;
        }
        Lab240.getMqtt().subscribe(out, 0, KEY);
        outCallback = (topic, msg) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(OUT_DATE_FORMAT, Locale.getDefault());
            this.outs.add(new OutLine(sdf.format(new Date()) + clearMsg(msg.toString()), OutLine.Type.OUT));
            ta.setData(this.outs);
            outsView.smoothScrollToPosition(this.outs.size()-1);
        };
        Lab240.getMqtt().addListener(out, outCallback);

        Lab240.getMqtt().subscribe(in, 0, KEY);
        inCallback = (topic, msg) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(IN_DATE_FORMAT, Locale.getDefault());
            this.outs.add(new OutLine(sdf.format(new Date()) + clearMsg(msg.toString()), OutLine.Type.IN));
            ta.setData(this.outs);
            outsView.smoothScrollToPosition(this.outs.size()-1);
        };
        Lab240.getMqtt().addListener(in, inCallback);

        Lab240.getMqtt().subscribe(log, 0, KEY);
        logCallback = (topic, msg) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(LOG_DATE_FORMAT, Locale.getDefault());
            this.outs.add(new OutLine(sdf.format(new Date()) + clearMsg(msg.toString()), OutLine.Type.LOG));
            ta.setData(this.outs);
            outsView.smoothScrollToPosition(this.outs.size()-1);
        };
        Lab240.getMqtt().addListener(log, inCallback);
    }

}