package com.francotte.contentproviderformusic.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.francotte.contentproviderformusic.BuildConfig
import com.francotte.contentproviderformusic.R
import com.francotte.contentproviderformusic.ui.MainViewModel
import com.francotte.contentproviderformusic.ui.theme.Aurora
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

private const val PRIVACY_POLICY_URL = "https://rodolphe18.github.io/musicplayer-privacy/"
private const val CONTACT_EMAIL = "rodolphefrancotte18@gmail.com"

// Teintes du dialog, alignées sur le thème Aurora.
private val DialogBg = Aurora.Night
private val TitleColor = Color.White
private val BodyColor = Color.White
private val SectionColor = Color(0xFFD6D8DC)

/**
 * Dialog des réglages (stateless), dans l'esprit de NowInAndroid mais au style MusicPlayer.
 */
@Composable
fun SettingsDialog(
    autoPlayOnStartup: Boolean,
    privacyOptionsRequired: Boolean,
    versionName: String,
    onToggleAutoPlay: (Boolean) -> Unit,
    onPrivacyPolicy: () -> Unit,
    onManageConsent: () -> Unit,
    onContact: () -> Unit,
    onOpenLicenses: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogBg,
        title = {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = TitleColor,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // --- Playback ---
                SectionTitle(stringResource(R.string.settings_playback))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_autoplay),
                        style = MaterialTheme.typography.bodyMedium,
                        color = BodyColor,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = autoPlayOnStartup,
                        onCheckedChange = onToggleAutoPlay,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Aurora.Purple,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
                        ),
                    )
                }

                // --- Privacy ---
                SectionTitle(stringResource(R.string.settings_privacy))
                SettingsRow(stringResource(R.string.settings_privacy_policy), onClick = onPrivacyPolicy)
                if (privacyOptionsRequired) {
                    SettingsRow(stringResource(R.string.settings_privacy_options), onClick = onManageConsent)
                }

                // --- About ---
                SectionTitle(stringResource(R.string.settings_about))
                Text(
                    text = stringResource(
                        R.string.settings_about_version,
                        stringResource(R.string.app_name),
                        versionName,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BodyColor,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                SettingsRow(stringResource(R.string.settings_contact), onClick = onContact)
                SettingsRow(stringResource(R.string.settings_oss_licenses), onClick = onOpenLicenses)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    style = MaterialTheme.typography.titleMedium,
                    color = SectionColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    )
}

/**
 * Wrapper branché sur le [MainViewModel] : câble état persistant, consentement et intents.
 */
@Composable
fun SettingsDialog(
    mainViewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val autoPlayOnStartup by mainViewModel.autoPlayOnStartup.collectAsStateWithLifecycle()

    SettingsDialog(
        autoPlayOnStartup = autoPlayOnStartup,
        privacyOptionsRequired = mainViewModel.isPrivacyOptionsRequired(),
        versionName = BuildConfig.VERSION_NAME,
        onToggleAutoPlay = mainViewModel::setAutoPlayOnStartup,
        onPrivacyPolicy = { context.openUrl(PRIVACY_POLICY_URL) },
        onManageConsent = { context.findActivity()?.let(mainViewModel::showPrivacyOptions) },
        onContact = { context.sendEmail(CONTACT_EMAIL) },
        onOpenLicenses = {
            OssLicensesMenuActivity.setActivityTitle(context.getString(R.string.settings_oss_licenses))
            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = SectionColor,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp),
    )
}

@Composable
private fun SettingsRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = BodyColor,
        fontWeight = FontWeight.Normal,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 4.dp, bottom = 8.dp),
    )
}

private fun Context.openUrl(url: String) {
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private fun Context.sendEmail(address: String) {
    runCatching { startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))) }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
