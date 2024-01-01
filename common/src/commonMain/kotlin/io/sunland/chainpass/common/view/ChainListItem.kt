package io.sunland.chainpass.common.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.Chain

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListItem(chain: Chain, onSelect: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    if (chain.isDraft) {
        Row(
            modifier = modifier.padding(all = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = chain.name.value)
            CircularProgressIndicator(modifier = Modifier.size(size = 16.dp), strokeWidth = 2.dp)
        }
    } else {
        val isDropDownMenuExpandedState = remember { mutableStateOf(false) }
        val dropDownMenuOffsetState = remember { mutableStateOf(DpOffset.Zero) }

        val density = LocalDensity.current

        Row(
            modifier = modifier
                .clickable { isDropDownMenuExpandedState.value = true }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent().changes.first()

                            if (event.pressed && (event.type == PointerType.Mouse || event.type == PointerType.Touch)) {
                                dropDownMenuOffsetState.value = with(density) {
                                    DpOffset(
                                        x = event.position.x.toDp() - 20.dp,
                                        y = event.position.y.toDp() - 40.dp
                                    )
                                }
                            }
                        }
                    }
                }.padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = chain.name.value)

            if (isDropDownMenuExpandedState.value) {
                DropdownMenu(
                    expanded = isDropDownMenuExpandedState.value,
                    onDismissRequest = { isDropDownMenuExpandedState.value = false },
                    modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                    offset = dropDownMenuOffsetState.value
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Open", fontSize = 14.sp) },
                        onClick = {
                            isDropDownMenuExpandedState.value = false

                            onSelect()
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.LockOpen, contentDescription = null) },
                        trailingIcon = { Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Delete", fontSize = 14.sp) },
                        onClick = {
                            isDropDownMenuExpandedState.value = false

                            onRemove()
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}