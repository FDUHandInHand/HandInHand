package cn.edu.fudan.blueflamingo.handinhand.lib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

import cn.edu.fudan.blueflamingo.handinhand.R;
import cn.edu.fudan.blueflamingo.handinhand.model.Message;

/**
 * The type App utility.
 */
public class AppUtility {

    /**
     * The constant APPNAME.
     */
    public final static String APPNAME = "HandInHand";
    /**
     * The constant STORAGE_URL.
     */
    public final static String STORAGE_URL = "http://121.199.64.117:8888/HandInHand/upload/";
    /**
     * The constant QUESTION_MODE.
     */
    public final static int QUESTION_MODE = 1;
    /**
     * The constant ANSWER_MODE.
     */
    public final static int ANSWER_MODE = 2;
    /**
     * The constant COMMENT_MODE.
     */
    public final static int COMMENT_MODE = 3;
    /**
     * The constant FAV_QUESTION_LIST.
     */
    public final static int FAV_QUESTION_LIST = 8;
    /**
     * The constant mDiskLruCache.
     */
    public static DiskLruCache mDiskLruCache;

    /**
     * Gets disk cache dir.
     *
     * @param context the context
     * @param uniqueName the unique name
     * @return the disk cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + uniqueName);
	}

    /**
     * Gets app version.
     *
     * @param context the context
     * @return the app version
     */
    public static int getAppVersion(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			Log.d("HandInHand", e.toString());
		}
		return 1;
	}

    /**
     * Md 5.
     *
     * @param inputText the input text
     * @return the string
     */
// md5加密
	public static String md5(String inputText) {
		return encrypt(inputText, "md5");
	}

    /**
     * Sha string.
     *
     * @param inputText the input text
     * @return the string
     */
// sha加密
	public static String sha(String inputText) {
		return encrypt(inputText, "sha-1");
	}

	/**
	 * md5或者sha-1加密
	 *
	 * @param inputText
	 *            要加密的内容
	 * @param algorithmName
	 *            加密算法名称：md5或者sha-1，不区分大小写
	 * @return
	 */
	private static String encrypt(String inputText, String algorithmName) {
		if (inputText == null) {
			throw new IllegalArgumentException("请输入要加密的内容");
		}
		if (algorithmName == null || "".equals(algorithmName.trim())) {
			algorithmName = "md5";
		}
		String encryptText = null;
		try {
			MessageDigest m = MessageDigest.getInstance(algorithmName);
			m.update(inputText.getBytes("UTF8"));
			byte s[] = m.digest();
			// m.digest(inputText.getBytes("UTF8"));
			return hex(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptText;
	}

	// 返回十六进制字符串
	private static String hex(byte[] arr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1,
					3));
		}
		return sb.toString();
	}

    /**
     * Download url to stream.
     *
     * @param urlString the url string
     * @param outputStream the output stream
     * @return the boolean
     */
    public static boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
			out = new BufferedOutputStream(outputStream, 8 * 1024);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

    /**
     * Trim upload.
     *
     * @param res the res
     * @return the string
     */
    public static String trimUpload(String res) {
		String[] tmp = res.split("/");
		return tmp[tmp.length - 1];
	}

    /**
     * Open disk lru cache.
     *
     * @param context the context
     * @param cacheSize the cache size
     * @return the boolean
     */
    public static boolean openDiskLruCache(Context context, int cacheSize) {
		try {
			File cacheDir = AppUtility.getDiskCacheDir(context, AppUtility.APPNAME);
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			mDiskLruCache = mDiskLruCache.open(cacheDir, AppUtility.getAppVersion(context),
					1, cacheSize);
			return true;
		} catch (Exception e) {
			Log.d(APPNAME, e.toString());
			return false;
		}
	}

    /**
     * Gets image.
     *
     * @param imgRes the img res
     * @param context the context
     * @return the image
     */
    public static Bitmap getImage(final String imgRes, Context context) {
        if (imgRes.equals("")) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.app);
            return ((BitmapDrawable) drawable).getBitmap();
        }
        String imgMD5 = md5(imgRes);
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(imgMD5);
            if (snapshot != null) {
                //cache hit
                InputStream inputStream = snapshot.getInputStream(0);
                return BitmapFactory.decodeStream(inputStream);
            } else {
                //cache miss
                final DiskLruCache.Editor editor = mDiskLruCache.edit(imgMD5);
                final OutputStream outputStream = editor.newOutputStream(0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (downloadUrlToStream(STORAGE_URL + imgRes, outputStream)) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                            mDiskLruCache.flush();
                            //Log.d("handinhand", "DOWNLOADING FROM NETWORK");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                snapshot = mDiskLruCache.get(imgMD5);
                InputStream inputStream = snapshot.getInputStream(0);
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Drawable drawable = context.getResources().getDrawable(R.drawable.app);
            return ((BitmapDrawable) drawable).getBitmap();
        }
    }
}
