package com.rix.womblab.presentation

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light", group = "Theme", showBackground = true)
@Preview(
    name = "Dark",
    group = "Theme",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
annotation class PreviewTheme

@Preview(widthDp = 150)
@Preview(widthDp = 300)
@Preview(widthDp = 500)
annotation class PreviewWidthSize


@Preview(name = "80%", group = "Font", fontScale = 0.80f, showBackground = true)
@Preview(name = "100%", group = "Font", fontScale = 1.0f, showBackground = true)
@Preview(name = "130%", group = "Font", fontScale = 1.3f, showBackground = true)
@Preview(name = "150%", group = "Font", fontScale = 1.5f, showBackground = true)
@Preview(name = "180%", group = "Font", fontScale = 1.8f, showBackground = true)
@Preview(name = "200%", group = "Font", fontScale = 2f, showBackground = true)
annotation class PreviewFontSize