package com.example.unknown

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unknown.api.UnknownInterface
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Editprofile : AppCompatActivity() {
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editprofile)

        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("auth_token", null)
        val savedNickname = sharedPref.getString("nickname", null)
        val textViewName: TextView = findViewById(R.id.textViewName)
        val editNickname: EditText = findViewById(R.id.edit_text_id)
        val saveChanges: Button = findViewById(R.id.saveChangesBtn)
        val back: ImageButton = findViewById(R.id.imageViewBack)

        textViewName.text = savedNickname

        saveChanges.setOnClickListener {
            val newNickname = editNickname.text.toString()

            if (newNickname.isNotEmpty()) {
                changeNickname(newNickname)
            }
        }

        back.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun changeNickname(nickname: String) {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofitBuilder = Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://scorpion-workable-panda.ngrok-free.app/")
            .build()
            .create(UnknownInterface::class.java)

        retrofitBuilder.changeNickname(nickname).enqueue(object: Callback<Void> {
            override fun onResponse(p0: Call<Void>, p1: Response<Void>) {
                if (p1.isSuccessful) {
                    val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("nickname", nickname).apply()

                    Toast.makeText(this@Editprofile, "Successfully changed nickname", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Editprofile, UnknownActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val error = p1.errorBody()?.string()
                    Toast.makeText(this@Editprofile, "Unable to change nickname: $error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<Void>, p1: Throwable) {
                Toast.makeText(this@Editprofile, "An internet error occurred", Toast.LENGTH_SHORT).show()
            }

        })
    }
}