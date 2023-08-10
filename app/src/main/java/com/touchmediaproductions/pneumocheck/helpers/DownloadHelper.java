package com.touchmediaproductions.pneumocheck.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DownloadHelper {

    public static byte[] downloadImageBytes(String imageUrl) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            URL url = new URL(imageUrl);

            try (InputStream stream = url.openStream()) {
                byte[] buffer = new byte[4096];

                while (true) {
                    int bytesRead = stream.read(buffer);
                    if (bytesRead < 0) {
                        break;
                    }
                    output.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
//            throw e;
        }

        return output.toByteArray();
    }

}
