package com.example.camera


import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.io.IOException

object ExifUtil {

    internal fun rotateBitmap(bitmap: Bitmap, _orientation: Int): Bitmap {
        var orientation = _orientation
        try {
            orientation = getExifOrientationFromAngle(orientation)

            if (orientation == 1) {
                return bitmap
            }

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_UNDEFINED -> {
                }

                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1.0f, 1.0f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180.0f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1.0f, -1.0f)
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90.0f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(-90.0f)
                    matrix.postScale(1.0f, -1.0f)
                }

                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90.0f)
                    matrix.postScale(1.0f, -1.0f)
                }

                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(-90.0f)
            }

            return try {
                val oriented =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                oriented
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                bitmap
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    private fun getExifRotation(file: File?): Int {
        if (file == null) return 0
        try {
            val exif = ExifInterface(file.absolutePath)
            return getRotateDegreeFromOrientation(
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )
            )
        } catch (e: IOException) {
            //Logger.e("An error occurred while getting the exif data: " + e.getMessage(), e);
        }

        return 0
    }

    private fun getExifRotation(context: Context, uri: Uri): Int {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Images.ImageColumns.ORIENTATION)
        return try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor == null || !cursor.moveToFirst()) {
                0
            } else cursor.getInt(0)
        } catch (ignored: RuntimeException) {
            0
        } finally {
            cursor?.close()
        }
    }

    internal fun getExifOrientation(context: Context, uri: Uri): Int {
        var orientation = 0
        try {
            val authority = uri.authority?.lowercase()
            orientation = if (authority?.endsWith("media") == true) {
                getExifRotation(context, uri)
            } else {
                getExifRotation(getFileFromUri(context, uri))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return orientation
    }

    private fun getRotateDegreeFromOrientation(orientation: Int): Int {
        var degree = 0
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            else -> {
            }
        }
        return degree
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        var filePath: String? = null

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    filePath = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            }
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            filePath = uri.path
        }// File
        return if (filePath != null) {
            File(filePath)
        } else null
    }


    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun getExifOrientationFromAngle(angle: Int): Int {
        return when (angle % 360) {
            0 -> ExifInterface.ORIENTATION_NORMAL
            90 -> ExifInterface.ORIENTATION_ROTATE_90
            180 -> ExifInterface.ORIENTATION_ROTATE_180
            270 -> ExifInterface.ORIENTATION_ROTATE_270
            else -> ExifInterface.ORIENTATION_NORMAL
        }
    }


}

