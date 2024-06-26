package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oxiadenine.chainpass.StorageType
import io.github.oxiadenine.chainpass.component.InputDialog
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.dialog_store_item_private_text
import io.github.oxiadenine.common.generated.resources.dialog_store_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun StoreDialog(onConfirm: (StorageType, Boolean) -> Unit, onCancel: () -> Unit, isSingle: Boolean) {
    var storageType by remember { mutableStateOf(StorageType.JSON) }
    var storeIsPrivate by remember { mutableStateOf(true) }

    val onStorageTypeDropdownMenuItemClick = { type: StorageType ->
        storageType = type
    }

    val onStorePrivateSwitchCheckedChange = { isPrivate: Boolean ->
        storeIsPrivate = isPrivate
    }

    val onInputDialogConfirmRequest = {
        onConfirm(storageType, storeIsPrivate)
    }

    InputDialog(
        onDismissRequest = onCancel,
        onConfirmRequest = onInputDialogConfirmRequest,
        title = {
            Text(
                text = stringResource(Res.string.dialog_store_title),
                modifier = Modifier.padding(all = 16.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSingle) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(Res.string.dialog_store_item_private_text))
                    Switch(
                        checked = storeIsPrivate,
                        onCheckedChange = onStorePrivateSwitchCheckedChange,
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                    )
                }
            }

            val focusRequester = remember { FocusRequester() }

            var dropdownMenuExpanded by remember { mutableStateOf(false) }

            Button(
                onClick = { dropdownMenuExpanded = true },
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand).focusRequester(focusRequester)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = storageType.name)
                    Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null)
                }

                if (dropdownMenuExpanded) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { dropdownMenuExpanded = false },
                        modifier = Modifier.width(width = 150.dp),
                        offset = DpOffset(x = 8.dp, y = (-16).dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = StorageType.JSON.name, fontSize = 14.sp) },
                            onClick = {
                                dropdownMenuExpanded = false

                                onStorageTypeDropdownMenuItemClick(StorageType.JSON)
                            },
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        )
                        DropdownMenuItem(
                            text = { Text(text = StorageType.CSV.name, fontSize = 14.sp) },
                            onClick = {
                                dropdownMenuExpanded = false

                                onStorageTypeDropdownMenuItemClick(StorageType.CSV)
                            },
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        )
                        DropdownMenuItem(
                            text = { Text(text = StorageType.TXT.name, fontSize = 14.sp) },
                            onClick = {
                                dropdownMenuExpanded = false

                                onStorageTypeDropdownMenuItemClick(StorageType.TXT)
                            },
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        )
                    }
                }
            }

            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
            }
        }
    }
}