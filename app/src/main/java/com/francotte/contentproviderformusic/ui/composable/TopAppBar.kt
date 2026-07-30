package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.annotation.DrawableRes
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongAppBar(
    modifier: Modifier = Modifier,
    title: String,
    @DrawableRes leftIcon: Int,
    @DrawableRes rightIcon: Int,
    searchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    actionIconContentDescription: String? = null,
    onActionClick: () -> Unit = {}
) {
    // En-tête aligné à gauche (style onglet "Mes playlists"), destiné à être placé comme
    // premier item de la LazyColumn : il défile donc avec la liste. Les actions recherche/
    // réglages (corail foncé) sont en bout de ligne. Aucun fond propre : celui de la liste
    // (conteneur du Scaffold) transparaît.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (searchActive) {
            IconButton(onClick = onSearchClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.cd_close_search),
                    tint = Color.Black,
                )
            }
            Box(Modifier.weight(1f)) {
                SearchField(query = searchQuery, onQueryChange = onSearchQueryChange)
            }
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.cd_clear),
                        tint = Color.Black,
                    )
                }
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSearchOpen) {
                Icon(
                    painter = painterResource(leftIcon),
                    contentDescription = stringResource(R.string.cd_search),
                    tint = Color.Black,
                )
            }
            IconButton(onClick = onActionClick) {
                Icon(
                    painter = painterResource(rightIcon),
                    contentDescription = actionIconContentDescription,
                    tint = Color.Black,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        cursorBrush = SolidColor(Color.Black),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
            innerTextField()
        }
    )
}
