package com.monkeycode.liquidui.ui.screens

import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.components.EmptyState
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.InfoBanner
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SecondaryButton
import com.monkeycode.liquidui.ui.components.StatTile
import com.monkeycode.liquidui.ui.components.Tag
import com.monkeycode.liquidui.ui.theme.AuroraAmber
import com.monkeycode.liquidui.ui.theme.AuroraMint
import com.monkeycode.liquidui.ui.theme.AuroraRose
import com.monkeycode.liquidui.ui.theme.AuroraViolet
import com.monkeycode.liquidui.ui.motion.Motion
import com.monkeycode.liquidui.ui.motion.StaggeredReveal
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.res.stringResource

private data class Principle(
    val index: String,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    val accent: androidx.compose.ui.graphics.Color
)


private data class FeatureMaterial(
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val statusRes: Int,
    val accent: androidx.compose.ui.graphics.Color
)

/** 首页「特色素材」5 条。status 只有三种取值，重复的共用一条资源。 */
private val featureMaterials = listOf(
    FeatureMaterial(R.string.home_material_01_name, R.string.home_material_01_description, R.string.home_material_status_integrated, AuroraMint),
    FeatureMaterial(R.string.home_material_02_name, R.string.home_material_02_description, R.string.home_material_status_new, AuroraAmber),
    FeatureMaterial(R.string.home_material_03_name, R.string.home_material_03_description, R.string.home_material_status_new, AuroraRose),
    FeatureMaterial(R.string.home_material_04_name, R.string.home_material_04_description, R.string.home_material_status_new, AuroraViolet),
    FeatureMaterial(R.string.home_material_05_name, R.string.home_material_05_description, R.string.home_material_status_asset, AuroraMint)
)

/** 首页「四条设计原则」。index 是 "01"–"04" 这种编号，不随语言变，留在代码里。 */
private val principles = listOf(
    Principle(
        index = "01",
        titleRes = R.string.home_principle_01_title,
        bodyRes = R.string.home_principle_01_body,
        accent = AuroraMint
    ),
    Principle(
        index = "02",
        titleRes = R.string.home_principle_02_title,
        bodyRes = R.string.home_principle_02_body,
        accent = AuroraViolet
    ),
    Principle(
        index = "03",
        titleRes = R.string.home_principle_03_title,
        bodyRes = R.string.home_principle_03_body,
        accent = AuroraAmber
    ),
    Principle(
        index = "04",
        titleRes = R.string.home_principle_04_title,
        bodyRes = R.string.home_principle_04_body,
        accent = AuroraRose
    )
)

@Composable
fun HomeScreen(
    settings: AppSettingsState,
    onOpenComponents: () -> Unit,
    onOpenMotion: () -> Unit
) {
    val staggerEnabled = !settings.reducedMotion

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            StaggeredReveal(index = 0, enabled = staggerEnabled) {
                Column(
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Tag(text = "Android · Jetpack Compose")
                    Text(
                        text = stringResource(R.string.home_text_1),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.home_text_2) +
                            stringResource(R.string.home_text_3),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            StaggeredReveal(index = 1, enabled = staggerEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        label = stringResource(R.string.home_label_1),
                        value = "42",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = stringResource(R.string.home_label_2),
                        value = "18",
                        accent = AuroraMint,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = stringResource(R.string.home_label_3),
                        value = "7",
                        accent = AuroraViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            StaggeredReveal(index = 2, enabled = staggerEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryButton(
                        onClick = onOpenComponents,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.home_text_4)) }
                    SecondaryButton(
                        onClick = onOpenMotion,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.home_text_5)) }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_text_6),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 22.dp, top = 12.dp)
            )
        }

        itemsIndexed(principles) { index, principle ->
            StaggeredReveal(
                index = 3 + index,
                enabled = staggerEnabled,
                staggerMs = Motion.Stagger.StandardList
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = principle.index,
                            style = MaterialTheme.typography.displaySmall,
                            color = principle.accent.copy(alpha = 0.85f)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(principle.titleRes),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(principle.bodyRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_text_7),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 22.dp, top = 14.dp)
            )
        }

        itemsIndexed(featureMaterials) { index, material ->
            StaggeredReveal(index = 8 + index, enabled = staggerEnabled) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Tag(text = stringResource(material.statusRes), accent = material.accent)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = stringResource(material.nameRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(material.descriptionRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            StaggeredReveal(index = 9, enabled = staggerEnabled) {
                InfoBanner(
                    text = stringResource(R.string.home_text_8),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
