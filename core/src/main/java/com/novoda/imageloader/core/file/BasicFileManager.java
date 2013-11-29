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
package com.novoda.imageloader.core.file;

import android.graphics.Bitmap;

import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.file.util.FileUtil;
import com.novoda.imageloader.core.network.UrlUtil;
import com.novoda.imageloader.core.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Handles the storage of cached files on disk.
 *
 * A basic implementation of {@link FileManager}.
 */
public class BasicFileManager implements FileManager {

    private static final String NAME_SIZE_SEPARATOR = "-";
    private static final String WIDTH_HEIGHT_SEPARATOR = "x";
    private final FileManagerSettings settings;

    public BasicFileManager(FileManagerSettings settings) {
        this.settings = settings;
    }

    /**
     * @param settings
     * @deprecated in 1.6.2, use {@link #BasicFileManager(FileManagerSettings)}
     */
    public BasicFileManager(LoaderSettings settings) {
        this.settings = new FileManagerSettings(settings.getCacheDir(), settings.shouldIncludeQueryInHash(), settings.getExpirationPeriod());
    }

    /**
     * Clean is removing all the files in the cache directory.
     */
    @Override
    public void clean() {
        deleteOldFiles(0);
    }

    /**
     * Removes all the files in the cache directory on storage where the timestamp is older then the expiration time.
     */
    @Override
    public void cleanOldFiles() {
        deleteOldFiles(settings.expirationPeriodInMillis);
    }

    @Override
    public String getFilePath(String imageUrl) {
        File file = getFile(imageUrl);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return null;
    }

    @Override
    public void saveBitmap(String fileName, Bitmap bitmap, int width, int height) {
        try {
            FileOutputStream out = new FileOutputStream(fileName + NAME_SIZE_SEPARATOR + width + WIDTH_HEIGHT_SEPARATOR + height);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            Log.warning(e.getMessage());
        }
    }

    @Override
    public File getFile(String url) {
        url = processUrl(url);
        String filename = String.valueOf(url.hashCode());
        return new File(settings.cacheDirectory, filename);
    }

    @Override
    public File getFile(String url, int width, int height) {
        url = processUrl(url);
        String filename = url.hashCode() + NAME_SIZE_SEPARATOR + width + WIDTH_HEIGHT_SEPARATOR + height;
        return new File(settings.cacheDirectory, filename);
    }

    private String processUrl(String url) {
        if (settings.includeQueryInHash) {
            return url;
        }
        return UrlUtil.removeQuery(url);
    }

    private void deleteOldFiles(final long expirationPeriod) {
        final String pathToCacheDir = settings.cacheDirectory.getAbsolutePath();
        Thread cleaner = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new FileUtil().reduceFileCache(pathToCacheDir, expirationPeriod);
                } catch (Throwable ignore) {
                }
            }
        });
        cleaner.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        cleaner.start();
    }

    public static class FileManagerSettings {
        private final File cacheDirectory;
        private final boolean includeQueryInHash;
        private final long expirationPeriodInMillis;

        public FileManagerSettings(File cacheDirectory, boolean includeQueryInHash, long expirationPeriodInMillis) {
            this.cacheDirectory = cacheDirectory;
            this.includeQueryInHash = includeQueryInHash;
            this.expirationPeriodInMillis = expirationPeriodInMillis;
        }
    }
}
