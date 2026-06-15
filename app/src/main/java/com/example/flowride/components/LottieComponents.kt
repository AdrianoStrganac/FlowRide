package com.example.flowride.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun SuccessAnimation(modifier: Modifier = Modifier) {
    val comp by rememberLottieComposition(LottieCompositionSpec.Url("https://assets10.lottiefiles.com/packages/lf20_5tkzkp69.json"))
    val progress by animateLottieCompositionAsState(comp, iterations = 1)

    LottieAnimation(
        composition = comp,
        progress = { progress },
        modifier = modifier.size(120.dp)
    )
}

@Composable
fun LoadingBicycleAnimation(modifier: Modifier = Modifier) {
    val comp by rememberLottieComposition(LottieCompositionSpec.Url("https://assets1.lottiefiles.com/packages/lf20_96bovdur.json"))
    val progress by animateLottieCompositionAsState(comp, iterations = LottieConstants.IterateForever)

    LottieAnimation(
        composition = comp,
        progress = { progress },
        modifier = modifier.size(150.dp)
    )
}

@Composable
fun EmptyRentalsAnimation(modifier: Modifier = Modifier) {
    val comp by rememberLottieComposition(LottieCompositionSpec.Url("https://assets5.lottiefiles.com/private_files/lf30_cg66scdy.json"))
    val progress by animateLottieCompositionAsState(comp, iterations = LottieConstants.IterateForever)

    LottieAnimation(
        composition = comp,
        progress = { progress },
        modifier = modifier.size(200.dp)
    )
}
