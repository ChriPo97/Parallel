package parallel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import javax.imageio.ImageIO;

/**
 *
 * @author Christoph
 */
public class Parallel extends RecursiveAction {

    private int[] mSource;
    private int mStart;
    private int mLength;
    private int[] mDestination;

    public Parallel(int[] src, int start, int length, int[] dst) {
        mSource = src;
        mStart = start;
        mLength = length;
        mDestination = dst;
    }

    protected void computeDirectly() {
        for (int index = mStart; index < mStart + mLength; index++) {
            int pixel = mSource[index];
            float alpha = (pixel >> 24) & 0xff;
            float red = (pixel >> 16) & 0xff;
            float green = (pixel >> 8) & 0xff;
            float blue = pixel & 0xff;
            float gray = (float) ((red * 0.21) + (green * 0.72) + (blue * 0.07));
            int dpixel = ((int)alpha << 24)
                    | (((int) gray) << 16)
                    | (((int) gray) << 8)
                    | (((int) gray));
            mDestination[index] = dpixel;
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
        invokeAll(new Parallel(mSource, mStart, split, mDestination),
                new Parallel(mSource, mStart + split, mLength - split, 
                mDestination));
    }

    public static void main(String[] args) throws Exception {
        String srcName = "space.png";
        File srcFile = new File(srcName);
        BufferedImage image = ImageIO.read(srcFile);  
        BufferedImage blurredImage = gray(image);
        String dstName = "space-gray.png";
        File dstFile = new File(dstName);
        ImageIO.write(blurredImage, "png", dstFile);
    }

    public static BufferedImage gray(BufferedImage srcImage) {
        int w = srcImage.getWidth();
        int h = srcImage.getHeight();
        int[] src = srcImage.getRGB(0, 0, w, h, null, 0, w);
        int[] dst = new int[src.length];
        Parallel fb = new Parallel(src, 0, src.length, dst);
        ForkJoinPool pool = new ForkJoinPool();
        long startTime = System.currentTimeMillis();
        pool.invoke(fb);
        long endTime = System.currentTimeMillis();
        System.out.println("Image blur took " + (endTime - startTime) + 
                " milliseconds.");
        BufferedImage dstImage =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        dstImage.setRGB(0, 0, w, h, dst, 0, w);
        return dstImage;
    }
}
