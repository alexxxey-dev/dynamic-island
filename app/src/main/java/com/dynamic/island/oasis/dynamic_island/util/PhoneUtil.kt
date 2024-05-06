package com.dynamic.island.oasis.dynamic_island.util

import android.app.Notification.Action
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.telecom.TelecomManager
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.MyContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class PhoneUtil(
    private val telecom:TelecomManager,
    private val contentResolver: ContentResolver,
    private val resources: Resources
) {

    fun defaultCallApp(): String? = try{
        telecom.defaultDialerPackage
    }catch (ex:Exception){
        ex.printStackTrace()
        null
    }

    fun callSessionPackage() = "com.android.server.telecom"


    private val default = MyContact(
        phoneNumber = "?",
        name =  resources.getString(R.string.unknown),
        letter = "?"
    )



    suspend fun getCallContact(title: String?): MyContact = withContext(Dispatchers.IO) {

        if(title==null) return@withContext default
        try {
            val cursor = contentResolver.query(
                Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(title)
                ),
                arrayOf(
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
                    ContactsContract.PhoneLookup.NUMBER
                ),
                null,
                null,
                null
            )!!

            cursor.moveToFirst()

            val photo = getPhoto(cursor)
            val name = getName(cursor) ?: title
            val number = getNumber(cursor) ?: title
            val letter = getLetter(name,number,title)
            val mContact =  MyContact(number, name, photo,letter)
            Logs.log("contact = $mContact")

            cursor.close()
            mContact
        }catch (ex:Exception){
            ex.printStackTrace()
           default
        }

    }

    private fun getLetter(name:String?,number:String?, title:String?):String{
        return if(!name.isNullOrEmpty()) {
            name.firstOrNull { it.isLetter() || it.isDigit() }.toString()
        } else if(!title.isNullOrEmpty()){
            title.firstOrNull { it.isLetter() || it.isDigit() }.toString()
        } else if (!number.isNullOrEmpty() && !number.startsWith("?")) {
            number.firstOrNull { it.isLetter() || it.isDigit() }.toString()
        } else "?"
    }

    private fun getNumber(cursor: Cursor):String? = try {
        val numberIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)
        cursor.getString(numberIndex)
    }catch (ex:Exception){
        ex.printStackTrace()
        null
    }
    private fun getPhoto(cursor: Cursor): Bitmap? = try {
        val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
        val uri = Uri.parse(cursor.getString(photoIndex))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    } catch (ex: Exception) {
        null
    }

    private fun getName(cursor: Cursor): String? = try {
        val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
        cursor.getString(nameIndex)
    } catch (ex: Exception) {
         null
    }
}