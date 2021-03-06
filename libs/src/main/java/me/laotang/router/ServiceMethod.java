package me.laotang.router;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;


import androidx.fragment.app.Fragment;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import kotlin.coroutines.Continuation;
import me.laotang.router.annotation.Action;
import me.laotang.router.annotation.Bundles;
import me.laotang.router.annotation.Extra;
import me.laotang.router.annotation.Flags;
import me.laotang.router.annotation.Go;
import me.laotang.router.annotation.GreenChannel;
import me.laotang.router.annotation.IsGreenChannel;
import me.laotang.router.annotation.RequestCode;
import me.laotang.router.annotation.Url;

final class ServiceMethod<R, T> {
    private final RawCall.Factory callFactory;
    private final CallAdapter<R, T> callAdapter;
    private final ParameterHandler<?>[] parameterHandlers;
    private final String relativeUrl;
    private final int mFlags;
    private final boolean isGreenChannel;
    private final boolean isKotlinSuspendFunction;

    ServiceMethod(Builder<R, T> builder) {
        this.callFactory = builder.retrofit.callFactory();
        this.callAdapter = builder.callAdapter;
        this.parameterHandlers = builder.parameterHandlers;
        this.relativeUrl = builder.relativeUrl;
        this.mFlags = builder.mFlags;
        this.isGreenChannel = builder.isGreenChannel;
        this.isKotlinSuspendFunction = builder.isKotlinSuspendFunction;
    }

    RawCall toCall(Object... args) {
        RequestBuilder requestBuilder = new RequestBuilder(relativeUrl, mFlags, isGreenChannel);
        ParameterHandler<Object>[] handlers = (ParameterHandler<Object>[]) parameterHandlers;
        int argumentCount = args != null ? args.length : 0;
        if (argumentCount != handlers.length) {
            throw new IllegalArgumentException("Argument count (" + argumentCount
                    + ") doesn't match expected count (" + handlers.length + ")");
        }
        for (int p = 0; p < argumentCount; p++) {
            if (handlers[p] != null) {
                handlers[p].apply(requestBuilder, args[p]);
            }
        }
        return callFactory.newCall(requestBuilder.build());
    }

    Object adapt(Call<R> call, Object[] args) {
        if(isKotlinSuspendFunction){
            //????????? ????????????
            Continuation<T> continuation = (Continuation<T>) args[args.length - 1];
            try {
                SuspendCall rawCall = (SuspendCall) callAdapter.adapt(call);
                return  KotlinExtensionsKt.await(rawCall,continuation);
            } catch (Exception e) {
                return  KotlinExtensionsKt.suspendAndThrow(e, continuation);
            }
        }
        return callAdapter.adapt(call);
    }

    static final class Builder<T, R> {
        final RouterRetrofit retrofit;
        final Method method;
        final Annotation[] methodAnnotations;
        final Annotation[][] parameterAnnotationsArray;
        final Type[] parameterTypes;

        boolean gotUrl;
        String relativeUrl;
        int mFlags = -1;
        boolean isGreenChannel;

        ParameterHandler<?>[] parameterHandlers;
        Type responseType;
        CallAdapter<T, R> callAdapter;
        boolean isKotlinSuspendFunction;

        Builder(RouterRetrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            this.parameterTypes = method.getGenericParameterTypes();
            this.parameterAnnotationsArray = method.getParameterAnnotations();
        }

