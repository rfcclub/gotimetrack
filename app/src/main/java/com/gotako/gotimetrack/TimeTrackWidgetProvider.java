package com.gotako.gotimetrack;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TimeTrackWidgetProvider extends AppWidgetProvider {

    private static final String TIME_STATUS = "STATUS";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                TimeTrackWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(),
                TimeTrackService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.time_track_widget);
            // Set the text
            remoteViews.setTextViewText(R.id.textWidgetWeekTime, TimeTrackCenter.weekTime());
            remoteViews.setTextViewText(R.id.textWidgetWorkingTime, TimeTrackCenter.workingTime());
            remoteViews.setTextViewText(R.id.textWidgetGoOutTime, TimeTrackCenter.goOutTime());
            // Register an onClickListener
            Intent intent1 = new Intent(context, MainActivity.class);
            intent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.layoutWidget, pendingIntent);
            createClickForButton(Utils.IN_STATUS, context, remoteViews, allWidgetIds);
            createClickForButton(Utils.OUT_STATUS, context, remoteViews, allWidgetIds);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private void createClickForButton(String status, Context context, RemoteViews remoteViews, int[] appWidgetIds) {
        Intent intentIn = new Intent(context, TimeTrackWidgetProvider.class);
        intentIn.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intentIn.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intentIn.putExtra(TIME_STATUS, status);
        PendingIntent pendingIntentIn = PendingIntent.getBroadcast(context, 0, intentIn, PendingIntent.FLAG_UPDATE_CURRENT);
        if (status.equals(Utils.IN_STATUS)) {
            remoteViews.setOnClickPendingIntent(R.id.imageWgInButton, pendingIntentIn);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.imageWgOutButton, pendingIntentIn);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.hasExtra(TIME_STATUS)) {
            String status = intent.getStringExtra(TIME_STATUS);
            TimeTrackCenter.instance().update(status);
        }
    }

    private static class ViewCache {
        public static RemoteViews remoteViews = null;
    }
}
