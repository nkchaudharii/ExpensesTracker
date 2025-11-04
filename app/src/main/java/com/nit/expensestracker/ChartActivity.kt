package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import android.graphics.Paint

// Task 11 & 12: Chart Activity for displaying Income/Expenses graphs
class ChartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive data from MainActivity
        val months = intent.getStringArrayExtra("MONTHS") ?: arrayOf()
        val incomeData = intent.getDoubleArrayExtra("INCOME_DATA") ?: doubleArrayOf()
        val expenseData = intent.getDoubleArrayExtra("EXPENSE_DATA") ?: doubleArrayOf()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChartScreen(
                        months = months.toList(),
                        incomeData = incomeData.toList(),
                        expenseData = expenseData.toList(),
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    months: List<String>,
    incomeData: List<Double>,
    expenseData: List<Double>,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income/Expenses Chart") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Income/Expenses",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Check if we have data
            if (months.isEmpty() || incomeData.isEmpty() || expenseData.isEmpty()) {
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
                // Custom Chart Composable
                CustomLineChart(
                    months = months,
                    incomeData = incomeData,
                    expenseData = expenseData
                )

                Spacer(modifier = Modifier.height(24.dp))

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

@Composable
fun CustomLineChart(
    months: List<String>,
    incomeData: List<Double>,
    expenseData: List<Double>
) {
    // Square aspect ratio without BoxWithConstraints
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // This makes it square
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
        // Adjust yAxisMax to have a minimum range for visibility
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

            // Draw Y-axis label using native canvas
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

            // Draw month label
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

                // Draw point
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

                // Draw point
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