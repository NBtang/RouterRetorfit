package me.laotang.router.impl

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.laotang.router.Call
import me.laotang.router.RouteInfo
import me.laotang.router.RouterRetrofit
import me.laotang.router.result.ActivityResult
import me.laotang.router.result.ActivityResultCallback
import me.laotang.router.result.ActivityResultLauncher
import java.lang.reflect.Type
import kotlin.coroutines.resumeWithException

class FlowActivityResultCallAdapter(retrofit: RouterRetrofit) : NavigationCallAdapter(retrofit) {

    override fun responseType(): Type {
        return ActivityResultCallback::class.java
    }

    override fun adapt(call: Call<RouteInfo>): Any {
        val routeInfo = call.execute()
        val routeProvider = retrofit.routeProvider(routeInfo.relativeUrl)
        val intent = routeProvider.adapt(routeInfo)
        if (intent != null && intent is Intent) {
            val launcher = if (routeInfo.isFromFragment) {
                ActivityResultLauncher(routeInfo.fragment)
            } else {
                ActivityResultLauncher(routeInfo.context as FragmentActivity)
            }
            return flow {
                emit(startActivityForResult(launcher, intent, routeInfo.requestCode))
            }

        }
        throw RuntimeException("not found intent by routePath:" + routeInfo.relativeUrl)
    }

    private suspend fun startActivityForResult(
        launcher: ActivityResultLauncher,
        intent: Intent,
        requestCode: Int
    ): ActivityResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                launcher.startActivityForResult(intent, requestCode) {
                    continuation.resumeWith(Result.success(it))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resumeWithException(e)
            }
        }
    }
}