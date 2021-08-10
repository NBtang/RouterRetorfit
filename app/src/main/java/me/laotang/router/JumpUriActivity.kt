package me.laotang.router

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class JumpUriActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let {
            Log.e("txf", it.toString())
        }
    }
}