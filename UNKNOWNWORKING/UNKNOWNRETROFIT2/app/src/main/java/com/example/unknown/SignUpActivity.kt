package com.example.unknown

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.unknown.api.SignUpResponse
import com.example.unknown.api.UnknownInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views
        val createEmailEditText: EditText = findViewById(R.id.create_email_edit_text)
        val createPasswordEditText: EditText = findViewById(R.id.create_password_edit_text)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirm_password_edit_text)
        val createNicknameEditText: EditText = findViewById(R.id.create_nickname_edit_text)
        val signUpButton: Button = findViewById(R.id.sign_up_button)
        val logInButton: TextView = findViewById(R.id.log_in_text)

        // Set up click listener for Sign Up button
        signUpButton.setOnClickListener {
            val email = createEmailEditText.text.toString().trim()
            val password = createPasswordEditText.text.toString().trim()
            val confirmedPass = confirmPasswordEditText.text.toString().trim()
            val nickname = createNicknameEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && nickname.isNotEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (isNicknameValid(nickname) && isPasswordValid(password)) {
                        if (confirmedPass == password) {
                            register(nickname, email, password)
                        } else {
                            Toast.makeText(applicationContext, "Passwords do not match", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Nickname or password contains invalid characters", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Invalid e-mail address", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(applicationContext, "Please fill in all fields", Toast.LENGTH_LONG).show()
            }
        }

        // Set up click listener for Log In text
        logInButton.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun isNicknameValid(nickname: String): Boolean {
        val nicknameRegex = "^[A-Za-z0-9._-]+$"
        return nickname.matches(Regex(nicknameRegex))
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex = "^[A-Za-z0-9._-]+$"
        return password.matches(Regex(passwordRegex))
    }

    private fun register(nickname: String, email: String, password: String) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://scorpion-workable-panda.ngrok-free.app/")
            .build()
            .create(UnknownInterface::class.java)

        retrofitBuilder.register(nickname, email, password).enqueue(object :
            Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful) {
                    val signUpResponse = response.body()
                    val token = signUpResponse?.token
                    val id = signUpResponse?.data?.id

                    if (token != null && id != null) {
                        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        sharedPref.edit().putString("auth_token", token).apply()
                        sharedPref.edit().putString("nickname", nickname).apply()
                        sharedPref.edit().putString("email", email).apply()
                        sharedPref.edit().putString("password", password).apply()
                        sharedPref.edit().putInt("user_id", id).apply()

                        Toast.makeText(this@SignUpActivity, "Account created successfully.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignUpActivity, UnknownActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignUpActivity, "Sign up failed. Token not received.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown error"
                    Log.d("Error", "$error")
                    // Show a more informative message based on the error response
                    when (response.code()) {
                        400 -> Toast.makeText(this@SignUpActivity, "Bad request. Check your input.", Toast.LENGTH_SHORT).show()
                        409 -> Toast.makeText(this@SignUpActivity, "Email already exists.", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@SignUpActivity, "Sign up failed: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(this@SignUpActivity, "Sign up failed. Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
