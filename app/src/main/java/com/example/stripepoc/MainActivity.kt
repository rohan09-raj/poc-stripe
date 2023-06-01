package com.example.stripepoc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stripepoc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    val myFragment = BlankFragment()
    supportFragmentManager.beginTransaction()
      .add(binding.fragmentContainer.id, myFragment)
      .commit()
  }
}