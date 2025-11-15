package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.PathEffect
import android.graphics.Paint
import kotlin.math.abs

// Task 11, 12 & 16: Chart Activity with swipe navigation
class ChartActivity : ComponentActivity() {
    private lateinit var db_Access: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database helper
        db_Access = DBHelper(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChartScreenWithSwipe(
                        dbHelper = db_Access,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db_Access.close()
    }
}

// Task 16: Chart screen with swipe navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreenWithSwipe(
    dbHelper: DBHelper,
    onBackPressed: () -> Unit
) {
    // Get all sheets sorted by date
    val all_Sheets = remember {
        dbHelper.getAllSheets().sortedWith(
            compareBy<ExpenseSheet> { it.year }.thenBy { it.month }
        )
    }

    // State to track current viewing window (4 months at a time)
    var current_Index by remember { mutableStateOf(maxOf(0, all_Sheets.size - 4)) }

    // Calculate the current window of sheets to display
    val display_Sheets = remember(current_Index, all_Sheets) {
        if (all_Sheets.isEmpty()) {
            emptyList()
        } else {
            val start_Index = current_Index.coerceIn(0, maxOf(0, all_Sheets.size - 1))
            val end_Index = minOf(start_Index + 4, all_Sheets.size)
            all_Sheets.subList(start_Index, end_Index)
        }
    }

    // Prepare chart data
    val all_months = display_Sheets.map { "${it.getMonthName().take(3)} ${it.year}" }
    val income_Data = display_Sheets.map { it.income }
    val expense_Data = display_Sheets.map { it.getTotalAmount() }

    // Calculate if navigation is possible
    val canSwipe_Left = current_Index > 0
    val canSwipe_Right = current_Index < maxOf(0, all_Sheets.size - 4)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income/Expenses Chart", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.White
                        )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Income/Expenses",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Task 16: Swipe instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Swipe left/right to navigate",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (canSwipe_Left) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Swipe left",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (canSwipe_Right) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Swipe right",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Check if we have data
            if (all_Sheets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available. Create expense sheets first.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Task 16: Custom Chart with swipe gesture detection
                SwipeableChart(
                    months = all_months,
                    incomeData = income_Data,
                    expenseData = expense_Data,
                    onSwipeLeft = {
                        if (canSwipe_Right) {
                            current_Index = minOf(current_Index + 1, all_Sheets.size - 4)
                        }
                    },
                    onSwipeRight = {
                        if (canSwipe_Left) {
                            current_Index = maxOf(current_Index - 1, 0)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display current range info
                Text(
                    text = "Showing ${display_Sheets.size} of ${all_Sheets.size} months",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (canSwipe_Left) {
                                current_Index = maxOf(current_Index - 1, 0)
                            }
                        },
                        enabled = canSwipe_Left,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Button(
                        onClick = {
                            if (canSwipe_Right) {
                                current_Index = minOf(current_Index + 1, all_Sheets.size - 4)
                            }
                        },
                        enabled = canSwipe_Right,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend - Updated colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = Color(0xFF008F82), label = "Income")
                    LegendItem(color = Color(0xFFE05780), label = "Expenses")
                }
            }
        }
    }
}

// Task 16: Swipeable chart composable with gesture detection
@Composable
fun SwipeableChart(
    months: List<String>,
    incomeData: List<Double>,
    expenseData: List<Double>,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var drag_Offset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Detect swipe direction based on accumulated drag
                        if (abs(drag_Offset) > 100f) { // Minimum swipe distance
                            if (drag_Offset > 0) {
                                // Swiped right - go to previous
                                onSwipeRight()
                            } else {
                                // Swiped left - go to next
                                onSwipeLeft()
                            }
                        }
                        drag_Offset = 0f
                    },
                    onDragCancel = {
                        drag_Offset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        drag_Offset += dragAmount
                    }
                )
            }
    ) {
        CustomLineChart(
            months = months,
            incomeData = incomeData,
            expenseData = expenseData
        )
    }
}

