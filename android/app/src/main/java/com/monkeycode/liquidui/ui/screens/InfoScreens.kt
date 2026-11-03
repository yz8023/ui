package com.monkeycode.liquidui.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.BuildConfig
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.PrimaryButton
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.features.PermissionRequestCard
import com.monkeycode.liquidui.ui.features.PermissionSpec

private data class OpenSourceCredit(
    @StringRes val nameRes: Int,
    @StringRes val authorRes: Int,
    @StringRes val purposeRes: Int,
    @StringRes val licenseRes: Int,
    val url: String,
)

/**
 * 开源致谢 21 条（14–21 为批 D 开源取材八件套的来源申明）。显示字段（名字/作者/用途/许可证）都是 @StringRes，
 * **url 留在代码里**：它不是文案，既是 LazyColumn item 的 key，也是 Intent 的目标。
 */
private val allCredits = listOf(
    OpenSourceCredit(R.string.info_credit_01_name, R.string.info_credit_01_author, R.string.info_credit_01_purpose, R.string.info_credit_01_license, "https://github.com/L1angK1NG/StudyTrack"),
    OpenSourceCredit(R.string.info_credit_02_name, R.string.info_credit_02_author, R.string.info_credit_02_purpose, R.string.info_credit_02_license, "https://github.com/kd64i/dyparse"),
    OpenSourceCredit(R.string.info_credit_03_name, R.string.info_credit_03_author, R.string.info_credit_03_purpose, R.string.info_credit_03_license, "https://github.com/chibatching/Miuix"),
    OpenSourceCredit(R.string.info_credit_04_name, R.string.info_credit_04_author, R.string.info_credit_04_purpose, R.string.info_credit_04_license, "https://github.com/kyant0/backdrop-android"),
    OpenSourceCredit(R.string.info_credit_05_name, R.string.info_credit_05_author, R.string.info_credit_05_purpose, R.string.info_credit_05_license, "https://github.com/kyant0/shapes"),
    OpenSourceCredit(R.string.info_credit_06_name, R.string.info_credit_06_author, R.string.info_credit_06_purpose, R.string.info_credit_06_license, "https://github.com/feitangyuan/motion-web"),
    OpenSourceCredit(R.string.info_credit_07_name, R.string.info_credit_07_author, R.string.info_credit_07_purpose, R.string.info_credit_07_license, "https://www.framer.com/motion/"),
    OpenSourceCredit(R.string.info_credit_08_name, R.string.info_credit_08_author, R.string.info_credit_08_purpose, R.string.info_credit_08_license, "https://github.com/CYQawa/YunX"),
    OpenSourceCredit(R.string.info_credit_09_name, R.string.info_credit_09_author, R.string.info_credit_09_purpose, R.string.info_credit_09_license, "https://github.com/material-foundation/material-color-utilities"),
    OpenSourceCredit(R.string.info_credit_10_name, R.string.info_credit_10_author, R.string.info_credit_10_purpose, R.string.info_credit_10_license, "https://developer.android.com/compose"),
    OpenSourceCredit(R.string.info_credit_11_name, R.string.info_credit_11_author, R.string.info_credit_11_purpose, R.string.info_credit_11_license, "https://fonts.google.com/icons"),
    OpenSourceCredit(R.string.info_credit_12_name, R.string.info_credit_12_author, R.string.info_credit_12_purpose, R.string.info_credit_12_license, "https://developer.android.com/guide/topics/manifest/activity-alias-element"),
    OpenSourceCredit(R.string.info_credit_13_name, R.string.info_credit_13_author, R.string.info_credit_13_purpose, R.string.info_credit_13_license, "https://www.sitepoint.com/changing-your-android-apps-icon-dynamically/"),
    OpenSourceCredit(R.string.info_credit_14_name, R.string.info_credit_14_author, R.string.info_credit_14_purpose, R.string.info_credit_14_license, "https://github.com/material-foundation/material-color-utilities"),
    OpenSourceCredit(R.string.info_credit_15_name, R.string.info_credit_15_author, R.string.info_credit_15_purpose, R.string.info_credit_15_license, "https://github.com/google/material-design-icons"),
    OpenSourceCredit(R.string.info_credit_16_name, R.string.info_credit_16_author, R.string.info_credit_16_purpose, R.string.info_credit_16_license, "https://github.com/kizitonwose/Calendar"),
    OpenSourceCredit(R.string.info_credit_17_name, R.string.info_credit_17_author, R.string.info_credit_17_purpose, R.string.info_credit_17_license, "https://github.com/android/compose-samples"),
    OpenSourceCredit(R.string.info_credit_18_name, R.string.info_credit_18_author, R.string.info_credit_18_purpose, R.string.info_credit_18_license, "https://github.com/Calvin-LL/Reorderable"),
    OpenSourceCredit(R.string.info_credit_19_name, R.string.info_credit_19_author, R.string.info_credit_19_purpose, R.string.info_credit_19_license, "https://github.com/google/accompanist"),
    OpenSourceCredit(R.string.info_credit_20_name, R.string.info_credit_20_author, R.string.info_credit_20_purpose, R.string.info_credit_20_license, "https://github.com/microsoft/fluentui-android"),
    OpenSourceCredit(R.string.info_credit_21_name, R.string.info_credit_21_author, R.string.info_credit_21_purpose, R.string.info_credit_21_license, "https://github.com/google/Accessibility-Test-Framework-for-Android"),
)

