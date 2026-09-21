package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.SoftSkyBlue

@Composable
fun BkpWatermark(
    modifier: Modifier = Modifier,
    subtle: Boolean = false,
    asPill: Boolean = false
) {
    if (asPill) {
        Surface(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            color = CardSurface.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(FocusAmber, CircleShape)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "developed by BKP",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = SoftSkyBlue
                )
            }
        }
    } else {
        Row(
            modifier = modifier.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(
                        if (subtle) SoftSkyBlue.copy(alpha = 0.4f) else SoftSkyBlue,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "developed by BKP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (subtle) FontWeight.Normal else FontWeight.Medium,
                letterSpacing = 1.1.sp,
                color = if (subtle) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )
        }
    }
}
