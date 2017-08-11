package cn.com.truly.ic.trulyemp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.icu.util.DateInterval;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import cn.com.truly.ic.trulyemp.R.drawable;

public class MyUtils {

    /**
     * 可以将所有Bean List转换成对应的Map List 主要用于填充SimpleAdapter
     *
     * @param list 需要转换的实体对象List
     * @return 已经转换好的Map映射List
     */
    public static List<HashMap<String, Object>> ConvertToMapList(List<?> list, String[] segments) {

        if (list.isEmpty())
            return null;
        Class<?> T = list.get(0).getClass();
        Field[] fields = T.getDeclaredFields();
        List<HashMap<String, Object>> result = new ArrayList<>();

        for (Object obj : list) {
            HashMap<String, Object> map = new HashMap<>();
            for (Field f : fields) {

                // 如果字段名不为空而且字段名不包含当前field，则跳过继续
                if (segments != null && !Arrays.asList(segments).contains(f.getName())) {
                    continue;
                }

                Object fieldValue;
                try {
                    Method method = T.getDeclaredMethod("get" + f.getName().substring(0, 1).toUpperCase(Locale.getDefault()) + f.getName().substring(1));
                    try {
                        fieldValue = method.invoke(obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } catch (NoSuchMethodException e) {
                    // 如果没有这个field对应的getter方法，则跳过然后继续
                    e.printStackTrace();
                    continue;
                }
                map.put(f.getName(), fieldValue);
            }
            result.add(map);
        }
        return result;
    }

    /**
     * 与一般的标准MD5不同，自己再加了一点变化
     *
     * @param input 需要md5加密的文本
     * @return md5加密的结果
     */
    public static String stringToMyMD5(String input) {

        if (input.length() > 2) {
            input = "Who" + input.substring(2) + "Are" + input.substring(0, 2) + "You";
        } else {
            input = "Who" + input + "Are" + input + "You";
        }

        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(input.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    /**
     * @return yyyy-MM-dd HH:mm:ss 的当前时间
     */
    public static String getDateTime(Date dt) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
        return df.format(dt);
    }

    public static String getDateStr(Date dt){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
        return df.format(dt);
    }

    public static Date addDays(Date dt,int days){
        Calendar calendar=new GregorianCalendar();
        calendar.setTime(dt);
        calendar.add(Calendar.DATE,days);
        return calendar.getTime();
    }

    /**
     * 检查手机是否有网络连接
     *
     * @param _context 当前Activity上下文
     * @return true or false
     */
    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivity.getActiveNetworkInfo()!=null){
            if(connectivity.getActiveNetworkInfo().isAvailable()){
                return true;
            }
        }
        return false;
    }

    /**
     * 弹出一个确定按钮的提示框
     *
     * @param context 当前上下文
     * @param title   标题栏
     * @param message 提示信息
     * @param status  true表示成功提示，false表示错误提示
     */
    public static void showAlertDialog(Context context, String title, String message,
                                       Boolean status, DialogInterface.OnClickListener okListener,
                                       DialogInterface.OnClickListener cancelListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message

        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon(status == null ? drawable.ic_warn : (status == true ? drawable.ic_check : drawable.ic_no));

        // Setting OK Button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(android.R.string.ok), okListener);

        if (cancelListener != null) {
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), cancelListener);
        }

        // Showing Alert Message
        alertDialog.show();
    }

    /**
     * 嵌套在ScrollView中的话需要手动根据内容设置ListView的高度
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() + 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    /**
     * 加载本地图片
     *
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从服务器取图片
     *
     * @param url
     * @return
     */
    public static Bitmap getHttpBitmap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return bitmap;
    }

    /**
     * 网络是否已连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * 设置fontawsome字体
     *
     * @param context
     * @param views
     */
    public static void setFont(Context context, List<TextView> views) {
        Typeface font = Typeface.createFromAsset(context.getAssets(),
                "fontawesome-webfont.ttf");
        for (TextView v : views) {
            v.setTypeface(font);
        }
    }

    /**
     * 构造ArrayList
     *
     * @param elements
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> createArrayList(T... elements) {
        ArrayList<T> list = new ArrayList<T>();
        for (T element : elements) {
            list.add(element);
        }
        return list;
    }

    public static class AES {


        //AES 128位key
        private static String AES_128_key = "1987121919890610";

        public static String encrypt(String cleartext) throws Exception {
            //byte[] rawKey = getRawKey(AES_128_key.getBytes());
            byte[] rawKey = AES_128_key.getBytes("utf-8");
            byte[] result = encrypt(rawKey, cleartext.getBytes());
            return Base64.encodeToString(result, 0);
            //return toHex(result);
        }

        public static String decrypt(String encrypted) throws Exception {
            //byte[] rawKey = getRawKey(AES_128_key.getBytes());
            byte[] rawKey = AES_128_key.getBytes("utf-8");
            //byte[] enc = toByte(encrypted);
            byte[] enc = Base64.decode(encrypted, 0);
            byte[] result = decrypt(rawKey, enc);
            return new String(result);
        }

/*		private static byte[] getRawKey(byte[] seed) throws Exception {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(seed);
			kgen.init(128, sr); // 192 and 256 bits may not be available
			SecretKey skey = kgen.generateKey();
			byte[] raw = skey.getEncoded();
			return raw;
		}*/

        private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(clear);
            return encrypted;
        }

        private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return decrypted;
        }

/*		public static String toHex(String txt) {
            return toHex(txt.getBytes());
		}

		public static String fromHex(String hex) {
			return new String(toByte(hex));
		}

		public static byte[] toByte(String hexString) {
			int len = hexString.length() / 2;
			byte[] result = new byte[len];
			for (int i = 0; i < len; i++)
				result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
			return result;
		}

		public static String toHex(byte[] buf) {
			if (buf == null)
				return "";
			StringBuffer result = new StringBuffer(2 * buf.length);
			for (int i = 0; i < buf.length; i++) {
				appendHex(result, buf[i]);
			}
			return result.toString();
		}

		private final static String HEX = "0123456789ABCDEF";

		private static void appendHex(StringBuffer sb, byte b) {
			sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
		}*/
    }


}
