package com.jarvis.nchat.presentation.bottomnav

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

// 1. Temporary Data Model for Preview
@Composable
fun AnimatedBottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White,
    unselectedColor: Color = Color.DarkGray,
    bubbleGradientStart: Color = Color(0xFB695BCB),
    bubbleGradientEnd: Color = Color(0xFF695BCB),
    barHeight: androidx.compose.ui.unit.Dp = 70.dp,
    bubbleSize: androidx.compose.ui.unit.Dp = 48.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    transitionDurationMs: Int = 650 // Smooth timing for the full effect
) {
    val selectedIndex by remember(currentRoute, items) {
        derivedStateOf { items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0) }
    }
    val density = LocalDensity.current

    val gap = 6.dp
    val topPokeSpace = bubbleSize / 2
    val totalHeight = barHeight + topPokeSpace

    val notchPath = remember { Path() }
    val slideEasing = remember { FastOutSlowInEasing }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        val slotWidth = this.maxWidth / items.size

        // Horizontal Movement
        val bubbleCenterX by animateDpAsState(
            targetValue = slotWidth * selectedIndex + slotWidth / 2,
            animationSpec = tween(durationMillis = transitionDurationMs, easing = slideEasing),
            label = "bubbleCenterX"
        )

        val bubbleOffsetY = remember { Animatable(0f) }
        val jumpPx = with(density) { 26.dp.toPx() }

        // Icon inside the ball animation state
        val ballIconScale = remember { Animatable(1f) }
        var activeIcon by remember { mutableStateOf(items[selectedIndex].icon) }

        LaunchedEffect(selectedIndex) {
            val riseMs = (transitionDurationMs * 0.45f).toInt()
            val fallMs = transitionDurationMs - riseMs

            // Launching the Jump Animation
            launch {
                bubbleOffsetY.snapTo(0f)
                bubbleOffsetY.animateTo(
                    targetValue = -jumpPx,
                    animationSpec = tween(durationMillis = riseMs, easing = LinearOutSlowInEasing)
                )
                bubbleOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = fallMs, easing = FastOutLinearInEasing)
                )
            }

            // Launching the Empty Ball & Merge Animation
            launch {
                // 1. Icon disappears immediately so ball travels EMPTY
                ballIconScale.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
                )

                // Swap the icon while it's invisible
                activeIcon = items[selectedIndex].icon

                // Wait for the ball to fly and arrive near the target
                delay(200)

                // 2. Icon appears right as the ball lands (merging effect)
                ballIconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )
            }
        }

        // WHITE BACKGROUND BAR WITH U-CUTOUT
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
        ) {
            val cx = with(density) { bubbleCenterX.toPx() }
            val cutoutRadius = with(density) { (bubbleSize / 2 + gap).toPx() }
            val corner = with(density) { cornerRadius.toPx() }
            val w = size.width
            val h = size.height
            val curveWidth = cutoutRadius * 1.8f
            val depth = cutoutRadius * 1.15f

            notchPath.reset()
            notchPath.moveTo(0f, corner)
            notchPath.quadraticTo(0f, 0f, corner, 0f)
            notchPath.lineTo(cx - curveWidth, 0f)
            notchPath.cubicTo(
                cx - curveWidth / 2, 0f,
                cx - cutoutRadius, depth,
                cx, depth
            )
            notchPath.cubicTo(
                cx + cutoutRadius, depth,
                cx + curveWidth / 2, 0f,
                cx + curveWidth, 0f
            )
            notchPath.lineTo(w - corner, 0f)
            notchPath.quadraticTo(w, 0f, w, corner)
            notchPath.lineTo(w, h)
            notchPath.lineTo(0f, h)
            notchPath.close()

            drawPath(notchPath, color = barColor)
        }

        // BACKGROUND GREY ICONS IN THE BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .noRippleClickable { onItemSelected(item.route) },
                    contentAlignment = Alignment.Center
                ) {
                    // Syncs with the ball landing. Waits 350ms before disappearing!
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 0f else 1f,
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = if (isSelected) 350 else 0
                        ),
                        label = "icon_scale"
                    )
                    if (scale > 0f) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = unselectedColor,
                            modifier = Modifier
                                .size(26.dp)
                                .scale(scale)
                        )
                    }
                }
            }
        }

        // THE PINK FLOATING BALL
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(bubbleSize)
                .graphicsLayer {
                    translationX = with(density) { (bubbleCenterX - bubbleSize / 2).toPx() }
                    val baseDrop = with(density) { 6.dp.toPx() }
                    translationY = with(density) { (topPokeSpace - bubbleSize / 2).toPx() } + baseDrop + bubbleOffsetY.value
                }
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(bubbleGradientStart, bubbleGradientEnd)
                    )
                )
                .noRippleClickable { onItemSelected(items[selectedIndex].route) },
            contentAlignment = Alignment.Center
        ) {
            // Icon dynamically scales to create the "going inside" effect
            Icon(
                imageVector = activeIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .scale(ballIconScale.value)
            )
        }
    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}




// 2. The Preview Composable
@Preview(showBackground = true, name = "Animated Bottom Bar Preview")
@Composable
fun AnimatedBottomNavBarPreview() {
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("search", "Search", Icons.Default.Search),
        BottomNavItem("profile", "Profile", Icons.Default.Person),
        BottomNavItem("settings", "Settings", Icons.Default.Settings)
    )

    var currentRoute by remember { mutableStateOf("home") }

    Scaffold(
        containerColor = Color(0xFFF0F0F0), // Light grey background to show white bar
        bottomBar = {
            AnimatedBottomNavBar(
                items = items,
                currentRoute = currentRoute,
                onItemSelected = { route -> currentRoute = route }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}