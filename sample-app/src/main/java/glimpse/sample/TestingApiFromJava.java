package glimpse.sample;

import android.app.Application;
import android.graphics.Bitmap;
import glimpse.core.BitmapUtils;
import glimpse.core.Glimpse;
import glimpse.glide.GlimpseTransformation;
import kotlin.Pair;

public class TestingApiFromJava {

    private static void howDoesItFeel() {
        Application app = null;
        Bitmap bitmap = null;

        Glimpse.init(app);

        Bitmap croppedBitmap = BitmapUtils.crop(bitmap, 0, 0, 0, 0, bitmap);
        croppedBitmap = BitmapUtils.crop(bitmap, 0, 0, 0, 0);

        Pair<Float, Float> focalPoints = BitmapUtils.findCenter(bitmap, 0, 0);
        focalPoints = BitmapUtils.findCenter(bitmap);

        Bitmap heatMap = BitmapUtils.debugHeatMap(bitmap, 0, 0);
        heatMap = BitmapUtils.debugHeatMap(bitmap);

        new GlimpseTransformation();
    }

}
