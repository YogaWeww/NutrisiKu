package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.components.OnboardingPageContent
import com.example.nutrisiku.ui.components.PageIndicator
import com.example.nutrisiku.ui.components.onboardingPages
import kotlinx.coroutines.launch

/**
 * Layar Onboarding yang memperkenalkan fitur-fitur aplikasi kepada pengguna baru.
 *
 * @param onFinishClick Aksi yang dipanggil saat pengguna menyelesaikan onboarding.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    Box(modifier = Modifier.fillMaxSize()) {
        // Pager untuk menampilkan halaman-halaman onboarding
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageContent(page = onboardingPages[pageIndex])
        }

        // Kontrol navigasi (indikator dan tombol)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageIndicator(
                numberOfPages = onboardingPages.size,
                selectedPage = pagerState.currentPage
            )
            Button(
                onClick = {
                    if (!isLastPage) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinishClick()
                    }
                }
            ) {
                val buttonText = if (!isLastPage) {
                    stringResource(R.string.button_next)
                } else {
                    stringResource(R.string.button_finish)
                }
                Text(buttonText)
            }
        }
    }
}
