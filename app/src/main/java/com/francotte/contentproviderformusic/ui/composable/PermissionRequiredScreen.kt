package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.ui.theme.Aurora

/**
 * Écran affiché quand l'accès aux fichiers audio est refusé.
 *
 * Sans lui, l'app ne composait rien du tout et l'utilisateur restait devant un écran
 * blanc, sans explication ni moyen de s'en sortir : le système ne redemande plus
 * après deux refus, l'app était donc définitivement inutilisable.
 */
@Composable
fun PermissionRequiredScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Aurora.CoralBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            EmptyState(
                icon = R.drawable.ic_settings,
                title = stringResource(R.string.permission_required_title),
                subtitle = stringResource(R.string.permission_required_message),
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Aurora.Purple,
                    contentColor = Color.White,
                ),
            ) {
                Text(text = stringResource(R.string.permission_open_settings))
            }
        }
    }
}
