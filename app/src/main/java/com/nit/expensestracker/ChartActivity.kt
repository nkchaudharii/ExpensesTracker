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
import android.graphics.Paint
import kotlin.math.abs

// Task 11, 12 & 16: Chart Activity with swipe navigation
class ChartActivity : ComponentActivity() {
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database helper
        dbHelper = DBHelper(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChartScreenWithSwipe(
                        dbHelper = dbHelper,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
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
    val allSheets = remember {
        dbHelper.getAllSheets().sortedWith(
            compareBy<ExpenseSheet> { it.year }.thenBy { it.month }
        )
    }

    // State to track current viewing window (4 months at a time)
    var currentIndex by remember { mutableStateOf(maxOf(0, allSheets.size - 4)) }

    // Calculate the current window of sheets to display
    val displaySheets = remember(currentIndex, allSheets) {
        if (allSheets.isEmpty()) {
            emptyList()
        } else {
            val startIndex = currentIndex.coerceIn(0, maxOf(0, allSheets.size - 1))
            val endIndex = minOf(startIndex + 4, allSheets.size)
            allSheets.subList(startIndex, endIndex)
        }
    }

    // Prepare chart data
    val months = displaySheets.map { "${it.getMonthName().take(3)} ${it.year}" }
    val incomeData = displaySheets.map { it.income }
    val expenseData = displaySheets.map { it.getTotalAmount() }

    // Calculate if navigation is possible
    val canSwipeLeft = currentIndex > 0
    val canSwipeRight = currentIndex < maxOf(0, allSheets.size - 4)

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
                        if (canSwipeLeft) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Swipe left",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (canSwipeRight) {
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
            if (allSheets.isEmpty()) {
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
                    months = months,
                    incomeData = incomeData,
                    expenseData = expenseData,
                    onSwipeLeft = {
                        if (canSwipeRight) {
                            currentIndex = minOf(currentIndex + 1, allSheets.size - 4)
                        }
                    },
                    onSwipeRight = {
                        if (canSwipeLeft) {
                            currentIndex = maxOf(currentIndex - 1, 0)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display current range info
                Text(
                    text = "Showing ${displaySheets.size} of ${allSheets.size} months",
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
                            if (canSwipeLeft) {
                                currentIndex = maxOf(currentIndex - 1, 0)
                            }
                        },
                        enabled = canSwipeLeft,
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
                            if (canSwipeRight) {
                                currentIndex = minOf(currentIndex + 1, allSheets.size - 4)
                            }
                        },
                        enabled = canSwipeRight,
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

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = Color(0xFF4CAF50), label = "Income")
                    LegendItem(color = Color(0xFFF44336), label = "Expenses")
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
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Detect swipe direction based on accumulated drag
                        if (abs(dragOffset) > 100f) { // Minimum swipe distance
                            if (dragOffset > 0) {
                                // Swiped right - go to previous
                                onSwipeRight()
                            } else {
                                // Swiped left - go to next
                                onSwipeLeft()
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
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
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define margins
        val leftMargin = 60f
        val rightMargin = 40f
        val topMargin = 40f
        val bottomMargin = 60f

        // Calculate chart area
        val chartAreaWidth = canvasWidth - leftMargin - rightMargin
        val chartAreaHeight = canvasHeight - topMargin - bottomMargin

        // Calculate max value for Y-axis scaling
        val maxIncome = incomeData.maxOrNull() ?: 0.0
        val maxExpense = expenseData.maxOrNull() ?: 0.0
        val maxValue = maxOf(maxIncome, maxExpense)
        val yAxisMax = if (maxValue > 0) {
            val calculated = maxValue * 1.2
            if (calculated < 100) 100.0 else calculated
        } else {
            1000.0
        }

        // Draw Y-axis
        drawLine(
            color = Color.Black,
            start = Offset(leftMargin, topMargin),
            end = Offset(leftMargin, canvasHeight - bottomMargin),
            strokeWidth = 3f
        )

        // Draw X-axis
        drawLine(
            color = Color.Black,
            start = Offset(leftMargin, canvasHeight - bottomMargin),
            end = Offset(canvasWidth - rightMargin, canvasHeight - bottomMargin),
            strokeWidth = 3f
        )

        // Draw Y-axis labels (amounts)
        val ySteps = 5
        for (i in 0..ySteps) {
            val yValue = (yAxisMax / ySteps) * i
            val yPos = canvasHeight - bottomMargin - (chartAreaHeight / ySteps * i)

            // Draw grid line
            drawLine(
                color = Color.LightGray,
                start = Offset(leftMargin, yPos),
                end = Offset(canvasWidth - rightMargin, yPos),
                strokeWidth = 1f
            )

            // Draw Y-axis label
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                    textAlign = Paint.Align.RIGHT
                }
                canvas.nativeCanvas.drawText(
                    "€${yValue.toInt()}",
                    leftMargin - 10f,
                    yPos + 10f,
                    paint
                )
            }
        }

        // Calculate spacing for X-axis
        val xSpacing = if (months.size > 1) {
            chartAreaWidth / (months.size - 1)
        } else {
            chartAreaWidth / 2
        }

        // Draw X-axis labels (months)
        months.forEachIndexed { index, month ->
            val xPos = leftMargin + (xSpacing * index)

            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                }
                canvas.nativeCanvas.drawText(
                    month,
                    xPos,
                    canvasHeight - bottomMargin + 40f,
                    paint
                )
            }
        }

        // Draw Income Line (Green)
        if (incomeData.size > 1) {
            val incomePath = Path()
            incomeData.forEachIndexed { index, value ->
                val xPos = leftMargin + (xSpacing * index)
                val yPos = canvasHeight - bottomMargin -
                        ((value / yAxisMax) * chartAreaHeight).toFloat()

                if (index == 0) {
                    incomePath.moveTo(xPos, yPos)
                } else {
                    incomePath.lineTo(xPos, yPos)
                }

                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = 8f,
                    center = Offset(xPos, yPos)
                )
            }

            drawPath(
                path = incomePath,
                color = Color(0xFF4CAF50),
                style = Stroke(width = 5f)
            )
        }

        // Draw Expenses Line (Red)
        if (expenseData.size > 1) {
            val expensePath = Path()
            expenseData.forEachIndexed { index, value ->
                val xPos = leftMargin + (xSpacing * index)
                val yPos = canvasHeight - bottomMargin -
                        ((value / yAxisMax) * chartAreaHeight).toFloat()

                if (index == 0) {
                    expensePath.moveTo(xPos, yPos)
                } else {
                    expensePath.lineTo(xPos, yPos)
                }

                drawCircle(
                    color = Color(0xFFF44336),
                    radius = 8f,
                    center = Offset(xPos, yPos)
                )
            }

            drawPath(
                path = expensePath,
                color = Color(0xFFF44336),
                style = Stroke(width = 5f)
            )
        }

        // Handle single data point case
        if (incomeData.size == 1) {
            val xPos = leftMargin + chartAreaWidth / 2
            val yPos = canvasHeight - bottomMargin -
                    ((incomeData[0] / yAxisMax) * chartAreaHeight).toFloat()
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 10f,
                center = Offset(xPos, yPos)
            )
        }

        if (expenseData.size == 1) {
            val xPos = leftMargin + chartAreaWidth / 2
            val yPos = canvasHeight - bottomMargin -
                    ((expenseData[0] / yAxisMax) * chartAreaHeight).toFloat()
            drawCircle(
                color = Color(0xFFF44336),
                radius = 10f,
                center = Offset(xPos, yPos)
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