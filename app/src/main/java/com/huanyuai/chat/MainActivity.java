package com.huanyuai.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HuanyuAI";
    private static final String ASSET_HTML = "file:///android_asset/user_client.html";
    private static final String BACKUP_FILE = "localStorage_backup.dat";
    private static final String ENC_KEY = "HuanyuAI2024SecKey!@#$%^&*()9876";
    private static final int REQUEST_STORAGE_PERM = 1003;
    private static final String SHARED_DIR = "HuanyuAI";
    private static final String SHARED_FILE = "shared_data.dat";
    private static final String VERSION = "4.0.0";

    private ValueCallback<Uri[]> filePathCallback;
    private String pendingJsCallback;
    private WebView webView;
    private ProgressBar progressBar;
    private TextView errorView;
    private FrameLayout container;

    private final ActivityResultLauncher<Intent> fileChooserLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (filePathCallback != null) {
                                Intent data = result.getData();
                                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                                    Uri uri = data.getData();
                                    if (uri != null) {
                                        filePathCallback.onReceiveValue(new Uri[]{uri});
                                    } else {
                                        filePathCallback.onReceiveValue(null);
                                    }
                                } else {
                                    filePathCallback.onReceiveValue(null);
                                }
                                filePathCallback = null;
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> pickFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (pendingJsCallback == null) return;
                            String path = "";
                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                                path = result.getData().getData().getPath();
                            }
                            if (webView != null) {
                                webView.evaluateJavascript(
                                        "javascript:try{if(window['" + pendingJsCallback + "'])window['" + pendingJsCallback + "']('" + path.replace("'", "\\'") + "')}catch(e){}",
                                        null
                                );
                            }
                            pendingJsCallback = null;
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 创建根容器
        container = new FrameLayout(this);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // 创建进度条
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 4
        ));
        progressBar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(0xFF8B6CFF)
        );
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(android.view.View.GONE);

        // 创建错误提示视图
        errorView = new TextView(this);
        errorView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        errorView.setGravity(android.view.Gravity.CENTER);
        errorView.setTextSize(16);
        errorView.setTextColor(0xFF888888);
        errorView.setVisibility(android.view.View.GONE);
        FrameLayout.LayoutParams errorLp = (FrameLayout.LayoutParams) errorView.getLayoutParams();
        errorLp.gravity = android.view.Gravity.CENTER;
        errorView.setLayoutParams(errorLp);
        errorView.setText("加载中...");

        // 创建 WebView
        try {
            webView = new WebView(this);
            webView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        } catch (Exception e) {
            Log.e(TAG, "Failed to create WebView", e);
            Toast.makeText(this, "WebView初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        container.addView(webView);
        container.addView(progressBar);
        container.addView(errorView);
        setContentView(container);

        setupWebView();
        webView.addJavascriptInterface(new AppBridge(this), "App");
        requestStoragePermission();
        loadMainPage();
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
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
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 启用硬件加速
        if (Build.VERSION.SDK_INT >= 19) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(android.view.View.VISIBLE);
                progressBar.setIndeterminate(true);
                errorView.setVisibility(android.view.View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(android.view.View.GONE);
                errorView.setVisibility(android.view.View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    progressBar.setVisibility(android.view.View.GONE);
                    errorView.setVisibility(android.view.View.VISIBLE);
                    String desc = error != null ? error.getDescription().toString() : "未知错误";
                    errorView.setText("页面加载失败，请重试\n错误: " + desc);
                    Log.e(TAG, "Page load error: " + desc);
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (request.isForMainFrame()) {
                    Log.w(TAG, "HTTP error: " + errorResponse.getStatusCode());
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    // 允许外部链接在 WebView 中加载
                    return false;
                }
                if (url.startsWith("file:///android_asset/")) {
                    return false;
                }
                // 其他协议尝试用外部浏览器打开
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Log.w(TAG, "Cannot open url: " + url, e);
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;
                try {
                    Intent intent = fileChooserParams.createIntent();
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    fileChooserLauncher.launch(Intent.createChooser(intent, "选择文件"));
                    return true;
                } catch (Exception e) {
                    MainActivity.this.filePathCallback = null;
                    Log.w(TAG, "File chooser failed", e);
                    return false;
                }
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                try {
                    request.grant(request.getResources());
                } catch (Exception e) {
                    Log.w(TAG, "Permission request failed", e);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) -> result.confirm())
                        .setOnCancelListener(dialog -> result.cancel())
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) -> result.confirm())
                        .setNegativeButton("取消", (dialog, which) -> result.cancel())
                        .setOnCancelListener(dialog -> result.cancel())
                        .show();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    progressBar.setVisibility(android.view.View.VISIBLE);
                    progressBar.setIndeterminate(false);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(android.view.View.GONE);
                }
            }
        });
    }

    private void loadMainPage() {
        try {
            webView.loadUrl(ASSET_HTML);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load main page", e);
            errorView.setVisibility(android.view.View.VISIBLE);
            errorView.setText("页面加载失败: " + e.getMessage());
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            // Android 11+ 不需要存储权限，使用 scoped storage
            notifyStorageGranted(true);
            return;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_STORAGE_PERM
                );
            } else {
                notifyStorageGranted(true);
            }
        } else {
            notifyStorageGranted(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERM) {
            boolean granted = grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
            notifyStorageGranted(granted);
        }
    }

    private void notifyStorageGranted(boolean granted) {
        if (webView != null) {
            webView.evaluateJavascript(
                    "javascript:try{if(typeof onStorageGranted==='function'){if(" + granted + ")onStorageGranted();}}catch(e){}",
                    null
            );
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }

    public class AppBridge {
        private final Context ctx;

        AppBridge(Context context) {
            this.ctx = context;
        }

        @JavascriptInterface
        public String getVersion() {
            return MainActivity.VERSION;
        }

        @JavascriptInterface
        public void setOnHomeView(boolean isHome) {
            // 由 JS 层管理视图状态
        }

        @JavascriptInterface
        public String getModelDir() {
            File file = new File(ctx.getFilesDir(), "models");
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        }

        @JavascriptInterface
        public String getDataDir() {
            return ctx.getFilesDir().getAbsolutePath();
        }

        @JavascriptInterface
        public String getCacheDir() {
            return ctx.getCacheDir().getAbsolutePath();
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
        public String getAppVersion() {
            return MainActivity.VERSION;
        }

        @JavascriptInterface
        public String getAppInfo() {
            return "幻语AI v" + MainActivity.VERSION + " (Build " + android.os.Build.DISPLAY + ")";
        }

        @JavascriptInterface
        public void showToast(String msg) {
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void copyToClipboard(String text) {
            try {
                ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("text", text));
                }
            } catch (Exception e) {
                Log.w(TAG, "Copy to clipboard failed", e);
            }
        }

        @JavascriptInterface
        public void pickFile(String callbackName) {
            MainActivity.this.pendingJsCallback = callbackName;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                pickFileLauncher.launch(Intent.createChooser(intent, "选择文件"));
            } catch (Exception e) {
                Log.w(TAG, "Pick file failed", e);
                if (webView != null) {
                    webView.evaluateJavascript(
                            "javascript:try{if(window['" + callbackName + "'])window['" + callbackName + "']('')}catch(e){}",
                            null
                    );
                }
            }
        }

        @JavascriptInterface
        public String saveFile(String fileName, String content) {
            try {
                File downloadsDir;
                if (Build.VERSION.SDK_INT >= 30) {
                    downloadsDir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "HuanyuAI");
                } else {
                    downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                }
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }
                File file = new File(downloadsDir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(content.getBytes("UTF-8"));
                fos.close();
                return file.getAbsolutePath();
            } catch (Exception e) {
                Log.w(TAG, "Save file failed", e);
                return "";
            }
        }

        @JavascriptInterface
        public String readFile(String path) {
            try {
                File file = new File(path);
                if (!file.exists()) return "";
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                return new String(data, "UTF-8");
            } catch (Exception e) {
                Log.w(TAG, "Read file failed", e);
                return "";
            }
        }

        @JavascriptInterface
        public void shareFile(String path) {
            try {
                File file = new File(path);
                Uri uri;
                if (Build.VERSION.SDK_INT >= 24) {
                    String authority = ctx.getPackageName() + ".fileprovider";
                    uri = FileProvider.getUriForFile(ctx, authority, file);
                } else {
                    uri = Uri.fromFile(file);
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ctx.startActivity(Intent.createChooser(intent, "分享文件"));
            } catch (Exception e) {
                Log.w(TAG, "Share file failed", e);
                Toast.makeText(ctx, "分享失败", Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void openBrowser(String url) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                ctx.startActivity(intent);
            } catch (Exception e) {
                Log.w(TAG, "Open browser failed", e);
                Toast.makeText(ctx, "无法打开链接", Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void downloadModel(final String url, final String fileName) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection conn = null;
                    try {
                        File modelDir = new File(ctx.getExternalFilesDir(null), "models");
                        if (!modelDir.exists()) {
                            modelDir.mkdirs();
                        }
                        final File file = new File(modelDir, fileName);
                        conn = (HttpURLConnection) new URL(url).openConnection();
                        conn.setConnectTimeout(30000);
                        conn.setReadTimeout(30000);
                        InputStream is = conn.getInputStream();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                        is.close();
                        ((Activity) ctx).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ctx, "模型下载完成: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (final Exception e) {
                        Log.e(TAG, "Download model failed", e);
                        ((Activity) ctx).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ctx, "下载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                }
            }).start();
        }

        @JavascriptInterface
        public String readSharedData() {
            try {
                File sharedDir = new File(Environment.getExternalStorageDirectory(), SHARED_DIR);
                File file = new File(sharedDir, SHARED_FILE);
                if (!file.exists()) return "{}";
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                byte[] decrypted = MainActivity.this.decryptData(data);
                if (decrypted != null) {
                    return new String(decrypted, "UTF-8");
                }
                return new String(data, "UTF-8");
            } catch (Exception e) {
                Log.w(TAG, "Read shared data failed", e);
                return "{}";
            }
        }

        @JavascriptInterface
        public boolean writeSharedData(String json) {
            try {
                File sharedDir = new File(Environment.getExternalStorageDirectory(), SHARED_DIR);
                if (!sharedDir.exists()) {
                    sharedDir.mkdirs();
                }
                File file = new File(sharedDir, SHARED_FILE);
                byte[] encrypted = MainActivity.this.encryptData(json);
                if (encrypted != null) {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(encrypted);
                    fos.close();
                } else {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(json.getBytes("UTF-8"));
                    fos.close();
                }
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Write shared data failed", e);
                return false;
            }
        }

        @JavascriptInterface
        public boolean testSharedData() {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), SHARED_DIR);
                if (!dir.exists()) {
                    return dir.mkdirs();
                }
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Test shared data failed", e);
                return false;
            }
        }

        @JavascriptInterface
        public boolean backupLocalStorage(String data) {
            try {
                File sharedDir = new File(Environment.getExternalStorageDirectory(), SHARED_DIR);
                if (!sharedDir.exists()) {
                    sharedDir.mkdirs();
                }
                File file = new File(sharedDir, BACKUP_FILE);
                byte[] encrypted = MainActivity.this.encryptData(data);
                if (encrypted != null) {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(encrypted);
                    fos.close();
                } else {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data.getBytes("UTF-8"));
                    fos.close();
                }
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Backup localStorage failed", e);
                return false;
            }
        }

        @JavascriptInterface
        public String restoreLocalStorage() {
            try {
                File file = new File(new File(Environment.getExternalStorageDirectory(), SHARED_DIR), BACKUP_FILE);
                if (!file.exists()) return "";
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                byte[] decrypted = MainActivity.this.decryptData(data);
                if (decrypted != null) {
                    return new String(decrypted, "UTF-8");
                }
                return new String(data, "UTF-8");
            } catch (Exception e) {
                Log.w(TAG, "Restore localStorage failed", e);
                return "";
            }
        }

        @JavascriptInterface
        public String getExternalFilesDir() {
            File dir = ctx.getExternalFilesDir(null);
            return dir != null ? dir.getAbsolutePath() : "";
        }

        @JavascriptInterface
        public String getDownloadsDir() {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        }

        @JavascriptInterface
        public boolean isNetworkAvailable() {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
            return false;
        }

        @JavascriptInterface
        public String getBatteryLevel() {
            android.content.IntentFilter ifilter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
            android.content.Intent batteryStatus = ctx.registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) {
                    return String.valueOf(level * 100 / scale);
                }
            }
            return "-1";
        }
    }

    private byte[] encryptData(String data) {
        try {
            // GZIP compress
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
            gzipOut.write(data.getBytes("UTF-8"));
            gzipOut.close();
            byte[] compressed = baos.toByteArray();

            // Derive AES key
            byte[] key = new byte[32];
            byte[] keyBytes = ENC_KEY.getBytes("UTF-8");
            System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));

            // AES/CBC/PKCS5Padding
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(compressed);

            // IV + encrypted data
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            result.write(iv);
            result.write(encrypted);
            return result.toByteArray();
        } catch (Exception e) {
            Log.w(TAG, "Encrypt failed", e);
            return null;
        }
    }

    private byte[] decryptData(byte[] data) {
        if (data == null || data.length < 32) return null;
        try {
            // Derive AES key
            byte[] key = new byte[32];
            byte[] keyBytes = ENC_KEY.getBytes("UTF-8");
            System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));

            // Extract IV
            byte[] iv = new byte[16];
            System.arraycopy(data, 0, iv, 0, 16);

            // Extract encrypted data
            int encryptedLen = data.length - 16;
            byte[] encrypted = new byte[encryptedLen];
            System.arraycopy(data, 16, encrypted, 0, encryptedLen);

            // Decrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            // GZIP decompress
            GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(decrypted));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            gzipIn.close();
            return baos.toByteArray();
        } catch (Exception e) {
            Log.w(TAG, "Decrypt failed", e);
            return null;
        }
    }
}