package com.project_checkmate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project_checkmate.R.mipmap
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreateTask(this)
        }
    }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun CreateTask(activity: ComponentActivity) {
    var name by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var category by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("dd, MMMM yyyy")
    val selectedDateText = remember { mutableStateOf("Due Date: ${dateFormat.format(selectedDate.time)}") }
    val categories = listOf("Personal", "Work", "Study", "Other")

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
            horizontalArrangement = Arrangement.SpaceBetween
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

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Category")
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                selectedValue = category,
                onValueSelected = { selectedCategory -> category = selectedCategory },
                items = categories,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val intent = Intent().apply {
                    putExtra("name", name)
                    putExtra("dueDate", dateFormat.format(selectedDate.time))
                    putExtra("category", category)
                }
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Create Task")
        }
    }
}

@Composable
fun DatePicker(
    context: Context,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit
) {
    val year = selectedDate.get(Calendar.YEAR)
    val month = selectedDate.get(Calendar.MONTH)
    val day = selectedDate.get(Calendar.DAY_OF_MONTH)

    val calendarImage = painterResource(id = mipmap.duedate_launcher_foreground)

    Image(
        painter = calendarImage,
        contentDescription = null,
        modifier = Modifier
            .size(24.dp)
            .clickable {
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate.set(year, month, dayOfMonth)
                        onDateSelected(selectedDate)
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }
    )
}


@Preview(showBackground = true)
@Composable
fun CreateTaskPreview() {
    CreateTask(ComponentActivity())
}

@Composable
fun ExposedDropdownMenuBox(
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    items: List<String>,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.clickable { expanded = true }) {
        Text(selectedValue)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(onClick = {
                    onValueSelected(item)
                    expanded = false
                }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item)
                        Button(onClick = { onValueSelected(item) }) {
                            Text("+")
                        }
                    }
                }
            }
        }
    }
}

