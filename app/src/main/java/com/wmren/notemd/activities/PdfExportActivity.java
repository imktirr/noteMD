package com.wmren.notemd.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.dx.stock.ProxyBuilder;
import com.wmren.notemd.R;
import com.wmren.notemd.tools.MarkdownParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class PdfExportActivity extends AppCompatActivity {

    private String noteTitle;
    private String noteContent;
    private String fileName;
    private String receive;
    private String html;
    private String output;
    private File pdfFile;

    WebView mWebView;
    private WebSettings mSettings;
    private String pdfFilePath;
    private ParcelFileDescriptor descriptor;
    private PageRange[] ranges;
    private PrintDocumentAdapter printAdapter;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_export);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.pdf_toolbar);
        toolbar.setTitle("预览PDF");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.round_arrow_back_24);
        }

        Intent intent = getIntent();
        noteTitle = intent.getStringExtra("noteTitle");
        noteContent = intent.getStringExtra("noteContent");
        fileName = noteTitle;
        noteTitle = "# " + noteTitle + "\n";
        receive = noteTitle + noteContent;
        html = MarkdownParser.parse(receive);
        output = MarkdownParser.configHtml(html);


        mWebView = findViewById(R.id.render_view);
        mProgressBar = findViewById(R.id.pb);
        mSettings = mWebView.getSettings();
        mSettings.setAllowContentAccess(true);
        mSettings.setBuiltInZoomControls(false);
        mSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mSettings.setJavaScriptEnabled(true);

        // 开启Application Cache功能
        mSettings.setAppCacheEnabled(true);

        //设置适配
        mSettings.setUseWideViewPort(true);
        mSettings.setLoadWithOverviewMode(true);
        mSettings.setDomStorageEnabled(true);
        mSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished (WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                // 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
//                view.loadUrl(url);
                return true;
            }
        });

        mWebView.loadDataWithBaseURL(null, output, "text/html", "utf-8", null);

    }

    public void printPDF (View view) {
        boolean hasPermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
        if (! hasPermission) {
            if (Build.VERSION.SDK_INT > 22) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            } else {
                Toast.makeText(PdfExportActivity.this, "请打开读写权限", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        webViewToPdf();
    }

    private void webViewToPdf () {
        mProgressBar.setVisibility(View.VISIBLE);
        File appDir = new File(Environment.getExternalStorageDirectory(), "/noteMD/pdfExport");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        pdfFilePath = appDir.getAbsolutePath() + "/" + fileName + ".pdf";
        //创建DexMaker缓存目录
        try {
            pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
            pdfFile.createNewFile();
            descriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_WRITE);
            // 设置打印参数
            PrintAttributes.MediaSize isoA4 = PrintAttributes.MediaSize.ISO_A4;
            PrintAttributes attributes = new PrintAttributes.Builder()
                    .setMediaSize(isoA4)
                    .setResolution(new PrintAttributes.Resolution("id", Context.PRINT_SERVICE, 500, 500))
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build();
            // 计算webview打印需要的页数
            int numberOfPages = ((mWebView.getContentHeight() * 500 / (isoA4.getHeightMils())));
            ranges = new PageRange[]{new PageRange(0, numberOfPages)};
            // 创建pdf文件缓存目录
            // 获取需要打印的webview适配器
            printAdapter = mWebView.createPrintDocumentAdapter("adapter");
            // 开始打印
            printAdapter.onStart();
            printAdapter.onLayout(attributes, attributes, new CancellationSignal(),
                    getLayoutResultCallback(new InvocationHandler() {
                        @Override
                        public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("onLayoutFinished")) {
                                // 监听到内部调用了onLayoutFinished()方法,即打印成功
                                onLayoutSuccess();
                            } else {
                                // 监听到打印失败或者取消了打印
                                Toast.makeText(PdfExportActivity.this, "导出失败,请重试", Toast.LENGTH_SHORT).show();
                            }
                            return null;
                        }
                    }), new Bundle());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: SHARE
        Intent share = new Intent(Intent.ACTION_SEND);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //如果在Android7.0以上,使用FileProvider获取Uri
            uri = FileProvider.getUriForFile(PdfExportActivity.this, "com.wmren.notemd.fileprovider", pdfFile);
        } else {
            //否则使用Uri.fromFile(file)方法获取Uri
            uri = Uri.fromFile(pdfFile);
        }
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("*/*");
        startActivity(Intent.createChooser(share, "分享至"));
    }

    private void onLayoutSuccess () throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PrintDocumentAdapter.WriteResultCallback callback = getWriteResultCallback(new InvocationHandler() {
                @Override
                public Object invoke (Object o, Method method, Object[] objects) {
                    if (method.getName().equals("onWriteFinished")) {
                        Toast.makeText(PdfExportActivity.this, "pdf导出至：" + pdfFilePath, Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(PdfExportActivity.this, "导出失败", Toast.LENGTH_SHORT).show();
                    }
                    return null;
                }
            });
            printAdapter.onWrite(ranges, descriptor, new CancellationSignal(), callback);
        }
    }

    public static PrintDocumentAdapter.LayoutResultCallback getLayoutResultCallback (InvocationHandler invocationHandler) throws IOException {
        return ProxyBuilder.forClass(PrintDocumentAdapter.LayoutResultCallback.class)
                .handler(invocationHandler)
                .build();
    }

    public static PrintDocumentAdapter.WriteResultCallback getWriteResultCallback (InvocationHandler invocationHandler) throws IOException {
        return ProxyBuilder.forClass(PrintDocumentAdapter.WriteResultCallback.class)
                .handler(invocationHandler)
                .build();
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            boolean hasPermission = true;
            for (int hasPer : grantResults) {
                if (hasPer == PermissionChecker.PERMISSION_DENIED) {
                    hasPermission = false;
                    break;
                }
            }
            if (hasPermission) {
                webViewToPdf();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //导航栏按钮选区时的监听事件
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
