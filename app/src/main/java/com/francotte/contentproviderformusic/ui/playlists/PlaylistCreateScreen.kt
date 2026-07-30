package com.francotte.contentproviderformusic.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.ui.composable.GlassTextField
import com.francotte.contentproviderformusic.ui.composable.GradientButton
import com.francotte.contentproviderformusic.ui.theme.Aurora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCreateScreen(
    onBack: () -> Unit,
    onCreate: (title: String, description: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Fond uni gris clair, volontairement sobre.
    val fieldBrush = SolidColor(Color(0xFFE8E8E8))
    val fieldText = Color.Black
    val fieldHint = Color(0xFF777777)

    Box(Modifier.fillMaxSize().background(Aurora.CoralBackground)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.create_playlist_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.cd_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                    ),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.playlist_name_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                    )
                    GlassTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = stringResource(R.string.playlist_name_hint),
                        singleLine = true,
                        containerBrush = fieldBrush,
                        textColor = fieldText,
                        placeholderColor = fieldHint,
                        cursorColor = fieldHint,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.playlist_description_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                    )
                    GlassTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = stringResource(R.string.playlist_description_hint),
                        singleLine = false,
                        minHeight = 110.dp,
                        containerBrush = fieldBrush,
                        textColor = fieldText,
                        placeholderColor = fieldHint,
                        cursorColor = fieldHint,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                GradientButton(
                    text = stringResource(R.string.create),
                    onClick = { onCreate(title.trim(), description.trim()) },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
