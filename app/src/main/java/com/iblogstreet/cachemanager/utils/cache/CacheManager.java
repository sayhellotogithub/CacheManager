package com.iblogstreet.cachemanager.utils.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

/**
 * 类描述：缓存管理类
 * 创建人：@Armyone
 * 创建时间：2018/3/8
 */

public class CacheManager {
    /**
     * 只使用内存缓存(LruCache)
     */
    public static final int ONLY_LRU = 1;
    /**
     * 只使用硬盘缓存
     */
    public static final int ONLY_DISKLRU = 2;

    /**
     * 同时使用内存缓存(LruCache)与硬盘缓存(DiskLruCache)
     */
    public static final int ALL_ALLOW = 0;
    /**
     * 硬盘缓存大小
     */
    public static final int DISK_SIZE = 0;
    /**
     * 内存缓存大小
     */
    public static final int MEMORY_SIZE = 1;
    /**
     * 硬盘缓存最大值
     */
    private static int sMaxSizeForDiskLruCache = 0;
    /**
     * 内存缓存最大值
     */
    private static int sMaxMemoryForLruCache = 0;
    /**
     * 硬盘缓存文件名称
     */
    private static String sDirNameForDiskLruCache = "";
    /**
     * 缓存类型
     */
    private static int sType = 0;
    /**
     * 硬盘缓存管理类
     */
    private DiskLruCacheManager mDiskLruCacheManager;
    /**
     * 内存管理类
     */
    private LruCacheManager mLruCacheManager;
    /**
     * 图片复用
     */
    private BitmapResuable mBitmapResuable;
    private Context mContext;
    private static CacheManager cacheManager;

    private CacheManager() {
    }

    /**
     * 获得实例
     *
     * @return
     */
    public static CacheManager getInstance() {
        if (null == cacheManager) {
            synchronized (CacheManager.class) {
                cacheManager = new CacheManager();
            }
        }
        return cacheManager;
    }

    public void init(Context context) {
        mContext = context;
        initCacheMananger();
        mBitmapResuable = new BitmapResuable();
    }

    private void initCacheMananger() {
        switch (sType) {
            case ALL_ALLOW:
                initDiskLruCacheManager();
                initLruCacheManager();
                break;
            case ONLY_DISKLRU:
                initDiskLruCacheManager();
                break;
            case ONLY_LRU:
                initLruCacheManager();
                break;
            default:
                break;
        }
    }

    private void initLruCacheManager() {
        if (sMaxMemoryForLruCache > 0) {
            mLruCacheManager = new LruCacheManager(sMaxMemoryForLruCache);
        } else {
            mLruCacheManager = new LruCacheManager();
        }
    }

    private void initDiskLruCacheManager() {
        if (sMaxSizeForDiskLruCache > 0 && !TextUtils.isEmpty(sDirNameForDiskLruCache)) {
            mDiskLruCacheManager = new DiskLruCacheManager(mContext, sDirNameForDiskLruCache, sMaxSizeForDiskLruCache * 1024 * 1024);
        } else if (sMaxSizeForDiskLruCache > 0) {
            mDiskLruCacheManager = new DiskLruCacheManager(mContext, sMaxMemoryForLruCache * 1024 * 1024);
        } else if (!TextUtils.isEmpty(sDirNameForDiskLruCache)) {
            mDiskLruCacheManager = new DiskLruCacheManager(mContext, sDirNameForDiskLruCache);
        } else {
            mDiskLruCacheManager = new DiskLruCacheManager(mContext);
        }
    }

    /**
     * 设置硬盘缓存的最大值，单位为兆（M）.
     *
     * @param maxSizeForDisk 硬盘缓存最大值，单位为兆（M）
     */
    public static void setMaxSize(int maxSizeForDisk) {
        sMaxSizeForDiskLruCache = maxSizeForDisk;
    }

    /**
     * 设置内存缓存的最大值，单位为兆（M）.
     *
     * @param maxMemory 内存缓存最大值，单位为兆（M）
     */
    public static void setMaxMemory(int maxMemory) {
        sMaxMemoryForLruCache = maxMemory;
    }

    /**
     * 设置硬盘缓存自定义的文件名
     *
     * @param dirName 自定义文件名
     */
    public static void setDirName(String dirName) {
        sDirNameForDiskLruCache = dirName;
    }

