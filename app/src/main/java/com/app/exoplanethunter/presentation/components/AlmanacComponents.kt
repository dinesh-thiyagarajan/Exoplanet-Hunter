package com.app.exoplanethunter.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.ButtonText
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextDim
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Line
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.MatchaDeep
import com.app.exoplanethunter.presentation.theme.OnMatcha
import com.app.exoplanethunter.presentation.theme.OnMatchaDeep
import com.app.exoplanethunter.presentation.theme.Raised
import com.app.exoplanethunter.presentation.theme.ScreenTitle
import com.app.exoplanethunter.presentation.theme.StarGlow
import com.app.exoplanethunter.presentation.theme.Surface

// ===========================================================================
// Matcha Night component kit, shared by every screen.
// ===========================================================================

/** Single-select filter chip: matcha when selected, surface otherwise. */
@Composable
fun AlmanacChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(if (selected) Matcha else Surface, label = "chipBg")
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(Corners.Pill)
            .background(bg)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = BodyFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (selected) OnMatcha else InkTextDim,
            maxLines = 1,
        )
    }
}

/** On/off chip, e.g. the orrery's "Habitable zone": matcha deep with a check when on. */
@Composable
fun ToggleChip(label: String, on: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(Corners.Pill)
            .background(if (on) MatchaDeep else Surface)
            .toggleable(value = on, role = Role.Switch, onValueChange = { onClick() })
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (on) {
            Icon(AppIcons.Check, contentDescription = null, tint = OnMatchaDeep, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(
            label,
            fontFamily = BodyFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (on) OnMatchaDeep else InkTextDim,
        )
    }
}

/** Small text action, e.g. "Compare" on the list header. Matcha when [active]. */
@Composable
fun AlmanacOutlinedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(Corners.Pill)
            .background(if (active) MatchaDeep else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            fontFamily = BodyFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (active) OnMatchaDeep else Matcha,
        )
    }
}

/** Primary pill button: matcha fill, Caprasimo label, 48 dp tall. */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(Corners.Pill)
            .background(if (enabled) Matcha else Raised)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = if (enabled) OnMatcha else InkTextFaint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, style = ButtonText, color = if (enabled) OnMatcha else InkTextFaint, maxLines = 1)
    }
}

/** Secondary pill button: [filled] uses the raised surface, otherwise a 1.5 dp outline. */
@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(Corners.Pill)
            .then(if (filled) Modifier.background(Raised) else Modifier.border(1.5.dp, Line, Corners.Pill))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(label, style = ButtonText, color = if (enabled) InkText else InkTextFaint, maxLines = 1)
        if (trailingIcon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(trailingIcon, null, tint = InkText, modifier = Modifier.size(16.dp))
        }
    }
}

/** Round 48 dp icon button on the surface colour, used in headers. */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Surface,
    tint: Color = InkText,
    size: Dp = 48.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

/** Plain 48 dp icon button with no container, e.g. back arrows in top bars. */
@Composable
fun BareIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = InkText,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/**
 * Header of a tab screen: Caprasimo 34 title, a faint count line under it, and round
 * icon actions on the right. Searching happens on its own screen, not in the header.
 */
@Composable
fun TabHeader(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = ScreenTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontFamily = BodyFamily, fontSize = 14.sp, color = InkTextFaint, maxLines = 1)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

/**
 * Top bar of a pushed screen: back arrow, then an optional Caprasimo title with a faint line
 * under it, and actions on the right.
 */
@Composable
fun BackTopBar(
    onBack: () -> Unit,
    backDescription: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    titleSize: Int = 28,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BareIconButton(AppIcons.Back, backDescription, onBack)
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            if (title != null) {
                Text(
                    title,
                    style = ScreenTitle.copy(fontSize = titleSize.sp, lineHeight = (titleSize + 4).sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (subtitle != null) {
                Text(subtitle, fontFamily = BodyFamily, fontSize = 14.sp, color = InkTextFaint, maxLines = 1)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/** A section card on the surface colour with 28 dp corners. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    small: Boolean = false,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(if (small) Corners.SmallCard else Corners.Card)
            .background(Surface)
            .padding(padding),
        content = content,
    )
}

/** A card with a dashed outline and no fill, used for AI insights and placeholders. */
@Composable
fun DashedCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val r = 24.dp.toPx()
                drawRoundRect(
                    color = Line,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                )
            }
            .padding(20.dp),
        content = content,
    )
}

/** Temperature reading: a dot in the step's colour and its word, e.g. "● Temperate". */
@Composable
fun TempIndicator(bucket: TempBucket, modifier: Modifier = Modifier, fontSize: Int = 12) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(bucket.color))
        Spacer(Modifier.width(5.dp))
        Text(bucket.label, fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = fontSize.sp, color = bucket.color)
    }
}

