package com.nit.expensestracker

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Calendar


// Simple data classes for monthly sheets and expenses
data class Expense(
    val id: Int,
    val amount: Double,
    val date: String,
    val description: String,

    )

// Each sheet records all of the expenses and income for a given month.
data class ExpenseSheet(
    val id: Int,
    val month: Int,
    val year: Int,
    val income: Double = 0.0,
    val expenses: List<Expense> = emptyList()
)
{
    fun getMonthName(): String = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )[month - 1]

    // Determines the total amount spent in the sheet.
    fun getTotalAmount(): Double = expenses.sumOf { it.amount }
}

// This object makes sure that each new sheet or expense has a unique ID.
object IdGenerator {
    private var currentId = 0

    // Set IDs to the highest value already present in the database.
    fun initializeFromDatabase(dbHelper: DBHelper) {
        val maxSheetId = dbHelper.getMaxSheetId()
        val maxExpenseId = dbHelper.getMaxExpenseId()
        currentId = maxOf(maxSheetId, maxExpenseId)
    }

    // Provides the subsequent unique ID
    fun generateId(): Int {
        currentId += 1
        return currentId
    }
}

// A screen with add and chart buttons that shows every saved expenditure sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetListScreen(
    sheets: List<ExpenseSheet>,
    onSheetClick: (ExpenseSheet) -> Unit,
    onCreateClick: () -> Unit,
    onChartClick: () -> Unit,
    onSheetDeleted: (ExpenseSheet) -> Unit = {},
    onSheetEdited: (ExpenseSheet) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Expenses Tracker",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),  //  GREEN TOP BAR
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },

        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // To create a new sheet, use the floating button
                ExtendedFloatingActionButton(
                    onClick = onCreateClick,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create New Sheet",
                            tint = Color.White
                        )
                    },
                    text = {
                        Text("New Sheet", color = Color.White)
                    }
                )

                // To view the chart, click the floating button.
                ExtendedFloatingActionButton(
                    onClick = onChartClick,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    icon = {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "View Chart",
                            tint = Color.White
                        )
                    },
                    text = {
                        Text("Income/Expenses", color = Color.White)
                    }
                )
            }
        }
    ) { paddingValues ->

        if (sheets.isEmpty()) {
            //When no sheets have been created yet, this is displayed.
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No expense sheets yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap + to create new sheet")
                }
            }
        } else {
            // Shows every created expense sheet
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sheets) { sheet ->
                    SheetCard(
                        sheet = sheet,
                        onClick = { onSheetClick(sheet) },
                        onDelete = { onSheetDeleted(sheet) },
                        onEdit = { onSheetEdited(sheet) }
                    )
                }
            }
        }
    }
}

