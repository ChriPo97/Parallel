package parallel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author Christoph
 */
public class Imperativ {

    private static int[] mSource;
    private static int mStart;
    private static int mLength;
    private static int[] mDestination;

    public static void main(String[] args) throws IOException {
        String srcName = "tiger.jpg";
        File srcFile = new File(srcName);
        BufferedImage image = ImageIO.read(srcFile);
        BufferedImage grayImage = gray(image);
        String dstName = "tiger-gray.jpg";
        File dstFile = new File(dstName);
        ImageIO.write(grayImage, "jpg", dstFile);
    }

    private static BufferedImage gray(BufferedImage srcImage) {
        int w = srcImage.getWidth();
        int h = srcImage.getHeight();
        int[] src = srcImage.getRGB(0, 0, w, h, null, 0, w);
        int[] dst = new int[src.length];
        mSource = src;
        mStart = 0;
        mLength = src.length;
        mDestination = dst;
        long startTime = System.currentTimeMillis();
        computeDirectly();
        long endTime = System.currentTimeMillis();
        System.out.println("Graying took " + (endTime - startTime)
                + " milliseconds.");
        BufferedImage dstImage
                = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        dstImage.setRGB(0, 0, w, h, dst, 0, w);
        return dstImage;
    }

    private static void computeDirectly() {
        for (int index = mStart; index < mStart + mLength; index++) {
            int pixel = mSource[index];
            float red = (pixel >> 16) & 0xff;
            float green = (pixel >> 8) & 0xff;
            float blue = pixel & 0xff;
            float gray = (float) ((red * 0.21) + (green * 0.72) + (blue * 0.07));
            if (isPrime((int) (red * green * blue * gray))) {
                int dpixel = ((255 << 16)
                        | (0) << 8)
                        | (0);
                mDestination[index] = dpixel;
            } else {
                int dpixel = (((int) gray) << 16)
                        | (((int) gray) << 8)
                        | (((int) gray));
                mDestination[index] = dpixel;
            }
        }
    }

    public static boolean isPrime(int num) {
        if (num == 2 || num == 3) {
            return true;
        }
        if (num % 2 == 0 || num % 3 == 0) {
            return false;
        }
        for (int i = 3; i <= Math.sqrt(num); i += 2) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

}
