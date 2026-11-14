@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.pulsehealth.app.ui.summary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.FavoriteCardId
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink

@Composable
fun FavoritesSheet(
    hidden: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text("Edit favorites", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Choose the metrics shown on your Summary.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            FavoriteCardId.ALL.forEach { card ->
                val isHidden = card.canonicalId in hidden || card.legacyAlias in hidden
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(card.canonicalId) }
                        .padding(vertical = 13.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "${if (isHidden) "Add" else "Remove"} ${card.defaultTitle} from favorites"
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(card.defaultTitle, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text(
                        if (isHidden) "Add" else "Remove",
                        color = if (isHidden) Color(0xFF087CF0) else PulsePink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            }
        }
    }
}