@Composable
fun CustomLineChart(
    months: List<String>,
    incomeData: List<Double>,
    expenseData: List<Double>
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        val canvas_Width = size.width
        val canvas_Height = size.height

        // Define margins - slightly adjusted for better spacing
        val left_Margin = 65f
        val right_Margin = 35f
        val top_Margin = 45f
        val bottom_Margin = 65f

        // Calculate chart area
        val chart_AreaWidth = canvas_Width - left_Margin - right_Margin
        val chart_AreaHeight = canvas_Height - top_Margin - bottom_Margin

        // Calculate max value for Y-axis scaling
        val max_Income = incomeData.maxOrNull() ?: 0.0
        val max_Expense = expenseData.maxOrNull() ?: 0.0
        val max_Value = maxOf(max_Income, max_Expense)
        val yAxisMax = if (max_Value > 0) {
            val calculated = max_Value * 1.25 // Changed from 1.2 to 1.25
            if (calculated < 100) 100.0 else calculated
        } else {
            1000.0
        }

        // Draw Y-axis with rounded cap
        drawLine(
            color = Color(0xFF37474F), // Slightly darker color
            start = Offset(left_Margin, top_Margin),
            end = Offset(left_Margin, canvas_Height - bottom_Margin),
            strokeWidth = 3.5f, // Slightly thicker
            cap = StrokeCap.Round
        )

        // Draw X-axis with rounded cap
        drawLine(
            color = Color(0xFF37474F),
            start = Offset(left_Margin, canvas_Height - bottom_Margin),
            end = Offset(canvas_Width - right_Margin, canvas_Height - bottom_Margin),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        // Draw Y-axis labels and grid lines with dashed pattern
        val y_Steps = 5
        for (i in 0..y_Steps) {
            val y_Value = (yAxisMax / y_Steps) * i
            val y_Pos = canvas_Height - bottom_Margin - (chart_AreaHeight / y_Steps * i)

            // Draw dashed grid line for unique look
            drawLine(
                color = Color.LightGray.copy(alpha = 0.6f),
                start = Offset(left_Margin, y_Pos),
                end = Offset(canvas_Width - right_Margin, y_Pos),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Draw Y-axis label with shadow effect
            drawIntoCanvas { canvas ->
                val paintt = Paint().apply {
                    color = android.graphics.Color.parseColor("#1976D2") // Blue shade
                    textSize = 30f
                    textAlign = Paint.Align.RIGHT
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText(
                    "€${y_Value.toInt()}",
                    left_Margin - 12f,
                    y_Pos + 8f,
                    paintt
                )
            }
        }

        // Calculate spacing for X-axis
        val x_Spacing = if (months.size > 1) {
            chart_AreaWidth / (months.size - 1)
        } else {
            chart_AreaWidth / 2
        }

        // Draw X-axis labels with custom styling
        months.forEachIndexed { index, month ->
            val x_Pos = left_Margin + (x_Spacing * index)

            // Draw small tick mark on X-axis
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(x_Pos, canvas_Height - bottom_Margin),
                end = Offset(x_Pos, canvas_Height - bottom_Margin + 8f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // Draw month label
            drawIntoCanvas { canvas ->
                val paintt = Paint().apply {
                    color = android.graphics.Color.parseColor("#424242")
                    textSize = 29f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText(
                    month,
                    x_Pos,
                    canvas_Height - bottom_Margin + 42f,
                    paintt
                )
            }
        }

        // Draw Income Line - CHANGED TO TEAL
        if (incomeData.size > 1) {
            val income_Path = Path()
            incomeData.forEachIndexed { index, value ->
                val x_Pos = left_Margin + (x_Spacing * index)
                val y_Pos = canvas_Height - bottom_Margin -
                        ((value / yAxisMax) * chart_AreaHeight).toFloat()

                if (index == 0) {
                    income_Path.moveTo(x_Pos, y_Pos)
                } else {
                    income_Path.lineTo(x_Pos, y_Pos)
                }

                // Draw larger circles with border for data points
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = Offset(x_Pos, y_Pos)
                )
                drawCircle(
                    color = Color(0xFF008F82), // Darker teal
                    radius = 7f,
                    center = Offset(x_Pos, y_Pos)
                )
            }

            // Draw thicker line with rounded joins
            drawPath(
                path = income_Path,
                color = Color(0xFF00B8A9), // Teal/Turquoise
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }

        // Draw Expenses Line - CHANGED TO PINK
        if (expenseData.size > 1) {
            val expense_Path = Path()
            expenseData.forEachIndexed { index, value ->
                val x_Pos = left_Margin + (x_Spacing * index)
                val y_Pos = canvas_Height - bottom_Margin -
                        ((value / yAxisMax) * chart_AreaHeight).toFloat()

                if (index == 0) {
                    expense_Path.moveTo(x_Pos, y_Pos)
                } else {
                    expense_Path.lineTo(x_Pos, y_Pos)
                }

                // Draw larger circles with border
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = Offset(x_Pos, y_Pos)
                )
                drawCircle(
                    color = Color(0xFFE05780), // Darker pink
                    radius = 7f,
                    center = Offset(x_Pos, y_Pos)
                )
            }

            // Draw thicker line
            drawPath(
                path = expense_Path,
                color = Color(0xFFFF6B9D), // Pink/Rose
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }

        // Handle single data point case with larger markers
        if (incomeData.size == 1) {
            val x_Pos = left_Margin + chart_AreaWidth / 2
            val y_Pos = canvas_Height - bottom_Margin -
                    ((incomeData[0] / yAxisMax) * chart_AreaHeight).toFloat()
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(x_Pos, y_Pos)
            )
            drawCircle(
                color = Color(0xFF00B8A9), // Teal
                radius = 9f,
                center = Offset(x_Pos, y_Pos)
            )
        }

        if (expenseData.size == 1) {
            val x_Pos = left_Margin + chart_AreaWidth / 2
            val y_Pos = canvas_Height - bottom_Margin -
                    ((expenseData[0] / yAxisMax) * chart_AreaHeight).toFloat()
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(x_Pos, y_Pos)
            )
            drawCircle(
                color = Color(0xFFFF6B9D), // Pink
                radius = 9f,
                center = Offset(x_Pos, y_Pos)
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}