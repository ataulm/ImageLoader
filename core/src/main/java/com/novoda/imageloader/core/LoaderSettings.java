/**
 * Copyright 2012 Novoda Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.novoda.imageloader.core;

import android.content.Context;
import android.os.Build;

import com.novoda.imageloader.core.bitmap.BitmapUtil;
import com.novoda.imageloader.core.cache.CacheManager;
import com.novoda.imageloader.core.cache.SoftMapCache;
import com.novoda.imageloader.core.file.BasicFileManager;
import com.novoda.imageloader.core.file.FileManager;
import com.novoda.imageloader.core.file.util.AndroidFileContext;
import com.novoda.imageloader.core.file.util.FileUtil;
import com.novoda.imageloader.core.loader.ConcurrentLoader;
import com.novoda.imageloader.core.loader.Loader;
import com.novoda.imageloader.core.loader.SimpleLoader;
import com.novoda.imageloader.core.network.NetworkManager;
import com.novoda.imageloader.core.network.UrlNetworkManager;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * LoaderSettings is used to customise the behaviour of the ImageLoader.
 * <p/>
 * Either use the default constructor to instantiate a LoaderSettings object with standard values, or use
 * {@link com.novoda.imageloader.core.LoaderSettings.Builder} to construct a customised variant.
 */
public class LoaderSettings {
    private static final int ONE_SECOND = 1000;
    private static final int ONE_HOUR = 3600 * ONE_SECOND;
    private static final int ONE_DAY = 24 * ONE_HOUR;
    private static final int ONE_WEEK = 7 * ONE_DAY;

    private static final int DEFAULT_READ_TIMEOUT = 10 * ONE_SECOND;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10 * ONE_SECOND;
    private static final int DEFAULT_EXPIRATION_PERIOD = ONE_WEEK;

    private static final boolean DEFAULT_INCLUDE_QUERY_IN_HASH = true;
    private static final boolean DEFAULT_DISCONNECT_ON_EVERY_CALL = false;
    private static final boolean DEFAULT_CLEAN_ON_SETUP = true;
    private static final boolean DEFAULT_USE_ASYNC_TASKS = true;
    private static final boolean DEFAULT_ALLOW_UPSAMPLING = false;
    private static final boolean DEFAULT_ALWAYS_USE_ORIGINAL_SIZE = false;

    private final BitmapUtil bitmapUtil;
    private final Map<String, String> headers;

    private CacheManager cacheManager;
    private CacheManager resourceCacheManager;
    private FileManager fileManager;
    private NetworkManager networkManager;
    private Loader loader;
    private File cacheDir;
    private int readTimeout;
    private int connectionTimeout;
    private long expirationPeriodInMillis;
    private boolean shouldIncludeQueryInHash;
    private boolean shouldCleanExpiredItemsInCacheOnSetup;
    private boolean shouldDisconnectOnEveryCall;
    private boolean shouldUseAsyncTasks;
    private boolean shouldAllowUpsampling;
    private boolean shouldAlwaysUseOriginalSize;

    public LoaderSettings() {
        this.bitmapUtil = new BitmapUtil();
        this.headers = new HashMap<String, String>();

        this.expirationPeriodInMillis = DEFAULT_EXPIRATION_PERIOD;
        this.readTimeout = DEFAULT_READ_TIMEOUT;
        this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

        this.shouldIncludeQueryInHash = DEFAULT_INCLUDE_QUERY_IN_HASH;
        this.shouldCleanExpiredItemsInCacheOnSetup = DEFAULT_CLEAN_ON_SETUP;
        shouldDisconnectOnEveryCall = DEFAULT_DISCONNECT_ON_EVERY_CALL;
        shouldUseAsyncTasks = DEFAULT_USE_ASYNC_TASKS;
        shouldAllowUpsampling = DEFAULT_ALLOW_UPSAMPLING;
        shouldAlwaysUseOriginalSize = DEFAULT_ALWAYS_USE_ORIGINAL_SIZE;
    }