// A little card with the name of the month, the total amount spent, and the number of expenses
@Composable
fun SheetCard(
    sheet: ExpenseSheet,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)  // LIGHT GREEN BACKGROUND
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The sheet's month and year
                Text(
                    text = "${sheet.getMonthName()} ${sheet.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Total entries for expenses
                Text(
                    "${sheet.expenses.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Action button for editing and deleting
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF4CAF50))
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // The total amount spent on this sheet
            Text(
                text = "Total Expenses: €%.2f".format(sheet.getTotalAmount()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF868686)
            )
        }
    }

    // Dialog conformation prior to sheet delection
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense Sheet") },
            text = { Text("Are you sure? All expenses in this sheet will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// The screen where a new monthly expense sheet can be added
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSheetScreen(
    existingSheets: List<ExpenseSheet> = emptyList(),
    onSheetCreated: (ExpenseSheet) -> Unit,
    onBackClick: () -> Unit
) {
    // Get the current date to display in the UI
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentYear = calendar.get(Calendar.YEAR)

    val weekday = java.text.SimpleDateFormat("EEEE").format(calendar.time)
    val monthName = ExpenseSheet(0, currentMonth, currentYear).getMonthName()
    val formattedDate = "$weekday, %02d $monthName $currentYear".format(currentDay)

    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var yearInput by remember { mutableStateOf(currentYear.toString()) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Back navigation and a green top bar
            TopAppBar(
                title = { Text("Create New Sheet", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Provides the user with the current date.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Current Date", style = MaterialTheme.typography.labelMedium)
                    Text(formattedDate, fontWeight = FontWeight.Bold)
                }
            }

            // The entry field for month
            Text("Enter Month (1-12)", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = if (selectedMonth == 0) "" else selectedMonth.toString(),
                onValueChange = {
                    val month = it.toIntOrNull()
                    if (month != null && month in 1..12) {
                        selectedMonth = month
                        errorMessage = ""
                    } else if (it.isEmpty()) {
                        selectedMonth = 0
                        errorMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter month (1-12)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // The entry filed for year
            Text("Enter Year", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = yearInput,
                onValueChange = { yearInput = it; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Display message validation
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Add a button at the bottom
            Button(
                onClick = {
                    if (selectedMonth == 0 || selectedMonth !in 1..12) {
                        errorMessage = "Enter a valid month (1-12)"
                        return@Button
                    }
                    val year = yearInput.toIntOrNull()
                    if (year == null) {
                        errorMessage = "Enter a valid year"
                        return@Button
                    }

                    // Check for duplicate sheet
                    val duplicate = existingSheets.any {
                        it.month == selectedMonth && it.year == year
                    }

                    if (duplicate) {
                        errorMessage = "A sheet for ${ExpenseSheet(0, selectedMonth, year).getMonthName()} $year already exists"
                        return@Button
                    }

                    onSheetCreated(ExpenseSheet(IdGenerator.generateId(), selectedMonth, year))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Create Sheet") }
        }
    }
}


// Editing screen for an existing expense sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheetScreen(
    sheet: ExpenseSheet,
    existingSheets: List<ExpenseSheet> = emptyList(),
    onSheetUpdated: (ExpenseSheet) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedMonth by remember { mutableStateOf(sheet.month) }
    var yearInput by remember { mutableStateOf(sheet.year.toString()) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Sheet", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Summary card for the current sheet
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Current Sheet")
                    Text("${sheet.getMonthName()} ${sheet.year}", fontWeight = FontWeight.Bold)
                    Text("${sheet.expenses.size} expenses • €%.2f total".format(sheet.getTotalAmount()))
                }
            }

            // Month input field
            Text("Enter Month (1-12)", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = if (selectedMonth == 0) "" else selectedMonth.toString(),
                onValueChange = {
                    val month = it.toIntOrNull()
                    if (month != null && month in 1..12) {
                        selectedMonth = month
                        errorMessage = ""
                    } else if (it.isEmpty()) {
                        selectedMonth = 0
                        errorMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter month (1-12)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Year input field
            Text("Enter Year", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = yearInput,
                onValueChange = { yearInput = it; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            // The button to store updated information
            Button(
                onClick = {
                    if (selectedMonth == 0 || selectedMonth !in 1..12) {
                        errorMessage = "Enter a valid month (1-12)"
                        return@Button
                    }
                    val year = yearInput.toIntOrNull()
                    if (year == null) {
                        errorMessage = "Enter a valid year"
                        return@Button
                    }

                    // Check if updating would create a duplicate (excluding current sheet)
                    val duplicate = existingSheets.any {
                        it.id != sheet.id &&
                                it.month == selectedMonth &&
                                it.year == year
                    }

                    if (duplicate) {
                        errorMessage = "A sheet for ${ExpenseSheet(0, selectedMonth, year).getMonthName()} $year already exists"
                        return@Button
                    }

                    onSheetUpdated(sheet.copy(month = selectedMonth, year = year))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Update Sheet") }
        }
    }
}


// Simple dropdown menu that lets the user choose a month name
@Composable
fun MonthDropdown(selectedMonth: Int, onMonthSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val months = listOf(
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(months[selectedMonth - 1], color = Color.Black)
                Text("▼", color = Color.Black)
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            months.forEachIndexed { index, month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        expanded = false
                        onMonthSelected(index + 1)
                    }
                )
            }
        }
    }
}

// A screen with all the information for a specific month (income, spending, balance)
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
            // The month name and a green header with a back button
            TopAppBar(
                title = { Text("${sheet.getMonthName()} ${sheet.year}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        // To add a new expense, use the floating button.
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                icon = {
                    Icon(Icons.Default.Add, contentDescription = "Add expense", tint = Color.White)
                },
                text = {
                    Text("Add Expense", color = Color.White)
                }
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Income section: displays or modifies the user's monthly income
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Monthly Income")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingIncome) {
                        OutlinedTextField(
                            value = incomeInput,
                            onValueChange = { incomeInput = it; incomeError = "" },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = incomeError.isNotEmpty(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        if (incomeError.isNotEmpty()) {
                            Text(incomeError, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // When editing income, click the save and cancel buttons.
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val newIncome = incomeInput.toDoubleOrNull()
                                    if (incomeInput.isEmpty()) {
                                        incomeError = "Income cannot be empty"; return@Button
                                    }
                                    if (newIncome == null || newIncome < 0) {
                                        incomeError = "Enter valid income"; return@Button
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
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                        }

                    } else {
                        // Shows the income amount while not modifying
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("€%.2f".format(sheet.income), fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = { isEditingIncome = true }) {
                                Text("Edit")
                            }
                        }
                    }
                }
            }

            // Monthly summary of total expenses
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Spent")
                    Text("€%.2f".format(sheet.getTotalAmount()), fontWeight = FontWeight.Bold)
                    Text("${sheet.expenses.size} expenses")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Calculates whether the user saved or overspent
            val balance = sheet.income - sheet.getTotalAmount()

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance >= 0)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(if (balance >= 0) "Surplus" else "Deficit")
                    Text("€%.2f".format(kotlin.math.abs(balance)), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display an expenses list or a message if none are present.
            if (sheet.expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No expenses yet")
                        Text("Click + to add an expense")
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
                            onEdit = { onExpenseUpdated(it) },
                            onDelete = { onExpenseDeleted(it) }
                        )
                    }
                }
            }
        }

        //Adds a new expense entry by opening a dialogue.
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


// One expense entry is displayed on the reusable item card.
@Composable
fun ExpenseItem(
    expense: Expense,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, fontWeight = FontWeight.Bold)
                Text(expense.date, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text("€%.2f".format(expense.amount), fontWeight = FontWeight.Bold)

            Column {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // Before removing an expense, confirm
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete '${expense.description}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(expense)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    // Clicking the edit icon opens the edit dialogue.
    if (showEditDialog) {
        EditExpenseDialog(
            expense = expense,
            onExpenseUpdated = { onEdit(it); showEditDialog = false },
            onDismiss = { showEditDialog = false }
        )
    }
}

// Dialog window to update the specifics of an existing expenditure
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    onExpenseUpdated: (Expense) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf(expense.description) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var date by remember { mutableStateOf(expense.date) }

    var descriptionError by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; descriptionError = "" },
                    label = { Text("Description") },
                    isError = descriptionError.isNotEmpty()
                )
                if (descriptionError.isNotEmpty())
                    Text(descriptionError, color = MaterialTheme.colorScheme.error)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = "" },
                    label = { Text("Amount") },
                    isError = amountError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                if (amountError.isNotEmpty())
                    Text(amountError, color = MaterialTheme.colorScheme.error)

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it; dateError = "" },
                    label = { Text("Date") },
                    isError = dateError.isNotEmpty()
                )
                if (dateError.isNotEmpty())
                    Text(dateError, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isEmpty()) { descriptionError = "Required"; return@Button }
                    val newAmount = amount.toDoubleOrNull()
                    if (newAmount == null || newAmount <= 0) { amountError = "Invalid"; return@Button }
                    if (date.isEmpty()) { dateError = "Required"; return@Button }

                    onExpenseUpdated(expense.copy(description = description, amount = newAmount, date = date))
                }
            ) { Text("Update") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Dialog to add a brand-new expense entry
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onExpenseAdded: (String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
    val defaultDate = "%02d/%02d/%04d".format(
        calendar.get(java.util.Calendar.DAY_OF_MONTH),
        calendar.get(java.util.Calendar.MONTH) + 1,
        calendar.get(java.util.Calendar.YEAR)
    )

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }

    var descriptionError by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; descriptionError = "" },
                    label = { Text("Description") },
                    isError = descriptionError.isNotEmpty()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = "" },
                    label = { Text("Amount") },
                    isError = amountError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isEmpty()) {
                        descriptionError = "Required"
                        return@Button
                    }

                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        amountError = "Invalid"
                        return@Button
                    }

                    onExpenseAdded(description, amountValue, date)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}