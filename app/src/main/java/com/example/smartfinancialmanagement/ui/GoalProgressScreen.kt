package com.example.smartfinancialmanagement.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ---------------------------------------------------------
   COLOR TOKENS — matches the dark "savings jar" design system
   --------------------------------------------------------- */
object GoalColors {
    val Bg = Color(0xFF090D1A)
    val Surface = Color(0xFF131A2E)
    val SurfaceHi = Color(0xFF1A2340)
    val Border = Color(0xFF232D4D)
    val Mint = Color(0xFF2FE0AC)
    val MintDim = Color(0xFF1A8F6F)
    val Coral = Color(0xFFFF6B7A)
    val Gold = Color(0xFFFFC857)
    val Blue = Color(0xFF5B8CFF)
    val Text = Color(0xFFEEF1FB)
    val TextMute = Color(0xFF8A92B2)
    val TextDim = Color(0xFF5B6389)
}

/* ---------------------------------------------------------
   DATA
   --------------------------------------------------------- */
data class SavingsEntry(
    val id: Long,
    val amount: Double,
    val note: String,
    val date: String
)

private fun currency(n: Double): String = "$" + String.format(Locale.US, "%.2f", n)

private fun todayLabel(): String =
    SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())

/* ---------------------------------------------------------
   MAIN SCREEN
   --------------------------------------------------------- */
@Composable
fun GoalProgressScreen(
    goalName: String = "Laptop Repair",
    targetAmount: Double = 500.0,
    currentSavedParam: Double = 0.0,
    monthlyRequirement: Double = 166.67,
    startDate: String = "14 / 07 / 2026",
    targetDate: String = "31 / 10 / 2026",
    onDeleteGoal: () -> Unit = {},
    onEditGoal: () -> Unit = {},
    onBack: () -> Unit = {},
    onAddEntry: (Double, String) -> Unit = { _, _ -> }
) {
    // For now we keep entries local state, but initialize current saved from parameter
    val entries = remember { mutableStateListOf<SavingsEntry>() }
    var nextId by remember { mutableStateOf(1L) }
    var showAddForm by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Long?>(null) }

    // We use the authoritative current amount passed from the parent activity (Firebase)
    val currentSaved = currentSavedParam
    
    val remaining = (targetAmount - currentSaved).coerceAtLeast(0.0)
    val percent = if (targetAmount > 0) ((currentSaved / targetAmount) * 100).coerceIn(0.0, 100.0) else 0.0
    val isCompleted = percent >= 100.0

    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            showCelebration = true
            delay(2600)
            showCelebration = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalColors.Bg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp)
        ) {
            item { TopBar(onBack) }
            item { Spacer(Modifier.height(16.dp)) }

            item {
                HeroCard(
                    goalName = goalName,
                    startDate = startDate,
                    targetDate = targetDate,
                    currentSaved = currentSaved,
                    targetAmount = targetAmount,
                    percent = percent,
                    isCompleted = isCompleted
                )
            }
            item { Spacer(Modifier.height(12.dp)) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Remaining",
                        value = currency(remaining),
                        valueColor = GoalColors.Coral
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Monthly need",
                        value = currency(monthlyRequirement),
                        valueColor = GoalColors.Gold
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }

            item { DatesCard(startDate, targetDate) }
            item { Spacer(Modifier.height(12.dp)) }

            item {
                SavingsLogCard(
                    entries = entries,
                    showAddForm = showAddForm,
                    currentSaved = currentSaved,
                    targetAmount = targetAmount,
                    onToggleAddForm = { showAddForm = !showAddForm },
                    onAddEntry = { amount, note ->
                        entries.add(
                            SavingsEntry(
                                id = nextId++,
                                amount = amount,
                                note = note,
                                date = todayLabel()
                            )
                        )
                        showAddForm = false
                        onAddEntry(amount, note)
                    },
                    editingId = editingId,
                    onStartEdit = { editingId = it },
                    onCancelEdit = { editingId = null },
                    onSaveEdit = { id, newAmount ->
                        val idx = entries.indexOfFirst { it.id == id }
                        if (idx != -1) entries[idx] = entries[idx].copy(amount = newAmount)
                        editingId = null
                    }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDeleteGoal,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoalColors.Coral),
                        border = BorderStroke(1.dp, GoalColors.Coral.copy(alpha = 0.35f))
                    ) { Text("Delete goal", fontWeight = FontWeight.Bold, fontSize = 13.5.sp) }

                    Button(
                        onClick = onEditGoal,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoalColors.SurfaceHi,
                            contentColor = GoalColors.Text
                        )
                    ) { Text("Edit goal", fontWeight = FontWeight.Bold, fontSize = 13.5.sp) }
                }
            }
        }

        if (showCelebration) {
            CelebrationOverlay(goalName = goalName)
        }
    }
}

