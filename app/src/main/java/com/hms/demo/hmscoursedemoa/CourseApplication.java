package com.hms.demo.hmscoursedemoa;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.huawei.hms.ads.HwAds;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.type.ReportPolicy;

import java.util.HashSet;
import java.util.Set;

public class CourseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HiAnalyticsInstance instance = HiAnalytics.getInstance(this);

        // Report an event upon app switching to the background.
        ReportPolicy moveBackgroundPolicy = ReportPolicy.ON_MOVE_BACKGROUND_POLICY;
        // Report an event at the specified interval.
        ReportPolicy scheduledTimePolicy = ReportPolicy.ON_SCHEDULED_TIME_POLICY;
        // Set the event reporting interval to 600 seconds.
        scheduledTimePolicy.setThreshold(600);
        Set<ReportPolicy> reportPolicies = new HashSet<>();
        // Add the ON_APP_LAUNCH_POLICY and ON_SCHEDULED_TIME_POLICY policies.
        reportPolicies.add(scheduledTimePolicy);
        reportPolicies.add(moveBackgroundPolicy);
        // Set the ON_MOVE_BACKGROUND_POLICY and ON_CACHE_THRESHOLD_POLICY policies.
        instance.setReportPolicies(reportPolicies);

        //Initializing Huawei Ads
        HwAds.init(this);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

    }
}
