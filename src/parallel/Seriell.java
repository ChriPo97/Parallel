/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parallel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Christoph
 */
public class Seriell {

    private static int[] mSource;
    private static int mStart;
    private static int mLength;
    private static int[] mDestination;

    public static void main(String[] args) throws IOException {

        String srcName = "space.png";
        File srcFile = new File(srcName);
        BufferedImage image = ImageIO.read(srcFile);

        System.out.println("Source image: " + srcName);

        BufferedImage blurredImage = gray(image);

        String dstName = "space-gray.png";
        File dstFile = new File(dstName);
        ImageIO.write(blurredImage, "png", dstFile);

        System.out.println("Output image: " + dstName);

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

        System.out.println("Array size is " + src.length);
        
        long startTime = System.currentTimeMillis();
        computeDirectly();
        long endTime = System.currentTimeMillis();
        System.out.println("Image blur took " + (endTime - startTime) + 
                " milliseconds.");
        BufferedImage dstImage =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        dstImage.setRGB(0, 0, w, h, dst, 0, w);

        return dstImage;
        
        
    }

    protected static void computeDirectly() {
        for (int index = mStart; index < mStart + mLength; index++) {
            int pixel = mSource[index];
            
            float alpha = (pixel >> 24) & 0xff;
            float red = (pixel >> 16) & 0xff;
            float green = (pixel >> 8) & 0xff;
            float blue = pixel & 0xff;
            float gray = (float) ((red * 0.21) + (green * 0.72) + (blue * 0.07));

            // Re-assemble destination pixel.
            int dpixel = ((int)alpha << 24)
                    | (((int) gray) << 16)
                    | (((int) gray) << 8)
                    | (((int) gray));
            mDestination[index] = dpixel;
        }
    }
    
}
