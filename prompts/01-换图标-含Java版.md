# F01 换图标提示词（Compose / Java 两版，换图标只认这一个功能）

```
按模板仓库 yz8023/ui 的 F01（docs/功能逐项手册.md）给我的 App 加"应用内换桌面图标"。
唯一权威实现：该仓库 android/app/src/main/java/com/monkeycode/liquidui/ui/features/AppIconSwitcher.kt
与同仓库 AndroidManifest.xml 的三 alias 段。不要使用该仓库"桌面图标动态切换"素材（已合并的历史变体），
不要混用两套 Manifest 结构。

我的情况：【图标套数与名称，如 3 套：默认/黑底/透明；我的 MainActivity 全名；包名;
UI 体系 Compose/View；语言 Kotlin/Java】

必须严格按下面做：
1. Manifest：MainActivity 本体去掉 LAUNCHER intent-filter；每套图标一个 <activity-alias>，
   只有默认那个 enabled="true"，其余 false；每个 alias 都要 exported="true"、
   targetActivity 指向 MainActivity、带 MAIN+LAUNCHER intent-filter。
2. 图标资源：每套一套 mipmap-anydpi-v26/ic_launcher_<名>.xml 自适应图标
   （background 颜色 + foreground 矢量 + monochrome），不要用裸 PNG 当 alias 图标。
3. 切换逻辑：先 setComponentEnabledSetting 启用目标，再逐个禁用其余，标志 DONT_KILL_APP；
   ComponentName 必须写完整类名（packageName + ".icon.Xxx"，不许只写相对名）；
   切换后逐个读回状态校验，失败要给用户可见提示，不要静默吞异常。
4. 当前选中项不要另存一份偏好当真相，直接读组件真实启用状态（可另存一份仅作 UI 兜底）。
5. 设置页入口形式不限（Compose 用 AppIconPicker 的样子，View/Java 用普通选项弹窗），
   但名字、资源、组件名在整个 App 里只能有一套，不许中途改名。

验收（汇报时逐项给出证据）：
- Merged Manifest 里有且仅有 1 个 enabled=true 的 LAUNCHER 组件；
- 真机逐套切换：桌面图标变化、无双图标/无消失、点新图标能进 App、杀进程后状态一致；
  写明机型与桌面、是否等待了几秒；
- 编译命令与输出。
做不到真机验证时明确写"未做真机验证"，不要写"已完成"。
```

## 附：Java / View 宿主的等价实现（直接可用，不要再自己翻译 Compose 版）

```java
package 【你的包名】;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/** F01 换图标：与 yz8023/ui 模板同一机制的 Java 版。组件名必须与 Manifest 里逐字一致。 */
public final class AppIconSwitcher {

    public enum Variant {
        DEFAULT(".icon.Default"),   // 与 Manifest 中 alias 的相对名逐字一致
        ALT_ONE(".icon.AltOne"),
        ALT_TWO(".icon.AltTwo");

        final String suffix;
        Variant(String suffix) { this.suffix = suffix; }
    }

    private AppIconSwitcher() {}

    public static Variant current(Context c) {
        PackageManager pm = c.getPackageManager();
        for (Variant v : Variant.values()) {
            int s = pm.getComponentEnabledSetting(component(c, v));
            if (s == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) return v;
        }
        return Variant.DEFAULT;
    }

    /** @return true=读回校验通过；false=被系统/桌面拒绝，调用方必须提示用户 */
    public static boolean apply(Context c, Variant target) {
        try {
            PackageManager pm = c.getPackageManager();
            // 1) 先启用目标，保证任何瞬间都至少有一个桌面入口
            pm.setComponentEnabledSetting(component(c, target),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            // 2) 再逐个禁用其余
            for (Variant v : Variant.values()) {
                if (v == target) continue;
                pm.setComponentEnabledSetting(component(c, v),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
            // 3) 读回校验，防止 ROM 静默拒绝造成"点了没反应还不知道"
            for (Variant v : Variant.values()) {
                int expected = (v == target)
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                if (pm.getComponentEnabledSetting(component(c, v)) != expected) return false;
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static ComponentName component(Context c, Variant v) {
        // 关键：第二个参数必须是完整类名，所以拼上包名；只写 ".icon.Xxx" 是历史事故写法
        return new ComponentName(c.getPackageName(), c.getPackageName() + v.suffix);
    }
}
```

注意三点（历史上全踩过）：

- 若你的默认入口想保留「MainActivity 本体带 LAUNCHER」而不是 alias，那是另一套结构（模板 F02），
  **只能二选一并在开工表里写明**，组件清单要把 MainActivity 本体也算进去，不要两套混着抄；
- 图标「切了桌面没变」先等 5–10 秒或回桌面再判失败，Launcher 缓存不是 bug，不要因此改方案；
- 每次想改图标资源格式（PNG/vector/adaptive）前先查模板手册 F01 红线，禁止来回横跳。
