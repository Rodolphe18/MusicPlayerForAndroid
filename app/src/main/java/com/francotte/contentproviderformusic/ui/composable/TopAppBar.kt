package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.annotation.DrawableRes
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.ui.theme.Aurora

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
    Box(
        Modifier
            .fillMaxWidth()
            .background(Aurora.Night)
    ) {
        CenterAlignedTopAppBar(
            title = {
                if (searchActive) {
                    SearchField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange
                    )
                } else {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            },
            navigationIcon = {
                if (searchActive) {
                    IconButton(onClick = onSearchClose) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Close search",
                            tint = Color.White
                        )
                    }
                } else {
                    IconButton(onClick = onSearchOpen) {
                        Icon(
                            painter = painterResource(leftIcon),
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }
            },
            actions = {
                if (searchActive) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "Clear",
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    IconButton(onClick = onActionClick) {
                        Icon(
                            painter = painterResource(rightIcon),
                            contentDescription = actionIconContentDescription,
                            tint = Color.White
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = modifier,
        )
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
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            if (query.isEmpty()) {
                Text(
                    text = "Search for a song…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            innerTextField()
        }
    )
}
