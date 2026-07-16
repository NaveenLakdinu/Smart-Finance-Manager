package com.example.smartfinancialmanagement.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------
// TOKENS
// ---------------------------------------------------------------
object SavingsColors {
    val Ink = Color(0xFF0B1F3A)
    val Ink2 = Color(0xFF0F2647)
    val Ink3 = Color(0xFF16305A)
    val Parchment = Color(0xFFFBF7EE)
    val Paper = Color(0xFFFFFFFF)
    val Brass = Color(0xFFC9A227)
    val BrassLight = Color(0xFFE8C766)
    val BrassDark = Color(0xFF9C7A16)
    val Emerald = Color(0xFF1F8A5F)
    val EmeraldSoft = Color(0xFFE4F3EC)
    val Amber = Color(0xFFD97706)
    val AmberSoft = Color(0xFFFCEEDA)
    val Slate = Color(0xFF22283A)
    val SlateSoft = Color(0xFF6B7280)
    val Rule = Color(0xFFE9E2D2)
    val Locked = Color(0xFFC7C2B4)
    val LockedBg = Color(0xFFF4F2EC)
    val Bronze = Color(0xFFC97B45)
    val Silver = Color(0xFFB9BDC4)
    val StarPurple = Color(0xFF8A4FD6)
}

// ---------------------------------------------------------------
// DATA MODELS
// ---------------------------------------------------------------
data class Achievement(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val medalColor: Color,
    val earned: Boolean,
    val progressPercent: Int = 0
)

data class SavingsGoalUI(
    val title: String,
    val emoji: String,
    val iconBg: Color,
    val iconTint: Color,
    val dueText: String,
    val percent: Int,
    val savedLabel: String,
    val totalLabel: String,
    val remainingLabel: String,
    val perMonthLabel: String,
    val progressColor: Color
)

// ---------------------------------------------------------------
// SCREEN
// ---------------------------------------------------------------
@Composable
fun SavingsPassportScreen(
    savingsLevel: String,
    earnedAchievements: List<Achievement>,
    lockedAchievements: List<Achievement>,
    goals: List<SavingsGoalUI>,
    onBackClick: () -> Unit = {},
    onAddGoalClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SavingsColors.Parchment)
            .statusBarsPadding()
    ) {
        item {
            HeroHeader(onBackClick = onBackClick)
        }
        item {
            LevelSealCard(
                level = savingsLevel,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-46).dp)
            )
        }
        item {
            AchievementCenterCard(
                earned = earnedAchievements,
                locked = lockedAchievements,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-30).dp)
            )
        }
        item {
            StreakBanner(
                title = "1-Month Saving Streak",
                subtitle = "Keep going to unlock Consistent Saver in 5 more months",
                litSegments = 1,
                totalSegments = 6,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-16).dp)
            )
        }
        item {
            GoalsHeader(onAddGoalClick = onAddGoalClick)
        }
        items(goals) { goal ->
            GoalCard(
                goal = goal,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
        item { Spacer(Modifier.height(28.dp)) }
    }
}

// ---------------------------------------------------------------
// HERO HEADER
// ---------------------------------------------------------------
@Composable
private fun HeroHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(SavingsColors.Ink, SavingsColors.Ink2, SavingsColors.Ink3)
                )
            )
            .padding(horizontal = 22.dp, vertical = 20.dp)
            .padding(bottom = 46.dp)
    ) {
        Column {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.09f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "TRACK · SAVE · ACHIEVE",
                color = SavingsColors.BrassLight,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Savings Passport",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------------------------------------------------------------
// LEVEL SEAL CARD
// ---------------------------------------------------------------
@Composable
private fun LevelSealCard(level: String, modifier: Modifier = Modifier) {
    SavingsCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF6E3A0),
                                SavingsColors.BrassLight,
                                SavingsColors.Brass,
                                SavingsColors.BrassDark
                            )
                        )
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎖️", fontSize = 28.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = "SAVINGS LEVEL",
                    color = SavingsColors.SlateSoft,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = level,
                    color = SavingsColors.Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Keep saving to unlock higher levels! 🎉",
                    color = SavingsColors.SlateSoft,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// ACHIEVEMENT CENTER
