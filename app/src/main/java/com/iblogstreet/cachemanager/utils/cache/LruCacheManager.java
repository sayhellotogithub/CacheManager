package com.iblogstreet.cachemanager.utils.cache;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

/**
 * 类描述：用于管理内存缓存
 * 创建人：@Armyone
 * 创建时间：2018/3/8
 */

public class LruCacheManager {
    private LruCache<String, Bitmap> lruCache;
    private OnLruCacheManagerCallBack mOnLruCacheManagerCallBack;

    public interface OnLruCacheManagerCallBack {
        void entryRemoved(Bitmap bitmap);
    }

    public void setOnLruCacheManagerCallBack(OnLruCacheManagerCallBack onLruCacheManagerCallBack) {
        this.mOnLruCacheManagerCallBack = onLruCacheManagerCallBack;
    }

    public LruCacheManager() {
        this((int) (Runtime.getRuntime().maxMemory() / 8));
    }

    public LruCacheManager(int maxSize) {
        lruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    return value.getAllocationByteCount();
                }
                return value.getByteCount();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                //如果是复用的话，则加入复用池
                if (!evicted) {
                    if (oldValue.isMutable()) {
                        if (null != mOnLruCacheManagerCallBack) {
                            mOnLruCacheManagerCallBack.entryRemoved(oldValue);
                        }
                    } else {
                        oldValue.recycle();
                    }
                }
            }
        };
    }

    public Bitmap putCache(String key, Bitmap bitmap) {
        Bitmap bitMapValue = getCache(key);
        if (bitMapValue == null) {
            if (null != lruCache && null != bitmap) {
                bitMapValue = lruCache.put(key, bitMapValue);
            }
        }
        return bitMapValue;
    }

    public Bitmap getCache(String key) {
        if (null != lruCache) {
            return lruCache.get(key);
        }
        return null;
    }

    public void deleteCache() {
        if (null != lruCache) {
            lruCache.evictAll();
        }
    }

    public void removeCache(String key) {
        if (null != lruCache) {
            lruCache.remove(key);
        }
    }

    public int size() {
        int size = 0;
        if (null != lruCache) {
            size += lruCache.size();
        }
        return size;
    }
}
