package com.project_checkmate

//noinspection UsingMaterialAndMaterial3Libraries
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.project_checkmate.ui.theme.extension.createImageFile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentUserId(): String {
    val currentUser = FirebaseAuth.getInstance().currentUser
    return currentUser?.uid.orEmpty()
}

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            MyApp()
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askPermissions() {
        // List of permissions your app requires
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (permission in requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(requiredPermissions)
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object Home : Screen("home")
    object CreateTask : Screen("create_task")
    object Main : Screen("main")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object Report : Screen("report")
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MyApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(emptyList<Task>()) }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            if (isLoggedIn) {
                BottomNavigation(
                    contentColor = LocalContentColor.current,
                    elevation = 8.dp
                ) {
                    BottomNavigationItem(
                        icon = {
                            val icon = Icons.Filled.List
                            Icon(
                                imageVector = icon,
                                contentDescription = "To do List",
                                tint = LocalContentColor.current
                            )
                        },
                        label = { Text("To do List") },
                        selected = navController.currentDestination?.route == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route)
                        }
                    )
                    BottomNavigationItem(
                        icon = {
                            val icon = Icons.Default.Add
                            Icon(
                                imageVector = icon,
                                contentDescription = "Create Task",
                                tint = LocalContentColor.current
                            )
                        },
                        label = { Text("Create Task") },
                        selected = navController.currentDestination?.route == Screen.CreateTask.route,
                        onClick = {
                            navController.navigate(Screen.CreateTask.route)
                        }
                    )
                    BottomNavigationItem(
                        icon = {
                            val icon = Icons.Default.Person
                            Icon(
                                imageVector = icon,
                                contentDescription = "Profile",
                                tint = LocalContentColor.current
                            )
                        },
                        label = { Text("Profile") },
                        selected = navController.currentDestination?.route == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp) // Adjust this value to match the height of your BottomNavigation
        ) {
            NavHost(navController, startDestination = Screen.LoginScreen.route) {
                composable(Screen.LoginScreen.route) {
                    LoginScreen(
                        navController,
                        onLoginSuccess = { loggedIn, userName ->
                            isLoggedIn = loggedIn
                            if (loggedIn) {
                                userEmail = userName
                                navController.navigate(Screen.Home.route)
                            }
                        },
                        auth = FirebaseAuth.getInstance()
                    )
                }
                composable(Screen.Home.route) {
                    HomeScreen(tasks) { updatedTasks ->
                        tasks = updatedTasks
                    }
                }
                composable(Screen.CreateTask.route) {
                    CreateTaskScreen(navController) { task ->
                        tasks = tasks + task
                    }
                }
                composable(Screen.Main.route) {
                    HomeScreen(tasks) { updatedTasks ->
                        tasks = updatedTasks
                    }
                }
                composable(Screen.Register.route) {
                    RegisterScreen(navController)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        navController = navController,
                        userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
                        isLoggedIn = isLoggedIn
                    ) {
                        isLoggedIn = false
                        navController.navigate(Screen.LoginScreen.route)
                    }
                }
                composable(Screen.Report.route) {
                    ReportScreen(completedTasks = tasks.filter { it.isCompleted})
                }
            }
        }
    }
}


@Composable
fun IconWithTint(imageVector: ImageVector, contentDescription: String, tint: Color) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint
    )
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(tasks: List<Task>, onTasksUpdated: (List<Task>) -> Unit) {
    var isSelectAllChecked by remember { mutableStateOf(false) }

    fun sortTasksByDueDate() {
        onTasksUpdated(tasks.sortedBy { it.dueDate })
    }

    val onDeleteCheckedTasks: () -> Unit = {
        val updatedTasks = tasks.filter { !it.isCompleted }
        onTasksUpdated(updatedTasks)
        isSelectAllChecked = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("CheckMate")
                },
                actions =
                {
                    IconButton(onClick = onDeleteCheckedTasks) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            )
        },
        content = {
            LazyColumn {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isSelectAllChecked,
                            onCheckedChange = {
                                isSelectAllChecked = !isSelectAllChecked
                                val updatedTasks = tasks.map { it.copy(isCompleted = isSelectAllChecked) }
                                onTasksUpdated(updatedTasks)
                            }
                        )
                        Text("Select All")
                    }
                }

                items(tasks.sortedBy { it.dueDate }) { task ->
                    TaskItem(
                        task = task,
                        onTaskCheckedChange = { checkedTask ->
                            val updatedTasks = tasks.map {
                                if (it == checkedTask) it.copy(isCompleted = !it.isCompleted)
                                else it
                            }
                            onTasksUpdated(updatedTasks)
                        }
                    )
                }
            }
        }
    )
}

