package com.nit.expensestracker

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Calendar

// Data classes to represent expense sheet and individual expenses
data class Expense(
    val id: Int,
    val description: String,
    val amount: Double,
    val date: String
)

data class ExpenseSheet(
    val id: Int,
    val month: Int,
    val year: Int,
    val income: Double = 0.0,
    val expenses: List<Expense> = emptyList()
) {
    fun getMonthName(): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Invalid Month"
        }
    }

    fun getTotalAmount(): Double {
        var total = 0.0
        for (expense in expenses) {
            total += expense.amount
        }
        return total
    }
}

// Simple ID generator with database support
object IdGenerator {
    private var currentId = 0

    fun initializeFromDatabase(dbHelper: DBHelper) {
        val maxSheetId = dbHelper.getMaxSheetId()
        val maxExpenseId = dbHelper.getMaxExpenseId()
        currentId = maxOf(maxSheetId, maxExpenseId)
    }

    fun generateId(): Int {
        currentId += 1
        return currentId
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetListScreen(
    sheets: List<ExpenseSheet>,
    onSheetClick: (ExpenseSheet) -> Unit,
    onCreateClick: () -> Unit,
    onChartClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                actions = {
                    IconButton(onClick = onChartClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "View Chart",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "Create new sheet")
            }
        }
    ) { paddingValues ->

        if (sheets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No expense sheets yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Click + to create your first sheet",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sheets) { sheet ->
                    SheetCard(
                        sheet = sheet,
                        onClick = { onSheetClick(sheet) }
                    )
                }
            }
        }
    }
}

