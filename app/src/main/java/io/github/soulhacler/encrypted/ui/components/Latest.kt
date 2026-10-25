package io.github.soulhacler.encrypted.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private class Holder<T>(var value: T?)

/**
 * Remembers the last non-null [value]. Screens use this so they keep drawing their final state
 * while animating out, after the view model has already cleared it.
 */
@Composable
fun <T : Any> rememberLatest(value: T?): T? {
    val holder = remember { Holder(value) }
    if (value != null) holder.value = value
    return holder.value
}