/* ---------------------------------------------------------
   TOP BAR
   --------------------------------------------------------- */
@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GoalColors.Surface)
                .border(1.dp, GoalColors.Border, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = GoalColors.TextMute, fontSize = 16.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                "SAVINGS GOAL",
                color = GoalColors.TextDim,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp
            )
            Text(
                "Goal Details",
                color = GoalColors.Text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ---------------------------------------------------------
   HERO CARD — jar visual + progress bar
   --------------------------------------------------------- */
@Composable
private fun HeroCard(
    goalName: String,
    startDate: String,
    targetDate: String,
    currentSaved: Double,
    targetAmount: Double,
    percent: Double,
    isCompleted: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(listOf(GoalColors.SurfaceHi, GoalColors.Surface))
            )
            .border(1.dp, GoalColors.Border, RoundedCornerShape(26.dp))
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(goalName, color = GoalColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Started ${if (startDate.length >= 5) startDate.take(5) else startDate} — due $targetDate",
                    color = GoalColors.TextMute,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusBadge(isCompleted)
        }

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            JarVisual(percent = percent, modifier = Modifier.size(width = 96.dp, height = 120.dp))
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                StatLine("Current saved", currency(currentSaved), GoalColors.Mint)
                HorizontalDivider(color = GoalColors.Border, thickness = 1.dp, modifier = Modifier.padding(vertical = 7.dp))
                StatLine("Target amount", currency(targetAmount), GoalColors.Text)
            }
        }

        Spacer(Modifier.height(18.dp))
        ProgressTrack(percent = percent)
    }
}

@Composable
private fun StatusBadge(isCompleted: Boolean) {
    val accent = if (isCompleted) GoalColors.Gold else GoalColors.Mint
    val label = if (isCompleted) "★ Completed" else "● Active"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.32f), RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(label, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = GoalColors.TextMute, fontSize = 12.5.sp)
        Text(value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProgressTrack(percent: Double) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent.toFloat(),
        animationSpec = tween(durationMillis = 700),
        label = "progress"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(50))
            .background(GoalColors.Bg)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (animatedPercent / 100f).coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(GoalColors.MintDim, GoalColors.Mint)))
        )
    }
}

/* ---------------------------------------------------------
   JAR VISUAL — animated liquid fill on a Canvas
   --------------------------------------------------------- */
