package me.laotang.router

import android.content.Context
import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.Flow
import me.laotang.router.annotation.Action
import me.laotang.router.annotation.RequestCode
import me.laotang.router.annotation.Url
import me.laotang.router.result.ActivityResult

interface RouterService {
    fun startTestUri(
        context: Context,
        @Url url: String,
        @RequestCode requestCode: Int,
        @Action action: String
    )

    fun startTestUri(
        fragment: Fragment,
        @Url url: String,
        @RequestCode requestCode: Int,
        @Action action: String
    )

//    suspend fun startTestUriSuspend(context: Context, @Url url: String)

    suspend fun startTestUriSuspend(context: Context, @Url url: String): ActivityResult

    fun startTestUriFlow(context: Context, @Url url: String): Flow<ActivityResult>

    fun findFragment(@Url url: String):Fragment

    companion object{
        private lateinit var applicationContext: Context

        private val INSTANCE: RouterService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            val routerRetrofit = RouterRetrofit.Builder()
                .addRouteProviderFactory(RouteProviderFactory())
                .build(applicationContext)
            routerRetrofit.create(RouterService::class.java)
        }

        fun getDefault(): RouterService {
            return INSTANCE
        }

        fun init(applicationContext: Context) {
            this.applicationContext = applicationContext
        }
    }
}