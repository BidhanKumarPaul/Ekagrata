package com.example.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.model.AppCategory
import com.example.ui.theme.AcademicIndigo
import com.example.ui.theme.CalmEmerald
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.SoftSkyBlue

@Composable
fun AppIconView(
    packageName: String,
    label: String,
    category: AppCategory,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val iconBitmap = produceState<android.graphics.Bitmap?>(initialValue = null, packageName) {
        value = try {
            val pm = context.packageManager
            val drawable: Drawable = pm.getApplicationIcon(packageName)
            val px = (size.value * context.resources.displayMetrics.density).toInt().coerceAtLeast(64)
            drawable.toBitmap(width = px, height = px)
        } catch (e: Exception) {
            null
        }
    }.value

    val bgColor = when (category) {
        AppCategory.URVARA -> SoftSkyBlue.copy(alpha = 0.2f)
        AppCategory.STUDY -> CalmEmerald.copy(alpha = 0.2f)
        AppCategory.WORK -> AcademicIndigo.copy(alpha = 0.2f)
        AppCategory.COMMUNICATION -> SoftSkyBlue.copy(alpha = 0.15f)
        AppCategory.SOCIAL, AppCategory.ENTERTAINMENT -> FocusAmber.copy(alpha = 0.2f)
        else -> CardSurface
    }

    val textColor = when (category) {
        AppCategory.URVARA -> SoftSkyBlue
        AppCategory.STUDY -> CalmEmerald
        AppCategory.WORK -> AcademicIndigo
        else -> MaterialTheme.colorScheme.onSurface
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap.asImageBitmap(),
            contentDescription = label,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            val initial = label.firstOrNull()?.uppercase() ?: "A"
            Text(
                text = initial,
                color = textColor,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
