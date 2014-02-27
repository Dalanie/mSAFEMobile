/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package melb.mSafe.utilities;

import melb.mSafe.ui.MainActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {


    /**
     * Expression for validating the url or ip-adress (ip v4/v6, dez/hex
     * possible)
     */
    public static final String UrlAndIPRegEx = "^((([hH][tT][tT][pP][sS]?|[fF][tT][pP])\\:\\/\\/)?([\\w\\.\\-]+(\\:[\\w\\.\\&%\\$\\-]+)*@)?"
            + "((([^\\s\\(\\)\\<\\>\\\\\\\"\\.\\[\\]\\,@;:]+)(\\.[^\\s\\(\\)\\<\\>\\\\\\\"\\.\\[\\]\\,@;:]+)*"
            + "(\\.[a-zA-Z]{2,4}))|((([01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d{1,2}|2[0-4]\\d|25[0-5])))"
            + "(\\b\\:(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0)\\b)?((\\/[^\\/]"
            + "[\\w\\.\\,\\?\\'\\\\\\/\\+&%\\$#\\=~_\\-@]*)*[^\\.\\,\\?\\\"\\'\\(\\)\\[\\]!;<>{}\\s\\x7F-\\xFF])?)$";

    /**
     * Base URL of the Server
     */
    public static final String SERVER_URL = "http://dalanie.de:9079";
    //public static final String SERVER_URL = "http://128.250.248.201:9079";
    //public static final String SERVER_URL = "http://192.168.0.19:9079";

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID = "78704287249";// "lyrical-caster-355";

    /**
     * Tag used on log messages.
     */
    public static final String TAG = "mSafe";

    /**
     * Intent used to display a message in the screen.
     */
    public static final String DISPLAY_MESSAGE_ACTION = "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    public static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by the
     * UI and the background service.
     *
     * @param context
     *            application's context.
     * @param message
     *            message to be displayed.
     */
    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    /**
     * Shows a Dialog with a title, a message, a EditText and two Buttons for OK
     * and Cancel. The user can't click OK when the EditText is empty.
     *
     * @param context
     *            The Context of the calling Activity
     * @param title
     *            Title of the Dialog
     * @param message
     *            Message of the Dialog
     * @param inputEditText
     *            The EditText used for this Dialog. You can modify it for
     *            example by setting the input type before passing it to this
     *            method. You can also read the text from the calling method.
     * @param onOkClickListener
     *            The Listener for clicking on the OK button.
     */
    public static void showDialog(Context context, String title,
                                  String message, EditText inputEditText,
                                  DialogInterface.OnClickListener onOkClickListener) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setView(inputEditText);

		/*
		 * OK button
		 */
        alert.setPositiveButton(context.getString(android.R.string.ok),
                onOkClickListener);
		/*
		 * Cancel button
		 */
        alert.setNegativeButton(context.getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
						/*
						 * Canceled.
						 */
                    }
                });

        final AlertDialog dialog = alert.show();
        if (dialog != null){
		/*
		 * Disable ok button.
		 */
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		/*
		 * Dis- and enable , dependent on the text entered
		 */
            inputEditText.addTextChangedListener(new TextWatcher() {
                /**
                 * Enable OK button if text entered, disable otherwise.
                 */
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(
                            !s.toString().equals("") && isValideUrl(s.toString()));
                }

                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    /**
     * Validates if the server-adress is correct (ipv4 || ipv6 || url)
     *
     * @param serverUrl
     * @return
     */
    public static boolean isValideUrl(String serverUrl) {
        return serverUrl.matches(UrlAndIPRegEx);
    }


    public static void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(String.format("Please set the %1$s constant and recompile the app.",
                    name));
        }
    }
}
