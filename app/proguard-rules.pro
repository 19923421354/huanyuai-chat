# Keep JavascriptInterface methods used by the WebView bridge
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
