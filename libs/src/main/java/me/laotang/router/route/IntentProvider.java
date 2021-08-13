package me.laotang.router.route;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import me.laotang.router.RouteInfo;

public abstract class IntentProvider implements RouteProvider {

    @Override
    public Object adapt(RouteInfo routeInfo) {
        Context context;
        if (routeInfo.isFromFragment()) {
            context = routeInfo.getFragment().requireContext();
        } else {
            context = routeInfo.getContext();
        }
        Intent intent = adapt(context);
        int flags = routeInfo.getFlags();
        if (flags != -1) {
            intent.setFlags(flags);
        }
        String action = routeInfo.getAction();
        if (!TextUtils.isEmpty(action)) {
            intent.setAction(action);
        }

        Bundle extras = routeInfo.getBundle();
        if (extras != null) {
            intent.putExtras(extras);
        }
        return intent;
    }

    public abstract Intent adapt(Context context);
}