    private LoaderSettings(BitmapUtil bitmapUtil, Map<String, String> headers, CacheManager cacheManager,
                           CacheManager resourceCacheManager, FileManager fileManager, NetworkManager networkManager,
                           Loader loader, File cacheDir, int readTimeout, int connectionTimeout,
                           long expirationPeriodInMillis, boolean shouldIncludeQueryInHash,
                           boolean shouldCleanExpiredItemsInCacheOnSetup, boolean shouldDisconnectOnEveryCall,
                           boolean shouldUseAsyncTasks, boolean shouldAllowUpsampling,
                           boolean shouldAlwaysUseOriginalSize) {
        this.bitmapUtil = bitmapUtil;
        this.headers = headers;

        this.cacheManager = cacheManager;
        this.resourceCacheManager = resourceCacheManager;
        this.fileManager = fileManager;
        this.networkManager = networkManager;

        this.loader = loader;
        this.cacheDir = cacheDir;
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
        this.expirationPeriodInMillis = expirationPeriodInMillis;

        this.shouldIncludeQueryInHash = shouldIncludeQueryInHash;
        this.shouldCleanExpiredItemsInCacheOnSetup = shouldCleanExpiredItemsInCacheOnSetup;
        this.shouldDisconnectOnEveryCall = shouldDisconnectOnEveryCall;
        this.shouldUseAsyncTasks = shouldUseAsyncTasks;
        this.shouldAllowUpsampling = shouldAllowUpsampling;
        this.shouldAlwaysUseOriginalSize = shouldAlwaysUseOriginalSize;
    }

    public BitmapUtil getBitmapUtil() {
        return bitmapUtil;
    }

    /**
     * Returns a File reference to the directory where images will be cached on disk.
     *
     * @return the cache directory
     */
    public File getCacheDir() {
        return cacheDir;
    }

    /**
     * Returns time in millis after which images cached to disk may be deleted.
     *
     * @return the time in millis after cached image will expire
     */
    public long getExpirationPeriod() {
        return expirationPeriodInMillis;
    }

    /**
     * Flag indicating whether queries of image urls should be used as part of the cache key.
     * <p/>
     * If true, urls with different query parameters are considered as distinct; {@code http://example.com/img.png?v=1}
     * and {@code http://example.com/img.png?v=2} will resolve as distinct urls.
     * <p/>
     * If false, it will consider them to be the same url.
     *
     * @return true if the query is part of the url, false if it is discarded
     */
    public boolean shouldIncludeQueryInHash() {
        return shouldIncludeQueryInHash;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public boolean getDisconnectOnEveryCall() {
        return shouldDisconnectOnEveryCall;
    }

    /**
     * Returns Build.VERSION.SDK_INT.
     * <p/>
     * Used so it can be mocked and allows testing against API-specific behaviours.
     *
     * @return sdkVersion
     */
    public int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    public CacheManager getCacheManager() {
        if (cacheManager == null) {
            cacheManager = new SoftMapCache();
        }
        return cacheManager;
    }

    public CacheManager getResCacheManager() {
        if (resourceCacheManager == null) {
            resourceCacheManager = new SoftMapCache();
        }
        return resourceCacheManager;
    }

    public NetworkManager getNetworkManager() {
        if (networkManager == null) {
            networkManager = new UrlNetworkManager(this);
        }
        return networkManager;
    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            fileManager = new BasicFileManager(this);
        }
        return fileManager;
    }

    /**
     * Specifies whether the LoaderSettings is configured to use asynchronous tasks.
     * <p/>
     * Defaults to true, and can be turned off using
     * {@link com.novoda.imageloader.core.LoaderSettings.Builder#doNotUseAsyncTasks()}
     *
     * @return shouldUseAsyncTasks the flag indicating whether or not to use asynchronous tasks
     */
    public boolean shouldUseAsyncTasks() {
        return shouldUseAsyncTasks;
    }

    public Loader getLoader() {
        if (loader == null) {
            if (shouldUseAsyncTasks()) {
                this.loader = new ConcurrentLoader(this);
            } else {
                this.loader = new SimpleLoader(this);
            }
        }
        return loader;
    }

    /**
     * should clean all expired items in the cache on setup
     *
     * @return
     */
    public boolean shouldCleanOnSetup() {
        return shouldCleanExpiredItemsInCacheOnSetup;
    }

    public boolean shouldAlwaysUseOriginalSize() {
        return shouldAlwaysUseOriginalSize;
    }

    /**
     * Flag to enable upsampling for small images.
     * <p/>
     * If true and the image is smaller than the requested size the image is resized to a larger image.
     * Default is false.
     *
     * @return true if upsampling is allowed
     */
    public boolean shouldAllowUpsampling() {
        return shouldAllowUpsampling;
    }

