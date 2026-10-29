package com.app.exoplanethunter.presentation.screens.spacefact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.BackTopBar
import com.app.exoplanethunter.presentation.components.BareIconButton
import com.app.exoplanethunter.presentation.components.CircleIconButton
import com.app.exoplanethunter.presentation.components.EmptyState
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.components.topBarInsets
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Corners
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.Matcha
import com.app.exoplanethunter.presentation.theme.Surface
import com.app.exoplanethunter.spacefacts.SpaceFact
import org.koin.androidx.compose.koinViewModel

/** The library of every space fact, each with its illustration disc. Search sits behind the icon. */
@Composable
fun SpaceFactLibraryScreen(
    onFactClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SpaceFactLibraryViewModel = koinViewModel()
) {
    val facts = viewModel.facts
    var searching by rememberSaveable { mutableStateOf(viewModel.query.isNotEmpty()) }

    Column(modifier = Modifier.fillMaxSize().background(Ink)) {
        BackTopBar(
            onBack = onBack,
            backDescription = stringResource(R.string.cd_back),
            title = stringResource(R.string.space_fact_library_title),
            subtitle = stringResource(R.string.space_fact_library_count, viewModel.totalCount),
            modifier = Modifier.topBarInsets(),
        ) {
            CircleIconButton(AppIcons.Search, stringResource(R.string.search_action), { searching = !searching })
        }

        Column(modifier = Modifier.screenContentInsets()) {
            if (searching) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(Corners.Pill)
                        .background(Surface)
                        .border(2.dp, Matcha, Corners.Pill)
                        .padding(start = 18.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) {
                        if (viewModel.query.isEmpty()) {
                            Text(stringResource(R.string.space_fact_library_search_hint), fontFamily = BodyFamily, fontSize = 16.sp, color = InkTextFaint)
                        }
                        BasicTextField(
                            value = viewModel.query,
                            onValueChange = viewModel::onQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = BodyFamily, fontSize = 16.sp, color = InkText),
                            cursorBrush = SolidColor(Matcha),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (viewModel.query.isNotEmpty()) {
                        BareIconButton(AppIcons.Close, stringResource(R.string.cd_clear), { viewModel.onQueryChange("") }, tint = InkTextFaint)
                    }
                }
            }

            if (facts.isEmpty()) {
                EmptyState(title = stringResource(R.string.search_no_results_title), hint = stringResource(R.string.space_fact_library_empty))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(facts, key = { it.id }) { fact ->
                        FactRow(fact = fact, onClick = { onFactClick(fact.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FactRow(fact: SpaceFact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Corners.SmallCard)
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FactDisc(fact.id, 56.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(fact.title, fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InkText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(fact.shortDescription, fontFamily = BodyFamily, fontSize = 13.sp, color = InkTextFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
