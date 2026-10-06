package controller.shared

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun shareMarkdownFile(
    context: Context,
    file: File
) {

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {

        type = "text/markdown"

        putExtra(
            Intent.EXTRA_STREAM,
            uri
        )

        addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            "Partilhar conversa"
        )
    )
}