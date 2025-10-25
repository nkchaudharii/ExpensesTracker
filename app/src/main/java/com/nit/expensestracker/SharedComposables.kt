package com.nit.expensestracker

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*

@Composable
fun ExpensesTrackerApp() {
    Column {
        WelcomeText()
        ExpensesList()
    }
}

@Composable
fun WelcomeText() {
    Text("Welcome to ExpensesTracker!")
}

@Composable
fun ExpensesList() {
    // Your expense list UI here
}