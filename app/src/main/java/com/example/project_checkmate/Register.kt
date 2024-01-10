package com.project_checkmate

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project_checkmate.ui.theme.ProjectCheckMateTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val firestore = FirebaseFirestore.getInstance()

class Register : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectCheckMateTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "register") {
                    composable("register") { RegisterScreen(navController) }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    var showDialog by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Error states for text fields
    var usernameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordStrengthError by remember { mutableStateOf(false) }

    rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .offset(y = (-150).dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.mipmap.logo_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                // Clear error when user starts typing
                usernameError = false
            },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            isError = usernameError,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                // Validate email format
                emailError = !isValidEmail(it)
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            isError = emailError,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
                // Check password strength
                passwordStrengthError = !isStrongPassword(it)
            },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            isError = passwordError || passwordStrengthError
        )

        Button(
            onClick = {
                // Check for empty fields and set error states
                usernameError = username.isBlank()
                emailError = email.isBlank() || !isValidEmail(email)
                passwordError = password.isBlank()
                passwordStrengthError = !isStrongPassword(password)

                // Check if the username already exists in Firestore
                if (!usernameError && !emailError && !passwordError && !passwordStrengthError) {
                    // Show confirmation dialog if username doesn't exist
                    showDialog = true
                } else {
                    // Show toast for failed registration
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text("Confirmation")
            },
            text = {
                Text("Are you sure you want to register?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Handle registration logic here
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Registration successful, store user data in Firestore
                                    val user = auth.currentUser
                                    val userData = hashMapOf(
                                        "username" to username,
                                        "email" to email,
                                        // Add other user data as needed
                                    )
                                    firestore.collection("users").document(user!!.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            // Registration and Firestore update successful
                                            showDialog = false
                                        }
                                        .addOnFailureListener { e ->
                                            // Firestore update failed, handle the error
                                            Toast.makeText(
                                                context,
                                                "Firestore update failed: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    // Show toast for successful registration
                                    Toast.makeText(
                                        context,
                                        "Registration successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Navigate to LoginScreen after registration
                                    navController.navigate(Screen.LoginScreen.route)
                                } else {
                                    // Registration failed, handle the error
                                    Toast.makeText(
                                        context,
                                        "Registration failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                ) {
                    Text("Register")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun checkUsernameExists(username: String, onComplete: (Boolean) -> Unit) {
    firestore.collection("users")
        .whereEqualTo("username", username)
        .get()
        .addOnSuccessListener { documents ->
            onComplete(!documents.isEmpty)
        }
        .addOnFailureListener { e ->
            // Handle the failure
            onComplete(false)
        }
}

// Function to check if the email is valid
private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// Function to check if the password is strong
private fun isStrongPassword(password: String): Boolean {
    // Customize the criteria for a strong password as needed
    val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$"
    return password.matches(passwordRegex.toRegex())
}
