package com.iblogstreet.cachemanager.utils.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 类描述：图片复用池
 * 创建人：@Armyone
 * 创建时间：2018/3/8
 */

public class BitmapResuable {
    private BitmapFactory.Options mOptions = new BitmapFactory.Options();
    /**
     * 图片复用池
     */
    private Set<WeakReference<Bitmap>> mResuablePool;
    //引用队列
    private ReferenceQueue referenceQueue;
    private Thread clearReferenceQueue;
    private boolean shutDown;

    public BitmapResuable() {
        this.mResuablePool = Collections.synchronizedSet(new HashSet<WeakReference<Bitmap>>());
    }

    private ReferenceQueue<Bitmap> getReferenceQueue() {
        if (null == referenceQueue) {
            //引用队列 当弱引用需要被回收 会放入队列
            referenceQueue = new ReferenceQueue<Bitmap>();
            clearReferenceQueue = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!shutDown) {
                        try {
                            Reference<Bitmap> reference = referenceQueue.remove();
                            Bitmap bitmap = reference.get();
                            if (null != bitmap && !bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            clearReferenceQueue.start();
        }
        return referenceQueue;
    }

    public Bitmap getReusable(int w, int h, int inSampleSize) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return null;
        }
        Bitmap reusable = null;
        Iterator<WeakReference<Bitmap>> iterator = mResuablePool.iterator();
        //迭代查找符合复用条件的Bitmap
        while (iterator.hasNext()) {
            Bitmap bitmap = iterator.next().get();
            if (null != bitmap) {
                //可以被复用
                if (checkInBitmap(bitmap, w, h, inSampleSize)) {
                    reusable = bitmap;
                    //移出复用池
                    iterator.remove();
                    break;
                }
            } else {
                iterator.remove();
            }
        }
        return reusable;
    }

    /**
     * 用于检查是否可以被复用
     *
     * @param bitmap
     * @param w
     * @param h
     * @param inSampleSize
     * @return
     */
    private boolean checkInBitmap(Bitmap bitmap, int w, int h, int inSampleSize) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return bitmap.getWidth() == w && bitmap.getHeight() == h
                    && inSampleSize == 1;
        }
        //如果缩放系数大于1 获得缩放后的宽与高
        if (inSampleSize > 1) {
            w /= inSampleSize;
            h /= inSampleSize;
        }
        int byteCout = w * h * getPixelsCout(bitmap.getConfig());
        return byteCout <= bitmap.getAllocationByteCount();
    }

    private int getPixelsCout(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        }
        return 2;
    }
}
