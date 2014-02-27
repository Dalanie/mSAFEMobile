package melb.mSafe.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewPropertyAnimator;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import melb.mSafe.R;
import melb.mSafe.gcm.PushUtilities;

/**
 * Created by Daniel on 30.01.14.
 */
public class DialogHelper {

    public static void setServerUrlDialog(final Activity context){
        final View view =  LayoutInflater.from(context).inflate(R.layout.check_valid_url_dialog, null);
        final ViewGroup inputLayout = (ViewGroup) view.findViewById(R.id.inputLayout);
        final View progress = view.findViewById(R.id.m_progress);
        final Button positive = (Button) inputLayout.findViewById(R.id.positive);
        final Button negative = (Button) inputLayout.findViewById(R.id.negative);
        final EditText input = (EditText) inputLayout.findViewById(R.id.input);
        input.setText(PrefUtilities.getInstance().getServerURL());

        final AlertDialog dialog = new AlertDialog.Builder(context).setView(view).setTitle(R.string.set_server_url_header).create();

        View.OnClickListener dialogClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.positive:
                        String url = input.getText().toString();
                        if (CommonUtilities.isValideUrl(url)){
                            crossFade(progress, inputLayout, context);
                            PrefUtilities.getInstance().setServerUrl(url);
                            PushUtilities.getInstance().relog();
                            dialog.dismiss();
                        }else{
                            invalidUrl(false);
                        }
                        break;
                    case R.id.negative:
                        dialog.dismiss();
                        break;
                }
            }

            private void serverError(final boolean hideProgressBar) {
                Animation shake = AnimationUtils.loadAnimation(context,
                        R.anim.shake);
                view.startAnimation(shake);
                shake.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (hideProgressBar){
                            crossFade(inputLayout, progress, context);
                        }
                        Crouton.makeText(context, "Could not connect to server", Style.ALERT, inputLayout).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }

            private void invalidUrl(final boolean hideProgressBar) {
                Animation shake = AnimationUtils.loadAnimation(context,
                        R.anim.shake);
                view.startAnimation(shake);
                shake.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (hideProgressBar){
                            crossFade(inputLayout, progress, context);
                        }
                        Crouton.makeText(context, context.getString(R.string.not_valid_format), Style.ALERT, inputLayout).show();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        };

        positive.setOnClickListener(dialogClickListener);
        negative.setOnClickListener(dialogClickListener);

        dialog.show();
    }


    private static void crossFade(final View firstView, final View secondView, Context context) {
        int mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        ViewPropertyAnimator.animate(firstView).alpha(0f);
        firstView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        ViewPropertyAnimator.animate(firstView).alpha(1f).setDuration(mShortAnimationDuration).setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        ViewPropertyAnimator.animate(secondView).alpha(0f).setDuration(mShortAnimationDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                secondView.setVisibility(View.GONE);
            }
        });

    }
}
