package app.accrescent.client.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    value: MutableState<TextFieldValue>,
    modifier: Modifier = Modifier,
    title: String = "",
    onSearchDisplayChanged: (String) -> Unit = {},
    onSearchDisplayClosed: () -> Unit = {},
    expandedInitially: Boolean = false,
    actions: @Composable RowScope.() -> Unit
) {
    CenterAlignedTopAppBar(
        title = {},
        actions = {
            val (expanded, onExpandedChanged) = remember {
                mutableStateOf(expandedInitially)
            }

            Crossfade(targetState = expanded) { isSearchFieldVisible ->
                when (isSearchFieldVisible) {
                    true -> ExpandedSearchView(
                        textFieldValue = value,
                        onSearchDisplayChanged = onSearchDisplayChanged,
                        onSearchDisplayClosed = onSearchDisplayClosed,
                        onExpandedChanged = onExpandedChanged,
                        modifier = modifier
                    )

                    false -> CollapsedSearchView(
                        title = title,
                        onExpandedChanged = onExpandedChanged,
                        modifier = modifier,
                        actions = actions,
                    )
                }
            }
        }
    )
}
