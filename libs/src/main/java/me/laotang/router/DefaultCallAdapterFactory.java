package me.laotang.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import kotlinx.coroutines.CancellableContinuation;

final class DefaultCallAdapterFactory extends CallAdapter.Factory {
    static final CallAdapter.Factory INSTANCE = new DefaultCallAdapterFactory();

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, RouterRetrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        if (rawType == void.class || rawType == Void.class || rawType == kotlin.Unit.class) {
            return new CallAdapter<RouteInfo, Object>() {
                @Override
                public Type responseType() {
                    return Void.class;
                }

                @Override
                public Object adapt(Call<RouteInfo> call) {
                    RouteInfo routeInfo = call.execute();
                    navigation(routeInfo);
                    return null;
                }
            };
        }
        if (getRawType(returnType) == Call.class) {
            final Type responseType = Utils.getCallResponseType(returnType);
            if (responseType == RouteInfo.class) {
                return new CallAdapter<RouteInfo, Call<RouteInfo>>() {
                    @Override
                    public Type responseType() {
                        return responseType;
                    }

                    @Override
                    public Call<RouteInfo> adapt(Call<RouteInfo> call) {
                        return call;
                    }
                };
            }
        }
        if(getRawType(returnType) == SuspendCall.class){
            final Type responseType = Utils.getCallResponseType(returnType);
            if (responseType == void.class || responseType == Void.class || responseType == kotlin.Unit.class) {
                return new CallAdapter<RouteInfo, SuspendCall<Object>>() {
                    @Override
                    public Type responseType() {
                        return responseType;
                    }

                    @Override
                    public SuspendCall<Object> adapt(Call<RouteInfo> call) {
                        return new SuspendCall<Object>(){
                            @Override
                            public void execute(CancellableContinuation<Object> continuation) {
                                navigation(call.execute());
                                continuation.resume(null,null);
                            }
                        };
                    }
                };
            }
        }
        return null;
    }

    private void navigation(RouteInfo routeInfo){
        Uri uri = Uri.parse(routeInfo.getRelativeUrl());
        Intent it = new Intent(Intent.ACTION_VIEW, uri);

        int flags = routeInfo.getFlags();
        if (flags != -1) {
            it.setFlags(flags);
        }

        // Set Actions
        String action = routeInfo.getAction();
        if (!TextUtils.isEmpty(action)) {
            it.setAction(action);
        }

        Bundle extras = routeInfo.getBundle();
        if (extras != null) {
            it.putExtras(extras);
        }
        if (routeInfo.isFromFragment()) {
            int requestCode = routeInfo.getRequestCode();
            if (requestCode > 0) {
                routeInfo.getFragment().startActivityForResult(it, requestCode);
            } else {
                routeInfo.getFragment().startActivity(it);
            }
        } else {
            Context context = routeInfo.getContext();
            if (!(context instanceof Activity)) {
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(it);
            } else {
                int requestCode = routeInfo.getRequestCode();
                if (requestCode > 0) {
                    ((Activity) context).startActivityForResult(it, requestCode);
                } else {
                    context.startActivity(it);
                }
            }
        }
    }
}
