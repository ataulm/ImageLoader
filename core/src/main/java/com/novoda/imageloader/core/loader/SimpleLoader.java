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
package com.novoda.imageloader.core.loader;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.OnImageLoadedListener;
import com.novoda.imageloader.core.loader.util.BitmapDisplayer;
import com.novoda.imageloader.core.loader.util.BitmapRetriever;
import com.novoda.imageloader.core.loader.util.SingleThreadedLoader;
import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageWrapper;

import java.io.File;
import java.lang.ref.WeakReference;

public class SimpleLoader implements Loader {

    private LoaderSettings loaderSettings;
    private SingleThreadedLoader singleThreadedLoader;
    private WeakReference<OnImageLoadedListener> onImageLoadedListener;

    public SimpleLoader(LoaderSettings loaderSettings) {
        this.loaderSettings = loaderSettings;
        singleThreadedLoader = new SingleThreadedLoader() {
            @Override
            protected Bitmap loadMissingBitmap(ImageWrapper iw) {
                return getBitmap(iw.getUrl(), iw.getWidth(), iw.getHeight(), iw.getImageView());
            }

            @Override
            protected void onBitmapLoaded(ImageWrapper iw, Bitmap bmp) {
                new BitmapDisplayer(bmp, iw).runOnUiThread();
                SimpleLoader.this.loaderSettings.getCacheManager().put(iw.getUrl(), bmp);
                onImageLoaded(iw.getImageView());
            }
        };
    }

    private Bitmap getResourceAsBitmap(ImageWrapper imageWrapper, int resId) {
        Bitmap bitmap = loaderSettings.getResCacheManager().get(String.valueOf(resId), imageWrapper.getWidth(), imageWrapper.getHeight());
        if (weCanUseThis(bitmap)) {
            return bitmap;
        }
        bitmap = loaderSettings.getBitmapUtil().decodeResourceBitmapAndScale(imageWrapper, resId, loaderSettings.isAllowUpsampling());
        loaderSettings.getResCacheManager().put(String.valueOf(resId), bitmap);
        return bitmap;
    }

    private Bitmap getBitmapFromMemoryCache(ImageWrapper imageWrapper) {
        return loaderSettings.getCacheManager().get(imageWrapper.getUrl(), imageWrapper.getHeight(), imageWrapper.getWidth());
    }

    private Bitmap getPreviewCachedBitmap(ImageWrapper imageWrapper) {
        return loaderSettings.getCacheManager().get(imageWrapper.getPreviewUrl(), imageWrapper.getPreviewHeight(), imageWrapper.getPreviewWidth());
    }

    @Override
    public void setLoadListener(WeakReference<OnImageLoadedListener> onImageLoadedListener) {
        this.onImageLoadedListener = onImageLoadedListener;
    }

    private Bitmap getBitmap(String url, int width, int height, ImageView imageView) {
        if (url != null && url.length() >= 0) {
            File file = loaderSettings.getFileManager().getFile(url);
            BitmapRetriever retriever = new BitmapRetriever(url, file, width, height, 0, false, true, imageView, loaderSettings, null);
            Bitmap bitmap = retriever.getBitmap();
            return bitmap;
        }
        return null;
    }

    @Override
    public void load(ImageView imageView) {
        if (weCannotUseThis(imageView)) {
            Log.w("ImageLoader", "You should never call load if you don't set a ImageTag on the view");
            return;
        }

        loadBitmap(new ImageWrapper(imageView));
    }

    private void loadBitmap(ImageWrapper imageWrapper) {
        Bitmap bitmap = getBitmapFromMemoryCache(imageWrapper);
        if (weCanUseThis(bitmap)) {
            imageWrapper.setBitmap(bitmap, false);
            return;
        }

        loadDefaultImage(imageWrapper);

        if (!imageWrapper.isUseCacheOnly()) {
            singleThreadedLoader.push(imageWrapper);
        }
    }

    private void loadDefaultImage(ImageWrapper imageWrapper) {
        if (imageWrapper.hasNoPreviewUrl()) {
            imageWrapper.setResourceBitmap(getResourceAsBitmap(imageWrapper, imageWrapper.getLoadingResourceId()));
            return;
        }

        Bitmap bitmap = getPreviewCachedBitmap(imageWrapper);
        if (weCanUseThis(bitmap)) {
            imageWrapper.setBitmap(getPreviewCachedBitmap(imageWrapper), false);
        } else {
            imageWrapper.setResourceBitmap(getResourceAsBitmap(imageWrapper, imageWrapper.getLoadingResourceId()));
        }
    }

    private void onImageLoaded(ImageView imageView) {
        if (onImageLoadedListener != null) {
            onImageLoadedListener.get().onImageLoaded(imageView);
        }
    }

    private boolean weCanUseThis(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled();
    }

    private boolean weCanUseThis(ImageView imageView) {
        Object tag = imageView.getTag();
        return tag != null && tag instanceof ImageTag;
    }

    private boolean weCannotUseThis(ImageView imageView) {
        return !weCanUseThis(imageView);
    }
}
