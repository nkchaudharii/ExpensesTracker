package com.nit.expensestracker

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Log

// Data class for expense sheet
data class ExpenseSheet(
    val month: String,
    val year: Int
)

// TASK 3: LazyList to display all sheets
@Composable
fun SheetListScreen(
    sheets: List<ExpenseSheet>,
    onSheetClick: (ExpenseSheet) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "My Expense Sheets",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Check if list is empty
        if (sheets.isEmpty()) {
            Text(
                text = "No sheets yet. Create one to get started!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // LazyColumn - scrollable list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sheets) { sheet ->
                    // Each item is a clickable card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // For now, just log the click
                                Log.d("SheetList", "Clicked: ${sheet.month} ${sheet.year}")
                                onSheetClick(sheet)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = sheet.month,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = sheet.year.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// TASK 2: Create new sheets (updated to work with LazyList)
@Composable
fun CreateSheetScreen(
    onSheetCreated: (ExpenseSheet) -> Unit
) {
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

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

        TextField(
            value = month,
            onValueChange = { month = it },
            label = { Text("Month (e.g., January)") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year (e.g., 2025)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (month.isNotBlank() && year.isNotBlank()) {
                    val yearInt = year.toIntOrNull()
                    if (yearInt != null) {
                        val newSheet = ExpenseSheet(month, yearInt)
                        onSheetCreated(newSheet)

                        // Clear inputs
                        month = ""
                        year = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Sheet")
        }
    }
}