        public ServiceMethod build() {

            //??????????????????
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            //??????????????????????????????
            int parameterCount = parameterAnnotationsArray.length;
            parameterHandlers = new ParameterHandler<?>[parameterCount];

            for (int p = 0, lastParameter = parameterCount - 1; p < parameterCount; p++) {
                Type parameterType = parameterTypes[p];
                if (Utils.getRawType(parameterType) != Continuation.class) {
                    if (Utils.hasUnresolvableType(parameterType)) {
                        throw parameterError(p, "Parameter type must not include a type variable or wildcard: %s",
                                parameterType);
                    }
                }
                Annotation[] parameterAnnotations = parameterAnnotationsArray[p];
                if (parameterAnnotations == null) {
                    throw parameterError(p, "No Retrofit annotation found.");
                }

                parameterHandlers[p] = parseParameter(p, parameterType, parameterAnnotations, p == lastParameter);
            }

            if (relativeUrl == null && !gotUrl) {
                throw methodError("Missing either URL or @Url parameter.");
            }

            Type returnType = method.getGenericReturnType();
            Annotation[] annotations = method.getAnnotations();
            Type adapterType = returnType;

            //?????????????????????????????????SuspendFunction
            if (isKotlinSuspendFunction) {
                Type[] parameterTypes = method.getGenericParameterTypes();
                Type responseType =
                        Utils.getParameterLowerBound(
                                0, (ParameterizedType) parameterTypes[parameterTypes.length - 1]);
                //??????adapterType
                adapterType = new Utils.ParameterizedTypeImpl(null, SuspendCall.class, responseType);
            }

            callAdapter = createCallAdapter(adapterType, annotations);
            responseType = this.callAdapter.responseType();

            return new ServiceMethod<>(this);
        }

        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof Go) {
                parseRelativeUrl(((Go) annotation).value());
            } else if (annotation instanceof Flags) {
                parseIntentFlags(((Flags) annotation).value());
            } else if (annotation instanceof GreenChannel) {
                parseGreenChannel(((GreenChannel) annotation).value());
            }
        }

        private void parseRelativeUrl(String value) {
            relativeUrl = value;
        }

        private void parseIntentFlags(int flags) {
            this.mFlags |= flags;
        }

        private void parseGreenChannel(boolean isGreenChannel) {
            this.isGreenChannel = isGreenChannel;
        }

        private ParameterHandler<?> parseParameter(
                int p, Type parameterType, Annotation[] annotations, boolean allowContinuation) {
            ParameterHandler<?> result = null;
            for (Annotation annotation : annotations) {
                ParameterHandler<?> annotationAction = parseParameterAnnotation(
                        p, parameterType, annotations, annotation);
                if (annotationAction == null) {
                    continue;
                }
                if (result != null) {
                    throw parameterError(p, "Multiple RouterRetrofit annotations found, only one allowed.");
                }
                result = annotationAction;
            }
            if (result == null) {
                if (allowContinuation) {
                    try {
                        if (Utils.getRawType(parameterType) == Continuation.class) {
                            isKotlinSuspendFunction = true;
                            return null;
                        }
                    } catch (NoClassDefFoundError ignored) {
                    }
                } else {
                    if (Utils.getRawType(parameterType) == Context.class) {
                        return new ParameterHandler.ContextParameter();
                    } else if (Utils.getRawType(parameterType) == Fragment.class) {
                        return new ParameterHandler.FragmentParameter();
                    }
                }
                throw parameterError(p, "No RouterRetrofit annotation found.");
            }
            return result;
        }

        private ParameterHandler<?> parseParameterAnnotation(
                int p, Type type, Annotation[] annotations, Annotation annotation) {

            if (annotation instanceof Url) {
                if (gotUrl) {
                    throw parameterError(p, "Multiple @Url method annotations found.");
                }
                if (relativeUrl != null) {
                    throw parameterError(p, "@Url cannot be used with Go annotations");
                }
                gotUrl = true;
                if (type == String.class) {
                    return new ParameterHandler.RelativeUrl();
                } else {
                    throw parameterError(p,
                            "@Url must be String");
                }
            } else if (annotation instanceof RequestCode) {
                if (type == Integer.class || type == int.class) {
                    return new ParameterHandler.RequestCode();
                } else {
                    throw parameterError(p,
                            "@RequestCode must be int");
                }
            } else if (annotation instanceof Action) {
                if (type == String.class) {
                    return new ParameterHandler.Action();
                } else {
                    throw parameterError(p,
                            "@Action must be String");
                }
            } else if (annotation instanceof Extra) {
                Extra extra = (Extra) annotation;
                String name = extra.value();
                Class<?> rawParameterType = Utils.getRawType(type);
                if (Iterable.class.isAssignableFrom(rawParameterType)) {
                    if (!(type instanceof ParameterizedType)) {
                        throw parameterError(p, rawParameterType.getSimpleName()
                                + " must include generic type (e.g., "
                                + rawParameterType.getSimpleName()
                                + "<String>)");
                    }
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                    return new ParameterHandler.ExtraIterable(name, iterableType);
                } else if (rawParameterType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                    return new ParameterHandler.ExtraArray(name, arrayComponentType);
                } else if (Parcelable.class.isAssignableFrom(rawParameterType)) {
                    return new ParameterHandler.ExtraParcelable(name);
                } else if (Serializable.class.isAssignableFrom(rawParameterType)) {
                    return new ParameterHandler.ExtraSerializable(name);
                } else {
                    Class<?> boxIfPrimitive = boxIfPrimitive(rawParameterType);
                    return new ParameterHandler.Extra(name, boxIfPrimitive);
                }
            } else if (annotation instanceof Bundles) {
                if (type == Bundle.class) {
                    return new ParameterHandler.Bundles();
                } else {
                    throw parameterError(p,
                            "@From must be Bundle");
                }
            } else if (annotation instanceof IsGreenChannel) {
                Class<?> rawParameterType = Utils.getRawType(type);
                if (boxIfPrimitive(rawParameterType) == Boolean.class) {
                    return new ParameterHandler.IsGreenChannel();
                } else {
                    throw parameterError(p,
                            "@IsGreenChannel must be Boolean");
                }
            }
            return null; // Not a RouterRetrofit annotation.
        }

        private CallAdapter<T, R> createCallAdapter(Type returnType, Annotation[] annotations) {
//            Type returnType = method.getGenericReturnType();
            if (Utils.hasUnresolvableType(returnType)) {
                throw methodError(
                        "Method return type must not include a type variable or wildcard: %s", returnType);
            }
//            Annotation[] annotations = method.getAnnotations();
            try {
                //noinspection unchecked
                return (CallAdapter<T, R>) retrofit.callAdapter(returnType, annotations);
            } catch (RuntimeException e) { // Wide exception range because factories are user code.
                throw methodError(e, "Unable to create call adapter for %s", returnType);
            }
        }

        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            message = String.format(message, args);
            return new IllegalArgumentException(message
                    + "\n    for method "
                    + method.getDeclaringClass().getSimpleName()
                    + "."
                    + method.getName(), cause);
        }

        private RuntimeException parameterError(
                Throwable cause, int p, String message, Object... args) {
            return methodError(cause, message + " (parameter #" + (p + 1) + ")", args);
        }

        private RuntimeException parameterError(int p, String message, Object... args) {
            return methodError(message + " (parameter #" + (p + 1) + ")", args);
        }
    }

    static Class<?> boxIfPrimitive(Class<?> type) {
        if (boolean.class == type) return Boolean.class;
        if (byte.class == type) return Byte.class;
        if (char.class == type) return Character.class;
        if (double.class == type) return Double.class;
        if (float.class == type) return Float.class;
        if (int.class == type) return Integer.class;
        if (long.class == type) return Long.class;
        if (short.class == type) return Short.class;
        return type;
    }
}
