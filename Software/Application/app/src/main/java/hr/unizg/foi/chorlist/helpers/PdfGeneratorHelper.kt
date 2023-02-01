package hr.unizg.foi.chorlist.helpers

import android.content.Context
import android.graphics.Color
import android.os.Environment
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import hr.unizg.foi.chorlist.models.views.ItemView
import hr.unizg.foi.chorlist.models.views.ShoppingListItemsView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * In charge of generating PDF
 *
 * @property context represents state of passed object
 * @property shoppingListsWithItems represents all shopping lists with belonging items
 * @constructor Create empty Pdf generator helper instance
 */
class PdfGeneratorHelper(
    private val context: Context,
    private val shoppingListsWithItems: List<ShoppingListItemsView>
) {

    private var pdfDocument: Document = Document(PageSize.A4)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            generatePdf()
        }
    }

    /**
     * Contains methods for creating and configuring PDF
     *
     */
    private fun generatePdf() {
        displayResult()
        openDocument()
        addHeader()
        addChores()
        closeDocument()
    }

    /**
     * In charge for opening PDF
     */
    private fun openDocument() {
        pdfDocument.open()
    }

    /**
     * In charge for closing PDF
     */
    private fun closeDocument() {
        pdfDocument.close()
    }

    /**
     * In charge for adding PDF header
     */
    private fun addHeader() {
        val headerText = drawHeaderText()
        val subheaderText = drawSubheaderText()
        val currentDate = drawCurrentDate()
        val lineSeparator = drawHeaderLine()
        val spacingAfterLineSeparator = addSpacing(30F)

        pdfDocument.apply {
            add(headerText)
            add(subheaderText)
            add(currentDate)
            add(lineSeparator)
            add(spacingAfterLineSeparator)
        }
    }

    /**
     * In charge for drawing chores to PDF
     */
    private fun addChores() {
        for (shoppingList: ShoppingListItemsView in shoppingListsWithItems) {
            val choreText = drawChoreName(shoppingList.description)
            val bottomChoreNameLine = drawChoreNameLine()

            pdfDocument.apply {
                add(choreText)
                add(bottomChoreNameLine)
            }

            for ((subChorePosition, itemsInShoppingList: ItemView) in
            shoppingList.items.withIndex()) {
                val subChoreText =
                    itemsInShoppingList.description?.let {
                        drawSubChoreName(it, subChorePosition + 1)
                    }
                pdfDocument.add(subChoreText)
            }
        }
    }

    /**
     * In charge for drawing sub chore style PDF
     */
    private fun drawSubChoreName(subChoreName: String, position: Int): Paragraph {
        val subChoreFont = Font(Font.FontFamily.TIMES_ROMAN, 15F, Font.BOLDITALIC)
        val subChoreText = Paragraph("$position. $subChoreName", subChoreFont)

        subChoreText.apply {
            indentationLeft = 20F
            spacingAfter = 5F
        }

        return subChoreText
    }

    /**
     * In charge for drawing chore style PDF
     */
    private fun drawChoreName(choreName: String): Paragraph {
        val choreFont = Font(Font.FontFamily.TIMES_ROMAN, 25F, Font.BOLDITALIC)
        val choreText = Paragraph(choreName, choreFont)

        choreText.spacingAfter = 5F

        return choreText
    }

    /**
     * In charge for drawing horizontal line below chore name
     */
    private fun drawChoreNameLine(): LineSeparator {
        val lineSeparator = LineSeparator()

        lineSeparator.apply {
            lineColor = BaseColor(Color.BLACK)
            lineWidth = 1F
        }

        return lineSeparator
    }

    /**
     * In charge for drawing header text
     */
    private fun drawHeaderText(): Paragraph {
        val headerFont = Font(Font.FontFamily.TIMES_ROMAN, 60F, Font.BOLDITALIC)
        val headerText = Paragraph("Chorlist", headerFont)

        headerText.apply {
            spacingAfter = 15F
            alignment = Paragraph.ALIGN_CENTER
            leading = 30F
        }

        return headerText
    }

    /**
     * In charge for drawing subheader text
     */
    private fun drawSubheaderText(): Paragraph {
        val subheaderFont = Font(Font.FontFamily.TIMES_ROMAN, 20F, Font.NORMAL)
        val subheaderText = Paragraph("My list of chores", subheaderFont)

        subheaderText.apply {
            spacingAfter = 10F
            alignment = Paragraph.ALIGN_CENTER
        }

        return subheaderText
    }

    /**
     * In charge for drawing date
     */
    private fun drawCurrentDate(): Paragraph {
        val calendar = Calendar.getInstance()
        val date = calendar.time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:MM:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        val dataFont = Font(Font.FontFamily.TIMES_ROMAN, 15F, Font.ITALIC)
        val dateText = Paragraph("Date: $formattedDate", dataFont)

        dateText.apply {
            spacingAfter = 5F
            alignment = Paragraph.ALIGN_RIGHT
        }

        return dateText
    }

    /**
     * In charge for drawing horizontal line header text
     */
    private fun drawHeaderLine(): LineSeparator {
        val lineSeparator = LineSeparator()

        lineSeparator.apply {
            lineColor = BaseColor(Color.BLACK)
            lineWidth = 2F
        }

        return lineSeparator
    }

    /**
     * In charge for adding spacing below element
     */
    private fun addSpacing(spacing: Float): Paragraph {
        val spacingTextFont = Font(Font.FontFamily.TIMES_ROMAN, 10F, Font.NORMAL)
        val spacingText = Paragraph("", spacingTextFont)

        spacingText.apply {
            spacingAfter = spacing
            alignment = Paragraph.ALIGN_CENTER
        }

        return spacingText
    }

    /**
     * In charge for displaying information to user about PDF file generation result
     */
    private fun displayResult() {
        try {
            PdfWriter.getInstance(
                pdfDocument, FileOutputStream(
                    Environment.getExternalStorageDirectory().path + "/Chorlist.pdf"
                )
            )
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "PDF file generated..", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Fail to generate PDF file..", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}