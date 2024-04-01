package se2.carcassonne.helper.resize;

import android.app.Activity;
import android.view.View;

public class FullscreenHelper {
    public static void setFullscreenAndImmersiveMode(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