    /**
     * 索引key对应的bitmap写入缓存
     *
     * @param key    缓存索引
     * @param bitmap bitmap格式数据
     */
    public void put(String key, Bitmap bitmap) {
        switch (sType) {
            case ALL_ALLOW:
                if (mLruCacheManager != null && mDiskLruCacheManager != null) {
                    //设置硬盘缓存成功后，再设置内存缓存
                    if (mDiskLruCacheManager.putDiskCache(key, bitmap)) {
                        mLruCacheManager.putCache(key, bitmap);
                    }
                }
                break;
            case ONLY_LRU:
                if (mLruCacheManager != null) {
                    mLruCacheManager.putCache(key, bitmap);
                }
                break;
            case ONLY_DISKLRU:
                if (mDiskLruCacheManager != null) {
                    mDiskLruCacheManager.putDiskCache(key, bitmap);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取索引key对应的缓存内容
     *
     * @param key 缓存索引key
     * @return key索引对应的Bitmap数据
     */
    public Bitmap get(String key) {
        Bitmap bitmap = null;
        switch (sType) {
            case ALL_ALLOW:
                if (mLruCacheManager != null && mDiskLruCacheManager != null) {
                    bitmap = mLruCacheManager.getCache(key);
                    if (bitmap == null) {
                        //如果硬盘缓存内容存在，内存缓存不存在。则在获取硬盘缓存后，将内容写入内存缓存
                        bitmap = mDiskLruCacheManager.getDiskCache(key);
                        mLruCacheManager.putCache(key, bitmap);
                    }
                }
                break;
            case ONLY_LRU:
                if (mLruCacheManager != null) {
                    bitmap = mLruCacheManager.getCache(key);
                }
                break;
            case ONLY_DISKLRU:
                if (mDiskLruCacheManager != null) {
                    bitmap = mDiskLruCacheManager.getDiskCache(key);
                }
                break;

            default:
                break;
        }
        return bitmap;
    }

    /**
     * 删除所有缓存
     */
    public void delete() {
        switch (sType) {
            case ALL_ALLOW:
                if (mLruCacheManager != null && mDiskLruCacheManager != null) {
                    mLruCacheManager.deleteCache();
                    mDiskLruCacheManager.deleteDiskCache();
                }
                break;
            case ONLY_LRU:
                if (mLruCacheManager != null) {
                    mLruCacheManager.deleteCache();
                }
                break;
            case ONLY_DISKLRU:
                if (mDiskLruCacheManager != null) {
                    mDiskLruCacheManager.deleteDiskCache();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 移除一条索引key对应的缓存
     *
     * @param key 索引
     */
    public void remove(String key) {
        switch (sType) {
            case ALL_ALLOW:
                if (mLruCacheManager != null && mDiskLruCacheManager != null) {
                    mLruCacheManager.removeCache(key);
                    mDiskLruCacheManager.removeDiskCache(key);
                }
                break;
            case ONLY_LRU:
                if (mLruCacheManager != null) {
                    mLruCacheManager.removeCache(key);
                }
                break;
            case ONLY_DISKLRU:
                if (mDiskLruCacheManager != null) {
                    mDiskLruCacheManager.removeDiskCache(key);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 缓存数据同步
     */
    public void flush() {
        switch (sType) {
            case ALL_ALLOW:
                if (mLruCacheManager != null && mDiskLruCacheManager != null) {
                    mDiskLruCacheManager.fluchCache();
                }
                break;
            case ONLY_LRU:
                break;
            case ONLY_DISKLRU:
                if (mDiskLruCacheManager != null) {
                    mDiskLruCacheManager.fluchCache();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置缓存模式
     *
     * @param cacheType ONLY_LRU、ONLY_DISK、ALL_ALLOW
     */
    public static void setCacheType(int cacheType) {
        sType = cacheType;
    }

    /**
     * 删除特定文件名的缓存文件
     *
     * @param dirName 文件名
     */
    public void deleteFile(String dirName) {
        if (mDiskLruCacheManager != null) {
            mDiskLruCacheManager.deleteFile(mContext, dirName);
        }
    }

    /**
     * 获取缓存大小——内存缓存+硬盘缓存
     *
     * @return
     */
    public int size() {
        int size = 0;
        if (mDiskLruCacheManager != null) {
            size += mDiskLruCacheManager.size();
        }
        if (mLruCacheManager != null) {
            size += mLruCacheManager.size();
        }
        return size;
    }

    /**
     * 获取缓存大小
     *
     * @param type 硬盘缓存类型：DISKSIZE、内存缓存类型：MEMORYSIZE
     * @return 对应类型的缓存大小
     */
    public int size(int type) {
        int size = 0;
        switch (type) {
            case DISK_SIZE:
                if (mDiskLruCacheManager != null) {
                    size += mDiskLruCacheManager.size();
                }
                break;
            case MEMORY_SIZE:
                if (mLruCacheManager != null) {
                    size += mLruCacheManager.size();
                }
                break;

            default:
                break;
        }
        return size;
    }

    /**
     * 关闭缓存
     */
    public void close() {
        if (mDiskLruCacheManager != null) {
            mDiskLruCacheManager.close();
        }
    }
}
