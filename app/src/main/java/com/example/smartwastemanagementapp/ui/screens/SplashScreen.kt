package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.theme.EcoGreen40
import com.example.smartwastemanagementapp.ui.theme.EcoGreen50
import com.example.smartwastemanagementapp.ui.theme.Teal40
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    // Navigate after delay
    LaunchedEffect(Unit) {
        delay(2500)
        currentOnTimeout()
    }

    // Fade-in + scale-up animation for the logo card
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)           // tiny delay so first frame renders the gradient first
        animStarted = true
    }

    val logoScale by animateFloatAsState(
        targetValue   = if (animStarted) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "logo_scale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue   = if (animStarted) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "alpha"
    )
    val textOffset by animateFloatAsState(
        targetValue   = if (animStarted) 0f else 40f,
        animationSpec = tween(800, delayMillis = 200, easing = FastOutSlowInEasing),
        label         = "text_offset"
    )

    // Infinite pulse for background rings
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 0.88f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Three loading dots – each gets its own infiniteTransition value at the top level
    val dot1 by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label         = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 150), RepeatMode.Reverse),
        label         = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 300), RepeatMode.Reverse),
        label         = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(EcoGreen40, EcoGreen50, Teal40))
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing decorative rings (drawn behind content)
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .alpha(0.07f)
                .background(Color.White, RoundedCornerShape(50))
        )
        Box(
            modifier = Modifier
                .size(210.dp)
                .scale(1.12f - (pulseScale - 0.88f))
                .alpha(0.05f)
                .background(Color.White, RoundedCornerShape(50))
        )

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.alpha(contentAlpha)
        ) {
            // Logo card
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(R.drawable.app_logo),
                    contentDescription = "CivicFix Logo",
                    modifier           = Modifier.size(88.dp).padding(4.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text     = "CivicFix",
                style    = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight    = FontWeight.ExtraBold,
                    fontSize      = 36.sp,
                    letterSpacing = (-1).sp
                ),
                color    = Color.White,
                modifier = Modifier.offset(y = textOffset.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "Smart Civic Waste Management",
                style     = MaterialTheme.typography.bodyLarge,
                color     = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(56.dp))

            // Animated loading dots (declared at top level – no repeat{} Composable)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(8.dp).scale(dot1)
                        .background(Color.White.copy(0.7f), RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier.size(8.dp).scale(dot2)
                        .background(Color.White.copy(0.7f), RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier.size(8.dp).scale(dot3)
                        .background(Color.White.copy(0.7f), RoundedCornerShape(50))
                )
            }
        }
    }
}