// ---------------------------------------------------------------
@Composable
private fun AchievementCenterCard(
    earned: List<Achievement>,
    locked: List<Achievement>,
    modifier: Modifier = Modifier
) {
    SavingsCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 Achievement Center",
                    color = SavingsColors.Ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SavingsColors.AmberSoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${earned.size} earned",
                        color = SavingsColors.BrassDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(2.dp))
            Text(
                text = "Complete milestones to unlock rewards",
                color = SavingsColors.SlateSoft,
                fontSize = 12.sp
            )

            if (earned.isNotEmpty()) {
                SectionLabel(text = "✦ EARNED", color = SavingsColors.Emerald, topPadding = 14.dp)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunked = earned.chunked(2)
                    chunked.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { badge ->
                                Box(modifier = Modifier.weight(1f)) {
                                    EarnedBadge(badge)
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (locked.isNotEmpty()) {
                SectionLabel(text = "🔒 LOCKED", color = SavingsColors.Locked, topPadding = 14.dp)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunked = locked.chunked(2)
                    chunked.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { badge ->
                                Box(modifier = Modifier.weight(1f)) {
                                    LockedBadge(badge)
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color, topPadding: Dp) {
    Text(
        text = text,
        color = color,
        fontSize = 10.5.sp,
        letterSpacing = 1.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = topPadding, bottom = 10.dp)
    )
}

@Composable
private fun EarnedBadge(achievement: Achievement) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(colors = listOf(Color(0xFFFFFCF3), Color(0xFFFDF6E1)))
            )
            .border(1.dp, SavingsColors.Rule, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(achievement.medalColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = achievement.emoji, fontSize = 17.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = achievement.title,
                color = SavingsColors.Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = achievement.subtitle,
                color = SavingsColors.SlateSoft,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SavingsColors.AmberSoft)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = "STAMPED ✓", color = SavingsColors.BrassDark, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // check mark corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clip(CircleShape)
                .background(SavingsColors.Emerald),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
        }
    }
}

@Composable
private fun LockedBadge(achievement: Achievement) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SavingsColors.LockedBg)
            .border(1.dp, SavingsColors.Rule, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3DFD3)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = achievement.emoji, fontSize = 17.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = achievement.title,
                color = Color(0xFF8B8776),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = achievement.subtitle,
                color = SavingsColors.SlateSoft,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            MiniProgressBar(percent = achievement.progressPercent)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "PROGRESS", color = Color(0xFF9C9885), fontSize = 10.sp)
                Text(text = "${achievement.progressPercent}%", color = Color(0xFF9C9885), fontSize = 10.sp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3DFD3)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF9C9885), modifier = Modifier.size(11.dp))
        }
    }
}

@Composable
private fun MiniProgressBar(percent: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE7E2D2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = percent / 100f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(colors = listOf(Color(0xFFC9C4B2), Color(0xFFADA88F)))
                )
        )
    }
}

// ---------------------------------------------------------------
// STREAK BANNER
// ---------------------------------------------------------------
@Composable
private fun StreakBanner(
    title: String,
    subtitle: String,
    litSegments: Int,
    totalSegments: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(SavingsColors.Ink2, SavingsColors.Ink3, Color(0xFF1C3C70))
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(colors = listOf(Color(0xFFFFB86B), SavingsColors.Amber))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🔥", fontSize = 20.sp)
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(totalSegments) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index < litSegments) SavingsColors.BrassLight
                                else Color.White.copy(alpha = 0.18f)
                            )
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// GOALS
// ---------------------------------------------------------------
@Composable
private fun GoalsHeader(onAddGoalClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Savings Goals",
            color = SavingsColors.Ink,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(colors = listOf(SavingsColors.Ink3, SavingsColors.Ink))
                )
                .clickable { onAddGoalClick() }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = "Add Goal", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GoalCard(goal: SavingsGoalUI, modifier: Modifier = Modifier) {
    SavingsCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(goal.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = goal.emoji, fontSize = 17.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        color = SavingsColors.Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "⏱️ ${goal.dueText}",
                        color = SavingsColors.SlateSoft,
                        fontSize = 10.5.sp
                    )
                }

                Text(
                    text = "${goal.percent}%",
                    color = goal.progressColor,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            CoinTrackProgressBar(percent = goal.percent, color = goal.progressColor)

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = goal.savedLabel, color = SavingsColors.Emerald, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Text(text = goal.totalLabel, color = SavingsColors.SlateSoft, fontSize = 11.5.sp)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatChip(label = "REMAINING", value = goal.remainingLabel, modifier = Modifier.weight(1f))
                StatChip(
                    label = "SAVE / MONTH",
                    value = goal.perMonthLabel,
                    valueColor = Color(0xFF2B4B8C),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = SavingsColors.Ink
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFF7F5EE))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = SavingsColors.SlateSoft, fontSize = 9.5.sp, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(2.dp))
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CoinTrackProgressBar(percent: Int, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFEFEBDF))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val tickColor = Color.White.copy(alpha = 0.35f)
                val step = 9.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = tickColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = null
                    )
                    x += step
                }
            }
        }
    }
}

@Composable
private fun SavingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SavingsColors.Paper)
            .border(1.dp, SavingsColors.Rule.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
    ) {
        content()
    }
}