/** The outlined "✦ AI" pill shown next to AI values. */
@Composable
fun AiChip(modifier: Modifier = Modifier, label: String = "AI") {
    Row(
        modifier = modifier
            .border(BorderStroke(1.5.dp, Matcha), Corners.Pill)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcons.Sparkle, null, tint = Matcha, modifier = Modifier.size(9.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Matcha)
    }
}

/**
 * The planet disc used in lists and grids: a circle in the temperature colour with a soft
 * top-left highlight and bottom-right shade. Gas giants also get diagonal bands.
 */
@Composable
fun PlanetDisc(color: Color, size: Dp, modifier: Modifier = Modifier, banded: Boolean = false) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .drawBehind {
                drawCircle(color)
                if (banded) {
                    val band = Color(color.red * 0.62f, color.green * 0.62f, color.blue * 0.62f)
                    val disc = androidx.compose.ui.graphics.Path().apply {
                        addOval(androidx.compose.ui.geometry.Rect(Offset.Zero, this@drawBehind.size))
                    }
                    clipPath(disc) {
                        rotate166 {
                            val step = this.size.height / 4.5f
                            var y = -this.size.height * 0.5f
                            while (y < this.size.height * 1.5f) {
                                drawRect(
                                    band.copy(alpha = 0.6f),
                                    topLeft = Offset(-this.size.width, y),
                                    size = androidx.compose.ui.geometry.Size(this.size.width * 3, step * 0.42f),
                                )
                                y += step
                            }
                        }
                    }
                }
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                        center = Offset(this.size.width * 0.3f, this.size.height * 0.28f),
                        radius = this.size.width * 0.55f,
                    )
                )
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        center = Offset(this.size.width * 0.32f, this.size.height * 0.3f),
                        radius = this.size.width * 0.95f,
                    )
                )
            },
    )
}

private inline fun androidx.compose.ui.graphics.drawscope.DrawScope.rotate166(block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    rotate(degrees = 166f - 180f, block = block)
}

/** A star glyph with a matching glow, e.g. on star-system rows. */
@Composable
fun StarGlyph(color: Color, size: Dp, modifier: Modifier = Modifier, companion: Color? = null) {
    Box(modifier = modifier.size(size + 8.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size + 8.dp)
                .drawBehind {
                    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.45f), Color.Transparent)))
                }
        )
        Box(Modifier.size(size).clip(CircleShape).background(color))
        if (companion != null) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(companion)
            )
        }
    }
}

/** Halo colour behind stars. */
val StarHalo: Color = StarGlow.copy(alpha = 0.42f)

/** Switch: 52×32 track, 24 dp knob; matcha when on, raised when off. */
@Composable
fun MatchaSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val knobOffset by animateDpAsState(if (checked) 24.dp else 4.dp, label = "knob")
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(Corners.Pill)
            .background(if (checked) Matcha else Raised)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        Box(
            Modifier
                .offset(x = knobOffset, y = 4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) OnMatcha else InkTextFaint)
        )
    }
}

/** Loading row: a raised disc and a surface bar, per the handoff. */
@Composable
fun SkeletonRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Raised))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth(0.55f).height(14.dp).clip(Corners.Pill).background(Surface))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.8f).height(10.dp).clip(Corners.Pill).background(Surface))
        }
    }
}

/** Empty or error state: optional art, a Caprasimo title, a hint, and an optional button. */
@Composable
fun EmptyState(
    title: String,
    hint: String?,
    modifier: Modifier = Modifier,
    art: (@Composable () -> Unit)? = null,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(modifier = modifier.padding(horizontal = 32.dp, vertical = 24.dp)) {
        if (art != null) {
            art()
            Spacer(Modifier.height(20.dp))
        }
        Text(title, style = ScreenTitle.copy(fontSize = 26.sp, lineHeight = 30.sp))
        if (hint != null) {
            Spacer(Modifier.height(10.dp))
            Text(hint, fontFamily = BodyFamily, fontSize = 15.sp, lineHeight = 22.sp, color = InkTextDim)
        }
        if (actionLabel != null) {
            Spacer(Modifier.height(18.dp))
            PrimaryButton(actionLabel, onAction)
        }
    }
}
