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

class Editpassword : AppCompatActivity() {
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editpassword)

        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("auth_token", null)
        val savedPassword = sharedPref.getString("password", null)
        val savedNickname = sharedPref.getString("nickname", null)
        val textViewName: TextView = findViewById(R.id.textViewName)
        val oldPassword: EditText = findViewById(R.id.old_password)
        val newPassword: EditText = findViewById(R.id.new_password)
        val confirmPassword: EditText = findViewById(R.id.confirm_password)
        val saveChanges: Button = findViewById(R.id.saveChangesBtn)
        val back: ImageButton = findViewById(R.id.imageViewBack)

        textViewName.text = savedNickname

        saveChanges.setOnClickListener {
            val inputOldPassword = oldPassword.text.toString()
            val inputNewPassword = newPassword.text.toString()
            val inputConfirmPassword = confirmPassword.text.toString()

            if (inputOldPassword.isNotEmpty() && inputNewPassword.isNotEmpty() &&
                inputConfirmPassword.isNotEmpty()) {
                if (inputOldPassword == savedPassword) {
                    if (inputConfirmPassword == inputNewPassword) {
                        changePassword(inputOldPassword, inputNewPassword)
                    } else {
                        Toast.makeText(
                            this@Editpassword,
                            "Password does not match",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@Editpassword,
                        "Old password is incorrect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        back.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
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

        retrofitBuilder.changePassword(oldPassword, newPassword).enqueue(object: Callback<Void> {
            override fun onResponse(p0: Call<Void>, p1: Response<Void>) {
                if (p1.isSuccessful) {
                    val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("password", newPassword).apply()

                    Toast.makeText(this@Editpassword, "Successfully changed password", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Editpassword, UnknownActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val error = p1.errorBody()?.string()
                    Toast.makeText(this@Editpassword, "Unable to change password: $error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<Void>, p1: Throwable) {
                Toast.makeText(this@Editpassword, "An internet error occurred", Toast.LENGTH_SHORT).show()
            }
        })
    }
}