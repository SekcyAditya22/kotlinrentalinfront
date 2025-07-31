package com.example.rentalinn.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentalinn.R

data class OnboardingPage(
    val image: Int,
    val title: String,
    val description: String
)

val onboardingPages = listOf(
    OnboardingPage(
        image = R.drawable.logonjay,
        title = "Selamat Datang di Rentalin",
        description = "Temukan kendaraan yang sesuai dengan kebutuhan Anda"
    ),
    OnboardingPage(
        image = R.drawable.logonjay,
        title = "Mudah dan Cepat",
        description = "Proses pemesanan yang mudah dan cepat dengan beberapa klik"
    ),
    OnboardingPage(
        image = R.drawable.logonjay,
        title = "Mulai Perjalanan Anda",
        description = "Siap untuk memulai? Mari bergabung dengan kami"
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            modifier = Modifier.weight(10f),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { position ->
            PagerScreen(onboardingPage = onboardingPages[position])
        }

        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }

        FinishButton(
            modifier = Modifier.weight(1f),
            pagerState = pagerState
        ) {
            onNavigateToLogin()
        }
    }
}

@Composable
fun PagerScreen(onboardingPage: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f),
            painter = painterResource(id = onboardingPage.image),
            contentDescription = "Onboarding Image"
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = onboardingPage.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .padding(top = 20.dp),
            text = onboardingPage.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FinishButton(
    modifier: Modifier,
    pagerState: PagerState,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier.padding(horizontal = 40.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = pagerState.currentPage == 2
        ) {
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "Mulai",
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 40.dp)
                )
            }
        }
    }
} 