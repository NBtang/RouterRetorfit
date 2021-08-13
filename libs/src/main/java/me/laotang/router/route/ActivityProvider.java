package me.laotang.router.route;

import android.content.Context;
import android.content.Intent;

public class ActivityProvider extends IntentProvider {

    private final Class<?> clazz;

    public ActivityProvider(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Intent adapt(Context context) {
        return new Intent(context, clazz);
    }

}
