package itamar.com.wallasoundcloud.global;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import itamar.com.wallasoundcloud.R;

public class Utils {

    public static final String TYPE_RAW = "raw";
    public static final String TYPE_MP3 = "mp3";
    public static final String TYPE_WAV = "wav";
    public static final int MAX_LOADING_SONG = 5;

    public static final int PLACE_HOLDER_IMAGE_SONG = R.drawable.image_placeholder;
    private static final String TAG = "itamar.com.wallasoundcloud";

    public static class ActivityUtils {

        public static boolean isPortraitScreen(Context context) {
            return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        }

        /**
         * @param message  The message you want show
         * @param duration SnackBar duration
         */
        public static void makeAndShowSnackBar(Activity activity, String message, @Snackbar.Duration int duration) {
            Snackbar.make(activity.findViewById(android.R.id.content), message, duration).show();
        }


        /**
         * This snackBar show until you dismiss it
         *
         * @param message The message you want show
         * @return The snackBar you need dismiss
         */
        public static Snackbar makeAndShowDialogProgress(Activity activity, String message) {
            Snackbar snackDialogProgress = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
            TextView textViewSnackBar = snackDialogProgress.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            //textViewSnackBar.setTextSize(22);
            ViewGroup contentLay = (ViewGroup) textViewSnackBar.getParent();
            contentLay.setPadding(16, 16, 16, 16);
            ProgressBar item = new ProgressBar(activity);
            contentLay.addView(item);
            snackDialogProgress.show();
            return snackDialogProgress;
        }

        public static void hideKeyboard(Activity activity) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


        public static void hideActionBar(AppCompatActivity activity) {
            android.app.ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            } else {
                ActionBar compactActionBar = activity.getSupportActionBar();
                if (compactActionBar != null) {
                    compactActionBar.hide();
                }
            }

        }
    }

    public static class AnimationUtils {

        /**
         * @param view         the view make animation
         * @param interpolator if null make default
         * @param duration     if null default is 1000
         */
        public static void setAnimationClicked(View view, BounceInterpolator interpolator, Integer duration) {
            if (interpolator == null) {
                interpolator = new BounceInterpolator(0.2, 15);
            }
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(view.getContext(), R.anim.bounce);
            anim.setInterpolator(interpolator);
            anim.setDuration(duration);
            view.startAnimation(anim);
        }

    }

    public static class SharedPreferencesUtils {
        public static final String TYPE_LAYOUT_MANAGER_GRID = "GRID";
        public static final String TYPE_LAYOUT_MANAGER_LINEAR_VERTICAL = "LINEAR_VERTICAL";
        static final String KEY_FIRST_VISIT = "KEY_FIRST_VISIT";
        private static final String KEY_TYPE_LAYOUT_MANAGER = "KEY_TYPE_LAYOUT_MANAGER";

        private static SharedPreferences getSharedPreferences(Context context) {
            return context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        }

        public static void saveCurrentLayoutType(Context context, String typeLayoutManger) {
            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putString(KEY_TYPE_LAYOUT_MANAGER, typeLayoutManger).apply();
        }

        public static String loadCurrentLayoutType(Context context) {
            return getSharedPreferences(context).getString(KEY_TYPE_LAYOUT_MANAGER, TYPE_LAYOUT_MANAGER_LINEAR_VERTICAL);
        }


        public static boolean isFirstVisit(Context context) {
            return getSharedPreferences(context).getBoolean(KEY_FIRST_VISIT, true);
        }

        @SuppressLint("ApplySharedPref")
        public static void setFirstVisit(Context context, boolean isFirstVisit) {
            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putBoolean(KEY_FIRST_VISIT, isFirstVisit);
            editor.commit();
        }
    }

    public static class NetworkingUtils {
        /**
         * @return If there is Internet (not necessarily fast and good)
         */
        public static boolean isOnline(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }


}
