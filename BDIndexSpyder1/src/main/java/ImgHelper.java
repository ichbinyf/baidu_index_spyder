import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.Level;


public class ImgHelper {

    /*
     * crop the image and paste into the correct one using css widht and margin params
     */
    public static int genImage(String path , ArrayList<Integer> wdArr ,ArrayList<Integer> mlArr   ) {
        int result = -1 ;
        int finalWidth = 0 ;
        try {
            for (Integer wd : wdArr) {
                finalWidth += wd;
            }
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            BufferedImage image = ImageIO.read(fis);
            int type = image.getType();
            BufferedImage finalImg = image.createGraphics().getDeviceConfiguration().
                    createCompatibleImage(finalWidth, 14);
            int index = 0;
            int xpos = 0;
            for (; index < mlArr.size(); index++) {
                int ml = mlArr.get(index);
                int wd = wdArr.get(index);
                if (wd == 0) {
                    continue;
                }
                //System.out.println(" cutting image ml=" + new Integer(ml).toString() + " wd="
                // + new Integer(wd).toString() + " index=" + new Integer(index).toString() + " xpos="
                // + new Integer(xpos).toString());
                BufferedImage biTemp = image.getSubimage(-1 * ml, 0, wd, 14);
                //ImageIO.write(biTemp, "png"
                //  , new File( path.replace(".png","_small2_"+new Integer(index).toString()+".png") ));
                finalImg.createGraphics().drawImage(biTemp, xpos, 0, null);
                xpos += wd;
            }
            ImageIO.write(finalImg, "png", new File(path.replace(".png", "_1.png")));
            result = extractSampleFromImg(finalImg, finalWidth);
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.INFO,e.getMessage(),ImgHelper.class);
            e.printStackTrace();
        }
        return result ;
    }

    /*
     * crop the image every 8 pixels width and ocr , return a multi-digit number
     */
    public static int extractSampleFromImg(BufferedImage finalImg  , int finalWidth ) {
        int result = 0 ;
        int sample1 , sample2 , sample3 , sample4 , sample5 ;
        for (int l = 0; l < finalWidth / 8; l++) {
            sample1 = 0 ;
            sample2 = 0 ;
            sample3 = 0 ;
            sample4 = 0 ;
            sample5 = 0 ;
            for (int k = 0; k < 8; k++) {
                Object data = finalImg.getRaster().getDataElements(k + l * 8, 3, null);
                int red = finalImg.getColorModel().getRed(data);
                if (red==255) sample1 += 1 ;

                data = finalImg.getRaster().getDataElements(k + l * 8, 7, null);
                red = finalImg.getColorModel().getRed(data);
                if (red==255) sample2 += 1 ;

                data = finalImg.getRaster().getDataElements(k + l * 8, 12, null);
                red = finalImg.getColorModel().getRed(data);
                if (red==255) sample3 += 1 ;
            }

            for (int j = 0; j < 14; j++) {
                Object data = finalImg.getRaster().getDataElements(1 + l * 8, j, null);
                int red = finalImg.getColorModel().getRed(data);
                if (red==255) sample4 += 1 ;

                data = finalImg.getRaster().getDataElements(6 + l * 8, j, null);
                red = finalImg.getColorModel().getRed(data);
                if (red==255) sample5 += 1 ;
            }
            //System.out.printf("sample: %d %d %d %d %d \n",sample1,sample2,sample3,sample4,sample5);
            //System.out.printf("sample ocr result: %d \n",ocrFromSample(sample1 , sample2 , sample3 ,sample4 , sample5) ) ;
            int digit = ocrFromSample(sample1 , sample2 , sample3 ,sample4 , sample5) ;
            if (digit != -1 ){
                result  = result * 10 + digit ;
            }
        }
        return result ;
    }

    /*
     * ocr function , using vertical and horizonal features to parse the image and return the 1-digit number
     */
    public static int ocrFromSample(int sample1 , int sample2 , int sample3 ,int sample4 , int sample5){
        if (sample1==3 && sample2== 5 && sample3== 4 && sample4== 7 && sample5== 4) return   6;
        if (sample1==6 && sample2== 1 && sample3== 1 && sample4== 1 && sample5== 3) return   7;
        if (sample1==1 && sample2== 2 && sample3== 1 && sample4== 3 && sample5== 1) return  4;
        if (sample1==2 && sample2== 2 && sample3== 2 && sample4== 6 && sample5== 6) return  0;
        if (sample1==4 && sample2== 3 && sample3== 4 && sample4== 4 && sample5== 7) return  3;
        if (sample1==4 && sample2== 2 && sample3== 6 && sample4== 5 && sample5== 4) return   2;
        if (sample1==4 && sample2== 5 && sample3== 3 && sample4== 3 && sample5== 7) return   9;
        if (sample1==4 && sample2== 4 && sample3== 4 && sample4== 7 && sample5== 7) return  8;
        if (sample1==6 && sample2== 5 && sample3== 4 && sample4== 6 && sample5== 5) return    5;
        if (sample1==1 && sample2== 1 && sample3== 5 && sample4== 0 && sample5== 1) return   1 ;
        return -1 ;
    }

}
