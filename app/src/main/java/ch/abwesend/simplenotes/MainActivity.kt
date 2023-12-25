package ch.abwesend.simplenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import ch.abwesend.simplenotes.repository.NoteRepository
import ch.abwesend.simplenotes.ui.theme.SimpleNotesTheme
import ch.abwesend.simplenotes.ui.theme.noteBackground
import ch.abwesend.simplenotes.view.components.LoadingIndicatorFullScreen
import kotlinx.coroutines.flow.first

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleNotesTheme {
                MainScreen()
            }
        }
    }

    @Composable
    private fun MainScreen() {
        val context = LocalContext.current
        val placeHolder = stringResource(id = R.string.notes_text_placeholder)

        var currentNotesText by remember { mutableStateOf(placeHolder) }
        var initialized by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            currentNotesText = NoteRepository.getNotes(context).first()
            initialized = true
        }

        Scaffold(topBar = { TopBar(currentNotesText) }) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (initialized) {
                    Notes(currentNotesText) { newValue ->
                        currentNotesText = newValue
                        NoteRepository.storeNotes(context, newValue)
                    }
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
    private fun TopBar(currentValue: String) {
        val colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondary
        )
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            colors = colors,
            actions = { TopBarActionMenu(currentValue) },
        )
    }

    @Composable
    private fun TopBarActionMenu(currentText: String) {
        var expanded: Boolean by remember { mutableStateOf(false) }
        val clipboardManager: ClipboardManager = LocalClipboardManager.current

        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.action_menu)
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.copy_to_clipboard)) },
                onClick = { clipboardManager.setText(AnnotatedString(currentText)) }
            )
        }
    }
}