package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.StorageType
import io.sunland.chainpass.common.component.InputDialog
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun ChainListItemKeyInput(
    inputActionType: InputActionType,
    onDismiss: () -> Unit,
    onConfirm: (Chain.Key, StoreOptions?, FilePath?) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    val keyState = remember { mutableStateOf("") }
    val keyErrorState = remember { mutableStateOf(false) }

    val onKeyChange = { value: String ->
        val chainKey = Chain.Key(value)

        keyState.value = chainKey.value
        keyErrorState.value = chainKey.validation.isFailure
    }

    val keyVisibleState = remember { mutableStateOf(false) }

    val onKeyVisibleChange = {
        keyVisibleState.value = !keyVisibleState.value
    }

    val storageIsPrivateState = remember { mutableStateOf(true) }
    val storageTypeState = remember { mutableStateOf(StorageType.JSON) }

    val storageTypeMenuExpandedState = remember { mutableStateOf(false) }

    val onStorageTypeChange = { value: StorageType ->
        storageTypeState.value = value
    }

    val onStorageIsPrivateChange = { value: Boolean ->
        storageIsPrivateState.value = value
    }

    val filePathState = remember { mutableStateOf("") }

    val onDone = {
        val chainKey = Chain.Key(keyState.value)
        val storeOptions = StoreOptions(storageIsPrivateState.value, storageTypeState.value)
        val filePath = FilePath(filePathState.value)

        keyErrorState.value = chainKey.validation.isFailure

        when (inputActionType) {
            InputActionType.SELECT, InputActionType.REMOVE -> if (!keyErrorState.value) {
                onConfirm(chainKey, null, null)
            }
            InputActionType.STORE -> if (!keyErrorState.value) {
                onConfirm(chainKey, storeOptions, null)
            }
            InputActionType.UNSTORE -> if (!keyErrorState.value) {
                onConfirm(chainKey, storeOptions, filePath)
            }
        }
    }

    val onKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            false
        } else when (keyEvent.key) {
            Key.Escape -> {
                onDismiss()

                true
            }
            Key.Enter -> {
                onDone()

                true
            }
            else -> false
        }
    }

    InputDialog(
        onDismissRequest = onDismiss,
        onConfirmRequest = onDone
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent(onKeyEvent),
                placeholder = { Text(text = "Key") },
                value = keyState.value,
                onValueChange = onKeyChange,
                trailingIcon = {
                    if (keyErrorState.value) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    } else {
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                            onClick = onKeyVisibleChange
                        ) {
                            Icon(imageVector = if (keyVisibleState.value) {
                                Icons.Default.Visibility
                            } else Icons.Default.VisibilityOff, contentDescription = null)
                        }
                    }
                },
                isError = keyErrorState.value,
                singleLine = true,
                visualTransformation = if (!keyVisibleState.value) {
                    PasswordVisualTransformation()
                } else VisualTransformation.None,
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = if (inputActionType == InputActionType.UNSTORE) {
                    KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                } else KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            )
            if (inputActionType == InputActionType.STORE) {
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
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
            if (inputActionType == InputActionType.UNSTORE) {
                val fileDialogOpenState = remember { mutableStateOf(false) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Select File")
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                            onClick = { fileDialogOpenState.value = true }
                        ) { Icon(imageVector = Icons.Default.FileOpen, contentDescription = null) }
                    }
                    if (filePathState.value.isNotEmpty()) {
                        Text(text = FilePath(filePathState.value).fileName)
                    }
                }

                if (fileDialogOpenState.value) {
                    val fileDialog = JFileChooser().apply {
                        dialogType = JFileChooser.OPEN_DIALOG
                        fileSelectionMode = JFileChooser.FILES_ONLY
                        isAcceptAllFileFilterUsed = false

                        addChoosableFileFilter(FileNameExtensionFilter("JSON", "json"))
                        addChoosableFileFilter(FileNameExtensionFilter("CSV", "csv"))
                    }

                    if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        filePathState.value = fileDialog.selectedFile.absolutePath
                    } else filePathState.value = ""

                    fileDialogOpenState.value = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}