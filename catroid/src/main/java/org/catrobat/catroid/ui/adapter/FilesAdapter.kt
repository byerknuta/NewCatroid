
package org.catrobat.catroid.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danvexteam.lunoscript_annotations.LunoClass
import org.catrobat.catroid.R
import org.catrobat.catroid.content.Project
import java.io.File

@LunoClass
class FilesAdapter(
    private val project: Project?,
    private val files: List<String>,
    private val onDelete: (String) -> Unit,
    private val onCopy: (String) -> Unit,
    private val onOpen: (String) -> Unit
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.file_name)
        val fileSize: TextView = view.findViewById(R.id.file_size)
        val fileIcon: ImageView = view.findViewById(R.id.file_icon)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileName = files[position]
        holder.fileName.text = fileName


        val extension = fileName.substringAfterLast('.', "").lowercase()
        val iconRes = when (extension) {
            "py", "lua", "js", "java", "kt", "xml", "json", "txt", "md" -> R.drawable.code_24px
            "png", "jpg", "jpeg", "webp" -> R.drawable.ic_draw_image
            "mp3", "wav", "ogg" -> R.drawable.ic_music_library
            else -> R.drawable.file_present_24px
        }
        holder.fileIcon.setImageResource(iconRes)


        project?.let {
            var file: File? = it.getFile(fileName) ?: it.getLib(fileName)

            if (file != null && file.exists()) {
                holder.fileSize.text = formatFileSize(file.length())
            } else {
                file = it.getLib(fileName)
                if (file != null && file.exists()) {
                    holder.fileSize.text = formatFileSize(file.length())
                } else {
                    holder.fileSize.text = "Файл не найден"
                }
            }
        }


        holder.deleteButton.setOnClickListener {
            onDelete(fileName)
        }


        holder.itemView.setOnClickListener {
            onOpen(fileName)
        }


        holder.itemView.setOnLongClickListener {
            onCopy(fileName)
            true
        }
    }

    fun formatFileSize(size: Long): String {
        val units = arrayOf("Б", "КБ", "МБ", "ГБ", "ТБ")
        var sizeInUnits = size.toDouble()
        var index = 0

        while (sizeInUnits >= 1024 && index < units.size - 1) {
            sizeInUnits /= 1024
            index++
        }

        return String.format("%.1f %s", sizeInUnits, units[index])
    }


    override fun getItemCount() = files.size
}
