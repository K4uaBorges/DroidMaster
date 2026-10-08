package controller.shared

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun createMarkdownShareIntent(
    context: Context,
    file: File
): Intent {

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    return Intent(Intent.ACTION_SEND).apply {

        type = "text/markdown"

        putExtra(
            Intent.EXTRA_STREAM,
            uri
        )

        addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }

}

fun shareMarkdownFile(context: Context, file: File) {
    val intent = createMarkdownShareIntent(context, file)
    context.startActivity(
        Intent.createChooser(
            intent,
            "Partilhar conversa"
        )
    )
}
