
/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package melb.mSafe.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import melb.mSafe.R;

public abstract class GCMCommand {
    public abstract void execute(Context context, String type, String extraData);

    protected void displayNotification(Context context, String message, PendingIntent intent, int smallIconDrawableId) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(0, new NotificationCompat.Builder(context)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(smallIconDrawableId)
                        .setTicker(message)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setContentIntent(intent)
                        .setAutoCancel(true)
                        .build());
    }
}