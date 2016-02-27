package fi.harism.android.intro.gl;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class GlUtils {

    public static void checkGLErrors() {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            for (int i = 3; i <= 4; ++i) {
                String stackTrace = Thread.currentThread().getStackTrace()[i].toString();
                Log.d("OpenGL", stackTrace + "  error=0x" + Integer.toHexString(error));
            }
        }
    }

    public static String loadString(Context context, String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    /**
     * Unlike the other implementation here, which uses the default "uniform"
     * treatment of t, this computation is used to calculate the same values but
     * introduces the ability to "parameterize" the t values used in the
     * calculation. This is based on Figure 3 from
     * http://www.cemyuksel.com/research/catmullrom_param/catmullrom.pdf
     *
     * @param p    An array of double values of length 4, where interpolation
     *             occurs from p1 to p2.
     * @param time An array of time measures of length 4, corresponding to each
     *             p value.
     * @param t    the actual interpolation ratio from 0 to 1 representing the
     *             position between p1 and p2 to interpolate the value.
     * @return
     */
    public static float interpolate(float[] p, float[] time, float t) {
        float L01 = p[0] * (time[1] - t) / (time[1] - time[0]) + p[1] * (t - time[0]) / (time[1] - time[0]);
        float L12 = p[1] * (time[2] - t) / (time[2] - time[1]) + p[2] * (t - time[1]) / (time[2] - time[1]);
        float L23 = p[2] * (time[3] - t) / (time[3] - time[2]) + p[3] * (t - time[2]) / (time[3] - time[2]);
        float L012 = L01 * (time[2] - t) / (time[2] - time[0]) + L12 * (t - time[0]) / (time[2] - time[0]);
        float L123 = L12 * (time[3] - t) / (time[3] - time[1]) + L23 * (t - time[1]) / (time[3] - time[1]);
        float C12 = L012 * (time[2] - t) / (time[2] - time[1]) + L123 * (t - time[1]) / (time[2] - time[1]);
        return C12;
    }
}
