package com.app.exoplanethunter.presentation.screens.spacefact

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.components.BackTopBar
import com.app.exoplanethunter.presentation.components.BareIconButton
import com.app.exoplanethunter.presentation.components.EmptyState
import com.app.exoplanethunter.presentation.components.PrimaryButton
import com.app.exoplanethunter.presentation.components.screenContentInsets
import com.app.exoplanethunter.presentation.components.topBarInsets
import com.app.exoplanethunter.presentation.theme.AlmanacCaption
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.Ink
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.StarOrange
import com.app.exoplanethunter.spacefacts.CustomTabLauncher
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpaceFactDetailScreen(
    factId: Int,
    onBack: () -> Unit,
    viewModel: SpaceFactDetailViewModel = koinViewModel()
) {
    LaunchedEffect(factId) { viewModel.load(factId) }

    val context = LocalContext.current
    val fact = viewModel.fact

    Column(modifier = Modifier.fillMaxSize().background(Ink)) {
        BackTopBar(
            onBack = onBack,
            backDescription = stringResource(R.string.cd_back),
            modifier = Modifier.topBarInsets(),
        ) {
            if (fact != null) {
                val chooserTitle = stringResource(R.string.space_fact_share)
                BareIconButton(AppIcons.Share, chooserTitle, onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, fact.title)
                        putExtra(Intent.EXTRA_TEXT, "${fact.title}\n\n${fact.detail}\n\n${fact.sourceUrl}")
                    }
                    context.startActivity(Intent.createChooser(send, chooserTitle))
                })
            }
        }

        if (fact == null) {
            EmptyState(title = stringResource(R.string.space_fact_title), hint = stringResource(R.string.space_fact_not_found))
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .screenContentInsets()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FactIllustration(fact.id)
            Spacer(Modifier.height(24.dp))
            if (viewModel.position > 0) {
                Text(
                    stringResource(R.string.space_fact_position, viewModel.position, viewModel.total),
                    fontFamily = BodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StarOrange,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(fact.title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(Modifier.height(14.dp))
            Text(
                fact.detail,
                fontFamily = BodyFamily, fontSize = 16.sp, lineHeight = 26.sp, color = InkText,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                label = stringResource(R.string.space_fact_read_more),
                onClick = {
                    viewModel.onSourceOpened()
                    CustomTabLauncher.open(context, fact.sourceUrl)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.space_fact_disclaimer), style = AlmanacCaption, modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(Modifier.height(40.dp))
        }
    }
}
