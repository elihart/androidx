/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.window

import androidx.compose.foundation.ClickableText
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.ui.test.createComposeRule
import androidx.ui.test.getUnclippedBoundsInRoot
import androidx.ui.test.onNodeWithText
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(Parameterized::class)
class PopupDismissTest(private val isFocusable: Boolean) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun initParameters(): Array<Any> = arrayOf(true, false)
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun clickOutsideToDismiss() {
        var dismissCounter = 0
        var btnClicksCounter = 0
        var btnPos: Offset = Offset.Zero

        val latch = CountDownLatch(1)

        rule.setContent {
            Box(Modifier.fillMaxSize()) {
                ClickableText(
                    text = AnnotatedString("Button"),
                    onClick = { btnClicksCounter++ },
                    modifier = Modifier.onGloballyPositioned {
                        // UiDevice needs screen relative coordinates
                        btnPos = it.localToGlobal(Offset.Zero)
                    }
                )

                Popup(
                    alignment = Alignment.Center,
                    isFocusable = isFocusable,
                    onDismissRequest = { dismissCounter++; latch.countDown() }
                ) {
                    Box(Modifier.preferredSize(100.dp, 100.dp)) {
                        Text(text = "Popup", textAlign = TextAlign.Center)
                    }
                }
            }
        }

        rule.runOnIdle {
            assertThat(dismissCounter).isEqualTo(0)
            assertThat(btnClicksCounter).isEqualTo(0)
        }

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val btnBounds = rule.onNodeWithText("Button").getUnclippedBoundsInRoot()

        with(rule.density) {
            // Need to click via UiDevice as this click has to propagate to multiple windows
            device.click(
                btnPos.x.toInt() + btnBounds.width.toIntPx() / 2,
                btnPos.y.toInt() + btnBounds.height.toIntPx() / 2
            )
        }

        // TODO: Unfortunately without the latch this test flakes on cuttlefish
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw AssertionError("Failed to wait for dismiss callback.")
        }

        rule.runOnIdle {
            assertThat(dismissCounter).isEqualTo(1)
            if (isFocusable) {
                // Focusable popup consumes touch events => button receives none
                assertThat(btnClicksCounter).isEqualTo(0)
            } else {
                // Not focusable popup doesn't consume touch events => button receives one
                assertThat(btnClicksCounter).isEqualTo(1)
            }
        }
    }
}
