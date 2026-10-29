package com.app.exoplanethunter.presentation.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.AlmanacChip
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.BareIconButton
import com.app.exoplanethunter.presentation.components.EmptyState
import com.app.exoplanethunter.presentation.components.PlanetRowCard
import com.app.exoplanethunter.presentation.components.SafeAreaInsets
import com.app.exoplanethunter.presentation.components.SectionCard
import com.app.exoplanethunter.presentation.components.StarSystemRow
import com.app.exoplanethunter.presentation.theme.AlmanacMeta
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.Surface
import androidx.compose.foundation.layout.windowInsetsPadding
import org.koin.androidx.compose.koinViewModel

/**
 * Search on its own screen: a focused pill field, result-type chips with counts, the
 * results, and recent searches when the field is empty.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onPlanetClick: (Long) -> Unit,
    onSystemClick: (Long) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .windowInsetsPadding(SafeAreaInsets)
    ) {
        // ---- Field ----
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BareIconButton(AppIcons.Back, stringResource(R.string.cd_back), onBack)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(Corners.Pill)
                    .background(Surface)
                    .border(2.dp, Matcha, Corners.Pill)
                    .padding(start = 18.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (viewModel.query.isEmpty()) {
                        Text(stringResource(R.string.search_hint), fontFamily = BodyFamily, fontSize = 16.sp, color = InkTextFaint)
                    }
                    BasicTextField(
                        value = viewModel.query,
                        onValueChange = viewModel::onQueryChanged,
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = BodyFamily, fontSize = 16.sp, color = InkText),
                        cursorBrush = SolidColor(Matcha),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.rememberQuery()
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    )
                }
                if (viewModel.query.isNotEmpty()) {
                    BareIconButton(AppIcons.Close, stringResource(R.string.cd_clear), { viewModel.onQueryChanged("") }, tint = InkTextFaint)
                }
            }
        }

        if (viewModel.query.isBlank()) {
            // ---- Recent searches ----
            if (viewModel.recent.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.search_prompt_title),
                    hint = stringResource(R.string.search_prompt),
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                SectionCard(modifier = Modifier.padding(16.dp), small = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.search_recent), style = AlmanacMeta, modifier = Modifier.weight(1f))
                        Text(
                            stringResource(R.string.search_clear_recent),
                            fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Matcha,
                            modifier = Modifier.clip(Corners.Pill).clickable(onClick = viewModel::clearRecent).padding(8.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.recent.forEach { q ->
                            Text(
                                q,
                                fontFamily = BodyFamily, fontSize = 15.sp, color = InkText,
                                modifier = Modifier
                                    .clip(Corners.Pill)
                                    .clickable { viewModel.onRecentSelected(q) }
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
            return@Column
        }

        // ---- Result type chips ----
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AlmanacChip(stringResource(R.string.search_tab_planets, viewModel.planets.size), viewModel.tab == SearchTab.Planets) {
                viewModel.onTabSelected(SearchTab.Planets)
            }
            AlmanacChip(stringResource(R.string.search_tab_systems, viewModel.systems.size), viewModel.tab == SearchTab.Systems) {
                viewModel.onTabSelected(SearchTab.Systems)
            }
        }

        val nothing = viewModel.hasResults && viewModel.planets.isEmpty() && viewModel.systems.isEmpty()
        if (nothing) {
            EmptyState(
                title = stringResource(R.string.search_no_results_title),
                hint = stringResource(R.string.search_no_results, viewModel.query.trim()),
            )
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 4.dp, bottom = 24.dp)) {
            when (viewModel.tab) {
                SearchTab.Planets -> items(viewModel.planets, key = { it.id }) { planet ->
                    PlanetRowCard(
                        planet = planet,
                        isFavorite = planet.planetName in viewModel.favoriteNames,
                        onToggleFavorite = { viewModel.toggleFavorite(planet) },
                        onClick = {
                            viewModel.rememberQuery()
                            onPlanetClick(planet.id)
                        },
                    )
                }
                SearchTab.Systems -> items(viewModel.systems, key = { it.id }) { system ->
                    StarSystemRow(
                        system = system,
                        onClick = {
                            viewModel.rememberQuery()
                            onSystemClick(system.id)
                        },
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }
            }
        }
    }
}
