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

    LaunchedEffect(Unit) {
        delay(2500)
        currentOnTimeout()
    }

    // Logo scale: 0.4 → 1.0
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    // Alpha: 0 → 1
    val alpha by animateFloatAsState(
        targetValue  = 1f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label        = "splash_alpha"
    )

    // Text slide-up offset: 40 → 0 dp
    val textOffset by animateFloatAsState(
        targetValue  = 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 300, easing = FastOutSlowInEasing),
        label        = "text_offset"
    )

    // Tagline fade
    val taglineAlpha by animateFloatAsState(
        targetValue  = 1f,
        animationSpec = tween(durationMillis = 600, delayMillis = 700, easing = LinearEasing),
        label        = "tagline_alpha"
    )

    // Pulsing ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue  = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EcoGreen40,
                        EcoGreen50,
                        Teal40
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative circles
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .alpha(0.08f)
                .background(Color.White, RoundedCornerShape(50))
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(1.1f - (pulseScale - 0.9f))
                .alpha(0.06f)
                .background(Color.White, RoundedCornerShape(50))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha)
        ) {
            // Logo card with shadow
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale)
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "CivicFix Logo",
                    modifier = Modifier
                        .size(90.dp)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App name
            Text(
                text = "CivicFix",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 36.sp,
                    letterSpacing = (-1).sp
                ),
                color  = Color.White,
                modifier = Modifier.offset(y = textOffset.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = "Smart Civic Waste Management",
                style    = MaterialTheme.typography.bodyLarge,
                color    = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(taglineAlpha)
            ) {
                repeat(3) { index ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue  = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, delayMillis = index * 150),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale)
                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}
