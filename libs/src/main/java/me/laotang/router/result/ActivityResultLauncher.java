package me.laotang.router.result;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ActivityResultLauncher {

    static final String TAG = ActivityResultLauncher.class.getSimpleName();
    ActivityResultLauncher.Lazy<ReportFragment> mReportFragment;

    public ActivityResultLauncher(@NonNull FragmentActivity activity) {
        this.mReportFragment = this.getLazySingleton(activity.getSupportFragmentManager());
    }

    public ActivityResultLauncher(@NonNull Fragment fragment) {
        this.mReportFragment = this.getLazySingleton(fragment.getChildFragmentManager());
    }

    @NonNull
    private ActivityResultLauncher.Lazy<ReportFragment> getLazySingleton(@NonNull final FragmentManager fragmentManager) {
        return new ActivityResultLauncher.Lazy<ReportFragment>() {
            private ReportFragment reportFragment;

            public synchronized ReportFragment get() {
                if (this.reportFragment == null) {
                    this.reportFragment = ActivityResultLauncher.this.getReportFragment(fragmentManager);
                }
                return this.reportFragment;
            }
        };
    }

    private ReportFragment getReportFragment(@NonNull FragmentManager fragmentManager) {
        ReportFragment reportFragment = this.findRxReportFragment(fragmentManager);
        boolean isNewInstance = reportFragment == null;
        if (isNewInstance) {
            reportFragment = new ReportFragment();
            fragmentManager.beginTransaction().add(reportFragment, TAG).commitNow();
        }
        return reportFragment;
    }

    private ReportFragment findRxReportFragment(@NonNull FragmentManager fragmentManager) {
        return (ReportFragment) fragmentManager.findFragmentByTag(TAG);
    }

    public void startActivityForResult(Intent intent, int requestCode, ActivityResultCallback resultCallback) {
        ((ReportFragment) this.mReportFragment.get()).navigation(intent, requestCode, resultCallback);
    }

    @FunctionalInterface
    public interface Lazy<V> {
        V get();
    }
}
