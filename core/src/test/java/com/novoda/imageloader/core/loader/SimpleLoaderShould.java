package com.novoda.imageloader.core.loader;

import android.text.TextUtils;
import android.widget.ImageView;

import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.OnImageLoadedListener;
import com.novoda.imageloader.core.cache.CacheManager;
import com.novoda.imageloader.core.file.FileManager;
import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.network.NetworkManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleLoaderShould {
    static final boolean FILE_DOES_NOT_EXIST = false;
    static final String URL = "dummy url";
    static final int WIDTH = 0;
    static final int HEIGHT = 0;

    SimpleLoader loader;
    LoaderSettings settings;
    ImageManager imageManager;
    ImageLoadedListener onImageLoaded = new ImageLoadedListener();
    FileManager fileManager;
    CacheManager cacheManager;

    @Before
    public void setUp() throws Exception {
        settings = mock(LoaderSettings.class);
        setupFileManager();
        cacheManager = mock(CacheManager.class);
        loader = new SimpleLoader(settings);

        when(settings.getLoader()).thenReturn(loader);
        when(settings.getCacheManager()).thenReturn(cacheManager);
        when(settings.getFileManager()).thenReturn(fileManager);
        when(settings.getNetworkManager()).thenReturn(mock(NetworkManager.class));

        imageManager = new ImageManager(settings);
        imageManager.setOnImageLoadedListener(onImageLoaded);
    }

    @Test
    public void notifyOnImageLoadedListener_whenImageLoadedFromNetwork() throws Exception {
        // TODO: do this one first - it should pass (existing behaviour)
        ImageView view = getImageViewWithValidImageTag();
        returnNullWhenBitmapRequestedFrom(cacheManager);

        imageManager.getLoader().load(view);

        assertTrue(onImageLoaded.called == 1);
    }

    @Ignore
    @Test
    public void notifyOnImageLoadedListener_whenImageLoadedFromCache() throws Exception {
        // TODO: do this one second - it should fail (fix not implemented)
    }

    class ImageLoadedListener implements OnImageLoadedListener {
        int called;

        @Override
        public void onImageLoaded(ImageView imageView) {
            called++;
        }
    }

    private void setupFileManager() {
        File file = mock(File.class);
        when(file.exists()).thenReturn(FILE_DOES_NOT_EXIST);

        fileManager = mock(FileManager.class);
        when(fileManager.getFile(URL, WIDTH, HEIGHT)).thenReturn(file);
    }

    private ImageView getImageViewWithValidImageTag() {
        ImageView view;
        view = mock(ImageView.class);
        ImageTag tag = mock(ImageTag.class);
        when(view.getTag()).thenReturn(tag);
        return view;
    }

    private void returnNullWhenBitmapRequestedFrom(CacheManager manager) {
        when(manager.get(URL, WIDTH, HEIGHT)).thenReturn(null);
    }
}
