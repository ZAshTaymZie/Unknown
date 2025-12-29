package com.example.unknown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.unknown.api.UnknownInterface
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val textViewName: TextView = findViewById(R.id.textViewName)
        val textViewEmail: TextView = findViewById(R.id.textViewEmail)
        val textViewPassword: TextView = findViewById(R.id.textViewPassword)
        val imageViewBack: ImageView = findViewById(R.id.imageViewBack)
        val editNickname: ImageButton = findViewById(R.id.iconEditNickname)
        val editPassword: ImageButton = findViewById(R.id.iconEditPassword)
        val buttonDeleteAccount: Button = findViewById(R.id.buttonDeleteAccount) // Add your delete button
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)
        val nickname = sharedPref.getString("nickname", null)
        val email = sharedPref.getString("email", null)
        val password = sharedPref.getString("password", null)

        if (nickname != null && email != null && password != null) {
            textViewName.text = nickname
            textViewEmail.text = email
            textViewPassword.text = password
        }

        imageViewBack.setOnClickListener {
            val intent = Intent(this, UnknownActivity::class.java)
            startActivity(intent)
            finish()
        }

        editNickname.setOnClickListener {
            val intent = Intent(this, Editprofile::class.java)
            startActivity(intent)
            finish()
        }

        editPassword.setOnClickListener {
            val intent = Intent(this, Editpassword::class.java)
            startActivity(intent)
            finish()
        }

        buttonDeleteAccount.setOnClickListener {
            // Handle your account deletion process here
            if (token != null) {
                deleteAccount(token)
            }
        }
    }

    private fun deleteAccount(token: String) {
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

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
            .baseUrl("https://exact-guinea-nearby.ngrok-free.app/")
            .build()
            .create(UnknownInterface::class.java)

        retrofitBuilder.deleteAccount("Bearer $token").enqueue(object: Callback<Void> {
            override fun onResponse(p0: Call<Void>, p1: Response<Void>) {
                if (p1.isSuccessful) {
                    Toast.makeText(this@Profile, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    sharedPref.edit().clear().apply()
                    val intent = Intent(this@Profile, SignUpActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Profile, "Failed to delete account", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<Void>, p1: Throwable) {
                Toast.makeText(this@Profile, "Failed to delete account", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
