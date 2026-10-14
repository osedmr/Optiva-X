package com.optivax.app.presentation.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.optivax.app.presentation.theme.OptivaColors

/** Category discovery has no ViewModel and never scans storage on entry. */
@Composable
fun FilesHomeScreen(onOpenCategory: (FilesCategoryTab) -> Unit) {
    Surface(color = OptivaColors.CanvasBase, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Dosyalar", style = MaterialTheme.typography.headlineMedium)
                Text("İncelemek istediğiniz kategoriyi seçin. Dosyalar kendiliğinden silinmez.",
                    color = OptivaColors.TextSecondary, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
            }
            FilesCategoryTab.entries.forEachIndexed { index, category ->
                if (index == 0 || index == 2) {
                    item {
                        Text(if (index == 0) "Mesajlaşma medyası" else "Dosya temizliği",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                }
                item(key = category.name) {
                    Card(onClick = { onOpenCategory(category) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(category.title, style = MaterialTheme.typography.titleMedium)
                            Text(category.description, style = MaterialTheme.typography.bodyMedium,
                                color = OptivaColors.TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
