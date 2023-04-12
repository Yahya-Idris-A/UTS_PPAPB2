package com.example.qiblatfinder2;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class CompassWidget extends AppWidgetProvider {

    private static final String mSharedPrefFile = "com.example.qiblatfinder2";
    private static final String COUNT_KEY = "count";
    private static LocationAccess locationAccess;
    private static Location myLocation;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int WidgetId) {

        SharedPreferences prefs = context.getSharedPreferences(mSharedPrefFile, 0);
        int count = prefs.getInt(COUNT_KEY + WidgetId, 0);
        count++;

        String dateString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.compass_widget);
        views.setTextViewText(R.id.appwidget_id, String.valueOf(WidgetId));
        views.setTextViewText(R.id.appwidget_update, dateString);
        views.setTextViewText(R.id.appwidget_count, String.valueOf(count));

        Intent intent = new Intent(context, CompassWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] idArray = new int[] {WidgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, WidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.button_update, pendingIntent);

        SharedPreferences.Editor prefEditor =prefs.edit();
        prefEditor.putInt(COUNT_KEY + WidgetId, count);
        prefEditor.apply();

        locationAccess = LocationAccess.getInstance(context);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            // Get the user's location
            locationAccess.getCurrentLocation(context, new LocationAccess.LocationCallback() {
                @Override
                public void onLocationResult(Location location) {
                    myLocation = location;
                    float latitude = (float) myLocation.getLatitude();
                    float longitude = (float) myLocation.getLongitude();
                    views.setTextViewText(R.id.latitude_value, String.format(Locale.getDefault(),"%d°", (int)latitude));
                    views.setTextViewText(R.id.longitude_value, String.format(Locale.getDefault(),"%d°", (int)longitude));
                    ;
                    appWidgetManager.updateAppWidget(WidgetId, views);
                }
                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(WidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}