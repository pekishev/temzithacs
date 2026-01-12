package ru.temzit.wificontrol;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.InputDeviceCompat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

/* loaded from: classes.dex */
public class SimpleClient extends Thread {
    static int CityId = 0;
    static boolean CityIdChanged = false;
    static String CityStr = "";
    static String CityTemp = "";
    private static final String HASH_ALGORITHM = "HmacSHA256";
    private static final String HASH_KEY = "seilrrskj34sljusd";
    static String Login = "";
    static final int MAX_AVAILABLE = 100;
    static String Pass = "";
    static int Period = 1;
    static String Serial = "";
    static String ServerIP = "192.168.4.1";
    static boolean UseServer = false;
    static int WeatherRequestTimeout = 0;

    /* renamed from: h */
    static Handler f51h = null;
    static boolean isStoped = false;
    static boolean is_created = false;
    static boolean needCheckSettings = false;
    static int sendCfg;
    static int sendParamNum;
    static int sendParamValue;
    String[] dec2hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    static eClientStates ClientState = eClientStates.NONE;
    static boolean statusLink = false;
    static int ReceivedPage = 0;
    static int connectErrors = 0;
    static String WifiLogin = "";
    static String WifiPswd = "";
    static int WifiMode = 0;
    static int WifiIndex = 0;
    static Map<Integer, String> stateMap = new HashMap<Integer, String>() { // from class: ru.temzit.wificontrol.SimpleClient.1
        {
            put(0, "стоп");
            put(1, "нагрев");
            put(2, "нагрев");
            put(3, "нагрев");
            put(4, "нагрев");
            put(5, "холод");
            put(100, "ГВС");
            put(101, "ГВС");
            put(102, "ГВС");
            put(103, "ГВС*");
        }
    };
    static String[] page_txt = {"ОСН ", "Т1 ", "Т2 ", "Т3 ", "Т4 "};
    static Map<Integer, String> gvsModeMap = new HashMap<Integer, String>() { // from class: ru.temzit.wificontrol.SimpleClient.2
        {
            put(0, "ВЫКЛ");
            put(1, "ТЭН");
            put(2, "10%");
            put(3, "20%");
            put(4, "30%");
            put(5, "40%");
            put(6, "50%");
            put(7, "60%");
            put(8, "70%");
            put(9, "80%");
            put(10, "90%");
            put(11, "100%");
        }
    };
    static Map<Integer, String> gvsModeExtraMap = new HashMap<Integer, String>() { // from class: ru.temzit.wificontrol.SimpleClient.3
        {
            put(0, "основн");
            put(1, "принуд");
        }
    };
    static int sec_lo = 0;
    static int sec_hi = 0;
    static int min_lo = 0;
    static int min_hi = 0;
    static int hour_lo = 0;
    static int hour_hi = 0;
    static String HostTime = "00:00";
    static float Tout = 0.0f;
    static float Tin = 0.0f;
    static int Tcond = 0;
    static int Tevap = 0;
    static int Tcond2 = 0;
    static int Tevap2 = 0;

    /* renamed from: Tf */
    static int f50Tf = 0;

    /* renamed from: Tb */
    static int f49Tb = 0;
    static float Tf_float = 0.0f;
    static float Tb_float = 0.0f;
    static int Tf2 = 0;
    static int Tb2 = 0;
    static float Tf_float2 = 0.0f;
    static float Tb_float2 = 0.0f;
    static int Tgvs = 0;
    static int Tgvs2 = 0;
    static int Dualmode = 0;
    static int TAmode = 0;
    static int TA_term = 0;
    static int CompFreq = 0;
    static int CompFreq2 = 0;
    static int PowerTen = 0;
    static int state = 0;
    static int Schedule = 0;
    static float Pout = 0.0f;
    static float Pout2 = 0.0f;
    static float Pin = 0.0f;

