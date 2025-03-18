package io.github.oxiadenine.chainpass.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow

@Composable
fun SnackbarHostState.isSnackbarVisible() = remember {
    snapshotFlow { currentSnackbarData != null }
}.collectAsState(false).value