@Composable
fun SheetCard(sheet: ExpenseSheet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${sheet.getMonthName()} ${sheet.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sheet.expenses.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total: €%.2f".format(sheet.getTotalAmount()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSheetScreen(
    onSheetCreated: (ExpenseSheet) -> Unit,
    onBackClick: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentYear = calendar.get(Calendar.YEAR)

    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var yearInput by remember { mutableStateOf(currentYear.toString()) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Sheet") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Current Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ExpenseSheet(0, currentMonth, currentYear).getMonthName()} $currentYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = "Select Month",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            MonthDropdown(
                selectedMonth = selectedMonth,
                onMonthSelected = { month ->
                    selectedMonth = month
                    errorMessage = ""
                }
            )

            Text(
                text = "Enter Year",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = yearInput,
                onValueChange = { newValue ->
                    yearInput = newValue
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 2025") },
                singleLine = true,
                isError = errorMessage.isNotEmpty()
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {

                    val year = yearInput.toIntOrNull()

                    if (yearInput.isEmpty()) {
                        errorMessage = "Please enter a year"
                        return@Button
                    }

                    if (year == null) {
                        errorMessage = "Please enter a valid year number"
                        return@Button
                    }

                    val newSheet = ExpenseSheet(
                        id = IdGenerator.generateId(),
                        month = selectedMonth,
                        year = year
                    )
                    onSheetCreated(newSheet)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Sheet", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun MonthDropdown(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(monthNames[selectedMonth - 1])
                Text("▼")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            monthNames.forEachIndexed { index, monthName ->
                DropdownMenuItem(
                    text = { Text(monthName) },
                    onClick = {
                        onMonthSelected(index + 1)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailScreen(
    sheet: ExpenseSheet,
    onBackClick: () -> Unit,
    onIncomeUpdated: (Double) -> Unit,
    onExpenseAdded: (String, Double, String) -> Unit,
    onExpenseUpdated: (Expense) -> Unit,
    onExpenseDeleted: (Expense) -> Unit
) {
    var isEditingIncome by remember { mutableStateOf(false) }
    var incomeInput by remember { mutableStateOf(sheet.income.toString()) }
    var incomeError by remember { mutableStateOf("") }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${sheet.getMonthName()} ${sheet.year}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddExpenseDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Monthly Income",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingIncome) {
                        OutlinedTextField(
                            value = incomeInput,
                            onValueChange = { newValue ->
                                incomeInput = newValue
                                incomeError = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Enter income amount") },
                            placeholder = { Text("0.00") },
                            singleLine = true,
                            isError = incomeError.isNotEmpty()
                        )

                        if (incomeError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = incomeError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Button(
                                onClick = {
                                    val newIncome = incomeInput.toDoubleOrNull()

                                    if (incomeInput.isEmpty()) {
                                        incomeError = "Income cannot be empty"
                                        return@Button
                                    }

                                    if (newIncome == null) {
                                        incomeError = "Please enter a valid number"
                                        return@Button
                                    }

                                    if (newIncome < 0) {
                                        incomeError = "Income cannot be negative"
                                        return@Button
                                    }

                                    onIncomeUpdated(newIncome)
                                    isEditingIncome = false
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Save") }

                            OutlinedButton(
                                onClick = {
                                    incomeInput = sheet.income.toString()
                                    isEditingIncome = false
                                    incomeError = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                        }

                    } else {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "€%.2f".format(sheet.income),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            OutlinedButton(
                                onClick = {
                                    incomeInput = sheet.income.toString()
                                    isEditingIncome = true
                                }
                            ) { Text("Edit") }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {

                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "€%.2f".format(sheet.getTotalAmount()),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${sheet.expenses.size} expenses recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (sheet.income - sheet.getTotalAmount() >= 0)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {

                    val balance = sheet.income - sheet.getTotalAmount()

                    Text(
                        text = if (balance >= 0) "Surplus" else "Deficit",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (balance >= 0)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "€%.2f".format(kotlin.math.abs(balance)),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sheet.expenses.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No expenses yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Click + to add your first expense",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sheet.expenses) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onEdit = { updatedExpense ->
                                onExpenseUpdated(updatedExpense)
                            },
                            onDelete = { expenseToDelete ->
                                onExpenseDeleted(expenseToDelete)
                            }
                        )
                    }
                }
            }
        }

        if (showAddExpenseDialog) {
            AddExpenseDialog(
                onExpenseAdded = { description, amount, date ->
                    onExpenseAdded(description, amount, date)
                    showAddExpenseDialog = false
                },
                onDismiss = { showAddExpenseDialog = false }
            )
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "€%.2f".format(expense.amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit expense",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete expense",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = {
                Text("Are you sure you want to delete '${expense.description}'? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(expense)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        EditExpenseDialog(
            expense = expense,
            onExpenseUpdated = { updatedExpense ->
                onEdit(updatedExpense)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    onExpenseUpdated: (Expense) -> Unit,
    onDismiss: () -> Unit
) {
    var descriptionInput by remember { mutableStateOf(expense.description) }
    var amountInput by remember { mutableStateOf(expense.amount.toString()) }
    var dateInput by remember { mutableStateOf(expense.date) }

    var descriptionError by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Expense",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { newValue ->
                        descriptionInput = newValue
                        descriptionError = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("e.g., Groceries, Rent") },
                    singleLine = true,
                    isError = descriptionError.isNotEmpty()
                )
                if (descriptionError.isNotEmpty()) {
                    Text(
                        text = descriptionError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { newValue ->
                        amountInput = newValue
                        amountError = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    isError = amountError.isNotEmpty()
                )
                if (amountError.isNotEmpty()) {
                    Text(
                        text = amountError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { newValue ->
                        dateInput = newValue
                        dateError = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date") },
                    placeholder = { Text("DD/MM/YYYY") },
                    singleLine = true,
                    isError = dateError.isNotEmpty()
                )
                if (dateError.isNotEmpty()) {
                    Text(
                        text = dateError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (descriptionInput.isEmpty()) {
                        descriptionError = "Description cannot be empty"
                        return@Button
                    }

                    val amount = amountInput.toDoubleOrNull()
                    if (amountInput.isEmpty()) {
                        amountError = "Amount cannot be empty"
                        return@Button
                    }
                    if (amount == null) {
                        amountError = "Please enter a valid number"
                        return@Button
                    }
                    if (amount <= 0) {
                        amountError = "Amount must be greater than zero"
                        return@Button
                    }

                    if (dateInput.isEmpty()) {
                        dateError = "Date cannot be empty"
                        return@Button
                    }

                    val updatedExpense = expense.copy(
                        description = descriptionInput,
                        amount = amount,
                        date = dateInput
                    )
                    onExpenseUpdated(updatedExpense)
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onExpenseAdded: (String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {

    // ✅ AUTO-DATE (Task 17)
    val calendar = java.util.Calendar.getInstance()
    val defaultDate = "%02d/%02d/%04d".format(
        calendar.get(java.util.Calendar.DAY_OF_MONTH),
        calendar.get(java.util.Calendar.MONTH) + 1,
        calendar.get(java.util.Calendar.YEAR)
    )

    var descriptionInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(defaultDate) }  // ✅ auto-filled date

    var descriptionError by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Expense",
                style= MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { newValue ->
                        descriptionInput = newValue
                        descriptionError = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("e.g., Groceries, Rent") },
                    singleLine = true,
                    isError = descriptionError.isNotEmpty()
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { newValue ->
                        amountInput = newValue
                        amountError = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    isError = amountError.isNotEmpty()
                )

                // ✅ Date auto-filled
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { newValue ->
                        dateInput = newValue
                        dateError = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date") },
                    placeholder = { Text("DD/MM/YYYY") },
                    singleLine = true,
                    isError = dateError.isNotEmpty()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {

                    if (descriptionInput.isEmpty()) {
                        descriptionError = "Description cannot be empty"
                        return@Button
                    }

                    val amount = amountInput.toDoubleOrNull()
                    if (amountInput.isEmpty() || amount == null || amount <= 0) {
                        amountError = "Enter a valid amount"
                        return@Button
                    }

                    onExpenseAdded(descriptionInput, amount, dateInput)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
