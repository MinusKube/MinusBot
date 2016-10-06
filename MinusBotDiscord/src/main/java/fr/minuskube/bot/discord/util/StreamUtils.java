package fr.minuskube.bot.discord.util;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    public static File tempFileFromImage(BufferedImage image, String prefix, String suffix) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);

        InputStream is = new ByteArrayInputStream(os.toByteArray());
        return tempFileFromInputStream(is, prefix, suffix);
    }

    public static File tempFileFromInputStream(InputStream is, String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();

        try(FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(is, out);
        }

        return tempFile;
    }

}
