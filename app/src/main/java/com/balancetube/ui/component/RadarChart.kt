package com.balancetube.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balancetube.domain.model.Category
import com.balancetube.domain.model.CategoryScore
import com.balancetube.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun RadarChart(
    categoryScores: List<CategoryScore>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.size(300.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f * 0.7f

        // Number of categories (should be 6)
        val categories = categoryScores.size
        val angleStep = 2 * PI / categories

        // Draw background web (grid lines)
        val levels = 5
        for (level in 1..levels) {
            val path = Path()
            val levelRadius = radius * (level.toFloat() / levels)

            for (i in 0 until categories) {
                val angle = i * angleStep - PI / 2
                val x = center.x + levelRadius * cos(angle).toFloat()
                val y = center.y + levelRadius * sin(angle).toFloat()

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()

            drawPath(
                path = path,
                color = Color.LightGray,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw axes
        for (i in 0 until categories) {
            val angle = i * angleStep - PI / 2
            val endX = center.x + radius * cos(angle).toFloat()
            val endY = center.y + radius * sin(angle).toFloat()

            drawLine(
                color = Color.LightGray,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw data polygon
        val dataPath = Path()
        categoryScores.forEachIndexed { index, categoryScore ->
            val angle = index * angleStep - PI / 2
            val normalizedValue = categoryScore.normalizedScore / 100f
            val dataRadius = radius * normalizedValue
            val x = center.x + dataRadius * cos(angle).toFloat()
            val y = center.y + dataRadius * sin(angle).toFloat()

            if (index == 0) {
                dataPath.moveTo(x, y)
            } else {
                dataPath.lineTo(x, y)
            }
        }
        dataPath.close()

        // Fill
        drawPath(
            path = dataPath,
            color = Color(0xFF2196F3).copy(alpha = 0.3f)
        )

        // Stroke
        drawPath(
            path = dataPath,
            color = Color(0xFF2196F3),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw category labels
        categoryScores.forEachIndexed { index, categoryScore ->
            val angle = index * angleStep - PI / 2
            val labelRadius = radius * 1.2f
            val x = center.x + labelRadius * cos(angle).toFloat()
            val y = center.y + labelRadius * sin(angle).toFloat()

            val categoryColor = getCategoryColor(categoryScore.category)
            val label = categoryScore.category.displayName

            val textLayoutResult = textMeasurer.measure(
                text = label,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = categoryColor
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x - textLayoutResult.size.width / 2f,
                    y - textLayoutResult.size.height / 2f
                )
            )
        }

        // Draw data points
        categoryScores.forEachIndexed { index, categoryScore ->
            val angle = index * angleStep - PI / 2
            val normalizedValue = categoryScore.normalizedScore / 100f
            val dataRadius = radius * normalizedValue
            val x = center.x + dataRadius * cos(angle).toFloat()
            val y = center.y + dataRadius * sin(angle).toFloat()

            drawCircle(
                color = Color(0xFF2196F3),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

private fun getCategoryColor(category: Category): Color {
    return when (category) {
        Category.KNOWLEDGE -> KnowledgeColor
        Category.ENTERTAINMENT -> EntertainmentColor
        Category.LIFESTYLE -> LifestyleColor
        Category.ARTS_MUSIC -> ArtsMusicColor
        Category.SELF_IMPROVEMENT -> SelfImprovementColor
        Category.SOCIAL_CREATOR -> SocialCreatorColor
    }
}
