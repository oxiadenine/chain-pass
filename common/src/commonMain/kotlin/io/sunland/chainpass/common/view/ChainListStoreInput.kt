package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.StorageType
import io.sunland.chainpass.common.component.InputDialog

data class StoreOptions(val type: StorageType, val isPrivate: Boolean, val isSingle: Boolean)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListStoreInput(isSingle: Boolean, onStore: (StoreOptions) -> Unit, onCancel: () -> Unit) {
    val isStoreTypeMenuExpandedState = remember { mutableStateOf(false) }

    val storeTypeState = remember { mutableStateOf(StorageType.JSON) }

    val onStoreTypeChange = { value: StorageType ->
        storeTypeState.value = value
    }

    val storeIsPrivateState = remember { mutableStateOf(true) }

    val onStoreIsPrivateChange = { value: Boolean ->
        storeIsPrivateState.value = value
    }

    val onDone = {
        val storeOptions = StoreOptions(storeTypeState.value, storeIsPrivateState.value, isSingle)

        onStore(storeOptions)
    }

    InputDialog(onDismissRequest = onCancel, onConfirmRequest = onDone) {
        Text(
            modifier = Modifier.padding(bottom = 32.dp),
            text = if (isSingle) "Single Store" else "Multiple Store"
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            if (isSingle) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Private")
                    Switch(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        checked = storeIsPrivateState.value,
                        onCheckedChange = onStoreIsPrivateChange
                    )
                }
            }
            Button(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = { isStoreTypeMenuExpandedState.value = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = storeTypeState.value.name)
                    Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null)
                }
                DropdownMenu(
                    modifier = Modifier.width(width = 150.dp),
                    expanded = isStoreTypeMenuExpandedState.value,
                    onDismissRequest = { isStoreTypeMenuExpandedState.value = false },
                    offset = DpOffset(x = 8.dp, y = (-16).dp)
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            isStoreTypeMenuExpandedState.value = false

                            onStoreTypeChange(StorageType.JSON)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "JSON", fontSize = 12.sp) }
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            isStoreTypeMenuExpandedState.value = false

                            onStoreTypeChange(StorageType.CSV)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "CSV", fontSize = 12.sp) }
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            isStoreTypeMenuExpandedState.value = false

                            onStoreTypeChange(StorageType.TXT)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "TXT", fontSize = 12.sp) }
                }
            }
        }
    }
}