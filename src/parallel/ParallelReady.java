package parallel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Christoph
 */
public class ParallelReady extends RecursiveAction {

    private int[] mSource;
    private int mStart;
    private int mLength;
    private int[] mDestination;

    public ParallelReady(int[] src, int start, int length, int[] dst) {
        mSource = src;
        mStart = start;
        mLength = length;
        mDestination = dst;
    }

    protected void computeDirectly() {
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
    protected static int sThreshold = 1000;

    @Override
    protected void compute() {
        if (mLength < sThreshold) {
            computeDirectly();
            return;
        }
        int split = mLength / 2;
        invokeAll(new ParallelReady(mSource, mStart, split, mDestination),
                new ParallelReady(mSource, mStart + split, mLength - split,
                        mDestination));
    }

    public static void main(String[] args) throws Exception {
        String srcName = "tiger.jpg";
        File srcFile = new File(srcName);
        BufferedImage image = ImageIO.read(srcFile);
        
        int w = image.getWidth();
        int h = image.getHeight();
        int[] src = image.getRGB(0, 0, w, h, null, 0, w);
        int[] dst = null;
        
        int chunkSize = (int) (src.length * 0.25);
        int numOfChunks = (int)Math.ceil((double)src.length / chunkSize);
        for (int i = 0; i < numOfChunks; i++) {
            int start = i * chunkSize;
            int length = Math.min(src.length - start, chunkSize);
            int[] part = new int[length];
            System.arraycopy(src, start, part, 0, length);
            dst = ArrayUtils.addAll(dst, gray(part));
        }
        
        BufferedImage dstImage
                = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        dstImage.setRGB(0, 0, w, h, dst, 0, w);
        String dstName = "tiger-gray.jpg";
        File dstFile = new File(dstName);
        ImageIO.write(dstImage, "jpg", dstFile);    //Also: "TIFF"
    }

    public static int[] gray(int[] src) {
        int[] dst = new int[src.length];
        ParallelReady fb = new ParallelReady(src, 0, src.length, dst);
        ForkJoinPool pool = new ForkJoinPool();
        long startTime = System.currentTimeMillis();
        pool.invoke(fb);
        long endTime = System.currentTimeMillis();
        System.out.println("Graying took " + (endTime - startTime)
                + " milliseconds.");
        return dst;
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