@SuppressLint("SimpleDateFormat")
@Composable
fun TaskItem(task: Task, onTaskCheckedChange: (Task) -> Unit) {
    val categoryColors = mapOf(
        "Personal" to Color(android.graphics.Color.parseColor("#c8e5ff")),  // Blue
        "Work" to Color(android.graphics.Color.parseColor("#ffcaca")),      // Red
        "Study" to Color(android.graphics.Color.parseColor("#feffb8")),     // Yellow
        "Other" to Color(android.graphics.Color.parseColor("#ffedc1")),     // Orange
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = if (task.isCompleted) {
            Color(android.graphics.Color.parseColor("#c4ffcb")) // Gunakan warna hijau ketika tugas dicentang
        } else {
            categoryColors[task.category] ?: Color.Gray // Gunakan warna kategori jika tugas belum dicentang
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = {
                    onTaskCheckedChange(task)
                },
            )
            Spacer(modifier = Modifier.width(8.dp))
            val textStyle = if (task.isCompleted) {
                MaterialTheme.typography.body1.copy(textDecoration = TextDecoration.LineThrough)
            } else {
                MaterialTheme.typography.body1
            }
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = textStyle.copy(textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due Date: ${task.dueDate} - Category: ${task.category}",
                    style = textStyle.copy(textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None),
                )
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun CreateTaskScreen(navController: NavHostController, onTaskCreated: (Task) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedCategory by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("dd, MMMM yyyy")
    val selectedDateText = remember { mutableStateOf("Due Date: ${dateFormat.format(selectedDate.time)}") }
    val categories = listOf("Personal", "Work", "Study", "Other")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDateText.value,
                modifier = Modifier.weight(1f)
            )

            DatePicker(context = LocalContext.current, selectedDate = selectedDate) { newDate ->
                selectedDate = newDate

                val formattedDate = dateFormat.format(newDate.time)
                selectedDateText.value = "Due Date: $formattedDate"
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Text("Category: $selectedCategory")
                if (expanded) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .widthIn(240.dp)
                            .background(Color.White)
                            .padding(4.dp)
                            .offset(x = 20.dp)
                    ) {
                        categories.forEachIndexed { _, category ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                },
                            ) {
                                Text(text = category)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    expanded = !expanded
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val task = Task(name, dateFormat.format(selectedDate.time), selectedCategory)
                    onTaskCreated(task)

                    navController.navigate(Screen.Home.route)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Create Task")
            }
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (Boolean, String) -> Unit,
    auth: FirebaseAuth
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Error states for text fields
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordStrengthError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .offset(y = (-170).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.logo_foreground),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Fit
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            isError = emailError,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
                // Check password strength
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

        val firestore = FirebaseFirestore.getInstance()

        Button(
            onClick = {
                // Check for empty fields and set error states
                emailError = email.isBlank() || !isValidEmail(email)
                passwordError = password.isBlank()

                // Perform login only if all fields are filled
                if (!emailError && !passwordError) {
                    // Authenticate with Firebase Authentication
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Login successful
                                val user = auth.currentUser
                                // Check if the user data exists in Firestore
                                user?.uid?.let { userId ->
                                    val firestore = FirebaseFirestore.getInstance()
                                    val userDocument = firestore.collection("users").document(userId)

                                    userDocument.get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                // User data found in Firestore
                                                onLoginSuccess(true, user.displayName.orEmpty())
                                            } else {
                                                // User data not found in Firestore
                                                Toast.makeText(
                                                    context, "User data not found.", Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Failed to access Firestore
                                            Toast.makeText(
                                                context, "Failed to access Firestore.", Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(
                                    context, "Authentication failed.", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    // Show toast for empty fields
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Or register here",
            color = MaterialTheme.colors.primary,
            modifier = Modifier.clickable {
                navController.navigate(Screen.Register.route)
            }
        )
    }
}

// Helper function to check if the given string is a valid email address
private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

@Composable
fun ProfileScreen(
    navController: NavHostController,
    userId: String,
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
) {
    var displayedUserName by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val choosePhotoDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current

    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        BuildConfig.APPLICATION_ID + ".provider", file
    )
    val photoUri = remember { mutableStateOf(Uri.EMPTY) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            Log.d("TESTCAMERA", uri.toString())
            photoUri.value = uri
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PHOTOPICKERURI", photoUri.value.toString())
            photoUri.value = uri
            Log.d("PhotoPicker", "Selected URI: $uri")
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    // Mendapatkan data pengguna dari Firestore dan mengatur displayedUserName
    LaunchedEffect(userId) {
        val firestore = FirebaseFirestore.getInstance()
        val userDocument = firestore.collection("users").document(userId)

        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    displayedUserName = username ?: ""
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreen", "Error getting user document", e)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUri.value.takeIf { it != null },
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White, CircleShape)
                    .clickable {
                        choosePhotoDialog.value = true
                    }
                    .aspectRatio(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material.Text(
            text = "Hi, ${displayedUserName}!",
            style = TextStyle(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Button
        Button(
            onClick = { navController.navigate(Screen.Report.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material.Text("View ${displayedUserName}'s Weekly Report")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            androidx.compose.material.Text("Logout")
        }
        if (choosePhotoDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    choosePhotoDialog.value = false
                },
                text = {
                    androidx.compose.material.Text("Choose The Image From")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            choosePhotoDialog.value = false
                        }
                    ) {
                        androidx.compose.material.Text("Galery")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                            choosePhotoDialog.value = false
                        }
                    ) {
                        androidx.compose.material.Text("Camera")
                    }
                }
            )
        }

        // Logout Confirmation Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                text = {
                    androidx.compose.material.Text("Are you sure you want to logout?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            onLogout()
                            navController.navigate(Screen.LoginScreen.route)
                        }
                    ) {
                        androidx.compose.material.Text("Logout")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        androidx.compose.material.Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ReportScreen(completedTasks: List<Task>) {
    var reviewText by remember { mutableStateOf("") }
    var reviews by remember { mutableStateOf(emptyList<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Here's your weekly report",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Completed Tasks:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                completedTasks.forEach { task ->
                    Text(task.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Your Review:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                reviews.forEach { userReview ->
                    Text(userReview,  fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            // Handle sending the review (e.g., save to a list)
                            // For simplicity, let's just print the review here
                            println("Review Sent: $reviewText")

                            // Add the review to the list of reviews
                            reviews = reviews + reviewText

                            // Clear the text field
                            reviewText = ""
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(8.dp)
                )
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MyApp()
}
