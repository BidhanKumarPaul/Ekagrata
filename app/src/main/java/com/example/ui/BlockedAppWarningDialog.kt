package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppInfo
import com.example.ui.components.BkpWatermark
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusCrimson
import com.example.ui.theme.SoftSkyBlue

@Composable
fun BlockedAppWarningDialog(
    app: AppInfo,
    activeGoalTitle: String,
    onBackToFocus: () -> Unit,
    onOpenAnyway: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onBackToFocus,
        containerColor = DeepObsidian,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = FocusAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Distraction Shield Active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "You are currently immersed in Kendrīkaraṇa for:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"$activeGoalTitle\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SoftSkyBlue
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "${app.label} is strictly blocked during Kendrīkaraṇa. Only apps in the Urvarā section are permitted to be used.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                BkpWatermark(subtle = true)
            }
        },
        confirmButton = {
            Button(
                onClick = onBackToFocus,
                modifier = Modifier.testTag("back_to_focus_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftSkyBlue,
                    contentColor = DeepObsidian
                )
            ) {
                Text("Stay Focused", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onOpenAnyway,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusCrimson)
            ) {
                Text("End Kendrīkaraṇa")
            }
        }
    )
}
