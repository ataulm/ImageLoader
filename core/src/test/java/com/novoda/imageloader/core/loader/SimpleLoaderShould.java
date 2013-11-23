package com.novoda.imageloader.core.loader;

import android.content.Context;

import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.cache.CacheManager;
import com.novoda.imageloader.core.file.FileManager;
import com.novoda.imageloader.core.network.NetworkManager;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleLoaderShould {
    ImageManager imageManager;
    SimpleLoader loader;

    @Before
    public void setUp() throws Exception {
        LoaderSettings loaderSettings = getLoaderSettings();
        imageManager = mock(ImageManager.class);
        loader = new SimpleLoader(loaderSettings);
    }

    @Test
    public void notifyOnImageLoadedListener_whenImageLoadedFromNetwork() throws Exception {
        // TODO: do this one first - it should pass (existing behaviour)
    }

    @Test
    public void notifyOnImageLoadedListener_whenImageLoadedFromCache() throws Exception {
        // TODO: do this one second - it should fail (fix not implemented)
    }

    private LoaderSettings getLoaderSettings() {
        LoaderSettings loaderSettings = mock(LoaderSettings.class);

        when(loaderSettings.getCacheManager()).thenReturn(mock(CacheManager.class));
        when(loaderSettings.getFileManager()).thenReturn(mock(FileManager.class));
        when(loaderSettings.getNetworkManager()).thenReturn(mock(NetworkManager.class));

        return loaderSettings;
    }
}
