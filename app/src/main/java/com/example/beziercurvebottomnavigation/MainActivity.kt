package com.example.beziercurvebottomnavigation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beziercurvebottomnavigation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        with(binding) {
            listOf(bottomNav, bottomNav2, bottomNav3).forEach {
                it.setNavigationItems(
                    listOf(
                        R.drawable.round_home_24,
                        R.drawable.round_notifications_24,
                        R.drawable.round_message_24
                    ) , R.drawable.round_message_24
                )
            }

            bottomNav.onItemSelection { iconId ->
                selectedTv.text = when (iconId) {
                    R.drawable.round_home_24 -> "Home"
                    R.drawable.round_notifications_24 -> "Notification"
                    R.drawable.round_message_24 -> "Messages"
                    else -> ""
                }
            }
        }
    }
}