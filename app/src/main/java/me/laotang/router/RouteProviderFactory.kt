package me.laotang.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import me.laotang.router.route.IntentProvider
import me.laotang.router.route.ActivityProvider
import me.laotang.router.route.FragmentProvider
import me.laotang.router.route.RouteProvider

class RouteProviderFactory : RouteProvider.Factory() {

    override fun get(routePath: String): RouteProvider? {
        return when (routePath) {
            "app://test.uri.activity" -> object : IntentProvider() {
                override fun adapt(context: Context): Intent {
                    val uri = Uri.parse(routePath)
                    return Intent(Intent.ACTION_VIEW, uri)
                }
            }
            "app://test.uri.activity2" -> ActivityProvider(
                JumpUriActivity::class.java
            )
            "app://test.fragment" -> FragmentProvider(
                MainFragment::class.java
            )
            else -> {
                return null
            }
        }
    }
}