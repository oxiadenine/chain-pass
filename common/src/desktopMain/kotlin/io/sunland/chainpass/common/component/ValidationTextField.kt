package io.sunland.chainpass.common.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun ValidationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    textStyle: TextStyle?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    isError: Boolean,
    errorMessage: String?,
    visualTransformation: VisualTransformation,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    singleLine: Boolean,
    colors: TextFieldColors?
) = TextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    textStyle = textStyle ?: LocalTextStyle.current,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = if (isError) {
        {
            if (errorMessage != null) {
                TooltipArea(
                    tooltip = {
                        Surface(modifier = Modifier.shadow(4.dp), elevation = 4.dp) {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                text = errorMessage,
                                fontSize = 12.sp,
                                color = MaterialTheme.colors.error
                            )
                        }
                    },
                    delayMillis = 100,
                    tooltipPlacement = TooltipPlacement.CursorPoint(
                        alignment = Alignment.CenterStart,
                        offset = DpOffset((-32).dp, 0.dp)
                    )
                ) { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else Icon(imageVector = Icons.Default.Info, contentDescription = null)
        }
    } else null,
    isError = isError,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    colors = colors ?: TextFieldDefaults.textFieldColors()
)