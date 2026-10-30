package com.monkeycode.liquidui.ui.features.ui2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF01
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF02
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF03
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF04
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF05
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF06
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF07
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF08
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF09
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF10
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF11
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF12
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF13
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF14
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF15
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF16
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF17
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF18
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF19
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF20
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF21
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF22
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF23
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF24
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF25
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF26
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF27
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF28
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF29
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF30
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF31
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF32
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF33
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF34
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF35
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF36
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF37
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF38
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF39
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF40
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF41
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF42
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF43
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF44
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF45
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewF46
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewG01
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewG02
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewG03
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewG04
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewG05
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewG06
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewM01
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewM02
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewM03
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewM04
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewM05
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewM06
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewM07
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV01
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV02
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV03
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV04
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV05
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV06
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV07
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV08
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewV09
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewX01
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewX02
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewX03
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewX04
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewW01
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewW02
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewW03
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewT01
import com.monkeycode.liquidui.ui.features.ui2.previews.PreviewT02

/** UI2 体验馆预览分发：每条都是真实可操作的 Compose 实现（非 HTML、非截图）。 */
@Composable
fun Ui2Preview(feature: Ui2Feature, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        when (feature.id) {
            "F01" -> PreviewF01()
            "F02" -> PreviewF02()
            "F03" -> PreviewF03()
            "F04" -> PreviewF04()
            "F05" -> PreviewF05()
            "F06" -> PreviewF06()
            "F07" -> PreviewF07()
            "F08" -> PreviewF08()
            "F09" -> PreviewF09()
            "F10" -> PreviewF10()
            "F11" -> PreviewF11()
            "F12" -> PreviewF12()
            "F13" -> PreviewF13()
            "F14" -> PreviewF14()
            "F15" -> PreviewF15()
            "F16" -> PreviewF16()
            "F17" -> PreviewF17()
            "F18" -> PreviewF18()
            "F19" -> PreviewF19()
            "F20" -> PreviewF20()
            "F21" -> PreviewF21()
            "F22" -> PreviewF22()
            "F23" -> PreviewF23()
            "F24" -> PreviewF24()
            "F25" -> PreviewF25()
            "F26" -> PreviewF26()
            "F27" -> PreviewF27()
            "F28" -> PreviewF28()
            "F29" -> PreviewF29()
            "F30" -> PreviewF30()
            "F31" -> PreviewF31()
            "F32" -> PreviewF32()
            "F33" -> PreviewF33()
            "F34" -> PreviewF34()
            "F35" -> PreviewF35()
            "F36" -> PreviewF36()
            "F37" -> PreviewF37()
            "F38" -> PreviewF38()
            "F39" -> PreviewF39()
            "F40" -> PreviewF40()
            "F41" -> PreviewF41()
            "F42" -> PreviewF42()
            "F43" -> PreviewF43()
            "F44" -> PreviewF44()
            "F45" -> PreviewF45()
            "F46" -> PreviewF46()
            "G01" -> PreviewG01()
            "G02" -> PreviewG02()
            "G03" -> PreviewG03()
            "G04" -> PreviewG04()
            "G05" -> PreviewG05()
            "G06" -> PreviewG06()
            "M01" -> PreviewM01()
            "M02" -> PreviewM02()
            "M03" -> PreviewM03()
            "M04" -> PreviewM04()
            "M05" -> PreviewM05()
            "M06" -> PreviewM06()
            "M07" -> PreviewM07()
            "V01" -> PreviewV01()
            "V02" -> PreviewV02()
            "V03" -> PreviewV03()
            "V04" -> PreviewV04()
            "V05" -> PreviewV05()
            "V06" -> PreviewV06()
            "V07" -> PreviewV07()
            "V08" -> PreviewV08()
            "V09" -> PreviewV09()
            "X01" -> PreviewX01()
            "X02" -> PreviewX02()
            "X03" -> PreviewX03()
            "X04" -> PreviewX04()
            "W01" -> PreviewW01()
            "W02" -> PreviewW02()
            "W03" -> PreviewW03()
            "T01" -> PreviewT01()
            "T02" -> PreviewT02()
            else -> Text(
                "预览建设中",
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