    /**
     * @param allowUpsampling
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setAllowUpsampling(boolean allowUpsampling) {
        this.shouldAllowUpsampling = allowUpsampling;
    }

    /**
     * @param networkManager
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    /**
     * @param fileManager
     * @see Builder#withFileManager(com.novoda.imageloader.core.file.FileManager)
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * @deprecated in 1.6.2. Use {@link LoaderSettings#shouldUseAsyncTasks} instead.
     */
    @Deprecated
    public boolean isUseAsyncTasks() {
        return shouldUseAsyncTasks();
    }

    /**
     * @deprecated in 1.6.2. Use {@link #shouldCleanOnSetup()} instead.
     */
    @Deprecated
    public boolean isCleanOnSetup() {
        return true;
    }

    /**
     * @deprecated in 1.6.2. Use {@link #shouldAllowUpsampling()} instead.
     */
    @Deprecated
    public boolean isAllowUpsampling() {
        return shouldAllowUpsampling();
    }

    /**
     * @param useAsyncTasks
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setUseAsyncTasks(boolean useAsyncTasks) {
        this.shouldUseAsyncTasks = useAsyncTasks;
    }

    /**
     * @param loader
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    private void setLoader(Loader loader) {
        this.loader = loader;
    }

    /**
     * Flag to disable image resizing.
     * Set this flag to true if you want to avoid bitmap resizing
     * Default is false.
     *
     * @return true if images are always cached in the original size
     * @deprecated in 1.6.2. Use {@link #shouldAlwaysUseOriginalSize()}
     */
    @Deprecated
    public boolean isAlwaysUseOriginalSize() {
        return shouldAlwaysUseOriginalSize();
    }

    /**
     * @param readTimeout
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * @param isQueryIncludedInHash
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setQueryIncludedInHash(boolean isQueryIncludedInHash) {
        this.shouldIncludeQueryInHash = isQueryIncludedInHash;
    }

    /**
     * @param cacheDir
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    /**
     * Flag indicating whether queries of image urls should be used as part of the cache key.
     * If set to false the cache returns the same image e.g.
     * for <code>http://king.com/img.png?v=1</code> and <code>http://king.com/img.png?v=2</code>
     *
     * @return true if urls with different queries refer to different images.
     * @deprecated in 1.6.2. Use {@link #shouldIncludeQueryInHash()}
     */
    @Deprecated
    public boolean isQueryIncludedInHash() {
        return shouldIncludeQueryInHash();
    }

    /**
     * @param connectionTimeout
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * @param key
     * @param value
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * @param alwaysUseOriginalSize
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setAlwaysUseOriginalSize(boolean alwaysUseOriginalSize) {
        this.shouldAlwaysUseOriginalSize = alwaysUseOriginalSize;
    }

    /**
     * @param expirationPeriod
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setExpirationPeriod(long expirationPeriod) {
        this.expirationPeriodInMillis = expirationPeriod;
    }

    /**
     * @param resCacheManager
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setResCacheManager(CacheManager resCacheManager) {
        this.resourceCacheManager = resCacheManager;
    }

    /**
     * @param cacheManager
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * @param disconnectOnEveryCall
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setDisconnectOnEveryCall(boolean disconnectOnEveryCall) {
        this.shouldDisconnectOnEveryCall = disconnectOnEveryCall;
    }

    /**
     * @param sdkVersion
     * @see com.novoda.imageloader.core.LoaderSettings#getSdkVersion()
     * @deprecated in 1.6.2. This method will be removed in a later version of ImageLoader; it should not be possible to
     * modify the LoaderSettings object after creation - use the Builder instead.
     */
    @Deprecated
    public void setSdkVersion(int sdkVersion) {
    }

    /**
     * Builder for the LoaderSettings.
     *
     * @deprecated in 1.6.2. Use LoaderSettings#Builder instead
     */
    @Deprecated
    public static class SettingsBuilder {

        private LoaderSettings settings;

        public SettingsBuilder() {
            settings = new LoaderSettings();
        }

        /**
         * Change setting of time period before cached images are removed from file storage.
         *
         * @param timePeriodInMillis time period in milli seconds
         * @return this SettingsBuilder
         */
        public SettingsBuilder withExpirationPeriod(long timePeriodInMillis) {
            settings.setExpirationPeriod(timePeriodInMillis);
            return this;
        }

