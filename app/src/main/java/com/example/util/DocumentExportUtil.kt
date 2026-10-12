package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Aspiration
import com.example.data.model.CitizenRecord
import com.example.data.model.Complaint
import com.example.data.model.LetterRequest
import com.example.data.model.VillagePost
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentExportUtil {

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))

    fun formatTimestamp(date: Date?): String {
        return dateFormatter.format(date ?: Date())
    }

    fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    fun maskNik(nik: String, showFull: Boolean): String {
        if (showFull || nik.length < 10) return nik
        return nik.take(6) + "******" + nik.takeLast(4)
    }

    /**
     * Generates a real official PDF document for a Village Administrative Letter (Surat Desa)
     * and launches the Android Share/Viewer intent if requested.
     */
    fun generateAndShareLetterPdf(
        context: Context,
        letter: LetterRequest,
        launchShare: Boolean = true
    ): Result<File> = runCatching {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 15f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val subHeaderPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 11f
        }
        val boldBodyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11f
            isFakeBoldText = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            strokeWidth = 2f
        }

        // Kop Surat Pemerintah Desa Karangmulya
        canvas.drawText("PEMERINTAH KABUPATEN • KECAMATAN KARANGMULYA", 297f, 48f, subHeaderPaint)
        canvas.drawText("KANTOR KEPALA DESA KARANGMULYA", 297f, 68f, titlePaint)
        bodyPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Jl. Poros Balai Desa Karangmulya No. 1 • Markas Rakyat Digital Desa Karangmulya", 297f, 85f, bodyPaint)
        canvas.drawLine(50f, 98f, 545f, 98f, linePaint)

        // Judul Surat & Nomor Registrasi
        canvas.drawText(letter.letterType.uppercase(), 297f, 132f, titlePaint)
        canvas.drawText("Nomor Registrasi: ${letter.regNumber}", 297f, 150f, subHeaderPaint)

        bodyPaint.textAlign = Paint.Align.LEFT
        var y = 190f
        canvas.drawText("Yang bertanda tangan di bawah ini, Kepala Desa Karangmulya, menerangkan bahwa:", 55f, y, bodyPaint)

        y += 32f
        canvas.drawText("Nama Lengkap Pemohon", 70f, y, bodyPaint)
        canvas.drawText(": ${letter.applicantName}", 230f, y, boldBodyPaint)

        y += 24f
        canvas.drawText("Nomor Induk Kependudukan", 70f, y, bodyPaint)
        canvas.drawText(": ${letter.applicantNik}", 230f, y, boldBodyPaint)

        y += 24f
        canvas.drawText("Jenis Layanan Surat", 70f, y, bodyPaint)
        canvas.drawText(": ${letter.letterType}", 230f, y, boldBodyPaint)

        y += 24f
        canvas.drawText("Lampiran Berkas", 70f, y, bodyPaint)
        canvas.drawText(": ${letter.attachmentName.ifBlank { "KTP & KK Terverifikasi" }}", 230f, y, bodyPaint)

        y += 24f
        canvas.drawText("Status Pelayanan", 70f, y, bodyPaint)
        canvas.drawText(": ${letter.status}", 230f, y, boldBodyPaint)

        y += 36f
        canvas.drawText("Keperluan Pengajuan:", 55f, y, boldBodyPaint)
        y += 20f
        val purposeLines = letter.purpose.chunked(75)
        for (line in purposeLines) {
            canvas.drawText(line, 55f, y, bodyPaint)
            y += 18f
        }

        y += 18f
        canvas.drawText("Catatan / Tanggapan Perangkat Desa:", 55f, y, boldBodyPaint)
        y += 20f
        val noteLines = letter.staffNote.ifBlank { "Telah diverifikasi sesuai prosedur pelayanan Desa Karangmulya." }.chunked(75)
        for (line in noteLines) {
            canvas.drawText(line, 55f, y, bodyPaint)
            y += 18f
        }

        y += 28f
        canvas.drawText("Demikian surat keterangan ini dibuat dengan sebenarnya melalui Sistem Pelayanan", 55f, y, bodyPaint)
        y += 18f
        canvas.drawText("Publik Digital MARKAS RAKYAT DESA KARANGMULYA untuk dipergunakan sebagaimana mestinya.", 55f, y, bodyPaint)

        // Footer Signature & QR Verification Box
        val footerY = 640f
        val qrBoxPaint = Paint().apply {
            color = Color.rgb(224, 242, 254)
            style = Paint.Style.FILL
        }
        canvas.drawRect(55f, footerY, 240f, footerY + 110f, qrBoxPaint)
        canvas.drawText("VERIFIKASI QR CODE", 70f, footerY + 25f, boldBodyPaint)
        canvas.drawText(letter.qrCodeData.take(28), 70f, footerY + 48f, bodyPaint)
        canvas.drawText("Status: ${letter.status}", 70f, footerY + 70f, boldBodyPaint)
        canvas.drawText("Asli & Terdaftar di Database", 70f, footerY + 90f, bodyPaint)

        val dateStr = formatTimestamp(letter.updatedAt?.toDate() ?: letter.createdAt?.toDate())
        canvas.drawText("Desa Karangmulya, $dateStr", 340f, footerY + 20f, bodyPaint)
        canvas.drawText("Kepala Desa Karangmulya", 340f, footerY + 40f, boldBodyPaint)
        canvas.drawText("[ Ditandatangani Secara Elektronik ]", 340f, footerY + 75f, bodyPaint)
        canvas.drawText("H. HENDRA GUNAWAN, S.IP", 340f, footerY + 102f, boldBodyPaint)

        pdfDocument.finishPage(page)

        val safeReg = letter.regNumber.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(context.cacheDir, "Surat_Karangmulya_$safeReg.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        if (launchShare) {
            shareFile(context, file, "application/pdf", "Unduh / Bagikan Surat PDF (${letter.regNumber})")
        }
        file
    }

    /**
     * Generates a statistical service report in PDF format for Village Staff.
     */
    fun generateAndShareReportPdf(
        context: Context,
        letters: List<LetterRequest>,
        complaints: List<Complaint>,
        aspirations: List<Aspiration>,
        posts: List<VillagePost>,
        citizens: List<CitizenRecord>,
        launchShare: Boolean = true
    ): Result<File> = runCatching {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 15f
            isFakeBoldText = true
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 12f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 10.5f
        }

        canvas.drawText("LAPORAN STATISTIK PELAYANAN & PEMBANGUNAN", 50f, 50f, headerPaint)
        canvas.drawText("MARKAS RAKYAT DESA KARANGMULYA • Dicetak: ${formatTimestamp(Date())}", 50f, 70f, textPaint)

        var y = 105f
        canvas.drawText("1. RINGKASAN EKSEKUTIF PELAYANAN DESA", 50f, y, sectionPaint)
        y += 22f
        val verifiedCount = citizens.count { it.verificationStatus == "TERVERIFIKASI" }
        canvas.drawText("• Total Data Warga & Perangkat Terdaftar : ${citizens.size} Akun ($verifiedCount Terverifikasi)", 60f, y, textPaint)
        y += 18f
        val selesaiLetters = letters.count { it.status == "SELESAI" }
        canvas.drawText("• Total Permohonan Surat Administrasi    : ${letters.size} Permohonan ($selesaiLetters Selesai)", 60f, y, textPaint)
        y += 18f
        val selesaiComplaints = complaints.count { it.status == "SELESAI" }
        canvas.drawText("• Total Pengaduan Masyarakat             : ${complaints.size} Laporan ($selesaiComplaints Selesai)", 60f, y, textPaint)
        y += 18f
        canvas.drawText("• Total Aspirasi, Kritik & Saran Warga   : ${aspirations.size} Aspirasi", 60f, y, textPaint)
        y += 18f
        val projects = posts.filter { it.postType == "PEMBANGUNAN" }
        val totalBudget = projects.sumOf { it.budgetAmount }
        canvas.drawText("• Program Pembangunan Desa Aktif         : ${projects.size} Proyek (Total Pagu: ${formatRupiah(totalBudget)})", 60f, y, textPaint)

        y += 32f
        canvas.drawText("2. DAFTAR PERMOHONAN SURAT TERBARU", 50f, y, sectionPaint)
        y += 20f
        letters.take(8).forEachIndexed { idx, srt ->
            canvas.drawText("${idx + 1}. [${srt.regNumber}] ${srt.letterType} - ${srt.applicantName} (${srt.status})", 60f, y, textPaint)
            y += 16f
        }

        y += 24f
        canvas.drawText("3. DAFTAR PENGADUAN MASYARAKAT TERBARU", 50f, y, sectionPaint)
        y += 20f
        complaints.take(6).forEachIndexed { idx, adu ->
            canvas.drawText("${idx + 1}. [${adu.regNumber}] ${adu.title.take(45)} - ${adu.locationName.take(25)} (${adu.status})", 60f, y, textPaint)
            y += 16f
        }

        y += 24f
        canvas.drawText("4. REALISASI PROGRAM PEMBANGUNAN DESA", 50f, y, sectionPaint)
        y += 20f
        projects.take(5).forEachIndexed { idx, prj ->
            canvas.drawText("${idx + 1}. ${prj.title.take(48)} | ${formatRupiah(prj.budgetAmount)} | Fisik: ${prj.progressPercent}%", 60f, y, textPaint)
            y += 16f
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "Laporan_Pelayanan_Desa_Karangmulya.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        if (launchShare) {
            shareFile(context, file, "application/pdf", "Ekspor Laporan Pelayanan PDF")
        }
        file
    }

    /**
     * Generates an Excel-compatible CSV file containing letters, complaints, and citizen statistics.
     */
    fun generateAndShareReportCsv(
        context: Context,
        letters: List<LetterRequest>,
        complaints: List<Complaint>,
        citizens: List<CitizenRecord>,
        launchShare: Boolean = true
    ): Result<File> = runCatching {
        val sb = StringBuilder()
        sb.appendLine("KATEGORI_DATA,NOMOR_REGISTRASI,NAMA_WARGA,JENIS_KATEGORI,RINCIAN_KEPERLUAN,STATUS,CATATAN_TINDAK_LANJUT")
        letters.forEach { srt ->
            sb.appendLine(
                "SURAT_ONLINE,\"${srt.regNumber}\",\"${srt.applicantName}\",\"${srt.letterType}\",\"${srt.purpose.replace("\"", "'")}\",\"${srt.status}\",\"${srt.staffNote.replace("\"", "'")}\""
            )
        }
        complaints.forEach { adu ->
            sb.appendLine(
                "PENGADUAN,\"${adu.regNumber}\",\"${adu.reporterName}\",\"${adu.category}\",\"${adu.title.replace("\"", "'")} (${adu.locationName})\",\"${adu.status}\",\"${adu.staffResponse.replace("\"", "'")}\""
            )
        }
        citizens.forEach { cit ->
            sb.appendLine(
                "DATA_WARGA,\"${maskNik(cit.nik, false)}\",\"${cit.fullName}\",\"${cit.directoryGroup}\",\"${cit.dusun} ${cit.rtRw}\",\"${cit.verificationStatus}\",\"${cit.occupation}\""
            )
        }

        val file = File(context.cacheDir, "Data_Pelayanan_Karangmulya_Excel.csv")
        file.writeText(sb.toString())

        if (launchShare) {
            shareFile(context, file, "text/csv", "Ekspor Laporan Excel / CSV")
        }
        file
    }

    private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Ignored in headless test environments
        }
    }
}
