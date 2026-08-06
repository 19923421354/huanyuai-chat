package com.huanyuai.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* loaded from: classes.dex */
public class MainActivity extends Activity {
    private static final String ASSET_HTML = "file:///android_asset/user_client.html";
    private static final String BACKUP_FILE = "localStorage_backup.dat";
    private static final String ENC_KEY = "HuanyuAI2024SecKey!@#$%^&*()9876";
    private static final int REQUEST_FILE_CHOOSER = 1001;
    private static final int REQUEST_PICK_FILE = 1002;
    private static final int REQUEST_STORAGE_PERM = 1003;
    private static final String SHARED_DIR = "HuanyuAI";
    private static final String SHARED_FILE = "shared_data.dat";
    private static final String VERSION = "3.13.0";
    private ValueCallback<Uri[]> filePathCallback;
    private String pendingJsCallback;
    private WebView webView;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        WebView webView = new WebView(this);
        this.webView = webView;
        setContentView(webView);
        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setCacheMode(-1);
        settings.setGeolocationEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(0);
        }
        this.webView.addJavascriptInterface(new AppBridge(this), "App");
        this.webView.setWebViewClient(new WebViewClient());
        this.webView.setWebChromeClient(new WebChromeClient() { // from class: com.huanyuai.chat.MainActivity.1
            @Override // android.webkit.WebChromeClient
            public boolean onShowFileChooser(WebView webView2, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = valueCallback;
                Intent intentCreateIntent = fileChooserParams.createIntent();
                intentCreateIntent.addCategory("android.intent.category.OPENABLE");
                try {
                    MainActivity.this.startActivityForResult(Intent.createChooser(intentCreateIntent, "选择文件"), MainActivity.REQUEST_FILE_CHOOSER);
                    return true;
                } catch (Exception unused) {
                    MainActivity.this.filePathCallback = null;
                    return false;
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onPermissionRequest(PermissionRequest permissionRequest) {
                permissionRequest.grant(permissionRequest.getResources());
            }

            @Override // android.webkit.WebChromeClient
            public boolean onJsConfirm(WebView webView2, String str, String str2, final JsResult jsResult) {
                new AlertDialog.Builder(MainActivity.this).setMessage(str2).setPositiveButton("确定", new DialogInterface.OnClickListener() { // from class: com.huanyuai.chat.MainActivity.1.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        jsResult.confirm();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() { // from class: com.huanyuai.chat.MainActivity.1.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        jsResult.cancel();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.huanyuai.chat.MainActivity.1.1
                    @Override // android.content.DialogInterface.OnCancelListener
                    public void onCancel(DialogInterface dialogInterface) {
                        jsResult.cancel();
                    }
                }).show();
                return true;
            }
        });
        requestStoragePermission();
        this.webView.loadUrl(ASSET_HTML);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < 23 || checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, REQUEST_STORAGE_PERM);
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == REQUEST_STORAGE_PERM) {
            boolean z = false;
            if (iArr.length > 0 && iArr[0] == 0) {
                z = true;
            }
            WebView webView = this.webView;
            if (webView != null) {
                webView.evaluateJavascript("javascript:try{if(typeof onStorageGranted==='function'){if(" + z + ")onStorageGranted();}}catch(e){}", null);
            }
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == REQUEST_FILE_CHOOSER && this.filePathCallback != null) {
            this.filePathCallback.onReceiveValue((i2 != -1 || intent == null || intent.getDataString() == null) ? null : new Uri[]{Uri.parse(intent.getDataString())});
            this.filePathCallback = null;
            return;
        }
        if (i != REQUEST_PICK_FILE || this.pendingJsCallback == null) {
            return;
        }
        String path = (i2 != -1 || intent == null || intent.getData() == null) ? "" : intent.getData().getPath();
        this.webView.evaluateJavascript("javascript:try{if(window['" + this.pendingJsCallback + "'])window['" + this.pendingJsCallback + "']('" + path.replace("'", "\\'") + "')}catch(e){}", null);
        this.pendingJsCallback = null;
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        WebView webView = this.webView;
        if (webView == null || !webView.canGoBack()) {
            super.onBackPressed();
        } else {
            this.webView.goBack();
        }
    }

    public class AppBridge {
        private final Context ctx;

        @JavascriptInterface
        public String getVersion() {
            return MainActivity.VERSION;
        }

        @JavascriptInterface
        public void setOnHomeView(boolean z) {
        }

        AppBridge(Context context) {
            this.ctx = context;
        }

        @JavascriptInterface
        public String getModelDir() {
            File file = new File(this.ctx.getFilesDir(), "models");
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        }

        @JavascriptInterface
        public String getDataDir() {
            return this.ctx.getFilesDir().getAbsolutePath();
        }

        @JavascriptInterface
        public String getCacheDir() {
            return this.ctx.getCacheDir().getAbsolutePath();
        }

        @JavascriptInterface
        public String getAndroidVersion() {
            return Build.VERSION.RELEASE;
        }

        @JavascriptInterface
        public String getDeviceInfo() {
            return "Android " + Build.VERSION.RELEASE + " / " + Build.MANUFACTURER + " " + Build.MODEL;
        }

        @JavascriptInterface
        public void showToast(String str) {
            Toast.makeText(this.ctx, str, 0).show();
        }

        @JavascriptInterface
        public void copyToClipboard(String str) {
            try {
                ((ClipboardManager) this.ctx.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("text", str));
            } catch (Exception unused) {
            }
        }

        @JavascriptInterface
        public void pickFile(String str) {
            MainActivity.this.pendingJsCallback = str;
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("*/*");
            intent.addCategory("android.intent.category.OPENABLE");
            try {
                ((Activity) this.ctx).startActivityForResult(Intent.createChooser(intent, "选择文件"), MainActivity.REQUEST_PICK_FILE);
            } catch (Exception unused) {
                MainActivity.this.webView.evaluateJavascript("javascript:try{if(window['" + str + "'])window['" + str + "']('')}catch(e){}", null);
            }
        }

        @JavascriptInterface
        public String saveFile(String str, String str2) throws IOException {
            try {
                File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!externalStoragePublicDirectory.exists()) {
                    externalStoragePublicDirectory.mkdirs();
                }
                File file = new File(externalStoragePublicDirectory, str);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(str2.getBytes("UTF-8"));
                fileOutputStream.close();
                return file.getAbsolutePath();
            } catch (Exception unused) {
                return "";
            }
        }

        @JavascriptInterface
        public String readFile(String str) throws IOException {
            try {
                File file = new File(str);
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bArr = new byte[(int) file.length()];
                fileInputStream.read(bArr);
                fileInputStream.close();
                return new String(bArr, "UTF-8");
            } catch (Exception unused) {
                return "";
            }
        }

        @JavascriptInterface
        public void shareFile(String str) {
            try {
                File file = new File(str);
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("*/*");
                intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(file));
                this.ctx.startActivity(Intent.createChooser(intent, "分享文件"));
            } catch (Exception unused) {
                Toast.makeText(this.ctx, "分享失败", 0).show();
            }
        }

        @JavascriptInterface
        public void openBrowser(String str) {
            try {
                this.ctx.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
            } catch (Exception unused) {
                Toast.makeText(this.ctx, "无法打开链接", 0).show();
            }
        }

        @JavascriptInterface
        public void downloadModel(final String str, final String str2) {
            try {
                new Thread(new Runnable() { // from class: com.huanyuai.chat.MainActivity.AppBridge.1
                    @Override // java.lang.Runnable
                    public void run() throws IOException {
                        try {
                            File file = new File(AppBridge.this.ctx.getExternalFilesDir(null), "models");
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            final File file2 = new File(file, str2);
                            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
                            httpURLConnection.setConnectTimeout(30000);
                            httpURLConnection.setReadTimeout(30000);
                            InputStream inputStream = httpURLConnection.getInputStream();
                            FileOutputStream fileOutputStream = new FileOutputStream(file2);
                            byte[] bArr = new byte[8192];
                            while (true) {
                                int i = inputStream.read(bArr);
                                if (i <= 0) {
                                    fileOutputStream.close();
                                    inputStream.close();
                                    httpURLConnection.disconnect();
                                    ((Activity) AppBridge.this.ctx).runOnUiThread(new Runnable() { // from class: com.huanyuai.chat.MainActivity.AppBridge.1.1
                                        @Override // java.lang.Runnable
                                        public void run() {
                                            Toast.makeText(AppBridge.this.ctx, "模型下载完成: " + file2.getAbsolutePath(), 1).show();
                                        }
                                    });
                                    return;
                                }
                                fileOutputStream.write(bArr, 0, i);
                            }
                        } catch (Exception e) {
                            ((Activity) AppBridge.this.ctx).runOnUiThread(new Runnable() { // from class: com.huanyuai.chat.MainActivity.AppBridge.1.2
                                @Override // java.lang.Runnable
                                public void run() {
                                    Toast.makeText(AppBridge.this.ctx, "下载失败: " + e.getMessage(), 1).show();
                                }
                            });
                        }
                    }
                }).start();
            } catch (Exception unused) {
                Toast.makeText(this.ctx, "下载启动失败", 0).show();
            }
        }

        @JavascriptInterface
        public String readSharedData() throws IOException {
            try {
                File file = new File(new File(Environment.getExternalStorageDirectory(), MainActivity.SHARED_DIR), MainActivity.SHARED_FILE);
                if (!file.exists()) {
                    return "{}";
                }
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bArr = new byte[(int) file.length()];
                fileInputStream.read(bArr);
                fileInputStream.close();
                byte[] bArrDecryptData = MainActivity.this.decryptData(bArr);
                if (bArrDecryptData != null) {
                    return new String(bArrDecryptData, "UTF-8");
                }
                return new String(bArr, "UTF-8");
            } catch (Exception unused) {
                return "{}";
            }
        }

        @JavascriptInterface
        public boolean writeSharedData(String str) throws IOException {
            try {
                File file = new File(Environment.getExternalStorageDirectory(), MainActivity.SHARED_DIR);
                if (!file.exists()) {
                    file.mkdirs();
                }
                File file2 = new File(file, MainActivity.SHARED_FILE);
                byte[] bArrEncryptData = MainActivity.this.encryptData(str);
                if (bArrEncryptData != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file2);
                    fileOutputStream.write(bArrEncryptData);
                    fileOutputStream.close();
                    return true;
                }
                FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                fileOutputStream2.write(str.getBytes("UTF-8"));
                fileOutputStream2.close();
                return true;
            } catch (Exception unused) {
                return false;
            }
        }

        @JavascriptInterface
        public boolean testSharedData() {
            try {
                File file = new File(Environment.getExternalStorageDirectory(), MainActivity.SHARED_DIR);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return false;
                    }
                }
                return true;
            } catch (Exception unused) {
                return false;
            }
        }

        @JavascriptInterface
        public boolean backupLocalStorage(String str) throws IOException {
            try {
                File file = new File(Environment.getExternalStorageDirectory(), MainActivity.SHARED_DIR);
                if (!file.exists()) {
                    file.mkdirs();
                }
                File file2 = new File(file, MainActivity.BACKUP_FILE);
                byte[] bArrEncryptData = MainActivity.this.encryptData(str);
                if (bArrEncryptData != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file2);
                    fileOutputStream.write(bArrEncryptData);
                    fileOutputStream.close();
                    return true;
                }
                FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                fileOutputStream2.write(str.getBytes("UTF-8"));
                fileOutputStream2.close();
                return true;
            } catch (Exception unused) {
                return false;
            }
        }

        @JavascriptInterface
        public String restoreLocalStorage() throws IOException {
            try {
                File file = new File(new File(Environment.getExternalStorageDirectory(), MainActivity.SHARED_DIR), MainActivity.BACKUP_FILE);
                if (!file.exists()) {
                    return "";
                }
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bArr = new byte[(int) file.length()];
                fileInputStream.read(bArr);
                fileInputStream.close();
                byte[] bArrDecryptData = MainActivity.this.decryptData(bArr);
                if (bArrDecryptData != null) {
                    return new String(bArrDecryptData, "UTF-8");
                }
                return new String(bArr, "UTF-8");
            } catch (Exception unused) {
                return "";
            }
        }

        @JavascriptInterface
        public String getExternalFilesDir() {
            return this.ctx.getExternalFilesDir(null).getAbsolutePath();
        }

        @JavascriptInterface
        public String getDownloadsDir() {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public byte[] encryptData(String str) throws BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gZIPOutputStream.write(str.getBytes("UTF-8"));
            gZIPOutputStream.close();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            byte[] bArr = new byte[32];
            byte[] bytes = ENC_KEY.getBytes("UTF-8");
            System.arraycopy(bytes, 0, bArr, 0, Math.min(bytes.length, 32));
            byte[] bArr2 = new byte[16];
            new SecureRandom().nextBytes(bArr2);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, new SecretKeySpec(bArr, "AES"), new IvParameterSpec(bArr2));
            byte[] bArrDoFinal = cipher.doFinal(byteArray);
            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
            byteArrayOutputStream2.write(bArr2);
            byteArrayOutputStream2.write(bArrDoFinal);
            return byteArrayOutputStream2.toByteArray();
        } catch (Exception unused) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public byte[] decryptData(byte[] bArr) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
        if (bArr != null) {
            try {
                if (bArr.length >= 32) {
                    byte[] bArr2 = new byte[32];
                    byte[] bytes = ENC_KEY.getBytes("UTF-8");
                    System.arraycopy(bytes, 0, bArr2, 0, Math.min(bytes.length, 32));
                    byte[] bArr3 = new byte[16];
                    System.arraycopy(bArr, 0, bArr3, 0, 16);
                    int length = bArr.length - 16;
                    byte[] bArr4 = new byte[length];
                    System.arraycopy(bArr, 16, bArr4, 0, length);
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(2, new SecretKeySpec(bArr2, "AES"), new IvParameterSpec(bArr3));
                    GZIPInputStream gZIPInputStream = new GZIPInputStream(new ByteArrayInputStream(cipher.doFinal(bArr4)));
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bArr5 = new byte[4096];
                    while (true) {
                        int i = gZIPInputStream.read(bArr5);
                        if (i <= 0) {
                            gZIPInputStream.close();
                            return byteArrayOutputStream.toByteArray();
                        }
                        byteArrayOutputStream.write(bArr5, 0, i);
                    }
                }
            } catch (Exception unused) {
            }
        }
        return null;
    }
}
