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
import io.sunland.chainpass.common.StorageOptions
import io.sunland.chainpass.common.StorageType
import io.sunland.chainpass.common.component.Dialog
import io.sunland.chainpass.common.component.DropdownMenu
import io.sunland.chainpass.common.component.DropdownMenuItem

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListStoreOptions(storageOptions: StorageOptions, onDismiss: () -> Unit, onConfirm: (StorageOptions) -> Unit) {
    val storageIsPrivateState = remember { mutableStateOf(storageOptions.isPrivate) }
    val storageTypeState = remember { mutableStateOf(storageOptions.type) }

    val storageTypeMenuExpandedState = remember { mutableStateOf(false) }

    val onStorageTypeChange = { value: StorageType ->
        storageTypeState.value = value
    }

    val onStorageIsPrivateChange = { value: Boolean ->
        storageIsPrivateState.value = value
    }

    Dialog(onDismissRequest = onDismiss, onConfirmRequest = {
        onConfirm(StorageOptions(storageIsPrivateState.value, storageTypeState.value))
    }) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Private")
                Switch(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    checked = storageIsPrivateState.value,
                    onCheckedChange = onStorageIsPrivateChange
                )
            }
            Button(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = { storageTypeMenuExpandedState.value = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.width(width = 60.dp).padding(start = 8.dp, end = 4.dp),
                        text = storageTypeState.value.name
                    )
                    Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null)
                }
                DropdownMenu(
                    modifier = Modifier.width(width = 150.dp),
                    expanded = storageTypeMenuExpandedState.value,
                    onDismissRequest = { storageTypeMenuExpandedState.value = false },
                    offset = DpOffset(x = 8.dp, y = (-16).dp)
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            storageTypeMenuExpandedState.value = false

                            onStorageTypeChange(StorageType.JSON)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "JSON", fontSize = 12.sp) }
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            storageTypeMenuExpandedState.value = false

                            onStorageTypeChange(StorageType.CSV)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "CSV", fontSize = 12.sp) }
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            storageTypeMenuExpandedState.value = false

                            onStorageTypeChange(StorageType.TXT)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "TXT", fontSize = 12.sp) }
                }
            }
        }
    }
}