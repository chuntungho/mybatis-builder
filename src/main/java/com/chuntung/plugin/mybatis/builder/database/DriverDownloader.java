/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.database;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DriverTypeEnum;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.HttpRequests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Resolves and downloads JDBC driver jars on demand, JetBrains Database-tool style.
 * Bundled drivers (MySQL / PostgreSQL) ship inside the plugin and need no download;
 * other {@link DriverTypeEnum} types are fetched from Maven Central into a shared
 * local directory and reused across projects.
 *
 * @author Chuntung Ho
 */
public class DriverDownloader {
    private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";

    private static final DriverDownloader instance = new DriverDownloader();

    public static DriverDownloader getInstance() {
        return instance;
    }

    /**
     * Shared directory holding downloaded driver jars, under the IDE system path.
     */
    public Path getDriversDir() {
        return Paths.get(PathManager.getSystemPath(), "mybatis-builder", "drivers");
    }

    /**
     * Expected local jar path for a downloadable driver, or {@code null} for
     * bundled / custom drivers.
     */
    public Path localJar(DriverTypeEnum type) {
        if (type == null || !type.isDownloadable()) {
            return null;
        }
        String[] gav = type.getMavenCoordinate().split(":");
        String artifact = gav[1];
        String version = gav[2];
        return getDriversDir().resolve(artifact + "-" + version + ".jar");
    }

    /**
     * @return true when the downloadable driver jar already exists on disk.
     */
    public boolean isPresent(DriverTypeEnum type) {
        Path jar = localJar(type);
        return jar != null && Files.isRegularFile(jar);
    }

    /**
     * Expected local jar path for any maven coordinate string, e.g. "com.mysql:mysql-connector-j:8.3.0"
     */
    public Path localJar(String mavenCoordinate) {
        if (mavenCoordinate == null || mavenCoordinate.trim().isEmpty()) return null;
        String[] gav = mavenCoordinate.split(":");
        if (gav.length < 3) return null;
        return getDriversDir().resolve(gav[1] + "-" + gav[2] + ".jar");
    }

    /**
     * Returns true when the jar for this maven coordinate already exists on disk.
     */
    public boolean isPresent(String mavenCoordinate) {
        Path jar = localJar(mavenCoordinate);
        return jar != null && Files.isRegularFile(jar);
    }

    /**
     * Resolves the driver library path to load for a connection:
     * explicit driverLibrary on the connection always takes precedence (user override);
     * registered driver (driverType == null) -> the connection's snapshot library;
     * bundled -> empty (plugin classpath); downloadable -> path of the downloaded jar.
     */
    public String resolveDriverLibrary(ConnectionInfo connectionInfo) {
        // explicit library on the connection always takes precedence (user override)
        if (StringUtil.stringHasValue(connectionInfo.getDriverLibrary())) {
            return connectionInfo.getDriverLibrary();
        }
        DriverTypeEnum type = connectionInfo.getDriverType();
        if (type == null) {
            return ""; // registered driver with no library — caller handles error
        }
        if (type.isDownloadable()) {
            Path jar = localJar(type);
            return jar != null ? jar.toString() : "";
        }
        return ""; // bundled — loaded from plugin classpath
    }

    /**
     * Downloads the driver jar for a raw maven coordinate from Maven Central.
     *
     * @throws IOException when the download or file move fails
     */
    public void download(String mavenCoordinate, ProgressIndicator indicator) throws IOException {
        if (mavenCoordinate == null || mavenCoordinate.trim().isEmpty()) {
            throw new IOException("No Maven coordinate specified");
        }
        String[] gav = mavenCoordinate.split(":");
        if (gav.length < 3) throw new IOException("Invalid Maven coordinate: " + mavenCoordinate);
        String groupPath = gav[0].replace('.', '/');
        String artifact = gav[1];
        String version = gav[2];
        String fileName = artifact + "-" + version + ".jar";
        String url = MAVEN_CENTRAL + groupPath + '/' + artifact + '/' + version + '/' + fileName;

        Path dir = getDriversDir();
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName);

        File tmp = Files.createTempFile(dir, artifact, ".part").toFile();
        try {
            HttpRequests.request(url).saveToFile(tmp, indicator);
            Files.move(tmp.toPath(), target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IOException("Failed to download driver from " + url + " - " + e.getMessage(), e);
        } finally {
            if (tmp.exists()) tmp.delete();
        }
    }

    /**
     * Downloads the driver jar from Maven Central into {@link #getDriversDir()}.
     * Safe to call when already present (re-download).
     *
     * @throws IOException when the download or file move fails
     */
    public void download(DriverTypeEnum type, ProgressIndicator indicator) throws IOException {
        if (type == null || !type.isDownloadable()) {
            throw new IOException("No downloadable driver for type " + type);
        }
        download(type.getMavenCoordinate(), indicator);
    }

    /**
     * @return the on-disk jar file name for a downloadable driver, or null otherwise.
     */
    public String jarName(DriverTypeEnum type) {
        Path jar = localJar(type);
        return jar != null ? jar.getFileName().toString() : null;
    }
}
