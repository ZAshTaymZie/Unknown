package com.example.unknown

import com.example.unknown.FeedbackFormActivity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.unknown.api.Comment
import com.example.unknown.api.CommentResponse
import com.example.unknown.api.Post
import com.example.unknown.api.UnknownInterface
import com.google.android.material.navigation.NavigationView
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UnknownActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var editTextPost: EditText
    private lateinit var buttonPost: Button
    private lateinit var linearLayoutPosts: LinearLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var hamburgerButton: ImageView
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private var user_id: Int = 0
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unknown)

        // Ensure the IDs match your layout file
        swipeRefresh = findViewById(R.id.swipeRefresh)
        editTextPost = findViewById(R.id.EditTextPost)
        buttonPost = findViewById(R.id.ButtonPost)
        linearLayoutPosts = findViewById(R.id.linearLayoutComments)
        drawerLayout = findViewById(R.id.drawerLayout)
        hamburgerButton = findViewById(R.id.hamburgerButton)
        navigationView = findViewById(R.id.navigationView)
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        user_id = sharedPreferences.getInt("user_id", 0)
        token = sharedPreferences.getString("auth_token", null)

        token?.let {
            getPosts(it) // Pass the token to getPosts
        }

        swipeRefresh.setOnRefreshListener {
            linearLayoutPosts.removeAllViews()

            token?.let { getPosts(it) }
            swipeRefresh.isRefreshing = false
        }

        buttonPost.setOnClickListener {
            val postContent = editTextPost.text.toString().trim()

            if (postContent.isNotEmpty()) {
                token?.let { it1 -> createPost(postContent, it1) }
                editTextPost.text.clear() // Clear input after posting
            }
        }

        hamburgerButton.setOnClickListener {
            Log.d("UnknownActivity", "Hamburger button clicked")
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        navigationView.setNavigationItemSelectedListener { it ->
            when (it.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                    sharedPref.edit().clear().apply()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_about_us -> {
                    val intent = Intent(this,Terms_policy::class.java)
                    startActivity(intent)
                }
                R.id.nav_feedback -> {
                    val intent = Intent(this,FeedbackFormActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun getPosts(token: String) {
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

        retrofitBuilder.getPosts().enqueue(object: Callback<List<Post>> {
            override fun onResponse(p0: Call<List<Post>>, p1: Response<List<Post>>) {
                if (p1.isSuccessful) {
                    val responseBody = p1.body()

                    if (responseBody != null) {
                        for (post in responseBody) {
                            val content = post.post
                            val id = post.id
                            val userId = post.user_id

                            addPost(content, id, userId)
                        }
                    }
                } else {
                    Toast.makeText(this@UnknownActivity, "Fetching posts failed.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<List<Post>>, p1: Throwable) {
                Toast.makeText(this@UnknownActivity, "Fetching posts failed.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createPost(post: String, token: String) {
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

        retrofitBuilder.post(post).enqueue(object: Callback<Post> {
            override fun onResponse(p0: Call<Post>, p1: Response<Post>) {
                if (p1.isSuccessful) {
                    val response = p1.body()
                    val token = response?.token
                    val id = response?.id
                    val userId = response?.user_id

                    Log.d("Token", "$token")

                    if (token != null && id != null && userId != null) {
                        addPost(post, id, userId)
                    }
                } else {
                    val error = p1.errorBody()?.string()
                    Log.e("Error", "$error")
                    Toast.makeText(this@UnknownActivity, "Creating post failed.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<Post>, p1: Throwable) {
                Toast.makeText(this@UnknownActivity, "Creating post failed.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deletePost(id: Int, token: String) {
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

        retrofitBuilder.deletePost("Bearer $token", id).enqueue(object: Callback<Void> {
            override fun onResponse(p0: Call<Void>, p1: Response<Void>) {
                if (p1.isSuccessful) {
                    Toast.makeText(this@UnknownActivity, "Successfully deleted post", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@UnknownActivity, "Failed to delete post", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<Void>, p1: Throwable) {
                Toast.makeText(this@UnknownActivity, "An internet error occured", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun deleteComment(id: Int) {
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

        retrofitBuilder.deleteComment("Bearer $token", id).enqueue(object: Callback<Void> {
            override fun onResponse(p0: Call<Void>, p1: Response<Void>) {
                if (p1.isSuccessful) {
                    Toast.makeText(this@UnknownActivity, "Successfully deleted comment.", Toast.LENGTH_SHORT).show()
                } else {
                    val error = p1.errorBody()?.string()
                    Log.e("Error", "$error")
                    Toast.makeText(this@UnknownActivity, "Unable to delete comment", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<Void>, p1: Throwable) {
                Toast.makeText(this@UnknownActivity, "An internet error occurred", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun addPost(content: String, id: Int, userId: Int) {
        // Create a new post container (LinearLayout with padding)
        val postContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16) // Padding inside the post container
        }

        // Create a horizontal layout for the profile image and text content
        val postLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(10, 0, 0, 0) // Padding inside post layout
        }

        // Create ImageView for profile image
        val profileImageView = ImageView(this).apply {
            setImageResource(R.drawable.pra) // Replace with actual image
            layoutParams = LinearLayout.LayoutParams(
                60, // Width
                60  // Height
            ).apply {
                setMargins(10, 5, 10, 5) // Positioning margins
            }
        }

        // Create a vertical layout for username and post content
        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Fill remaining space
            )
        }

        // Create TextView for username
        val usernameTextView = TextView(this).apply {
            text = "Anonymous" // Change if usernames are dynamic
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 5) // Space below username
            }
        }

        // Create TextView for post content
        val postContentTextView = TextView(this).apply {
            text = content
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Add username and content TextViews to the text layout
        textLayout.addView(usernameTextView)
        textLayout.addView(postContentTextView)

        // Add profile image and text layout to the post layout
        postLayout.addView(profileImageView)
        postLayout.addView(textLayout)

        // Create delete TextView for post
        val deleteTextView = TextView(this).apply {
            text = "Delete"
            textSize = 14f
            setTextColor(Color.RED) // Red color for delete button
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END // Align to right
                setMargins(8, 0, 0, 0)
            }
            setOnClickListener {
                token?.let { token -> deletePost(id, token) }
                linearLayoutPosts.removeView(postContainer) // Remove the post when clicked
            }

            if (user_id == userId) {
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        // Add delete TextView to post layout
        postLayout.addView(deleteTextView)

        // Add post layout to the post container
        postContainer.addView(postLayout)

        // Create a scrollable comment section
        val commentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create a ScrollView for the comment section
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200 // Fixed height for comment section
            )
            
            isVerticalScrollBarEnabled = false
            isNestedScrollingEnabled = true
            addView(commentContainer) // Add comment container to ScrollView
        }

        getComments(commentContainer, id)

        // Create a layout for adding comments
        val commentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 5, 0, 0) // Padding above comment input
        }

        // Create EditText for entering a comment
        val editTextComment = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Use weight to fill remaining space
            )
            hint = "Add a comment..."
            setPadding(8, 8, 8, 8)
        }

        // Create a button for submitting the comment
        val buttonComment = Button(this).apply {
            text = "Comment"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 0, 0) // Spacing margin
            }
            setOnClickListener {
                val commentContent = editTextComment.text.toString().trim()
                if (commentContent.isNotEmpty()) {
                    createComment(commentContent, id, commentContainer) // Pass a username
                    editTextComment.text.clear() // Clear the comment input after posting
                }
            }
        }

        // Add comment input and button to comment layout
        commentLayout.addView(editTextComment)
        commentLayout.addView(buttonComment)

        // Add ScrollView for comments and the comment layout to post container
        postContainer.addView(scrollView)
        postContainer.addView(commentLayout)

        // Create a border line below each post
        val borderLine = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2 // Height of the border
            )
            setBackgroundColor(Color.GRAY)
        }

        // Add border line below the comment section
        postContainer.addView(borderLine)

        // Add the post container to the linear layout at the top
        linearLayoutPosts.addView(postContainer, 0)
    }

    private fun addComment(commentContainer: LinearLayout, content: String, commentId: Int, userId: Int) {
        // Create a horizontal layout for the comment and delete button
        val commentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(10, 10, 10, 10)
        }

        // Create the vertical layout for username and comment text
        val commentTextLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Fill remaining space
            )
        }

        // Create TextView for username
        val usernameTextView = TextView(this).apply {
            text = "Anonymous" // Dynamic username
            textSize = 14f
            setTextColor(Color.BLACK)
        }

        // Create TextView for comment content
        val commentContentTextView = TextView(this).apply {
            text = content
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }

        // Add username and comment content to the vertical layout
        commentTextLayout.addView(usernameTextView)
        commentTextLayout.addView(commentContentTextView)

        // Create delete TextView for comment
        val deleteCommentTextView = TextView(this).apply {
            text = "Delete"
            textSize = 12f
            setTextColor(Color.RED)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 0, 0) // Margin for positioning
            }
            setOnClickListener {
                deleteComment(commentId)
                commentContainer.removeView(commentLayout) // Remove the comment when clicked
            }

            visibility = if (user_id == userId) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // Add username and comment text to the comment layout
        commentLayout.addView(commentTextLayout)
        commentLayout.addView(deleteCommentTextView)

        // Add the comment layout to the comment container
        commentContainer.addView(commentLayout)
    }

    private fun getComments(container: LinearLayout, postId: Int) {
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

        retrofitBuilder.getComments("Bearer $token", postId).enqueue(object: Callback<List<Comment>> {
            override fun onResponse(p0: Call<List<Comment>>, p1: Response<List<Comment>>) {
                if (p1.isSuccessful) {
                    val comments = p1.body()

                    if (comments != null) {
                        for (comment in comments) {
                            addComment(container, comment.comment, comment.id, comment.user_id)
                        }
                    }
                } else {
                    val errorResponse = p1.errorBody()?.string()
                    Log.d("Error", "$errorResponse")
                    Toast.makeText(this@UnknownActivity, "Failed to fetch comments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(p0: Call<List<Comment>>, p1: Throwable) {
                Toast.makeText(this@UnknownActivity, "Failed to fetch comments.", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun createComment(comment: String, postId: Int, commentContainer: LinearLayout) {
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

        retrofitBuilder.comment("Bearer $token", postId, comment).enqueue(object: Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    commentContainer.removeAllViews()
                    getComments(commentContainer, postId)

                    Toast.makeText(this@UnknownActivity, "Comment posted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("CommentError", "$error")
                    Toast.makeText(this@UnknownActivity, "Failed to post comment", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                Toast.makeText(this@UnknownActivity, "Failed to post comment", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

