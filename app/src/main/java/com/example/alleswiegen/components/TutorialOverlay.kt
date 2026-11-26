package com.example.alleswiegen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.text.AnnotatedString

@Composable
fun TutorialHighlightBox(
    text: String,
    modifier: Modifier = Modifier
) {
    TutorialHighlightBox(
        annotatedText = AnnotatedString(text),
        modifier = modifier
    )
}

@Composable
fun TutorialHighlightBox(
    annotatedText: AnnotatedString,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = emptyMap()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TutorialPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TutorialPulseAlpha"
    )

    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Tutorial",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = annotatedText,
                inlineContent = inlineContent,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