        /**
         * Change flag indicating whether queries of image urls should be used as part of the cache key.
         * <p/>
         * If set to false the cache returns the same image e.g. for {@code http://king.com/img.png?v=1} and
         * {@code http://king.com/img.png?v=2}.
         *
         * @param enableQueryInHashGeneration set to false if querys in urls should be ignored.
         * @return this SettingsBuilder.
         */
        public SettingsBuilder withEnableQueryInHashGeneration(boolean enableQueryInHashGeneration) {
            settings.setQueryIncludedInHash(enableQueryInHashGeneration);
            return this;
        }

        public SettingsBuilder withConnectionTimeout(int connectionTimeout) {
            settings.setConnectionTimeout(connectionTimeout);
            return this;
        }

        public SettingsBuilder withReadTimeout(int readTimeout) {
            settings.setReadTimeout(readTimeout);
            return this;
        }

        public SettingsBuilder addHeader(String key, String value) {
            settings.addHeader(key, value);
            return this;
        }

        public SettingsBuilder withDisconnectOnEveryCall(boolean disconnectOnEveryCall) {
            settings.setDisconnectOnEveryCall(disconnectOnEveryCall);
            return this;
        }

        public SettingsBuilder withCacheManager(CacheManager cacheManager) {
            settings.setCacheManager(cacheManager);
            return this;
        }

        public SettingsBuilder withResCacheManager(CacheManager resCacheManager) {
            settings.setResCacheManager(resCacheManager);
            return this;
        }

        public SettingsBuilder withAsyncTasks(boolean useAsyncTasks) {
            settings.setUseAsyncTasks(useAsyncTasks);
            return this;
        }

        public SettingsBuilder withCacheDir(File file) {
            settings.setCacheDir(file);
            return this;
        }

        /**
         * Changes flag to enable upsampling for small images.
         * If true and the image is smaller than the requested size
         * the image is resized to a larger image. Default is false.
         *
         * @param allowUpsampling set to true if you want to enlarge small images
         * @return this SettingsBuilder
         */
        public SettingsBuilder withUpsampling(boolean allowUpsampling) {
            settings.setAllowUpsampling(allowUpsampling);
            return this;
        }

        /**
         * Changes flag to disable image resizing.
         * Set the flag to true if you want to avoid bitmap resizing. Default is false.
         *
         * @param alwaysUseOriginalSize set to true if you want to avoid bitmap resizing
         * @return this SettingsBuilder
         */
        public SettingsBuilder withoutResizing(boolean alwaysUseOriginalSize) {
            settings.setAlwaysUseOriginalSize(alwaysUseOriginalSize);
            return this;
        }

        public SettingsBuilder withFileManager(FileManager fileManager) {
            settings.setFileManager(fileManager);
            return this;
        }

        public SettingsBuilder withNetworkManager(NetworkManager networkManager) {
            settings.setNetworkManager(networkManager);
            return this;
        }

        public SettingsBuilder withLoader(Loader loader) {
            settings.setLoader(loader);
            return this;
        }

        public LoaderSettings build(Context context) {
            File dir = new FileUtil().prepareCacheDirectory(new AndroidFileContext(context));
            settings.setCacheDir(dir);
            settings.setSdkVersion(Build.VERSION.SDK_INT);
            return settings;
        }

    }

    /**
     * Provides an easy way of configuring the LoaderSettings.
     *
     * @see LoaderSettings
     */
    public static class Builder {

        private LoaderSettings settings;
        private final BitmapUtil bitmapUtil;
        private final Map<String, String> headers;

        private CacheManager cacheManager;
        private CacheManager resourceCacheManager;
        private FileManager fileManager;
        private NetworkManager networkManager;

        private Loader loader;
        private File cacheDir;
        private int readTimeout;
        private int connectionTimeout;
        private long expirationPeriodInMillis;
        private boolean shouldIncludeQueryInHash;
        private boolean shouldDisconnectOnEveryCall;
        private boolean shouldCleanExpiredItemsInCacheOnSetup;
        private boolean shouldUseAsyncTasks;
        private boolean shouldAllowUpsampling;
        private boolean shouldAlwaysUseOriginalSize;