@Composable
private fun InfoPage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.info_text_5),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp),
                )
                Text(title, style = MaterialTheme.typography.displayMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        content()
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    InfoPage(
        title = context.getString(R.string.info_title_1),
        subtitle = context.getString(R.string.info_subtitle_1),
        onBack = onBack,
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(context.getString(R.string.common_library_subtitle), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(context.getString(R.string.info_text_1), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("v${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(10.dp))
                Text(context.getString(R.string.info_text_2), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { SectionTitle(context.getString(R.string.info_section_title_1)) }
        allCredits.forEach { credit ->
            item(key = credit.url) {
                GlassCard(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(credit.url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(intent) }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(credit.nameRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            // 「作者：%1$s」的实参本身也是文案资源，这里嵌一层 stringResource
                            Text(stringResource(R.string.info_text_20, stringResource(credit.authorRes)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(credit.purposeRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(R.string.info_text_21, stringResource(credit.licenseRes)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(context.getString(R.string.info_text_3), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            PrimaryButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yz8023/ui")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) { Text(context.getString(R.string.info_text_4)) }
        }
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val tkDemoText31 = stringResource(R.string.tk_demo_text_31)
    val infoSubtitle2 = stringResource(R.string.info_subtitle_2)
    InfoPage(
        title = tkDemoText31,
        subtitle = infoSubtitle2,
        onBack = onBack,
    ) {
        item { SectionTitle(stringResource(R.string.info_section_title_1_2)) }
        item { InfoCard(stringResource(R.string.info_text_6), stringResource(R.string.info_text_7)) }
        item { InfoCard(stringResource(R.string.info_text_8), stringResource(R.string.info_text_9)) }
        item { InfoCard(stringResource(R.string.components_content_desc_1), stringResource(R.string.info_text_10)) }
        item { InfoCard(stringResource(R.string.info_text_11), stringResource(R.string.info_text_12)) }
        item { InfoCard(stringResource(R.string.info_text_13), stringResource(R.string.info_text_14)) }
    }
}

@Composable
fun PermissionCenterScreen(onBack: () -> Unit) {
    val pvNavText25 = stringResource(R.string.pv_nav_text_25)
    val infoSubtitle3 = stringResource(R.string.info_subtitle_3)
    InfoPage(
        title = pvNavText25,
        subtitle = infoSubtitle3,
        onBack = onBack,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item {
                PermissionRequestCard(
                    PermissionSpec(
                        permission = Manifest.permission.POST_NOTIFICATIONS,
                        titleRes = R.string.info_title_2,
                        descriptionRes = R.string.info_desc_1,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                InfoCard(
                    stringResource(R.string.info_text_15),
                    stringResource(R.string.info_text_16),
                )
            }
        } else {
            item {
                PermissionRequestCard(
                    PermissionSpec(
                        permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                        titleRes = R.string.info_title_3,
                        descriptionRes = R.string.info_desc_2,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { InfoCard(stringResource(R.string.info_title_2), stringResource(R.string.info_text_17)) }
        }
        item { InfoCard(stringResource(R.string.info_text_18), stringResource(R.string.info_text_19)) }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
