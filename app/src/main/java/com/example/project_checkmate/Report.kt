package com.project_checkmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project_checkmate.ui.theme.ProjectCheckMateTheme

class ReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectCheckMateTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "report") {
                    composable("report") {
                        ReportScreen(
                            completedTasks = emptyList(),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProjectCheckMateTheme {
        ReportScreen(
            completedTasks = emptyList(),
        )
    }
}