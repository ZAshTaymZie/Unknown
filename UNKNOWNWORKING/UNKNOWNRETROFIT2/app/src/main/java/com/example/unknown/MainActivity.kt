package com.example.unknown

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.unknown.api.LoginResponse
import com.example.unknown.api.UnknownInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views
        val signInTextView: TextView = findViewById(R.id.sign_in_text)
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val signInButton: Button = findViewById(R.id.sign_in_button)
        val dontHaveAccountTextView: TextView = findViewById(R.id.dont_have_account_text)
        val signUpButton: TextView = findViewById(R.id.sign_up_text)
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)

        if (token != null) {
            val intent = Intent(this@MainActivity, UnknownActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set up click listener for Sign In button
        signInButton.setOnClickListener {
            // Validate input
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
            } else {
                // Show a toast message and navigate to UnknownActivity on successful sign-in
                Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()
                login(email, password) // Optional: finish this activity to prevent going back
            }
        }

        // Set up click listener for Sign Up button
        signUpButton.setOnClickListener {
            // Create an Intent to navigate to SignUpActivity
            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login(email: String, password: String) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://scorpion-workable-panda.ngrok-free.app/")
            .build()
            .create(UnknownInterface::class.java)

        retrofitBuilder.login(email, password,).enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val id = loginResponse?.data?.id
                    val token = loginResponse?.token
                    val nickname = loginResponse?.data?.nickname

                    if (token != null && id != null) {
                        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        sharedPref.edit().putString("auth_token", token).apply()
                        sharedPref.edit().putString("nickname", nickname).apply()
                        sharedPref.edit().putString("email", email).apply()
                        sharedPref.edit().putString("password", password).apply()
                        sharedPref.edit().putInt("user_id", id).apply()

                        Toast.makeText(this@MainActivity, "Login successful.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, UnknownActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, "Login failed. Token not received.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Login failed. Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