    /* renamed from: P */
    static int f48P = 0;
    static float Flow = 0.0f;
    static float Flow2 = 0.0f;
    static long Failures = 0;
    static int cfgState = 0;
    static int ModeSet = 0;
    static int TroomSet = 0;
    static int TgvsSet = 0;
    static int TwaterSet = 0;
    static int GVSModeSet = 0;
    static int Begin = 0;
    static int End = 24;
    static String config = "01101E00F6E7052300000A0500010613590F030000000F0000055A00";
    static String RTCconfig = "00000010101400000000000000101014000000000000001010140000000000000010101400000000";
    static int ControllerRevision = -1;
    static String msgToShow = "";
    static boolean isMsgShown = false;

    enum eClientStates {
        NONE,
        SLEEP,
        PREPARE,
        SEND
    }

    private static int hexToBin(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        char c2 = 'A';
        if ('A' > c || c > 'F') {
            c2 = 'a';
            if ('a' > c || c > 'f') {
                return 0;
            }
        }
        return (c - c2) + 10;
    }

    public static float int16ToFloat(int i) {
        if (i >= 32768) {
            i -= 65536;
        }
        return i / 10.0f;
    }

    public static int int16ToSignedInt(int i) {
        return i < 32768 ? i : i - 65536;
    }

    static class eMessageType {
        public static final int CONNECTSTATE = 4;
        public static final int DATA = 1;
        public static final int ERROR = 0;
        public static final int RTCSETTINGS = 3;
        public static final int SETTINGS = 2;
        public static final int WIFISETTINGS = 5;

        eMessageType() {
        }
    }

    static class RecvPacketType {
        public static final byte DATA0 = 0;
        public static final byte DATA1 = 1;
        public static final byte RTCSETTINGS = 3;
        public static final byte SETTINGS = 2;
        public static final byte WIFISETTINGS = 5;

        RecvPacketType() {
        }
    }

    static class SendPacketType {
        public static final byte CHANGE_ONE_RTCSETTING = 51;
        public static final byte CHANGE_ONE_SETTING = 50;
        public static final byte GET_RTCSETTINGS = 54;
        public static final byte GET_SETTINGS = 52;
        public static final byte GET_STATE = 48;
        public static final byte GET_WIFISETTINGS = 57;
        public static final byte NONE = 0;
        public static final byte SEND_RTCSETTINGS = 55;
        public static final byte SEND_SETTINGS = 53;
        public static final byte SEND_WIFISETTINGS = 58;

        SendPacketType() {
        }
    }

    public static SimpleClient createMyClient(Handler handler) {
        f51h = handler;
        return new SimpleClient();
    }

