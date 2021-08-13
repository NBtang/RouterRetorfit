package me.laotang.router

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RouterService.init(applicationContext)

        findViewById<View>(R.id.tv_test).setOnClickListener {
            lifecycleScope.launch {
//                val result = RouterService.getDefault().startTestUriSuspend(this@MainActivity,"app://test.uri.activity2")
                RouterService.getDefault().startTestUriFlow(this@MainActivity,"app://test.uri.activity2")
                    .collect {
                        it
                    }
            }
        }

        val fragment = RouterService.getDefault().findFragment("app://test.fragment")

        supportFragmentManager
            .beginTransaction()
            .add(fragment, "MainFragment")
            .commit()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}