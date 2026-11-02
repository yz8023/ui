package com.monkeycode.liquidui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.CompactSwitch
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.GlassMetricCard
import com.monkeycode.liquidui.ui.components.GlassHeroCard
import com.monkeycode.liquidui.ui.components.GlassCapsuleButton
import com.monkeycode.liquidui.ui.components.GlassDialog
import com.monkeycode.liquidui.ui.components.GlassIconButton
import com.monkeycode.liquidui.ui.components.ErrorState
import com.monkeycode.liquidui.ui.components.GlassTextField
import com.monkeycode.liquidui.ui.components.LoadingState
import com.monkeycode.liquidui.ui.components.Meter
import com.monkeycode.liquidui.ui.components.OfflineBanner
import com.monkeycode.liquidui.ui.components.PreferenceCard
import com.monkeycode.liquidui.ui.components.PreferenceRow
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.components.SegmentedTabs
import com.monkeycode.liquidui.ui.components.SuccessBanner
import com.monkeycode.liquidui.ui.components.Tag
import com.monkeycode.liquidui.ui.theme.AppIcons
import com.monkeycode.liquidui.ui.theme.AuroraAmber
import com.monkeycode.liquidui.ui.theme.AuroraMint
import com.monkeycode.liquidui.ui.theme.AuroraRose
import com.monkeycode.liquidui.ui.theme.AuroraViolet

@Composable
fun ComponentsScreen() {
    var text by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var analytics by rememberSaveable { mutableStateOf(false) }
    var segmented by rememberSaveable { mutableIntStateOf(0) }
    var meter by remember { mutableFloatStateOf(0.62f) }
    var showDialog by remember { mutableStateOf(false) }
    var capsule by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.components_text_1),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.components_text_2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_1), Modifier.padding(top = 8.dp)) }
        item {
            GlassHeroCard(
                title = stringResource(R.string.components_title_1),
                value = stringResource(R.string.components_value_1),
                badge = stringResource(R.string.components_text_3),
                subtitle = stringResource(R.string.components_subtitle_1),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GlassMetricCard(stringResource(R.string.components_text_4), stringResource(R.string.components_text_5), Modifier.weight(1f), AuroraMint, stringResource(R.string.components_text_6))
                GlassMetricCard(stringResource(R.string.components_text_7), stringResource(R.string.components_text_8), Modifier.weight(1f), AuroraViolet, stringResource(R.string.components_text_9))
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(stringResource(R.string.common_all), stringResource(R.string.common_appearance), stringResource(R.string.components_text_10)).forEachIndexed { index, label ->
                    GlassCapsuleButton(capsule == index, label, { capsule = index })
                }
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_2)) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryButton(onClick = {}) { Text(stringResource(R.string.components_text_11)) }
                    SecondaryButton(onClick = {}) { Text(stringResource(R.string.components_text_12)) }
                    GlassIconButton(
                        icon = AppIcons.Notifications,
                        contentDescription = stringResource(R.string.common_notification),
                        onClick = {}
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryButton(onClick = {}, enabled = false) { Text(stringResource(R.string.components_text_13)) }
                    Tag(text = "ease-out-back · 180ms")
                }
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_3)) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                GlassTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = stringResource(R.string.components_label_1),
                    placeholder = stringResource(R.string.components_placeholder_1),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.components_label_2),
                    supportingText = stringResource(R.string.components_text_14),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_4)) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                SegmentedTabs(
                    labels = listOf(stringResource(R.string.components_text_15), stringResource(R.string.common_light), stringResource(R.string.common_dark)),
                    selectedIndex = segmented,
                    onSelected = { segmented = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Tag(text = stringResource(R.string.components_text_16), accent = AuroraMint)
                    Tag(text = stringResource(R.string.components_text_17), accent = AuroraAmber)
                    Tag(text = stringResource(R.string.components_text_18), accent = AuroraRose)
                    Tag(text = stringResource(R.string.components_text_19), accent = AuroraViolet)
                }
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_5)) }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Notifications,
                    title = stringResource(R.string.components_title_2),
                    subtitle = stringResource(R.string.components_subtitle_2),
                    trailing = {
                        CompactSwitch(
                            checked = notifications,
                            onCheckedChange = { notifications = it }
                        )
                    }
                )
            }
        }
        item {
            PreferenceCard {
                PreferenceRow(
                    icon = AppIcons.Shield,
                    title = stringResource(R.string.components_title_3),
                    subtitle = stringResource(R.string.components_subtitle_3),
                    trailing = {
                        CompactSwitch(
                            checked = analytics,
                            onCheckedChange = { analytics = it }
                        )
                    }
                )
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_6)) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(meter * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
                    SecondaryButton(onClick = {
                        meter = when {
                            meter < 0.3f -> 0.62f
                            meter < 0.7f -> 1f
                            else -> 0.18f
                        }
                    }) { Text(stringResource(R.string.components_text_20)) }
                }
                Spacer(Modifier.height(12.dp))
                Meter(progress = meter)
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_7)) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                LoadingState(
                    title = stringResource(R.string.components_title_4),
                    message = stringResource(R.string.components_message_1),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                SuccessBanner(stringResource(R.string.components_text_21))
                Spacer(Modifier.height(8.dp))
                OfflineBanner(text = stringResource(R.string.components_text_22))
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                ErrorState(
                    title = stringResource(R.string.components_title_5),
                    message = stringResource(R.string.components_message_2),
                    action = { SecondaryButton(onClick = {}) { Text(stringResource(R.string.common_retry)) } }
                )
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_8)) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.components_text_23),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton(onClick = { showDialog = true }) { Text(stringResource(R.string.components_text_24)) }
            }
        }

        item { SectionTitle(stringResource(R.string.components_section_title_9)) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val swatches = listOf(
                    "primary" to MaterialTheme.colorScheme.primary,
                    "secondary" to MaterialTheme.colorScheme.secondary,
                    "tertiary" to MaterialTheme.colorScheme.tertiary,
                    "mint" to AuroraMint,
                    "amber" to AuroraAmber,
                    "rose" to AuroraRose
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    swatches.forEach { (name, color) ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .liquidGlassSwatch(color)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        GlassDialog(
            onDismissRequest = { showDialog = false },
            title = stringResource(R.string.components_title_6),
            onConfirm = { showDialog = false },
            confirmText = stringResource(R.string.components_confirm_1)
        ) {
            Text(
                text = stringResource(R.string.components_text_25),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Modifier.liquidGlassSwatch(color: Color): Modifier =
    clip(RoundedCornerShape(14.dp)).background(color)
