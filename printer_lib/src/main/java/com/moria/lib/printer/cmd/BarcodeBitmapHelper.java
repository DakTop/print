package com.moria.lib.printer.cmd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * Author  moria
 * Date    2020/9/11
 * Time    17:08
 */
public class BarcodeBitmapHelper {

    public static Bitmap createBarcode(String contents, int desiredWidth, int desiredHeight) {
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;
        Bitmap barcodeBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth, desiredHeight);
        return barcodeBitmap;
    }


    private static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int desiredWidth, int desiredHeight) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(contents, format, desiredWidth, desiredHeight, null);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    protected static Bitmap mixtureBitmap(Bitmap bCBitmap) {
        //创建一个图层，然后在这个图层上绘制bCBitmap
        Bitmap bitmap = Bitmap.createBitmap(bCBitmap.getWidth(), bCBitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bCBitmap, 0, 0, null);
        canvas.save();
        canvas.restore();
        return bitmap;

    }


}
