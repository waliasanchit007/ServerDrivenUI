package com.example.serverdrivenui;

import android.content.Context;
import app.cash.redwood.protocol.host.HostProtocol;
import app.cash.redwood.treehouse.TreehouseApp;
import app.cash.redwood.treehouse.TreehouseAppFactoryAndroidKt;
import app.cash.zipline.loader.ManifestVerifier;
import app.cash.zipline.loader.ZiplineHttpClient;
import okhttp3.OkHttpClient;

public class TreehouseHelper {
    public static TreehouseApp.Factory createTreehouseAppFactory(
        Context context,
        ZiplineHttpClient httpClient,
        ManifestVerifier manifestVerifier,
        HostProtocol.Factory hostProtocolFactory
    ) {
        return TreehouseAppFactoryAndroidKt.TreehouseAppFactory(
            context,
            httpClient,
            manifestVerifier,
            null, // embeddedFileSystem
            null, // embeddedDir
            "zipline", // cacheName
            50L * 1024L * 1024L, // cacheMaxSizeInBytes
            com.example.serverdrivenui.LoggingLoaderEventListener.INSTANCE, // loaderEventListener
            8, // concurrentDownloads
            new app.cash.redwood.treehouse.MemoryStateStore(), // stateStore
            app.cash.redwood.leaks.LeakDetector.Companion.none(), // leakDetector
            hostProtocolFactory
        );
    }
}
