package cn.lsmya.apkupdate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import cn.lsmya.apkupdate.ProgressManager.ProgressListener;
import cn.lsmya.apkupdate.ProgressManager.ProgressManager;
import cn.lsmya.apkupdate.ProgressManager.body.ProgressInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VersionUtils {

    private static VersionUtils mInstance;
    private VersionListener mVersionListener;
    private ProgressDialog progressDialog;

    private static WeakReference<Activity> activityWeakReference;
    private Handler mHandler;

    public void checkVersion(VersionListener versionListener) {
        this.mVersionListener = versionListener;

        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody formBody = new FormBody.Builder().add("key", "").build();
        Request.Builder builder = new Request
                .Builder()
                .post(formBody)
                .url("");
        okHttpClient.newCall(builder.build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activityWeakReference.get(), "请检查网络并重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseData = response.body().string();
                        switch (response.code()) {
                            case 200:
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jsonObject = new JSONObject(responseData);
                                            int localCode = getVersionCode(activityWeakReference.get());
                                            int versionCode = jsonObject.getInt("versionCode");
                                            String versionName = jsonObject.getString("versionCode");
                                            String apkUrl = jsonObject.getString("versionCode");
                                            String versionInfo = jsonObject.getString("versionCode");

                                            VersionBeen versionBeen = new VersionBeen();
                                            versionBeen.setVersionCode(versionCode);
                                            versionBeen.setVersionName(versionName);
                                            versionBeen.setApkUrl(apkUrl);
                                            versionBeen.setVersionInfo(versionInfo);

                                            if (versionCode > localCode) {
                                                mVersionListener.onUpdate(versionBeen, responseData);
                                            } else {
                                                mVersionListener.onNotUpdate();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            mVersionListener.onNotUpdate();
                                        }
                                    }
                                });
                                break;
                            default:
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mVersionListener.onNotUpdate();
                                    }
                                });
                                break;
                        }
                    }
                });

    }

    public void downApk(String url, String apkName) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = ProgressManager.getInstance().with(builder).build();

        final int[] progressInit = {0};
        progressDialog = new ProgressDialog(activityWeakReference.get());
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        download(okHttpClient, url, apkName, new OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                progressDialog.cancel();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                setIntentDataAndType(activityWeakReference.get(),
                        intent, "application/vnd.android.package-archive", file, true);
                activityWeakReference.get().startActivity(intent);
            }

            @Override
            public void onDownloading(ProgressInfo progress) {
                int percent = progress.getPercent();
                if (percent >= progressInit[0]) {
                    progressDialog.setProgress(progress.getPercent());
                    progressInit[0] = percent;
                }
            }

            @Override
            public void onDownloadFailed() {
                progressDialog.cancel();
                Toast.makeText(activityWeakReference.get(), "下载失败！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private void download(OkHttpClient okHttpClient, String url, final String apkName, final OnDownloadListener listener) {
        Request request = new Request
                .Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onDownloadFailed();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    if (is != null) {
                        File file = new File(Environment.getExternalStorageDirectory(), apkName);
                        fos = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch = -1;
                        while ((ch = is.read(buf)) != -1) {
                            fos.write(buf, 0, ch);
                        }
                    }
                    fos.flush();
                    if (fos != null) {
                        fos.close();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDownloadSuccess(new File(Environment.getExternalStorageDirectory(), apkName));
                        }
                    });

                } catch (Exception e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDownloadFailed();
                        }
                    });
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        ProgressManager.getInstance().addResponseListener(url, new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                listener.onDownloading(progressInfo);
            }

            @Override
            public void onError(long l, Exception e) {
                listener.onDownloadFailed();
            }
        });
    }

    public interface OnDownloadListener {
        void onDownloadSuccess(File file);

        void onDownloading(ProgressInfo progress);

        void onDownloadFailed();
    }


    public static VersionUtils with(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);

        if (mInstance == null) {
            synchronized (VersionUtils.class) {
                if (mInstance == null) {
                    mInstance = new VersionUtils();
                }
            }
        }
        return mInstance;
    }

    private VersionUtils() {
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    private Uri getUriForFile(Context context, File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = getUriForFile24(context, file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    private Uri getUriForFile24(Context context, File file) {
        Uri fileUri = VersionFileProvider.getUriForFile(context,
                "cn.lsmya.apkupdate.apkupdate", file);
        return fileUri;
    }

    private void setIntentDataAndType(Context context, Intent intent, String type, File file, boolean writeAble) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriForFile(context, file), type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type);
        }
    }
}
