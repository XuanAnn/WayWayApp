/*
 * Copyright 2007-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

public class MavenWrapperDownloader {

    private static final String WRAPPER_PROPERTIES_PATH =
            ".mvn/wrapper/maven-wrapper.properties";

    private static final String WRAPPER_JAR_PATH =
            ".mvn/wrapper/maven-wrapper.jar";

    private static final String PROPERTY_NAME_WRAPPER_URL = "wrapperUrl";

    public static void main(String[] args) {
        System.out.println("- Downloader started");
        File baseDirectory = new File(args.length > 0 ? args[0] : ".");
        System.out.println("- Using base directory: " + baseDirectory.getAbsolutePath());

        File wrapperProperties = new File(baseDirectory, WRAPPER_PROPERTIES_PATH);
        if (!wrapperProperties.isFile()) {
            System.err.println("- ERROR " + WRAPPER_PROPERTIES_PATH + " not found");
            System.exit(1);
        }

        Properties properties = new Properties();
        try (InputStream inputStream = new java.io.FileInputStream(wrapperProperties)) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("- ERROR loading " + WRAPPER_PROPERTIES_PATH);
            e.printStackTrace();
            System.exit(1);
        }

        String wrapperUrl = properties.getProperty(PROPERTY_NAME_WRAPPER_URL);
        if (wrapperUrl == null || wrapperUrl.isBlank()) {
            System.err.println("- ERROR " + PROPERTY_NAME_WRAPPER_URL + " is missing in " + WRAPPER_PROPERTIES_PATH);
            System.exit(1);
        }

        File wrapperJar = new File(baseDirectory, WRAPPER_JAR_PATH);
        if (wrapperJar.isFile()) {
            System.out.println("- Wrapper jar already exists: " + wrapperJar.getAbsolutePath());
            return;
        }

        System.out.println("- Downloading " + wrapperUrl);
        try {
            downloadFileFromURL(wrapperUrl, wrapperJar);
            System.out.println("- Done: " + wrapperJar.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("- ERROR downloading wrapper jar");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void downloadFileFromURL(String urlString, File destination) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "wayway-backend-maven-wrapper");

        int code = connection.getResponseCode();
        if (code >= 400) {
            throw new IOException("HTTP " + code + " from " + urlString);
        }

        destination.getParentFile().mkdirs();
        try (InputStream inputStream = connection.getInputStream();
             ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(destination)) {
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } finally {
            connection.disconnect();
        }
    }
}

