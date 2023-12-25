package ch.abwesend.simplenotes.view.components

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.simplenotes.R

@Composable
fun YesNoDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes yesButtonLabel: Int = R.string.yes,
    @StringRes noButtonLabel: Int = R.string.no,
    onYes: () -> Unit,
    onNo: () -> Unit,
) = YesNoDialog(
    title = title,
    text = { Text(text = stringResource(id = text)) },
    yesButtonLabel = stringResource(id = yesButtonLabel),
    noButtonLabel = stringResource(id = noButtonLabel),
    onYes = onYes,
    onNo = onNo,
)

@Composable
fun YesNoDialog(
    @StringRes title: Int,
    text: @Composable () -> Unit,
    yesButtonEnabled: Boolean = true,
    yesButtonLabel: String = stringResource(id = R.string.yes),
    noButtonLabel: String = stringResource(id = R.string.no),
    onYes: () -> Unit,
    onNo: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = text,
        onDismissRequest = onNo,
        confirmButton = {
            Button(onClick = onYes, enabled = yesButtonEnabled) {
                Text(yesButtonLabel)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onNo) {
                Text(noButtonLabel)
            }
        },
    )

    BackHandler { onNo() }
}