    public static String toHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder(bArr.length * 2);
        Formatter formatter = new Formatter(sb);
        for (byte b : bArr) {
            formatter.format("%02x", Byte.valueOf(b));
        }
        return sb.toString();
    }

    public static String hashMac(String str, String str2) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(str2.getBytes(), HASH_ALGORITHM);
            Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
            mac.init(secretKeySpec);
            return toHexString(mac.doFinal(str.getBytes()));
        } catch (InvalidKeyException unused) {
            throw new SignatureException("error building signature, invalid key HmacSHA256");
        } catch (NoSuchAlgorithmException unused2) {
            throw new SignatureException("error building signature, no such algorithm in device HmacSHA256");
        }
    }

    protected int GetParamValue(int i) {
        int i2 = i * 2;
        char cCharAt = config.charAt(i2);
        int iHexToBin = hexToBin(config.charAt(i2 + 1)) | (hexToBin(cCharAt) << 4);
        return iHexToBin > 127 ? iHexToBin + InputDeviceCompat.SOURCE_ANY : iHexToBin;
    }

    public static void CalcCRC(byte[] bArr, int i) {
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            i2 += bArr[i3];
        }
        bArr[i] = (byte) (i2 & 255);
    }

    public int getWord(byte[] bArr, int i) {
        return (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8);
    }

    public void ParseParam(byte[] bArr) {
        sec_lo = bArr[61] & 15;
        sec_hi = (bArr[61] >> 4) & 15;
        min_lo = bArr[60] & 15;
        min_hi = (bArr[60] >> 4) & 15;
        hour_lo = bArr[59] & 15;
        hour_hi = (bArr[59] >> 4) & 15;
        if (bArr[0] == 1) {
            ReceivedPage = bArr[47] & 255;
            ModeSet = bArr[48] & 255;
            Begin = bArr[49] & 255;
            End = bArr[50] & 255;
            TroomSet = bArr[51] & 255;
            TwaterSet = bArr[52] & 255;
            TgvsSet = bArr[53] & 255;
            GVSModeSet = bArr[56] & 255;
            ControllerRevision = ((bArr[45] & 255) * 100) + (bArr[46] & 255);
            System.out.println("ControllerRevision: " + ControllerRevision);
        }
        if (ControllerRevision > 264 && (bArr[44] & 1) == 0) {
            Dualmode = 1;
        }
        HostTime = "" + hour_hi + hour_lo + ":" + min_hi + min_lo;
        state = getWord(bArr, 2);
        Schedule = getWord(bArr, 4);
        Tout = int16ToFloat(getWord(bArr, 6));
        Tin = ((float) getWord(bArr, 8)) / 10.0f;
        Tf_float = ((float) getWord(bArr, 10)) / 10.0f;
        Tf_float2 = ((float) getWord(bArr, 34)) / 10.0f;
        Tb_float = ((float) getWord(bArr, 12)) / 10.0f;
        Tb_float2 = getWord(bArr, 36) / 10.0f;
        float f = Tf_float;
        f50Tf = (int) f;
        Tf2 = (int) f;
        float f2 = Tb_float;
        f49Tb = (int) f2;
        Tb2 = (int) f2;
        Tcond = (int) int16ToFloat(getWord(bArr, 14));
        Tcond2 = (int) int16ToFloat(getWord(bArr, 38));
        Tevap = (int) int16ToFloat(getWord(bArr, 16));
        Tevap2 = (int) int16ToFloat(getWord(bArr, 40));
        Tgvs = getWord(bArr, 18) / 10;
        Flow = getWord(bArr, 20) & 255;
        Flow2 = (getWord(bArr, 20) >> 8) & 255;
        CompFreq = getWord(bArr, 24) & 255;
        CompFreq2 = (getWord(bArr, 24) >> 8) & 255;
        Failures = getWord(bArr, 32);
        Pin = getWord(bArr, 30) / 10.0f;
        Pout = ((int) ((((Flow * 1.25f) * 60.0f) * (Tf_float - Tb_float)) / 100.0f)) / 10.0f;
        Pout2 = ((int) ((((Flow2 * 1.25f) * 60.0f) * (Tf_float2 - Tb_float2)) / 100.0f)) / 10.0f;
    }

    protected boolean get_weather_city_id() throws IOException {
        String strSubstring;
        int iIndexOf;
        boolean z = false;
        try {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://rp5.ru/Погода_в_" + CityStr).openConnection();
            httpsURLConnection.connect();
            StringBuilder sb = new StringBuilder();
            if (200 == httpsURLConnection.getResponseCode()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\n");
                }
            }
            String string = sb.toString();
            int iIndexOf2 = string.indexOf("'/docs/xml/ru?id=");
            if (iIndexOf2 > 0 && (iIndexOf = (strSubstring = string.substring(iIndexOf2 + 17)).indexOf("'")) > 0 && iIndexOf < 10) {
                CityId = Integer.parseInt(strSubstring.substring(0, iIndexOf));
                CityIdChanged = true;
                z = true;
            }
            System.out.println("City id is: " + CityId + "\n");
        } catch (Exception e) {
            System.out.println(e);
        }
        return z;
    }

    protected boolean get_weather_forecast() throws IOException {
        int iIndexOf;
        boolean z = false;
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://rp5.ru/htmla.php?id=" + CityId + "&sc=1").openConnection();
            httpURLConnection.connect();
            StringBuilder sb = new StringBuilder();
            if (200 == httpURLConnection.getResponseCode()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\n");
                }
            }
            String string = sb.toString();
            int iIndexOf2 = string.indexOf("<td width=* style=\"padding: 4px;\">");
            if (iIndexOf2 > 0 && (iIndexOf = (string = string.substring(iIndexOf2 + 34)).indexOf("<")) > 0 && iIndexOf < 10) {
                string = string.substring(0, iIndexOf);
                CityTemp = string;
                z = true;
            }
            System.out.println("CityTemp: " + string + "\n");
        } catch (Exception e) {
            System.out.println(e);
        }
        return z;
    }

    protected void sendErrorMessage(Exception exc) {
        Message message = new Message();
        message.arg1 = 0;
        message.arg2 = 2;
        f51h.sendMessage(message);
        System.out.println("SimpleClient: init error: " + exc);
    }

    public byte[] xor_crypt(byte[] bArr, int i) {
        byte[] bArr2 = new byte[i];
        for (int i2 = 0; i2 < i; i2++) {
            bArr2[i2] = (byte) (bArr[i2] ^ (-91));
        }
        return bArr2;
    }

    protected boolean sendPacket(Socket socket) throws IOException {
        byte[] bArr = new byte[512];
        try {
            if (sendCfg == 53) {
                bArr[0] = SendPacketType.SEND_SETTINGS;
                bArr[1] = 26;
                for (int i = 0; i < 26; i++) {
                    int i2 = i * 2;
                    bArr[i + 2] = (byte) ((hexToBin(config.charAt(i2)) << 4) | hexToBin(config.charAt(i2 + 1)));
                }
                bArr[31] = 0;
                CalcCRC(bArr, 31);
                socket.getOutputStream().write(bArr, 0, 32);
            } else if (sendCfg == 58) {
                System.out.println("SimpleClient: Send SEND_WIFISETTINGS");
                bArr[0] = SendPacketType.SEND_WIFISETTINGS;
                bArr[1] = 60;
                byte[] bytes = WifiLogin.getBytes(Charset.forName("UTF-8"));
                byte[] bytes2 = WifiPswd.getBytes(Charset.forName("UTF-8"));
                byte[] bArrXor_crypt = xor_crypt(bytes, bytes.length);
                byte[] bArrXor_crypt2 = xor_crypt(bytes2, bytes2.length);
                System.arraycopy(bArrXor_crypt, 0, bArr, 2, bArrXor_crypt.length);
                System.arraycopy(bArrXor_crypt2, 0, bArr, 30, bArrXor_crypt2.length);
                bArr[58] = (byte) (WifiMode & 255);
                bArr[59] = (byte) (WifiIndex & 255);
                bArr[60] = 0;
                bArr[61] = 0;
                bArr[62] = 0;
                bArr[63] = 0;
                CalcCRC(bArr, 63);
                socket.getOutputStream().write(bArr, 0, 64);
            } else if (sendCfg == 50) {
                bArr[0] = SendPacketType.CHANGE_ONE_SETTING;
                bArr[1] = 0;
                bArr[2] = 0;
                bArr[3] = (byte) (sendParamNum & 255);
                bArr[4] = 0;
                bArr[5] = (byte) (sendParamValue & 255);
                bArr[6] = 0;
                bArr[7] = 0;
                CalcCRC(bArr, 7);
                socket.getOutputStream().write(bArr, 0, 8);
            } else if (sendCfg == 51) {
                bArr[0] = SendPacketType.CHANGE_ONE_RTCSETTING;
                bArr[1] = 0;
                bArr[2] = 0;
                bArr[3] = (byte) (sendParamNum & 255);
                bArr[4] = 0;
                bArr[5] = (byte) (sendParamValue & 255);
                bArr[6] = 0;
                bArr[7] = 0;
                CalcCRC(bArr, 7);
                socket.getOutputStream().write(bArr, 0, 8);
            } else if (sendCfg == 52) {
                System.out.println("SimpleClient: Send GET_SETTINGS request");
                bArr[0] = SendPacketType.GET_SETTINGS;
                socket.getOutputStream().write(bArr, 0, 1);
            } else if (sendCfg == 54) {
                System.out.println("SimpleClient: Send GET_RTCSETTINGS request");
                bArr[0] = SendPacketType.GET_RTCSETTINGS;
                socket.getOutputStream().write(bArr, 0, 1);
            } else if (sendCfg == 57) {
                System.out.println("SimpleClient: Send GET_WIFISETTINGS request");
                bArr[0] = SendPacketType.GET_WIFISETTINGS;
                socket.getOutputStream().write(bArr, 0, 1);
            } else if (sendCfg == 55) {
                bArr[0] = SendPacketType.SEND_RTCSETTINGS;
                bArr[1] = 40;
                for (int i3 = 0; i3 < 40; i3++) {
                    int i4 = i3 * 2;
                    bArr[i3 + 2] = (byte) ((hexToBin(RTCconfig.charAt(i4)) << 4) | hexToBin(RTCconfig.charAt(i4 + 1)));
                }
                bArr[41] = 0;
                CalcCRC(bArr, 41);
                socket.getOutputStream().write(bArr, 0, 42);
            } else {
                bArr[0] = SendPacketType.GET_STATE;
                bArr[1] = 0;
                socket.getOutputStream().write(bArr, 0, 2);
            }
            return true;
        } catch (Exception e) {
            sendErrorMessage(e);
            return false;
        }
    }

    protected boolean waitData(Socket socket) {
        int i = 0;
        while (socket.getInputStream().available() < 64) {
            try {
                sleep(10L);
                if (i >= 200) {
                    return false;
                }
                i++;
            } catch (Exception e) {
                sendErrorMessage(e);
                return true;
            }
        }
        return true;
    }

    public boolean getData(Socket socket, byte[] bArr) {
        try {
            if (socket.getInputStream().read(bArr) != 64) {
                return true;
            }
            int i = 0;
            for (int i2 = 0; i2 < 62; i2++) {
                i += bArr[i2] & 255;
                System.out.print(this.dec2hex[(bArr[i2] >> 4) & 15] + this.dec2hex[bArr[i2] & 15]);
            }
            System.out.print(this.dec2hex[(bArr[62] >> 4) & 15] + this.dec2hex[bArr[62] & 15]);
            System.out.print(this.dec2hex[(bArr[63] >> 4) & 15] + this.dec2hex[bArr[63] & 15]);
            System.out.println("");
            System.out.println("SimpleClient: ");
            int word = getWord(bArr, 62);
            if (i == word) {
                return false;
            }
            System.out.println("SimpleClient:  " + Integer.toHexString(i) + " не равно " + Integer.toHexString(word) + "(" + ((int) bArr[62]) + ")(" + ((int) bArr[63]) + ")");
            return true;
        } catch (Exception e) {
            sendErrorMessage(e);
            return true;
        }
    }

    public String getUTF8StringFromBuf(byte[] bArr, int i, int i2) {
        ArrayList arrayList = new ArrayList();
        for (int i3 = i; i3 < i + i2 && bArr[i3] != 0; i3++) {
            arrayList.add(Byte.valueOf(bArr[i3]));
        }
        Byte[] bArr2 = (Byte[]) arrayList.toArray(new Byte[arrayList.size()]);
        byte[] bArr3 = new byte[arrayList.size()];
        int length = bArr2.length;
        int i4 = 0;
        int i5 = 0;
        while (i4 < length) {
            bArr3[i5] = bArr2[i4].byteValue();
            i4++;
            i5++;
        }
        return new String(bArr3, Charset.forName("UTF-8"));
    }

    public String getUTF8StringFromBufCrypt(byte[] bArr, int i, int i2) {
        ArrayList arrayList = new ArrayList();
        for (int i3 = i; i3 < i + i2 && bArr[i3] != 0; i3++) {
            arrayList.add(Byte.valueOf(bArr[i3]));
        }
        Byte[] bArr2 = (Byte[]) arrayList.toArray(new Byte[arrayList.size()]);
        byte[] bArr3 = new byte[arrayList.size()];
        int length = bArr2.length;
        int i4 = 0;
        int i5 = 0;
        while (i4 < length) {
            bArr3[i5] = bArr2[i4].byteValue();
            i4++;
            i5++;
        }
        return new String(xor_crypt(bArr3, bArr3.length), Charset.forName("UTF-8"));
    }

    public boolean unpackData(byte[] bArr) {
        Message message = new Message();
        byte b = bArr[0];
        if (b == 0 || b == 1) {
            System.out.println("SimpleClient: Recv DATA msg " + ((int) bArr[0]));
            ParseParam(bArr);
            sendCfg = 0;
            message.arg1 = 1;
        } else {
            String str = "";
            if (b == 2) {
                System.out.println("SimpleClient: Recv SETTINGS msg");
                for (int i = 2; i < 32; i++) {
                    str = str + this.dec2hex[(bArr[i] >> 4) & 15] + this.dec2hex[bArr[i] & 15];
                }
                if (needCheckSettings) {
                    if (str.compareTo(config) == 0) {
                        return false;
                    }
                    System.out.println("SimpleClient: Recv error SETTINGS!!!");
                    return true;
                }
                config = str;
                sendCfg = 0;
                message.arg1 = 2;
            } else if (b == 3) {
                System.out.println("SimpleClient: Recv RTCSETTINGS msg");
                for (int i2 = 2; i2 < 42; i2++) {
                    str = str + this.dec2hex[(bArr[i2] >> 4) & 15] + this.dec2hex[bArr[i2] & 15];
                }
                if (needCheckSettings) {
                    if (str.compareTo(RTCconfig) == 0) {
                        return false;
                    }
                    System.out.println("SimpleClient: Recv error RTCSETTINGS!!!");
                    return true;
                }
                RTCconfig = str;
                sendCfg = 0;
                message.arg1 = 3;
            } else if (b == 5) {
                System.out.println("SimpleClient: Recv WIFISETTINGS msg");
                String uTF8StringFromBufCrypt = getUTF8StringFromBufCrypt(bArr, 2, 28);
                String uTF8StringFromBufCrypt2 = getUTF8StringFromBufCrypt(bArr, 30, 28);
                byte b2 = bArr[58];
                byte b3 = bArr[59];
                System.out.println("SimpleClient: Recv Wifi login: " + uTF8StringFromBufCrypt);
                System.out.println("SimpleClient: Recv Wifi pass: " + uTF8StringFromBufCrypt2);
                System.out.println("SimpleClient: Recv Wifi mode: " + ((int) b2));
                System.out.println("SimpleClient: Recv Wifi index: " + ((int) b3));
                if (needCheckSettings) {
                    if (!WifiLogin.equals(uTF8StringFromBufCrypt) || !WifiPswd.equals(uTF8StringFromBufCrypt2) || WifiMode != b2 || WifiIndex != b3) {
                        System.out.println("SimpleClient: Recv error WIFISETTINGS!!!");
                        return true;
                    }
                    System.out.println("SimpleClient: Recv correct WIFISETTINGS!!!");
                    needCheckSettings = false;
                    sendCfg = 0;
                    return false;
                }
                WifiLogin = uTF8StringFromBufCrypt;
                WifiPswd = uTF8StringFromBufCrypt2;
                WifiMode = b2;
                WifiIndex = b3;
                sendCfg = 0;
                message.arg1 = 5;
            }
        }
        System.out.println("SimpleClient: sendMessage handle state_msg");
        f51h.sendMessage(message);
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:108:0x04c0  */
    /* JADX WARN: Removed duplicated region for block: B:120:0x04e3  */
    /* JADX WARN: Removed duplicated region for block: B:130:0x050e A[Catch: InterruptedException -> 0x0535, TryCatch #0 {InterruptedException -> 0x0535, blocks: (B:127:0x04f8, B:128:0x0506, B:130:0x050e, B:132:0x0523, B:133:0x052f), top: B:140:0x04f8 }] */
    /* JADX WARN: Removed duplicated region for block: B:150:0x04ee A[EXC_TOP_SPLITTER, SYNTHETIC] */
    @Override // java.lang.Thread, java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void run() throws java.lang.Exception {
        /*
            Method dump skipped, instructions count: 1348
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: ru.temzit.wificontrol.SimpleClient.run():void");
    }

    public static String getJsonFromServer(String str) throws IOException {
        URLConnection uRLConnectionOpenConnection = new URL(str).openConnection();
        uRLConnectionOpenConnection.setConnectTimeout(2000);
        uRLConnectionOpenConnection.setReadTimeout(2000);
        return new BufferedReader(new InputStreamReader(uRLConnectionOpenConnection.getInputStream())).readLine();
    }
}
