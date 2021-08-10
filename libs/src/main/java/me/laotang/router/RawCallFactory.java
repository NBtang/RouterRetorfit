package me.laotang.router;

import android.content.Context;

final class RawCallFactory implements RawCall.Factory {
    Context context;

    RawCallFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public RawCall newCall(RouteRequest routeRequest) {
        return new RawCallImpl(routeRequest, this.context);
    }

    static class RawCallImpl implements RawCall {
        Context applicationContext;
        RouteRequest routeRequest;

        RawCallImpl(RouteRequest routeRequest, Context context) {
            this.routeRequest = routeRequest;
            this.applicationContext = context.getApplicationContext();
        }

        @Override
        public RouteInfo execute() {
            RouteInfo routerInfo = new RouteInfo();
            routerInfo.setRelativeUrl(routeRequest.getRelativeUrl());
            routerInfo.setFlags(routeRequest.getFlags());
            routerInfo.setBundle(routeRequest.getExtras());
            routerInfo.setGreenChannel(routeRequest.isGreenChannel());
            routerInfo.setRequestCode(routeRequest.getRequestCode());
            routerInfo.setAction(routeRequest.getAction());
            if (routeRequest.fromFragment) {
                routerInfo.setFragment(routeRequest.fragmentWeakReference.get());
            } else {
                Context context = routeRequest.contextWeakReference.get();
                if (context == null) {
                    context = this.applicationContext;
                }
                routerInfo.setContext(context);
            }
            return routerInfo;
        }
    }
}
