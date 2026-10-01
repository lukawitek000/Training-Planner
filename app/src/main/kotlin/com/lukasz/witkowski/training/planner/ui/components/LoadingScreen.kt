package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.ui.theme.Dimens

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String = ""
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag("loading")
        )
        if(message.isNotEmpty()){
            Spacer(modifier = Modifier.height(Dimens.large))
            Text(message)
        }

    }
}