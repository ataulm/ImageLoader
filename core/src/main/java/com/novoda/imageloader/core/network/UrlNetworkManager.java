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
package com.novoda.imageloader.core.network;

import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.exception.ImageNotFoundException;
import com.novoda.imageloader.core.file.util.FileUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Basic implementation of the {@link NetworkManager} using URL connection.
 */
public class UrlNetworkManager implements NetworkManager {

    private static final String CONNECTION_HEADER_FIELD = "Location";
    private static final int TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 3;

    private final NetworkManagerSettings settings;

    private int manualRedirects;

    public UrlNetworkManager(NetworkManagerSettings settings) {
        this.settings = settings;
    }

    /**
     * @deprecated in 1.6.2. Use {@link #UrlNetworkManager(NetworkManagerSettings)}
     */
    public UrlNetworkManager(LoaderSettings settings) {
        this(settings, new FileUtil());
    }

    /**
     * @deprecated in 1.6.2. Use {@link #UrlNetworkManager(NetworkManagerSettings)}
     */
    public UrlNetworkManager(LoaderSettings settings, FileUtil fileUtil) {
        this.settings = new NetworkManagerSettings(fileUtil, settings.getHeaders(), settings.getConnectionTimeout(),
                settings.getReadTimeout(), settings.shouldDisconnectOnEveryCall());
    }

    @Override
    public void retrieveImage(String url, File f) {
        InputStream is = null;
        OutputStream os = null;
        HttpURLConnection conn = null;
        try {
            conn = openConnection(url);
            conn.setConnectTimeout(settings.connectionTimeout);
            conn.setReadTimeout(settings.readTimeout);

            handleHeaders(conn);

            if (conn.getResponseCode() == TEMP_REDIRECT) {
                redirectManually(f, conn);
            } else {
                is = conn.getInputStream();
                os = new FileOutputStream(f);
                settings.fileUtil.copyStream(is, os);
            }
        } catch (FileNotFoundException fnfe) {
            throw new ImageNotFoundException();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null && settings.shouldDisconnectOnEveryCall) {
                conn.disconnect();
            }
            settings.fileUtil.closeSilently(is);
            settings.fileUtil.closeSilently(os);
        }
    }

    private void handleHeaders(HttpURLConnection conn) {
        Map<String, String> headers = settings.headers;
        if (headers != null) {
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
    }

    public void redirectManually(File f, HttpURLConnection conn) {
        manualRedirects++;
        if (manualRedirects <= MAX_REDIRECTS) {
            retrieveImage(conn.getHeaderField(CONNECTION_HEADER_FIELD), f);
        } else {
            manualRedirects = 0;
        }
    }

    @Override
    public InputStream retrieveInputStream(String url) {
        HttpURLConnection conn = null;
        try {
            conn = openConnection(url);
            conn.setConnectTimeout(settings.connectionTimeout);
            conn.setReadTimeout(settings.readTimeout);
            return conn.getInputStream();
        } catch (FileNotFoundException fnfe) {
            throw new ImageNotFoundException();
        } catch (Throwable ex) {
            return null;
        }
    }

    protected HttpURLConnection openConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    public static class NetworkManagerSettings {
        private final FileUtil fileUtil;
        private final int connectionTimeout;
        private final int readTimeout;
        private final boolean shouldDisconnectOnEveryCall;
        private final Map<String, String> headers;

        public NetworkManagerSettings(FileUtil fileUtil, Map<String, String> headers, int connectionTimeout, int readTimeout, boolean shouldDisconnectOnEveryCall) {
            this.fileUtil = fileUtil;
            this.headers = headers;
            this.connectionTimeout = connectionTimeout;
            this.readTimeout = readTimeout;
            this.shouldDisconnectOnEveryCall = shouldDisconnectOnEveryCall;
        }
    }
}
