package com.example.serverdrivenui;

import android.content.Context;
import dev.konduit.protocol.host.HostProtocol;
import dev.konduit.treehouse.TreehouseApp;
import dev.konduit.treehouse.TreehouseAppFactoryAndroidKt;
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
            new dev.konduit.treehouse.MemoryStateStore(), // stateStore
            dev.konduit.leaks.LeakDetector.Companion.none(), // leakDetector
            hostProtocolFactory
        );
    }
}
