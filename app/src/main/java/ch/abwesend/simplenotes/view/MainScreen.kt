package ch.abwesend.simplenotes.view

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ch.abwesend.simplenotes.R
import ch.abwesend.simplenotes.repository.NoteRepository
import ch.abwesend.simplenotes.ui.theme.noteBackground
import ch.abwesend.simplenotes.view.components.LoadingIndicatorFullScreen
import ch.abwesend.simplenotes.view.components.YesNoDialog
import kotlinx.coroutines.flow.first

@ExperimentalMaterial3Api
object MainScreen {
    private val lineSeparator: String by lazy { System.lineSeparator() }

    /**
     * The value is only loaded initially from the Data-Store.
     * The intermediate state is kept here directly for performance-reasons.
     */
    @Composable
    fun Screen() {
        val context = LocalContext.current
        val placeHolder = stringResource(id = R.string.notes_text_placeholder)

        var initialNotesText: String? by remember { mutableStateOf(null) }
        var currentNotesText by remember { mutableStateOf(placeHolder) }
        var initialized by remember { mutableStateOf(false) }

        val onValueChanged: (String) -> Unit = { newValue ->
            currentNotesText = newValue
            NoteRepository.storeNotes(context, newValue)
        }

        LaunchedEffect(Unit) {
            currentNotesText = NoteRepository.getNotes(context).first()

            if (!currentNotesText.startsWith(lineSeparator)) {
                currentNotesText = lineSeparator + currentNotesText
            }

            initialNotesText = currentNotesText
            initialized = true
        }

        Scaffold(topBar = { TopBar(currentNotesText, initialNotesText, onValueChanged) }) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (initialized) {
                    Notes(currentNotesText, onValueChanged)
                } else {
                    LoadingIndicatorFullScreen(textAfterIndicator = R.string.loading_notes)
                }
            }
        }
    }

    @Composable
    private fun Notes(currentValue: String, valueChanged: (String) -> Unit) {
        BasicTextField(
            value = currentValue,
            onValueChange = valueChanged,
            singleLine = false,
            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .fillMaxSize()
                .background(noteBackground)
                .padding(10.dp)
                .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
        )
    }

    @Composable
    private fun TopBar(currentText: String, initialText: String?, textChanged: (String) -> Unit) {
        var revertedText: String? by remember { mutableStateOf(null) }

        val colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondary
        )

        TopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            colors = colors,
            actions = {
                TopBarActionMenu(
                    currentText = currentText,
                    initialText = initialText,
                    revertedText = revertedText,
                    clearText = { textChanged("") },
                    revertChanges = {
                        revertedText = currentText
                        textChanged(initialText.orEmpty())
                    },
                    undoRevertChanges = {
                        textChanged(revertedText.orEmpty())
                        revertedText = null
                    }
                )
            },
        )
    }

    @Composable
    private fun TopBarActionMenu(
        currentText: String,
        initialText: String?,
        revertedText: String?,
        clearText: () -> Unit,
        revertChanges: () -> Unit,
        undoRevertChanges: () -> Unit,
    ) {
        var expanded: Boolean by remember { mutableStateOf(false) }
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val menuItemClicked: () -> Unit = { expanded = false }

        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.action_menu)
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val copyLabel = stringResource(id = R.string.copy_to_clipboard)
            DropdownMenuItem(
                text = { Text(copyLabel) },
                onClick = {
                    clipboardManager.setText(AnnotatedString(currentText))
                    menuItemClicked()
                }
            )

            Divider()

            MenuItemClear {
                clearText()
                menuItemClicked()
            }

            MenuSectionRevertChanges(
                initialText = initialText,
                revertedText = revertedText,
                revertChanges = {
                    revertChanges()
                    menuItemClicked()
                },
                undoRevertChanges = {
                    undoRevertChanges()
                    menuItemClicked()
                }
            )
        }
    }

    @Composable
    private fun MenuItemClear(clearText: () -> Unit) {
        var showConfirmationDialog: Boolean by remember { mutableStateOf(false) }

        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.clear_notes)) },
            onClick = { showConfirmationDialog = true }
        )

        if (showConfirmationDialog) {
            YesNoDialog(
                title = R.string.clear_notes_confirmation_title,
                text = R.string.clear_notes_confirmation_text,
                yesButtonLabel = R.string.delete,
                noButtonLabel = R.string.cancel,
                onYes = {
                    showConfirmationDialog = false
                    clearText()
                },
                onNo = { showConfirmationDialog = false }
            )
        }
    }

    @Composable
    private fun MenuSectionRevertChanges(
        initialText: String?,
        revertedText: String?,
        revertChanges: () -> Unit,
        undoRevertChanges: () -> Unit,
    ) {
        initialText?.let {
            Divider()
            MenuItemRevertChanges { revertChanges() }
        }

        revertedText?.let {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.undo_revert_changes)) },
                onClick = undoRevertChanges
            )
        }
    }

    @Composable
    private fun MenuItemRevertChanges(revertText: () -> Unit) {
        var showConfirmationDialog: Boolean by remember { mutableStateOf(false) }

        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.revert_changes)) },
            onClick = { showConfirmationDialog = true }
        )

        if (showConfirmationDialog) {
            YesNoDialog(
                title = R.string.revert_changes_confirmation_title,
                text = R.string.revert_changes_confirmation_text,
                onYes = {
                    showConfirmationDialog = false
                    revertText()
                },
                onNo = { showConfirmationDialog = false }
            )
        }
    }
}