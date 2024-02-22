package org.deep_thinker.dl.mnist.util;

import org.deep_thinker.dl.mnist.DigitData;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Loads the Mnist dataset and put it in a List of DigitData
 */
public class FileUtil {

    private final static int LABEL_FILE_MAGIC_INT = 2049;
    private final static int IMG_FILE_MAGIC_INT = 2051;

    public static List<DigitData> loadImageData(String filePrefix) {
        List<DigitData> images = null;
        ClassLoader loader = FileUtil.class.getClassLoader();
        String imgFileName = filePrefix + "-images-idx3-ubyte";
        String lblFileName = filePrefix + "-labels-idx1-ubyte";
        try (
                DataInputStream imageIS = new DataInputStream(loader.getResourceAsStream(imgFileName));
                DataInputStream labelIS = new DataInputStream(loader.getResourceAsStream(lblFileName));
        ) {

            if (imageIS.readInt() != IMG_FILE_MAGIC_INT)
                throw new IOException("Unknown file format for " + imgFileName);

            if (labelIS.readInt() != LABEL_FILE_MAGIC_INT)
                throw new IOException("Unknown file format for " + lblFileName);

            int nImages = imageIS.readInt();
            int nLabels = labelIS.readInt();

            if (nImages != nLabels)
                throw new IOException(format("File %s and %s contains data for different number of images", imgFileName, lblFileName));

            images = new ArrayList<>(nImages);

            int rows = imageIS.readInt();
            int cols = imageIS.readInt();

            byte[] data = new byte[rows * cols];


            for (int i = 0; i < nImages; i++) {
                double[] img = new double[rows * cols];
                //noinspection ResultOfMethodCallIgnored
                imageIS.read(data, 0, data.length);
                for (int d = 0; d < img.length; d++)
                    img[d] = (data[d] & 255) / 255.0;

                images.add(new DigitData(img, labelIS.readByte()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return images;
    }

}
