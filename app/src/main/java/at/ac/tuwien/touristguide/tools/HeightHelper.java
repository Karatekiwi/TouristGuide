package at.ac.tuwien.touristguide.tools;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.Window;


/**
 * @author Manu Weilharter
 * Use to calculate perfect height for EditText View
 */
public class HeightHelper {

    private int height;
    private Activity activity;

    private int[] static_heights = {10, 20, 170, 10, 10, 10, 40, 10, 100};

    public HeightHelper(Activity activity) {
        this.activity = activity;

        int statics = 0;
        for (int static_height : static_heights) {
            statics += static_height;
        }

        height = getDisplayHeight() - statics - getStatusBarHeight() - getTitleBarHeight();
    }

    private int getDisplayHeight() {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.y;
    }


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public int getTitleBarHeight() {
        Rect rect = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return contentViewTop - statusBarHeight;
    }

    public int getHeight() {
        return height;
    }

}
