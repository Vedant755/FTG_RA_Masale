package com.ftg.famasale.Screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ftg.famasale.R
import com.ftg.famasale.Utils.SharedPrefManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(sharedPrefManager.getToken().isNullOrBlank()){
            supportFragmentManager.beginTransaction().replace(R.id.container, LoginPage()).commit()
        }else{
            supportFragmentManager.beginTransaction().replace(R.id.container, DashboardPage()).commit()
        }
    }
}