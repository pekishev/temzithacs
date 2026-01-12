package ru.temzit.wificontrol;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import ru.temzit.wificontrol.SimpleClient;

/* loaded from: classes.dex */
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    static Context AppContext = null;
    private static final int IDM_OPEN = 101;
    private static final int IDM_SAVE = 102;
    static boolean KeepScreenOn = false;
    static int LastFailuresValue = 0;
    static String PhoneNumber = "";
    static int SelectedParameter = 0;
    static boolean SendFailureSms = false;
    static SimpleClient SimpleClientThread = null;
    static ConnState connState = null;
    static boolean get_settings = false;
    static ImageView imageView = null;
    static boolean isConnectOnline = false;
    static boolean isConnectOnlinePrev = true;
    static boolean is_render = false;
    static ImageView iv_RtcState;
    static ImageView iv_WifiSettings;
    static LinearLayout llPowerCompArea2;
    static LinearLayout llTA_area;
    static LinearLayout llTWaterArea1;
    static LinearLayout llTWaterArea2;
    static TextView tv_Begin;
    static TextView tv_Clock;
    static TextView tv_CompFreq;
    static TextView tv_CompFreq2;
    static TextView tv_Debug;
    static TextView tv_End;
    static TextView tv_Errors;
    static TextView tv_Extra;
    static TextView tv_Flow;
    static TextView tv_GVSModeSet;
    static TextView tv_Loading;
    static TextView tv_Pin;
    static TextView tv_Pout;
    static TextView tv_Schedule;
    static TextView tv_State;
    static TextView tv_TA_term;
    static TextView tv_Tb;
    static TextView tv_Tb2;
    static TextView tv_Tcond;
    static TextView tv_Tcond2;
    static TextView tv_Tevap;
    static TextView tv_Tevap2;
    static TextView tv_Tf;
    static TextView tv_Tf2;
    static TextView tv_Tgvs;
    static TextView tv_TgvsSet;
    static TextView tv_Tin;
    static TextView tv_Tout;
    static TextView tv_TroomSet;
    static TextView tv_TwaterSet;
    static TextView tv_Version;
    final int REQUEST_CODE = 0;
    final int REQUEST_RTC = 1;
    Dialog dialog_setip;
    Dialog dialog_setwifi;
    private Sensor mAmbientTemp;
    private SensorManager mSensorManager;
    SharedPreferences mSettings;
    static TextView[] powerScale = new TextView[10];
    static TextView[] heaterScale = new TextView[3];
    static CountDownTimer CfgTimer = new CountDownTimer(2000, 500) { // from class: ru.temzit.wificontrol.MainActivity.1
        @Override // android.os.CountDownTimer
        public void onTick(long j) {
        }

        @Override // android.os.CountDownTimer
        public void onFinish() {
            MainActivity.tv_Loading.setVisibility(4);
            Toast.makeText(MainActivity.AppContext, "не дождались", 1).show();
            SimpleClient.sendCfg = 0;
            MainActivity.get_settings = false;
        }
    };

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onSetMode(View view) {
    }

    class ConnState {
        int lost = 0;
        int received = 0;
        int fail = 0;

        ConnState() {
        }
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent sensorEvent) {
        float f = sensorEvent.values[0];
        tv_Pout = (TextView) findViewById(temzit.wificontrol.R.id.PowerOut);
        tv_Pout.setText(String.valueOf(f));
    }

    protected void render() {
        if (is_render) {
            tv_Extra.setText(SimpleClient.CityTemp);
            tv_Clock.setText(SimpleClient.HostTime);
            if (SimpleClient.Tout > 0.0f) {
                tv_Tout.setText("+" + SimpleClient.Tout);
            } else {
                tv_Tout.setText("" + SimpleClient.Tout);
            }
            if (SimpleClient.Tin > 99.9d) {
                tv_Tin.setText("+99.9");
            } else {
                tv_Tin.setText("+" + SimpleClient.Tin);
            }
            if (SimpleClient.f50Tf > 99) {
                tv_Tf.setText("+99");
            } else {
                tv_Tf.setText("+" + SimpleClient.f50Tf);
            }
            if (SimpleClient.f49Tb > 99) {
                tv_Tb.setText("+99");
            } else {
                tv_Tb.setText("+" + SimpleClient.f49Tb);
            }
            if (SimpleClient.Tgvs > 99) {
                tv_Tgvs.setText("+99");
            } else {
                tv_Tgvs.setText("+" + SimpleClient.Tgvs);
            }
            tv_Tcond.setText("+" + SimpleClient.Tcond);
            tv_Tevap.setText("+" + SimpleClient.Tevap);
            tv_Tcond2.setText("+" + SimpleClient.Tcond2);
            tv_Tevap2.setText("+" + SimpleClient.Tevap2);
            tv_Pin.setText(SimpleClient.Pin + "\nкВт");
            tv_CompFreq.setText(SimpleClient.CompFreq + "Гц");
            tv_CompFreq2.setText(SimpleClient.CompFreq2 + "Гц");
            tv_State.setText(SimpleClient.stateMap.get(Integer.valueOf(SimpleClient.state)));
            if (SimpleClient.Dualmode == 0) {
                llTWaterArea2.setVisibility(4);
                llPowerCompArea2.setVisibility(4);
                tv_Tf2.setText("");
                tv_Tb2.setText("");
                tv_Pout.setText(SimpleClient.Pout + "\nкВт");
                tv_Flow.setText(SimpleClient.Flow + "\nл/мин");
            } else {
                llTWaterArea2.setVisibility(0);
                llPowerCompArea2.setVisibility(0);
                if (SimpleClient.Tf2 > 99) {
                    tv_Tf2.setText("+99");
                } else {
                    tv_Tf2.setText("+" + SimpleClient.Tf2);
                }
                if (SimpleClient.Tb2 > 99) {
                    tv_Tb2.setText("+99");
                } else {
                    tv_Tb2.setText("+" + SimpleClient.Tb2);
                }
                tv_Pout.setText(SimpleClient.Pout + "/" + SimpleClient.Pout2 + "\nкВт");
                tv_Flow.setText(Math.round(SimpleClient.Flow) + "/" + Math.round(SimpleClient.Flow2) + "\nл/мин");
            }
            if (SimpleClient.TAmode != 0) {
                llTA_area.setVisibility(0);
                tv_TA_term.setText("+" + SimpleClient.TA_term);
            } else {
                llTA_area.setVisibility(4);
            }
            if (SimpleClient.Schedule > 1) {
                tv_Schedule.setText(SimpleClient.page_txt[SimpleClient.Schedule - 1]);
            } else {
                tv_Schedule.setText(SimpleClient.page_txt[0]);
            }
            if (!SimpleClient.UseServer && SimpleClient.ControllerRevision == 0) {
                tv_Begin.setVisibility(4);
                tv_End.setVisibility(4);
                tv_TwaterSet.setVisibility(4);
                tv_TroomSet.setVisibility(4);
                tv_TgvsSet.setVisibility(4);
                tv_GVSModeSet.setVisibility(4);
            } else {
                tv_Begin.setVisibility(0);
                tv_End.setVisibility(0);
                tv_TwaterSet.setVisibility(0);
                tv_TroomSet.setVisibility(0);
                tv_TgvsSet.setVisibility(0);
                tv_GVSModeSet.setVisibility(0);
                if (SimpleClient.Schedule < 2) {
                    tv_Begin.setText("00:00");
                    tv_End.setText("24:00");
                } else {
                    tv_Begin.setText(SimpleClient.Begin + ":00");
                    tv_End.setText(SimpleClient.End + ":00");
                }
                tv_TwaterSet.setText("+" + SimpleClient.TwaterSet);
                tv_TgvsSet.setText("+" + SimpleClient.TgvsSet);
                if (SimpleClient.TroomSet > 16) {
                    tv_TroomSet.setText("+" + SimpleClient.TroomSet);
                } else {
                    tv_TroomSet.setText("нет");
                }
                if (SimpleClient.ReceivedPage == 0) {
                    tv_GVSModeSet.setText(SimpleClient.gvsModeMap.get(Integer.valueOf(SimpleClient.GVSModeSet)));
                } else {
                    tv_GVSModeSet.setText(SimpleClient.gvsModeExtraMap.get(Integer.valueOf(SimpleClient.GVSModeSet)));
                }
            }
            tv_Debug.setText("R:" + connState.received + "L:" + connState.lost + "TCP:" + connState.fail);
            tv_Version.setText("Version 1.7.6");
            int i = 0;
            while (true) {
                TextView[] textViewArr = powerScale;
                if (i >= textViewArr.length) {
                    break;
                }
                if (textViewArr[i] != null) {
                    if (i + 1 <= SimpleClient.f48P) {
                        powerScale[i].setVisibility(0);
                    } else {
                        powerScale[i].setVisibility(4);
                    }
                }
                i++;
            }
            for (TextView textView : heaterScale) {
                textView.setVisibility(4);
            }
            if (SimpleClient.PowerTen >= 10 && SimpleClient.PowerTen <= 85) {
                heaterScale[0].setVisibility(0);
            } else if (SimpleClient.PowerTen >= 86 && SimpleClient.PowerTen <= 170) {
                heaterScale[0].setVisibility(0);
                heaterScale[1].setVisibility(0);
            } else if (SimpleClient.PowerTen >= 171 && SimpleClient.PowerTen <= 255) {
                heaterScale[0].setVisibility(0);
                heaterScale[1].setVisibility(0);
                heaterScale[2].setVisibility(0);
            }
            if (SimpleClient.Failures == 0) {
                tv_Errors.setVisibility(4);
            } else {
                tv_Errors.setVisibility(0);
            }
            if (SimpleClient.UseServer && (SimpleClient.cfgState == 1 || SimpleClient.cfgState == 2 || SimpleClient.cfgState == 3)) {
                iv_RtcState.setVisibility(0);
            } else {
                iv_RtcState.setVisibility(4);
            }
            if (SimpleClient.UseServer) {
                iv_WifiSettings.setVisibility(4);
            } else {
                iv_WifiSettings.setVisibility(0);
            }
            if (!SimpleClient.msgToShow.isEmpty() && !SimpleClient.isMsgShown) {
                SimpleClient.isMsgShown = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(SimpleClient.msgToShow);
                builder.setTitle("Сообщение от сервера");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: ru.temzit.wificontrol.MainActivity.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                    }
                });
                builder.setCancelable(true);
                builder.create().show();
            }
        }
        boolean z = isConnectOnlinePrev;
        boolean z2 = isConnectOnline;
        if (z != z2) {
            isConnectOnlinePrev = z2;
            updateConnectStateImage();
        }
        if (SimpleClient.CityIdChanged) {
            SharedPreferences.Editor editorEdit = this.mSettings.edit();
            editorEdit.putInt("CityId", SimpleClient.CityId);
            editorEdit.apply();
            SimpleClient.CityIdChanged = false;
        }
    }

    protected void updateConnectStateImage() {
        if (isConnectOnline) {
            imageView.setImageResource(android.R.drawable.presence_online);
        } else {
            imageView.setImageResource(android.R.drawable.presence_busy);
        }
        imageView.invalidate();
    }

    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(temzit.wificontrol.R.layout.activity_main);
        this.mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        if (this.mSettings.contains("IP")) {
            SimpleClient.ServerIP = this.mSettings.getString("IP", "");
        }
        if (this.mSettings.contains("UseServer")) {
            SimpleClient.UseServer = this.mSettings.getBoolean("UseServer", false);
        }
        if (this.mSettings.contains("Period")) {
            SimpleClient.Period = this.mSettings.getInt("Period", 1);
        }
        if (this.mSettings.contains("Login")) {
            SimpleClient.Login = this.mSettings.getString("Login", "");
        }
        if (this.mSettings.contains("Pass")) {
            SimpleClient.Pass = this.mSettings.getString("Pass", "");
        }
        if (this.mSettings.contains("Serial")) {
            SimpleClient.Serial = this.mSettings.getString("Serial", "");
        }
        if (this.mSettings.contains("CityId")) {
            SimpleClient.CityId = this.mSettings.getInt("CityId", 0);
        }
        if (this.mSettings.contains("CityStr")) {
            SimpleClient.CityStr = this.mSettings.getString("CityStr", "");
        }
        if (this.mSettings.contains("PhoneNumber")) {
            PhoneNumber = this.mSettings.getString("PhoneNumber", "");
        }
        if (this.mSettings.contains("SendFailureSms")) {
            SendFailureSms = this.mSettings.getBoolean("SendFailureSms", false);
        }
        if (this.mSettings.contains("KeepScreenOn")) {
            KeepScreenOn = this.mSettings.getBoolean("KeepScreenOn", false);
        }
        if (KeepScreenOn) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
        this.mSensorManager = (SensorManager) getSystemService("sensor");
        this.mAmbientTemp = this.mSensorManager.getDefaultSensor(13);
        if (!SimpleClient.is_created) {
            connState = new ConnState();
            SimpleClientThread = SimpleClient.createMyClient(new Handler(Looper.getMainLooper()) { // from class: ru.temzit.wificontrol.MainActivity.3
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    System.out.println("MainActivity: handleMessage Recv new Message");
                    int i = message.arg1;
                    if (i == 0) {
                        if (message.arg2 == 1) {
                            MainActivity.connState.lost++;
                        }
                        if (message.arg2 == 2) {
                            MainActivity.connState.fail++;
                        }
                        System.out.println("MainActivity: handleMessage Recv ERROR");
                        MainActivity.this.render();
                        return;
                    }
                    if (i == 1) {
                        MainActivity.connState.received++;
                        System.out.println("MainActivity: handleMessage Recv DATA");
                        MainActivity.this.render();
                        return;
                    }
                    if (i == 2) {
                        System.out.println("MainActivity: handleMessage Recv SETTINGS");
                        MainActivity.connState.received++;
                        MainActivity.this.Settings();
                        return;
                    }
                    if (i == 3) {
                        System.out.println("MainActivity: handleMessage Recv RTCSETTINGS");
                        MainActivity.connState.received++;
                        MainActivity.this.RTCSettings();
                        return;
                    }
                    if (i == 4) {
                        System.out.println("MainActivity: handleMessage Recv CONNECTSTATE");
                        if (message.arg2 == 0) {
                            MainActivity.this.setConnectState(false);
                        } else {
                            MainActivity.this.setConnectState(true);
                        }
                        MainActivity.this.updateConnectStateImage();
                        return;
                    }
                    if (i != 5) {
                        return;
                    }
                    MainActivity.CfgTimer.cancel();
                    MainActivity.this.findViewById(temzit.wificontrol.R.id.Loading).setVisibility(4);
                    System.out.println("MainActivity: handleMessage Recv WIFISETTINGS");
                    MainActivity.connState.received++;
                    EditText editText = (EditText) MainActivity.this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiLogin);
                    EditText editText2 = (EditText) MainActivity.this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiPswd);
                    Spinner spinner = (Spinner) MainActivity.this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiMode);
                    Spinner spinner2 = (Spinner) MainActivity.this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiIndex);
                    editText.setText(SimpleClient.WifiLogin);
                    editText2.setText(SimpleClient.WifiPswd);
                    spinner.setSelection(SimpleClient.WifiMode - 2);
                    spinner2.setSelection(SimpleClient.WifiIndex);
                    SimpleClient.isStoped = false;
                    MainActivity.this.dialog_setwifi.show();
                }
            });
            SimpleClientThread.start();
            SimpleClient.is_created = true;
        }
        init();
        is_render = true;
        render();
        this.dialog_setip = new Dialog(this);
        this.dialog_setwifi = new Dialog(this);
        this.dialog_setip.setTitle("Настройки подключения");
        this.dialog_setip.setContentView(temzit.wificontrol.R.layout.dlg_setip);
        this.dialog_setwifi.setTitle("Настройки wifi подключения");
        this.dialog_setwifi.setContentView(temzit.wificontrol.R.layout.dlg_setwifi);
        Spinner spinner = (Spinner) this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiMode);
        Spinner spinner2 = (Spinner) this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiIndex);
        ArrayAdapter<CharSequence> arrayAdapterCreateFromResource = ArrayAdapter.createFromResource(this, temzit.wificontrol.R.array.wifi_mode_list, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> arrayAdapterCreateFromResource2 = ArrayAdapter.createFromResource(this, temzit.wificontrol.R.array.wifi_index_list, android.R.layout.simple_spinner_item);
        arrayAdapterCreateFromResource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterCreateFromResource2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter((SpinnerAdapter) arrayAdapterCreateFromResource);
        spinner2.setAdapter((SpinnerAdapter) arrayAdapterCreateFromResource2);
        spinner.setSelection(0);
        spinner2.setSelection(0);
        ((EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.PhoneNumber)).addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        updateConnectStateImage();
    }

    public void onClick(View view) {
        EditText editText = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.setIP);
        CheckBox checkBox = (CheckBox) this.dialog_setip.findViewById(temzit.wificontrol.R.id.UseServer);
        EditText editText2 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Period);
        EditText editText3 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Login);
        EditText editText4 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Pass);
        EditText editText5 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Serial);
        EditText editText6 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.CityId);
        EditText editText7 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.CityStr);
        CheckBox checkBox2 = (CheckBox) this.dialog_setip.findViewById(temzit.wificontrol.R.id.SendFailureSms);
        EditText editText8 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.PhoneNumber);
        CheckBox checkBox3 = (CheckBox) this.dialog_setip.findViewById(temzit.wificontrol.R.id.KeepScreenOn);
        editText.setText(SimpleClient.ServerIP);
        checkBox.setChecked(SimpleClient.UseServer);
        editText2.setText("" + SimpleClient.Period);
        editText3.setText(SimpleClient.Login);
        editText4.setText(SimpleClient.Pass);
        editText5.setText(SimpleClient.Serial);
        editText6.setText("" + SimpleClient.CityId);
        editText7.setText(SimpleClient.CityStr);
        checkBox2.setChecked(SendFailureSms);
        editText8.setText(PhoneNumber);
        checkBox3.setChecked(KeepScreenOn);
        TextView textView = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvSetIP);
        TextView textView2 = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvPeriod);
        TextView textView3 = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvLogin);
        TextView textView4 = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvPass);
        TextView textView5 = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvSerial);
        boolean zIsChecked = checkBox.isChecked();
        editText.setEnabled(!zIsChecked);
        textView.setEnabled(!zIsChecked);
        editText2.setEnabled(zIsChecked);
        textView2.setEnabled(zIsChecked);
        editText3.setEnabled(zIsChecked);
        textView3.setEnabled(zIsChecked);
        editText4.setEnabled(zIsChecked);
        textView4.setEnabled(zIsChecked);
        editText5.setEnabled(zIsChecked);
        textView5.setEnabled(zIsChecked);
        editText8.setEnabled(SendFailureSms);
        SimpleClient.isStoped = true;
        this.dialog_setip.show();
    }

    public void onGraphButton(View view) {
        startActivityForResult(new Intent(this, (Class<?>) GraphActivity.class), 0);
    }

    public void onSettings(View view) throws InterruptedException {
        if (!SimpleClient.UseServer && SimpleClient.ControllerRevision == 0) {
            Toast.makeText(this, "Функция не доступна", 0).show();
            return;
        }
        if (!SimpleClient.statusLink) {
            Toast.makeText(this, "Нет связи", 0).show();
            return;
        }
        if (get_settings) {
            return;
        }
        findViewById(temzit.wificontrol.R.id.Loading).setVisibility(0);
        SimpleClient.isStoped = true;
        waitSimpleClientSleepState();
        SimpleClient.sendCfg = 52;
        SimpleClientThread.interrupt();
        get_settings = true;
        CfgTimer.start();
    }

    public void onWifiSettings(View view) throws InterruptedException {
        if (!SimpleClient.statusLink) {
            Toast.makeText(this, "Нет связи", 0).show();
            return;
        }
        findViewById(temzit.wificontrol.R.id.Loading).setVisibility(0);
        SimpleClient.isStoped = true;
        waitSimpleClientSleepState();
        SimpleClient.sendCfg = 57;
        System.out.println("MainActivity:interrupt onWifiSettings");
        SimpleClientThread.interrupt();
        CfgTimer.start();
    }

    public void Settings() {
        if (get_settings) {
            CfgTimer.cancel();
            Intent intent = new Intent(this, (Class<?>) SettingsActivity.class);
            intent.putExtra("config", SimpleClient.config);
            intent.putExtra("ControllerRevision", SimpleClient.ControllerRevision);
            SimpleClient.isStoped = true;
            System.out.println("MainActivity:start activity Settings");
            startActivityForResult(intent, 0);
        }
    }

    public void onRTCSettings(View view) throws InterruptedException {
        if (!SimpleClient.UseServer && SimpleClient.ControllerRevision == 0) {
            Toast.makeText(this, "Функция не доступна", 0).show();
            return;
        }
        if (!SimpleClient.statusLink) {
            Toast.makeText(this, "Нет связи", 0).show();
            return;
        }
        if (get_settings) {
            return;
        }
        findViewById(temzit.wificontrol.R.id.Loading).setVisibility(0);
        SimpleClient.isStoped = true;
        waitSimpleClientSleepState();
        SimpleClient.sendCfg = 54;
        System.out.println("MainActivity:interrupt onRTCSettings");
        SimpleClientThread.interrupt();
        get_settings = true;
        CfgTimer.start();
    }

    public void RTCSettings() {
        if (get_settings) {
            CfgTimer.cancel();
            Intent intent = new Intent(this, (Class<?>) ScheduleActivity.class);
            intent.putExtra("config", SimpleClient.RTCconfig);
            SimpleClient.isStoped = true;
            System.out.println("MainActivity:start activity RTCSettings");
            startActivityForResult(intent, 1);
        }
    }

    public void onErrorsClick(View view) {
        Intent intent = new Intent(this, (Class<?>) ErrorsActivity.class);
        intent.putExtra("failures", SimpleClient.Failures);
        System.out.println("MainActivity:start activity Failures");
        startActivityForResult(intent, 0);
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) throws InterruptedException {
        if (i2 != -1) {
            Toast.makeText(this, "Без изменений", 0).show();
        } else if (i == 0) {
            String stringExtra = intent.getStringExtra("config");
            if (!SimpleClient.config.equalsIgnoreCase(stringExtra)) {
                waitSimpleClientSleepState();
                SimpleClient.config = stringExtra;
                Toast.makeText(this, "Настройки изменились", 0).show();
                SimpleClient.sendCfg = 53;
            } else {
                Toast.makeText(this, "Без изменений", 0).show();
            }
        } else if (i == 1) {
            String stringExtra2 = intent.getStringExtra("config");
            if (!SimpleClient.config.equalsIgnoreCase(stringExtra2)) {
                waitSimpleClientSleepState();
                SimpleClient.RTCconfig = stringExtra2;
                Toast.makeText(this, "Настройки изменились", 0).show();
                SimpleClient.sendCfg = 55;
            } else {
                Toast.makeText(this, "Без изменений", 0).show();
            }
        }
        get_settings = false;
        System.out.println("MainActivity:interrupt");
        SimpleClientThread.interrupt();
        findViewById(temzit.wificontrol.R.id.Loading).setVisibility(4);
    }

    public void dlgSetWifiOnClickOK(View view) throws InterruptedException {
        EditText editText = (EditText) this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiLogin);
        EditText editText2 = (EditText) this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiPswd);
        Spinner spinner = (Spinner) this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiMode);
        Spinner spinner2 = (Spinner) this.dialog_setwifi.findViewById(temzit.wificontrol.R.id.WifiIndex);
        String string = editText.getText().toString();
        String string2 = editText2.getText().toString();
        if (!string.equals(SimpleClient.WifiLogin) || !string2.equals(SimpleClient.WifiPswd) || spinner.getSelectedItemPosition() != SimpleClient.WifiMode - 2 || spinner2.getSelectedItemPosition() != SimpleClient.WifiIndex) {
            SimpleClient.isStoped = true;
            waitSimpleClientSleepState();
            SimpleClient.WifiLogin = string;
            SimpleClient.WifiPswd = string2;
            SimpleClient.WifiMode = spinner.getSelectedItemPosition() + 2;
            SimpleClient.WifiIndex = spinner2.getSelectedItemPosition();
            Toast.makeText(this, "Настройки изменились", 0).show();
            SimpleClient.sendCfg = 58;
            System.out.println("MainActivity:interrupt");
            SimpleClientThread.interrupt();
        }
        this.dialog_setwifi.dismiss();
    }

    public void dlgSetWifiOnClickCancel(View view) {
        this.dialog_setwifi.dismiss();
    }

    public void dlgOnClickOK(View view) throws InterruptedException {
        EditText editText = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.setIP);
        CheckBox checkBox = (CheckBox) this.dialog_setip.findViewById(temzit.wificontrol.R.id.UseServer);
        EditText editText2 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Period);
        EditText editText3 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Login);
        EditText editText4 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Pass);
        EditText editText5 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Serial);
        EditText editText6 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.CityId);
        EditText editText7 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.CityStr);
        EditText editText8 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.PhoneNumber);
        CheckBox checkBox2 = (CheckBox) this.dialog_setip.findViewById(temzit.wificontrol.R.id.SendFailureSms);
        CheckBox checkBox3 = (CheckBox) this.dialog_setip.findViewById(temzit.wificontrol.R.id.KeepScreenOn);
        String string = editText.getText().toString();
        boolean zIsChecked = checkBox.isChecked();
        String string2 = editText2.getText().toString();
        int i = string2.matches("^[1-9]\\d*$") ? Integer.parseInt(string2) : 1;
        String string3 = editText3.getText().toString();
        String string4 = editText4.getText().toString();
        String string5 = editText5.getText().toString();
        PhoneNumber = editText8.getText().toString();
        String string6 = editText6.getText().toString();
        int i2 = string6.matches("^[1-9]\\d*$") ? Integer.parseInt(string6) : 0;
        String string7 = editText7.getText().toString();
        SendFailureSms = checkBox2.isChecked();
        KeepScreenOn = checkBox3.isChecked();
        if (KeepScreenOn) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
        SimpleClient.isStoped = true;
        waitSimpleClientSleepState();
        SimpleClient.ServerIP = string;
        SimpleClient.UseServer = zIsChecked;
        SimpleClient.Period = i > 0 ? i : 1;
        SimpleClient.Login = string3;
        SimpleClient.Pass = string4;
        SimpleClient.Serial = string5;
        SimpleClient.CityId = i2;
        if (!SimpleClient.CityStr.equals(string7)) {
            SimpleClient.CityId = 0;
            i2 = 0;
        }
        SimpleClient.CityStr = string7;
        SimpleClient.WeatherRequestTimeout = 0;
        SimpleClientThread.interrupt();
        SharedPreferences.Editor editorEdit = this.mSettings.edit();
        editorEdit.putString("IP", string);
        editorEdit.putBoolean("UseServer", zIsChecked);
        editorEdit.putInt("Period", i);
        editorEdit.putString("IP", string);
        editorEdit.putString("Login", string3);
        editorEdit.putString("Pass", string4);
        editorEdit.putString("Serial", string5);
        editorEdit.putInt("CityId", i2);
        editorEdit.putString("CityStr", string7);
        editorEdit.putString("PhoneNumber", PhoneNumber);
        editorEdit.putBoolean("SendFailureSms", SendFailureSms);
        editorEdit.putBoolean("KeepScreenOn", KeepScreenOn);
        editorEdit.apply();
        this.dialog_setip.dismiss();
    }

    public void dlgOnClickCancel(View view) throws InterruptedException {
        waitSimpleClientSleepState();
        SimpleClientThread.interrupt();
        this.dialog_setip.dismiss();
    }

    public void dlgOnSendFailureSms(View view) {
        ((EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.PhoneNumber)).setEnabled(((CheckBox) this.dialog_setip.findViewById(temzit.wificontrol.R.id.SendFailureSms)).isChecked());
    }

    public void dlgOnUseServer(View view) {
        boolean zIsChecked = ((CheckBox) this.dialog_setip.findViewById(temzit.wificontrol.R.id.UseServer)).isChecked();
        EditText editText = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.setIP);
        EditText editText2 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Period);
        EditText editText3 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Login);
        EditText editText4 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Pass);
        EditText editText5 = (EditText) this.dialog_setip.findViewById(temzit.wificontrol.R.id.Serial);
        TextView textView = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvSetIP);
        TextView textView2 = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvPeriod);
        TextView textView3 = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvLogin);
        TextView textView4 = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvPass);
        TextView textView5 = (TextView) this.dialog_setip.findViewById(temzit.wificontrol.R.id.tvSerial);
        editText.setEnabled(!zIsChecked);
        textView.setEnabled(!zIsChecked);
        editText2.setEnabled(zIsChecked);
        textView2.setEnabled(zIsChecked);
        editText3.setEnabled(zIsChecked);
        textView3.setEnabled(zIsChecked);
        editText4.setEnabled(zIsChecked);
        textView4.setEnabled(zIsChecked);
        editText5.setEnabled(zIsChecked);
        textView5.setEnabled(zIsChecked);
    }

    public void setConnectState(boolean z) {
        if (isConnectOnline != z) {
            isConnectOnline = z;
        }
    }

    protected void init() {
        AppContext = getApplicationContext();
        tv_Debug = (TextView) findViewById(temzit.wificontrol.R.id.Debug);
        tv_Version = (TextView) findViewById(temzit.wificontrol.R.id.Version);
        tv_Clock = (TextView) findViewById(temzit.wificontrol.R.id.Clock);
        tv_Tout = (TextView) findViewById(temzit.wificontrol.R.id.Tout);
        tv_Tin = (TextView) findViewById(temzit.wificontrol.R.id.Tin);
        tv_Tf = (TextView) findViewById(temzit.wificontrol.R.id.f63Tf);
        tv_Tb = (TextView) findViewById(temzit.wificontrol.R.id.f62Tb);
        tv_Tf2 = (TextView) findViewById(temzit.wificontrol.R.id.Tf2);
        tv_Tb2 = (TextView) findViewById(temzit.wificontrol.R.id.Tb2);
        llTWaterArea1 = (LinearLayout) findViewById(temzit.wificontrol.R.id.TWaterArea1);
        llTWaterArea2 = (LinearLayout) findViewById(temzit.wificontrol.R.id.TWaterArea2);
        llPowerCompArea2 = (LinearLayout) findViewById(temzit.wificontrol.R.id.PowerCompArea2);
        llTA_area = (LinearLayout) findViewById(temzit.wificontrol.R.id.TA_area);
        tv_Tgvs = (TextView) findViewById(temzit.wificontrol.R.id.Tgvs);
        tv_CompFreq = (TextView) findViewById(temzit.wificontrol.R.id.CompFreq);
        tv_CompFreq2 = (TextView) findViewById(temzit.wificontrol.R.id.CompFreq2);
        tv_Schedule = (TextView) findViewById(temzit.wificontrol.R.id.Schedule);
        tv_State = (TextView) findViewById(temzit.wificontrol.R.id.State);
        tv_Tcond = (TextView) findViewById(temzit.wificontrol.R.id.Tcond);
        tv_Tevap = (TextView) findViewById(temzit.wificontrol.R.id.Tevap);
        tv_Tcond2 = (TextView) findViewById(temzit.wificontrol.R.id.Tcond2);
        tv_Tevap2 = (TextView) findViewById(temzit.wificontrol.R.id.Tevap2);
        tv_Pout = (TextView) findViewById(temzit.wificontrol.R.id.PowerOut);
        tv_Pin = (TextView) findViewById(temzit.wificontrol.R.id.PowerIn);
        tv_Flow = (TextView) findViewById(temzit.wificontrol.R.id.Flow);
        tv_TA_term = (TextView) findViewById(temzit.wificontrol.R.id.TA_term);
        tv_TwaterSet = (TextView) findViewById(temzit.wificontrol.R.id.SetWater);
        tv_TgvsSet = (TextView) findViewById(temzit.wificontrol.R.id.SetGVS);
        tv_TroomSet = (TextView) findViewById(temzit.wificontrol.R.id.SetAir);
        tv_GVSModeSet = (TextView) findViewById(temzit.wificontrol.R.id.ModeGVS);
        tv_Begin = (TextView) findViewById(temzit.wificontrol.R.id.Begin);
        tv_End = (TextView) findViewById(temzit.wificontrol.R.id.End);
        tv_Extra = (TextView) findViewById(temzit.wificontrol.R.id.Extra);
        tv_Loading = (TextView) findViewById(temzit.wificontrol.R.id.Loading);
        tv_Errors = (TextView) findViewById(temzit.wificontrol.R.id.Err);
        iv_RtcState = (ImageView) findViewById(temzit.wificontrol.R.id.RtcState);
        iv_WifiSettings = (ImageView) findViewById(temzit.wificontrol.R.id.WifiSettings);
        int i = 0;
        powerScale[0] = (TextView) findViewById(temzit.wificontrol.R.id.f53P1);
        powerScale[1] = (TextView) findViewById(temzit.wificontrol.R.id.f54P2);
        powerScale[2] = (TextView) findViewById(temzit.wificontrol.R.id.f55P3);
        powerScale[3] = (TextView) findViewById(temzit.wificontrol.R.id.f56P4);
        powerScale[4] = (TextView) findViewById(temzit.wificontrol.R.id.f57P5);
        powerScale[5] = (TextView) findViewById(temzit.wificontrol.R.id.f58P6);
        powerScale[6] = (TextView) findViewById(temzit.wificontrol.R.id.f59P7);
        powerScale[7] = (TextView) findViewById(temzit.wificontrol.R.id.f60P8);
        powerScale[8] = (TextView) findViewById(temzit.wificontrol.R.id.f61P9);
        powerScale[9] = (TextView) findViewById(temzit.wificontrol.R.id.P10);
        int i2 = 0;
        while (true) {
            TextView[] textViewArr = powerScale;
            if (i2 >= textViewArr.length) {
                break;
            }
            textViewArr[i2].setVisibility(4);
            i2++;
        }
        heaterScale[0] = (TextView) findViewById(temzit.wificontrol.R.id.H33);
        heaterScale[1] = (TextView) findViewById(temzit.wificontrol.R.id.H66);
        heaterScale[2] = (TextView) findViewById(temzit.wificontrol.R.id.H100);
        while (true) {
            TextView[] textViewArr2 = heaterScale;
            if (i < textViewArr2.length) {
                textViewArr2[i].setVisibility(4);
                i++;
            } else {
                imageView = (ImageView) findViewById(temzit.wificontrol.R.id.Link);
                return;
            }
        }
    }

    protected void waitSimpleClientSleepState() throws InterruptedException {
        while (SimpleClient.ClientState != SimpleClient.eClientStates.SLEEP) {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException unused) {
                System.out.println("MainActivity: interrupted");
            }
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onResume() throws InterruptedException {
        super.onResume();
        init();
        is_render = true;
        render();
        SimpleClient.isStoped = true;
        waitSimpleClientSleepState();
        SimpleClientThread.interrupt();
    }

    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        init();
        is_render = true;
        render();
        SimpleClient.isStoped = false;
    }

    @Override // android.app.Activity
    protected void onRestart() {
        super.onRestart();
        init();
        is_render = true;
        render();
        SimpleClient.isStoped = false;
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        is_render = false;
        SimpleClient.isStoped = true;
    }

    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        is_render = false;
        SimpleClient.isStoped = true;
    }

    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        is_render = false;
        SimpleClient.isStoped = true;
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity, android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i != 0) {
            return;
        }
        if (iArr.length > 0 && iArr[0] == 0) {
            Toast.makeText(getApplicationContext(), "Permission granted", 0).show();
        } else {
            Toast.makeText(getApplicationContext(), "Permission denied", 0).show();
        }
    }
}
