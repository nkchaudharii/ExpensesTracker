package com.nit.expensestracker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ExpensesTracker.db"
        private const val DATABASE_VERSION = 1

        // Table for expense sheets
        private const val TABLE_SHEETS = "expense_sheets"
        private const val COLUMN_SHEET_ID = "id"
        private const val COLUMN_MONTH = "month"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_INCOME = "income"

        // Table for individual expenses
        private const val TABLE_EXPENSES = "expenses"
        private const val COLUMN_EXPENSE_ID = "id"
        private const val COLUMN_SHEET_ID_FK = "sheet_id"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create expense sheets table
        val createSheetsTable = """
            CREATE TABLE $TABLE_SHEETS (
                $COLUMN_SHEET_ID INTEGER PRIMARY KEY,
                $COLUMN_MONTH INTEGER NOT NULL,
                $COLUMN_YEAR INTEGER NOT NULL,
                $COLUMN_INCOME REAL DEFAULT 0.0
            )
        """.trimIndent()

        // Create expenses table with foreign key to sheets
        val createExpensesTable = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_EXPENSE_ID INTEGER PRIMARY KEY,
                $COLUMN_SHEET_ID_FK INTEGER NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                FOREIGN KEY($COLUMN_SHEET_ID_FK) REFERENCES $TABLE_SHEETS($COLUMN_SHEET_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createSheetsTable)
        db?.execSQL(createExpensesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SHEETS")
        onCreate(db)
    }

    // Enable foreign key constraints
    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db?.setForeignKeyConstraintsEnabled(true)
    }

    // ========== EXPENSE SHEET OPERATIONS ==========

    // Insert a new expense sheet
    fun insertSheet(sheet: ExpenseSheet): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SHEET_ID, sheet.id)
            put(COLUMN_MONTH, sheet.month)
            put(COLUMN_YEAR, sheet.year)
            put(COLUMN_INCOME, sheet.income)
        }
        return db.insert(TABLE_SHEETS, null, values)
    }

    // Update an existing expense sheet
    fun updateSheet(sheet: ExpenseSheet): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MONTH, sheet.month)
            put(COLUMN_YEAR, sheet.year)
            put(COLUMN_INCOME, sheet.income)
        }
        return db.update(
            TABLE_SHEETS,
            values,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheet.id.toString())
        )
    }

    // Update only the income for a sheet
    fun updateSheetIncome(sheetId: Int, income: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INCOME, income)
        }
        return db.update(
            TABLE_SHEETS,
            values,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheetId.toString())
        )
    }

    // Delete an expense sheet (will also delete associated expenses due to CASCADE)
    fun deleteSheet(sheetId: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_SHEETS,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheetId.toString())
        )
    }

    // Get all expense sheets
    fun getAllSheets(): List<ExpenseSheet> {
        val sheets = mutableListOf<ExpenseSheet>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_SHEETS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_YEAR ASC, $COLUMN_MONTH ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_SHEET_ID))
                val month = getInt(getColumnIndexOrThrow(COLUMN_MONTH))
                val year = getInt(getColumnIndexOrThrow(COLUMN_YEAR))
                val income = getDouble(getColumnIndexOrThrow(COLUMN_INCOME))

                // Get expenses for this sheet
                val expenses = getExpensesForSheet(id)

                sheets.add(
                    ExpenseSheet(
                        id = id,
                        month = month,
                        year = year,
                        income = income,
                        expenses = expenses
                    )
                )
            }
        }
        cursor.close()
        return sheets
    }

    // Get a single sheet by ID
    fun getSheetById(sheetId: Int): ExpenseSheet? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_SHEETS,
            null,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheetId.toString()),
            null,
            null,
            null
        )

        var sheet: ExpenseSheet? = null
        with(cursor) {
            if (moveToFirst()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_SHEET_ID))
                val month = getInt(getColumnIndexOrThrow(COLUMN_MONTH))
                val year = getInt(getColumnIndexOrThrow(COLUMN_YEAR))
                val income = getDouble(getColumnIndexOrThrow(COLUMN_INCOME))

                // Get expenses for this sheet
                val expenses = getExpensesForSheet(id)

                sheet = ExpenseSheet(
                    id = id,
                    month = month,
                    year = year,
                    income = income,
                    expenses = expenses
                )
            }
        }
        cursor.close()
        return sheet
    }

    // ========== EXPENSE OPERATIONS ==========

    // Insert a new expense
    fun insertExpense(sheetId: Int, expense: Expense): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EXPENSE_ID, expense.id)
            put(COLUMN_SHEET_ID_FK, sheetId)
            put(COLUMN_DESCRIPTION, expense.description)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_DATE, expense.date)
        }
        return db.insert(TABLE_EXPENSES, null, values)
    }

    // Update an existing expense
    fun updateExpense(expense: Expense): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DESCRIPTION, expense.description)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_DATE, expense.date)
        }
        return db.update(
            TABLE_EXPENSES,
            values,
            "$COLUMN_EXPENSE_ID = ?",
            arrayOf(expense.id.toString())
        )
    }

    // Delete an expense
    fun deleteExpense(expenseId: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_EXPENSES,
            "$COLUMN_EXPENSE_ID = ?",
            arrayOf(expenseId.toString())
        )
    }

    // Get all expenses for a specific sheet
    fun getExpensesForSheet(sheetId: Int): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_SHEET_ID_FK = ?",
            arrayOf(sheetId.toString()),
            null,
            null,
            "$COLUMN_EXPENSE_ID ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_EXPENSE_ID))
                val description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT))
                val date = getString(getColumnIndexOrThrow(COLUMN_DATE))

                expenses.add(
                    Expense(
                        id = id,
                        description = description,
                        amount = amount,
                        date = date
                    )
                )
            }
        }
        cursor.close()
        return expenses
    }

    // Get a single expense by ID
    fun getExpenseById(expenseId: Int): Expense? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_EXPENSE_ID = ?",
            arrayOf(expenseId.toString()),
            null,
            null,
            null
        )

        var expense: Expense? = null
        with(cursor) {
            if (moveToFirst()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_EXPENSE_ID))
                val description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT))
                val date = getString(getColumnIndexOrThrow(COLUMN_DATE))

                expense = Expense(
                    id = id,
                    description = description,
                    amount = amount,
                    date = date
                )
            }
        }
        cursor.close()
        return expense
    }

    // ========== UTILITY OPERATIONS ==========

    // Get the maximum ID used for sheets (for ID generation)
    fun getMaxSheetId(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX($COLUMN_SHEET_ID) FROM $TABLE_SHEETS", null)
        var maxId = 0
        if (cursor.moveToFirst()) {
            maxId = cursor.getInt(0)
        }
        cursor.close()
        return maxId
    }

    // Get the maximum ID used for expenses (for ID generation)
    fun getMaxExpenseId(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX($COLUMN_EXPENSE_ID) FROM $TABLE_EXPENSES", null)
        var maxId = 0
        if (cursor.moveToFirst()) {
            maxId = cursor.getInt(0)
        }
        cursor.close()
        return maxId
    }
}