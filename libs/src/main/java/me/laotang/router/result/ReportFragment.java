package me.laotang.router.result;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.atomic.AtomicInteger;

public class ReportFragment extends Fragment {

    public static AtomicInteger count = new AtomicInteger(10);

    private SparseArray<ActivityResultCallback> mActivityResultCallbacks = new SparseArray<>();

    public ReportFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    void navigation(Intent intent, int requestCode, ActivityResultCallback resultCallback) {
        int code = requestCode > 0 ? requestCode : count.getAndIncrement();
        mActivityResultCallbacks.put(code, resultCallback);
        startActivityForResult(intent, code);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultCallback callback = mActivityResultCallbacks.get(requestCode);
        if (callback != null) {
            mActivityResultCallbacks.remove(requestCode);
            callback.onActivityResult(new ActivityResult(resultCode, data));
        }
    }
}
