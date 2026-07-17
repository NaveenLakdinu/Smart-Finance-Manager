package com.example.smartfinancialmanagement.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/* ---------------------------------------------------------
   MODELS
   --------------------------------------------------------- */
enum class GoalStatus { ONGOING, COMPLETED, INCOMPLETE }

data class Goal(
    val id: String,
    val name: String,
    val icon: String,
    val target: Double,
    val current: Double,
    val status: GoalStatus
) {
    val percent: Float
        get() = if (target > 0) ((current / target) * 100).toFloat().coerceIn(0f, 100f) else 0f

    val remaining: Double
        get() = (target - current).coerceAtLeast(0.0)
}

private fun money(n: Double): String =
    "LKR " + String.format(Locale.US, "%,.2f", n)

private enum class GoalFilter(val label: String, val status: GoalStatus?) {
    ALL("All", null),
    ONGOING("Ongoing", GoalStatus.ONGOING),
    COMPLETED("Completed", GoalStatus.COMPLETED),
    INCOMPLETE("Incomplete", GoalStatus.INCOMPLETE)
}

/* ---------------------------------------------------------
   SAVING MANAGER — home / dashboard screen
   --------------------------------------------------------- */
@Composable
fun SavingManagerScreen(
    totalSavingsGoal: Double,
    goals: List<Goal>,
    onBack: () -> Unit = {},
    onAddGoal: () -> Unit = {},
    onAllGoals: () -> Unit = {},
    onAchievements: () -> Unit = {},
    onReport: () -> Unit = {},
    onSeeAllActive: () -> Unit = {},
    onGoalClick: (Goal) -> Unit = {}
) {
    val ongoingGoals = goals.filter { it.status == GoalStatus.ONGOING }
    val totalCurrent = goals.sumOf { it.current }
    val overallPercent = if (totalSavingsGoal > 0)
        ((totalCurrent / totalSavingsGoal) * 100).toFloat().coerceIn(0f, 100f)
    else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalColors.Bg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp)
    ) {
        item { ManagerTopBar(title = "Saving Manager", onBack = onBack) }
        item { Spacer(Modifier.height(16.dp)) }

        item { TotalGoalHero(totalSavingsGoal = totalSavingsGoal, overallPercent = overallPercent) }
        item { Spacer(Modifier.height(24.dp)) }

        item {
            Text(
                "Saving actions",
                color = GoalColors.Text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item { Spacer(Modifier.height(12.dp)) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = "🎯",
                    iconBg = GoalColors.Mint,
                    label = "Add goal",
                    onClick = onAddGoal
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = "🏦",
                    iconBg = GoalColors.Blue,
                    label = "All goals",
                    onClick = onAllGoals
                )
            }
        }
        item { Spacer(Modifier.height(26.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Active goals",
                    color = GoalColors.Text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "See all →",
                    color = GoalColors.Mint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSeeAllActive() }
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }

        if (ongoingGoals.isEmpty()) {
            item {
                Text(
                    "No ongoing goals yet — tap Add Goal to start one.",
                    color = GoalColors.TextDim,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(ongoingGoals, key = { it.id }) { goal ->
                ActiveGoalCard(goal = goal, onClick = { onGoalClick(goal) })
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ManagerTopBar(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GoalColors.SurfaceHi)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = GoalColors.Text, fontSize = 16.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(title, color = GoalColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TotalGoalHero(totalSavingsGoal: Double, overallPercent: Float) {
    val animatedPercent by animateFloatAsState(
        targetValue = overallPercent,
        animationSpec = tween(700),
        label = "overallProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GoalColors.SurfaceHi)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(24.dp))
            .padding(22.dp)
    ) {
        Text(
            "TOTAL SAVINGS GOAL",
            color = GoalColors.TextMute,
            fontSize = 11.sp,
            letterSpacing = 1.1.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            money(totalSavingsGoal),
            color = GoalColors.Text,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(GoalColors.Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (animatedPercent / 100f).coerceIn(0.03f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(GoalColors.MintDim, GoalColors.Mint)))
            )
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: String,
    iconBg: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(GoalColors.Surface)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBg.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = GoalColors.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ActiveGoalCard(goal: Goal, onClick: () -> Unit) {
    val animatedPercent by animateFloatAsState(
        targetValue = goal.percent,
        animationSpec = tween(700),
        label = "goalProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GoalColors.Surface)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(goal.name, color = GoalColors.Text, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${animatedPercent.toInt()}%",
                color = GoalColors.Mint,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Target: ${money(goal.target)}",
            color = GoalColors.TextMute,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(GoalColors.Bg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (animatedPercent / 100f).coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(GoalColors.MintDim, GoalColors.Mint)))
            )
        }
    }
}

/* ---------------------------------------------------------
   SAVING GOALS — searchable, filterable list with a FAB to add
   --------------------------------------------------------- */
@Composable
fun SavingGoalsListScreen(
    goals: List<Goal>,
    onBack: () -> Unit = {},
    onAddGoal: () -> Unit = {},
    onEditGoal: (Goal) -> Unit = {},
    onDeleteGoal: (Goal) -> Unit = {},
    onGoalClick: (Goal) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(GoalFilter.ALL) }

    val filtered = goals
        .filter { activeFilter.status == null || it.status == activeFilter.status }
        .filter { it.name.contains(query, ignoreCase = true) }

    Box(modifier = Modifier.fillMaxSize().background(GoalColors.Bg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
        ) {
            item { ListTopBar(onBack = onBack) }
            item { Spacer(Modifier.height(16.dp)) }

            item {
                SearchField(
                    query = query,
                    onQueryChange = { query = it }
                )
            }
            item { Spacer(Modifier.height(14.dp)) }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GoalFilter.entries.toList()) { filter ->
                        FilterChip(
                            label = filter.label,
                            selected = filter == activeFilter,
                            onClick = { activeFilter = filter }
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(18.dp)) }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        "No goals match here yet.",
                        color = GoalColors.TextDim,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(filtered, key = { it.id }) { goal ->
                    GoalListCard(
                        goal = goal,
                        onClick = { onGoalClick(goal) },
                        onEdit = { onEditGoal(goal) },
                        onDelete = { onDeleteGoal(goal) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(GoalColors.Blue)
                .clickable { onAddGoal() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ListTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GoalColors.SurfaceHi)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = GoalColors.Text, fontSize = 16.sp)
        }
        Text("Saving Goals", color = GoalColors.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GoalColors.SurfaceHi),
            contentAlignment = Alignment.Center
        ) {
            Text("⋮", color = GoalColors.TextMute, fontSize = 18.sp)
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GoalColors.Surface)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔍", fontSize = 14.sp, color = GoalColors.TextMute)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Search goals...", color = GoalColors.TextDim, fontSize = 14.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = GoalColors.Text, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(GoalColors.Mint),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) GoalColors.Mint else GoalColors.Surface)
            .border(
                width = 1.dp,
                color = if (selected) GoalColors.Mint else GoalColors.Border,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF04241B) else GoalColors.TextMute,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GoalListCard(goal: Goal, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val animatedPercent by animateFloatAsState(
        targetValue = goal.percent,
        animationSpec = tween(700),
        label = "listGoalProgress"
    )
    val (statusBg, statusColor, statusLabel) = when (goal.status) {
        GoalStatus.ONGOING -> Triple(GoalColors.Mint.copy(alpha = 0.14f), GoalColors.Mint, "Ongoing")
        GoalStatus.COMPLETED -> Triple(GoalColors.Gold.copy(alpha = 0.14f), GoalColors.Gold, "Completed")
        GoalStatus.INCOMPLETE -> Triple(GoalColors.Coral.copy(alpha = 0.14f), GoalColors.Coral, "Incomplete")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GoalColors.Surface)
            .border(1.dp, GoalColors.Border, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(goal.name, color = GoalColors.Text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconTag(symbol = "✎", tint = GoalColors.Blue, onClick = onEdit)
                IconTag(symbol = "🗑", tint = GoalColors.Coral, onClick = onDelete)
            }
        }
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("TARGET", color = GoalColors.TextDim, fontSize = 10.5.sp, letterSpacing = 0.6.sp)
                Text(money(goal.target), color = GoalColors.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("CURRENT", color = GoalColors.TextDim, fontSize = 10.5.sp, letterSpacing = 0.6.sp)
                Text(money(goal.current), color = GoalColors.Mint, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(GoalColors.Bg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (animatedPercent / 100f).coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(GoalColors.MintDim, GoalColors.Mint)
                        )
                    )
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Remaining: ${money(goal.remaining)}", color = GoalColors.TextMute, fontSize = 12.sp)
            Text("${animatedPercent.toInt()}%", color = GoalColors.Mint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(statusBg)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(statusLabel, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IconTag(symbol: String, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(tint.copy(alpha = 0.14f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = tint, fontSize = 13.sp)
    }
}
