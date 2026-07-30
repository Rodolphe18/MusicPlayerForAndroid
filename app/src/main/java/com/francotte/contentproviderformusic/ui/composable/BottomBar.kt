package com.francotte.contentproviderformusic.ui.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import com.francotte.contentproviderformusic.ui.theme.Aurora
import kotlinx.collections.immutable.ImmutableList

/** Gris neutre des onglets non sélectionnés de la bottom bar. */
private val UnselectedGray = Color(0xFF9E9E9E)

/** Hauteur compacte de la bottom bar et de ses onglets. */
val BottomBarHeight = 68.dp


@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    destinations: ImmutableList<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?
) {
    NavigationBar(
        modifier = modifier
            .height(BottomBarHeight)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - 2.dp.toPx()
                drawLine(
                    color = Color(0xFFE5E5E5),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            },
        containerColor = Aurora.CoralBackground,
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)

            CustomNavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        painter = painterResource(destination.icon),
                        contentDescription = null
                    )
                },
                selectedIcon = {
                    Icon(
                        painter = painterResource(destination.selectedIcon),
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        stringResource(destination.titleTextId),
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        modifier = Modifier.offset(y = (-2).dp),
                    )
                }
            )
        }
    }
}

/**
 * Onglet custom : pas de pastille indicatrice, icône + libellé teintés en corail quand
 * sélectionné (gris sinon), avec un espacement vertical réduit entre l'icône et le texte.
 */
@Composable
fun RowScope.CustomNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: @Composable () -> Unit = icon,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
) {
    val tint = if (selected) Aurora.VividCoral else UnselectedGray
    Column(
        modifier = modifier
            .weight(1f)
            .height(BottomBarHeight)
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides tint) {
            if (selected) selectedIcon() else icon()
        }
        if (label != null && (alwaysShowLabel || selected)) {
            CompositionLocalProvider(LocalContentColor provides tint) {
                label()
            }
        }
    }
}



