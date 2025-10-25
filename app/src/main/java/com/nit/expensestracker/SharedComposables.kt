package com.nit.expensestracker

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Simple data class to hold our expense sheet info
data class ExpenseSheet(
    val month: String,
    val year: Int
)

@Composable
fun CreateSheetScreen() {
    // State for our form fields and list
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var sheetsList by remember { mutableStateOf(listOf<ExpenseSheet>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create New Expense Sheet",
            style = MaterialTheme.typography.headlineMedium
        )

        // Month input
        TextField(
            value = month,
            onValueChange = { month = it },
            label = { Text("Month (e.g., January)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Year input
        TextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year (e.g., 2025)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Create button
        Button(
            onClick = {
                // Basic validation
                if (month.isNotBlank() && year.isNotBlank()) {
                    val yearInt = year.toIntOrNull()
                    if (yearInt != null) {
                        // Add the new sheet to our list
                        val newSheet = ExpenseSheet(month, yearInt)
                        sheetsList = sheetsList + newSheet

                        // Clear the form
                        month = ""
                        year = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Sheet")
        }

        // Show the list of created sheets
        if (sheetsList.isNotEmpty()) {
            Text(
                text = "Your Sheets:",
                style = MaterialTheme.typography.titleMedium
            )

            sheetsList.forEach { sheet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${sheet.month} ${sheet.year}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}