@Composable
private fun JarVisual(percent: Double, modifier: Modifier = Modifier) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent.toFloat(),
        animationSpec = tween(durationMillis = 700),
        label = "jarFill"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val jarPath = Path().apply {
                moveTo(w * 0.19f, h * 0.07f)
                lineTo(w * 0.81f, h * 0.07f)
                lineTo(w * 0.81f, h * 0.15f)
                cubicTo(w * 0.81f, h * 0.18f, w * 0.77f, h * 0.20f, w * 0.77f, h * 0.23f)
                lineTo(w * 0.77f, h * 0.83f)
                cubicTo(w * 0.77f, h * 0.92f, w * 0.69f, h * 0.97f, w * 0.5f, h * 0.97f)
                cubicTo(w * 0.31f, h * 0.97f, w * 0.23f, h * 0.92f, w * 0.23f, h * 0.83f)
                lineTo(w * 0.23f, h * 0.23f)
                cubicTo(w * 0.23f, h * 0.20f, w * 0.19f, h * 0.18f, w * 0.19f, h * 0.15f)
                close()
            }

            // jar outline
            drawPath(jarPath, color = GoalColors.Bg)
            drawPath(jarPath, color = GoalColors.Border, style = Stroke(width = 2f))

            // lid
            drawRoundRect(
                color = GoalColors.Border,
                topLeft = Offset(w * 0.17f, h * 0.03f),
                size = androidx.compose.ui.geometry.Size(w * 0.66f, h * 0.07f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )

            // liquid fill, clipped to jar shape
            clipPath(jarPath) {
                val fillTop = h - (animatedPercent / 100f) * (h * 0.90f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(GoalColors.Mint, GoalColors.MintDim),
                        startY = fillTop,
                        endY = h
                    ),
                    topLeft = Offset(0f, fillTop),
                    size = androidx.compose.ui.geometry.Size(w, h - fillTop)
                )
            }
        }

        Text(
            "${animatedPercent.toInt()}%",
            color = GoalColors.Text,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ---------------------------------------------------------
   STAT / DATE CARDS
   --------------------------------------------------------- */
@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String, valueColor: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(GoalColors.Surface)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text(label.uppercase(), color = GoalColors.TextDim, fontSize = 11.sp, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DatesCard(startDate: String, targetDate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GoalColors.Surface)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("START DATE", color = GoalColors.TextDim, fontSize = 11.sp, letterSpacing = 0.8.sp)
            Text(startDate, color = GoalColors.Text, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }
        Column {
            Text("TARGET DATE", color = GoalColors.TextDim, fontSize = 11.sp, letterSpacing = 0.8.sp)
            Text(targetDate, color = GoalColors.Text, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ---------------------------------------------------------
   SAVINGS LOG — add + inline edit
   --------------------------------------------------------- */
@Composable
private fun SavingsLogCard(
    entries: List<SavingsEntry>,
    showAddForm: Boolean,
    currentSaved: Double,
    targetAmount: Double,
    onToggleAddForm: () -> Unit,
    onAddEntry: (Double, String) -> Unit,
    editingId: Long?,
    onStartEdit: (Long) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (Long, Double) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GoalColors.Surface)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Savings log", color = GoalColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onToggleAddForm,
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoalColors.Mint, contentColor = Color(0xFF04241B)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("＋ Add savings", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(visible = showAddForm, enter = expandVertically(), exit = shrinkVertically()) {
            AddSavingsForm(
                currentSaved = currentSaved,
                targetAmount = targetAmount,
                onSubmit = onAddEntry,
                onCancel = onToggleAddForm
            )
        }

        Spacer(Modifier.height(10.dp))

        if (entries.isEmpty()) {
            Text(
                "No savings recorded yet — add your first one above.",
                color = GoalColors.TextDim,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entries.reversed().forEach { entry ->
                    SavingsLogItem(
                        entry = entry,
                        isEditing = editingId == entry.id,
                        onStartEdit = { onStartEdit(entry.id) },
                        onCancelEdit = onCancelEdit,
                        onSaveEdit = { newAmount -> onSaveEdit(entry.id, newAmount) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSavingsForm(
    currentSaved: Double,
    targetAmount: Double,
    onSubmit: (Double, String) -> Unit,
    onCancel: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val showWarning = targetAmount > 0.0 && (currentSaved + parsedAmount) > targetAmount

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GoalColors.Bg)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text("AMOUNT SAVED", color = GoalColors.TextMute, fontSize = 11.sp, letterSpacing = 0.6.sp)
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it; showError = false },
            placeholder = { Text("e.g. 40.00", color = GoalColors.TextDim) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = showError,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            colors = fieldColors()
        )

        AnimatedVisibility(visible = showWarning) {
            Text(
                "Warning: This entry exceeds your target amount!",
                color = GoalColors.Gold,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(10.dp))
        Text("NOTE (OPTIONAL)", color = GoalColors.TextMute, fontSize = 11.sp, letterSpacing = 0.6.sp)
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            placeholder = { Text("e.g. Tuition allowance leftover", color = GoalColors.TextDim) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            colors = fieldColors()
        )

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoalColors.TextMute),
                border = BorderStroke(1.dp, GoalColors.Border)
            ) { Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold) }

            Button(
                onClick = {
                    val value = amountText.toDoubleOrNull()
                    if (value == null || value <= 0.0) {
                        showError = true
                    } else {
                        onSubmit(value, noteText.trim())
                        amountText = ""
                        noteText = ""
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoalColors.Mint, contentColor = Color(0xFF04241B))
            ) { Text("Save entry", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = GoalColors.Surface,
    unfocusedContainerColor = GoalColors.Surface,
    focusedTextColor = GoalColors.Text,
    unfocusedTextColor = GoalColors.Text,
    focusedBorderColor = GoalColors.Mint,
    unfocusedBorderColor = GoalColors.Border,
    cursorColor = GoalColors.Mint
)

@Composable
private fun SavingsLogItem(
    entry: SavingsEntry,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (Double) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoalColors.Bg)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GoalColors.Mint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$", color = GoalColors.Mint, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.width(12.dp))

        if (isEditing) {
            var editValue by remember { mutableStateOf(entry.amount.toString()) }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(100.dp),
                    colors = fieldColors(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
                TextButton(onClick = {
                    editValue.toDoubleOrNull()?.let { if (it >= 0) onSaveEdit(it) }
                }) { Text("Save", color = GoalColors.Mint, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                TextButton(onClick = onCancelEdit) { Text("Cancel", color = GoalColors.TextMute, fontSize = 12.sp) }
            }
        } else {
            Column(Modifier.weight(1f)) {
                Text(currency(entry.amount), color = GoalColors.Mint, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                Text(
                    if (entry.note.isBlank()) "Savings entry" else entry.note,
                    color = GoalColors.TextMute,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }
            Text(entry.date, color = GoalColors.TextDim, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoalColors.Surface)
                    .border(1.dp, GoalColors.Border, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onStartEdit() },
                contentAlignment = Alignment.Center
            ) {
                Text("✎", color = GoalColors.TextMute, fontSize = 13.sp)
            }
        }
    }
}

/* ---------------------------------------------------------
   CELEBRATION ANIMATION
   --------------------------------------------------------- */
private class Particle(
    val xOffset: Float,
    val yOffset: Float,
    val color: Color,
    val size: Float,
    val speed: Float,
    val drift: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
private fun CelebrationOverlay(goalName: String) {
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animatable.animateTo(1f, animationSpec = tween(2600, easing = LinearEasing))
    }
    
    val particles = remember {
        List(75) {
            Particle(
                xOffset = Random.nextFloat(),
                yOffset = Random.nextFloat() * -0.5f,
                color = listOf(GoalColors.Mint, GoalColors.Coral, GoalColors.Gold, Color(0xFF9D4EDD)).random(),
                size = Random.nextFloat() * 15f + 15f,
                speed = Random.nextFloat() * 1.5f + 1.0f,
                drift = (Random.nextFloat() - 0.5f) * 2f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isCircle = Random.nextBoolean()
            )
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val p = animatable.value
            val alpha = if (p > 0.8f) ((1f - p) / 0.2f).coerceIn(0f, 1f) else 1f
            
            particles.forEach { particle ->
                val x = size.width * particle.xOffset + (particle.drift * p * size.width * 0.3f)
                val y = size.height * particle.yOffset + (particle.speed * p * size.height * 1.2f)
                val rotation = particle.rotationSpeed * p
                
                withTransform({
                    translate(left = x, top = y)
                    rotate(rotation)
                }) {
                    if (particle.isCircle) {
                        drawCircle(color = particle.color, radius = particle.size / 2f, alpha = alpha)
                    } else {
                        drawRect(
                            color = particle.color,
                            topLeft = Offset(-particle.size / 2f, -particle.size / 2f),
                            size = androidx.compose.ui.geometry.Size(particle.size, particle.size),
                            alpha = alpha
                        )
                    }
                }
            }
        }
        
        AnimatedVisibility(
            visible = animatable.value < 0.9f,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(GoalColors.SurfaceHi)
                    .border(2.dp, GoalColors.Mint, RoundedCornerShape(24.dp))
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Goal Achieved!", color = GoalColors.Mint, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(goalName, color = GoalColors.Text, fontSize = 15.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF090D1A)
@Composable
private fun GoalProgressScreenPreview() {
    MaterialTheme {
        GoalProgressScreen()
    }
}