        public Builder() {
            bitmapUtil = new BitmapUtil();
            headers = new HashMap<String, String>();

            expirationPeriodInMillis = DEFAULT_EXPIRATION_PERIOD;
            shouldIncludeQueryInHash = DEFAULT_INCLUDE_QUERY_IN_HASH;
            connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
            readTimeout = DEFAULT_READ_TIMEOUT;
            shouldCleanExpiredItemsInCacheOnSetup = DEFAULT_CLEAN_ON_SETUP;
            shouldDisconnectOnEveryCall = DEFAULT_DISCONNECT_ON_EVERY_CALL;
            shouldUseAsyncTasks = DEFAULT_USE_ASYNC_TASKS;
            shouldAllowUpsampling = DEFAULT_ALLOW_UPSAMPLING;
            shouldAlwaysUseOriginalSize = DEFAULT_ALWAYS_USE_ORIGINAL_SIZE;
        }

        /**
         * Sets expiration of cached images from file storage.
         *
         * @param expirationPeriodInMillis the length in millis after which cached images can be removed from storage
         * @return this LoaderSettings.Builder
         */
        public Builder withExpirationPeriod(long expirationPeriodInMillis) {
            this.expirationPeriodInMillis = expirationPeriodInMillis;
            return this;
        }

        /**
         * Change flag indicating whether queries of image urls should be used as part of the cache key.
         * If set to false the cache returns the same image e.g. for {@code http://king.com/img.png?v=1} and
         * {@code http://king.com/img.png?v=2}.
         *
         * @return this LoaderSettings.Builder
         */
        public Builder discardQueryDuringHashGeneration() {
            shouldIncludeQueryInHash = false;
            return this;
        }

        public Builder doNotCleanFileCacheOnSetup() {
            shouldCleanExpiredItemsInCacheOnSetup = false;
            return this;
        }

        /**
         * @param connectionTimeout
         * @return this LoaderSettings.Builder
         */
        public Builder withConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * @param readTimeout
         * @return this LoaderSettings.Builder
         */
        public Builder withReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * @param key
         * @param value
         * @return this LoaderSettings.Builder
         */
        public Builder addHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder disconnectOnEveryCall() {
            shouldDisconnectOnEveryCall = true;
            return this;
        }

        public Builder withCacheManager(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
            return this;
        }

        public Builder withResCacheManager(CacheManager resourceCacheManager) {
            this.resourceCacheManager = resourceCacheManager;
            return this;
        }

        /**
         * @return this LoaderSettings.Builder
         * @see com.novoda.imageloader.core.LoaderSettings#shouldUseAsyncTasks()
         */
        public Builder doNotUseAsyncTasks() {
            shouldUseAsyncTasks = false;
            return this;
        }

        public Builder withCacheDir(File cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        /**
         * Changes flag to enable upsampling for small images.
         * If true and the image is smaller than the requested size
         * the image is resized to a larger image. Default is false.
         *
         * @return this SettingsBuilder
         */
        public Builder allowUpsampling() {
            shouldAllowUpsampling = true;
            return this;
        }

        /**
         * Changes flag to disable image resizing.
         * Set the flag to true if you want to avoid bitmap resizing. Default is false.
         *
         * @return this SettingsBuilder
         */
        public Builder disableResizing() {
            shouldAlwaysUseOriginalSize = true;
            return this;
        }

        public Builder withFileManager(FileManager fileManager) {
            this.fileManager = fileManager;
            return this;
        }

        public Builder withNetworkManager(NetworkManager networkManager) {
            this.networkManager = networkManager;
            return this;
        }

        public Builder withLoader(Loader loader) {
            this.loader = loader;
            return this;
        }

        public LoaderSettings build(Context context) {
            if (cacheDir == null) {
                cacheDir = new FileUtil().prepareCacheDirectory(new AndroidFileContext(context));
            }

            setupFileManager();

            settings = new LoaderSettings(bitmapUtil, headers, cacheManager, resourceCacheManager, fileManager,
                    networkManager, loader, cacheDir, readTimeout, connectionTimeout, expirationPeriodInMillis,
                    shouldIncludeQueryInHash, shouldCleanExpiredItemsInCacheOnSetup, shouldDisconnectOnEveryCall,
                    shouldUseAsyncTasks, shouldAllowUpsampling, shouldAlwaysUseOriginalSize);

            return settings;
        }

        private void setupFileManager() {
            if (fileManager == null) {
                BasicFileManager.FileManagerSettings fileManagerSettings = new BasicFileManager.FileManagerSettings(
                        cacheDir, shouldIncludeQueryInHash, expirationPeriodInMillis);
                fileManager = new BasicFileManager(fileManagerSettings);
            }

            if (shouldCleanExpiredItemsInCacheOnSetup) {
                fileManager.cleanOldFiles();
            }
        }
    }
}
