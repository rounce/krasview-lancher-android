package ru.krasview.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SendErrorReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, SendErrorActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("stacktrace", intent.getStringExtra("stacktrace"));
        i.putExtra("packageName", intent.getStringExtra("packageName"));
        context.startActivity(i);
    }
}
