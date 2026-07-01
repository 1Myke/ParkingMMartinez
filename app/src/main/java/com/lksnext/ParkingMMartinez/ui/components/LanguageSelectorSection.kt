package com.lksnext.ParkingMMartinez.ui.components

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val languages = listOf(
        Pair("en", "English 🇬🇧"),
        Pair("es", "Español 🇪🇸"),
        Pair("eu", "Euskara \uD83C\uDDF5\uD83C\uDDF2"),
        Pair("fr", "Français 🇫🇷"),
        Pair("pt", "Português 🇵🇹"),
        Pair("de", "Deutsch 🇩🇪"),
        Pair("it", "Italiano 🇮🇹")
    )

    var expanded by remember { mutableStateOf(false) }

    val currentLocaleCode = configuration.locales[0]?.language ?: "en"
    val currentLanguageName = languages.find { it.first == currentLocaleCode }?.second ?: "English 🇬🇧"

    Box(modifier = modifier.width(180.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentLanguageName,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LksOrange,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .exposedDropdownSize()
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                languages.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (code == currentLocaleCode) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val localeManager = context.getSystemService(LocaleManager::class.java)
                                localeManager?.applicationLocales = LocaleList.forLanguageTags(code)
                            } else {
                                val locale = java.util.Locale.forLanguageTag(code)
                                java.util.Locale.setDefault(locale)
                                configuration.setLocale(locale)
                            }
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}