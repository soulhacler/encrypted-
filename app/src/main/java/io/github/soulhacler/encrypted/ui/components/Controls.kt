package io.github.soulhacler.encrypted.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.soulhacler.encrypted.ui.theme.EncryptedType
import io.github.soulhacler.encrypted.ui.theme.Neon

/** A big, glowing, party-proof button. */
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colour: Color = Neon.Cyan,
    filled: Boolean = true,
    enabled: Boolean = true,
    height: Dp = 68.dp,
    fontSize: TextUnit = 20.sp,
    icon: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "pressScale")
    val shape = RoundedCornerShape(20.dp)
    val tint = if (enabled) colour else Neon.Outline
    val content = if (filled && enabled) Neon.Background else tint

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale)
            .heightIn(min = height)
            .shadow(if (enabled && filled) 18.dp else 0.dp, shape, ambientColor = tint, spotColor = tint)
            .clip(shape)
            .background(if (filled) tint else tint.copy(alpha = 0.08f))
            .border(2.dp, tint, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text.uppercase(),
                color = content,
                fontFamily = EncryptedType.Mono,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** A selectable pill used for categories, difficulties and timers. */
@Composable
fun NeonChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colour: Color = Neon.Cyan,
    subtitle: String? = null,
    leading: String? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(shape)
            .background(if (selected) colour.copy(alpha = 0.18f) else Neon.Surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) colour else Neon.Outline, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        if (leading != null) Text(leading, fontSize = 22.sp)
        Text(
            text = text,
            color = if (selected) colour else Neon.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (subtitle != null) {
            Text(subtitle, color = Neon.Muted, fontSize = 12.sp, fontFamily = EncryptedType.Mono)
        }
    }
}

/** Section label in the cipher style, e.g. "// CATEGORY". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, colour: Color = Neon.Cyan) {
    Text(
        text = "// " + text.uppercase(),
        style = EncryptedType.Label,
        color = colour,
        modifier = modifier.padding(top = 20.dp, bottom = 10.dp),
    )
}

/** Back arrow plus a monospace title, used at the top of secondary screens. */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth().height(64.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Neon.White)
        }
        Text(
            text = title.uppercase(),
            fontFamily = EncryptedType.Mono,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 3.sp,
            color = Neon.White,
        )
    }
}

/** A small dot in a participant's colour. */
@Composable
fun ColourDot(colour: Color, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    Box(modifier.size(size).clip(CircleShape).background(colour))
}
