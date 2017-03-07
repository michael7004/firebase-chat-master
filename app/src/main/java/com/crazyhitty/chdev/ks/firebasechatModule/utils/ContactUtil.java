package com.crazyhitty.chdev.ks.firebasechatModule.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by indianic on 14/02/17.
 */

public class ContactUtil {

    private static String contactID;
    private static String contactNumber;
    private static String contactName;
    Bitmap photo;

    /**
     * To get contact id
     */
    public static String getContatctID(Context context, Uri uri) {
        // getting contacts ID
        Cursor cursorID = context.getContentResolver().query(uri,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);
        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursorID.close();
        Log.d("Cont", "" + contactID);
        return contactID;
    }

    /**
     * To get contact number
     */
    public static String getContatctNumber(Context context, String contactID) {
//       String contactNumber = null;
        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cursorPhone.close();
        Log.d("Cont", "" + contactNumber);
        return contactNumber;
    }

    /**
     * To get contact name
     */
    public static String getContatctName(Context context, Uri uriContact) {


//        String contactName = null;

        // querying contact data store
        Cursor cursor = context.getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        Log.d("Cont", "" + contactName);
        return contactName;

    }

    /**
     * To get specific email id of user
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getNameEmailDetails(Context context, Uri uri) {


        String email = "";
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(uri, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (cur1.moveToNext()) {
                    //to get the contact names
                    String name = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    Log.e("Name :", name);
                    email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Log.e("Email", email);

                }
                cur1.close();
            }
        }
        return email;
    }

    /**
     * To get the all email ids which is stored in your device.
     */
    public static ArrayList<String> getNameEmailDetails(Context context) {
        ArrayList<String> names = new ArrayList<String>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (cur1.moveToNext()) {
                    //to get the contact names
                    String name = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    Log.e("Name :", name);
                    String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Log.e("Email", email);
                    if (email != null) {
                        names.add(name);
                    }
                }
                cur1.close();
            }
        }
        return names;
    }


    /**
     * Retrieving the thumbnail-sized photo
     */

    public static InputStream getPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /**
     * Retrieving the larger photo version
     */
    public static InputStream openDisplayPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * To export all contact as vcf file
     */
    public static void getVCF(Context context) {
        final String vfile = "Contacts.vcf";
        Cursor phones = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        phones.moveToFirst();
        for (int i = 0; i < phones.getCount(); i++) {
            String lookupKey = phones.getString(phones
                    .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_VCARD_URI,
                    lookupKey);
            AssetFileDescriptor fd;
            try {
                fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
                FileInputStream fis = fd.createInputStream();
                byte[] buf = new byte[(int) fd.getDeclaredLength()];
                fis.read(buf);
                String VCard = new String(buf);
                String path = Environment.getExternalStorageDirectory()
                        .toString() + File.separator + vfile;
                FileOutputStream mFileOutputStream = new FileOutputStream(path,
                        true);
                mFileOutputStream.write(VCard.toString().getBytes());
                phones.moveToNext();
                Log.d("Vcard", VCard);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     * Converting Strings parameters into the vcf file
     */
    public static void getVCFFile(Context context, String name, String email, String phonenumber) {

        try {

            File vcfFile = new File(context.getExternalFilesDir(null), "generated.vcf");
            FileWriter fw = new FileWriter(vcfFile);
            fw.write("BEGIN:VCARD\r\n");
            fw.write("VERSION:3.0\r\n");
            fw.write("N:" + "" + ";" + "" + "\r\n");
            fw.write("FN:" + name + " " + "" + "\r\n");
            fw.write("ORG:" + "" + "\r\n");
            fw.write("TITLE:" + "" + "\r\n");
            fw.write("TEL;TYPE=WORK,VOICE:" + phonenumber + "\r\n");
            fw.write("TEL;TYPE=HOME,VOICE:" + "" + "\r\n");
            fw.write("ADR;TYPE=WORK:;;" + "" + ";" + "" + ";" + "" + ";" + "" + ";" + "" + "\r\n");
            fw.write("EMAIL;TYPE=PREF,INTERNET:" + email + "\r\n");
            fw.write("END:VCARD\r\n");
            fw.close();
//            Intent i = new Intent();
//            i.setAction(android.content.Intent.ACTION_SEND);
////            i.setDataAndType(Uri.fromFile(vcfFile), "text/x-vcard");
//            i.setType(ContactsContract.Contacts.CONTENT_VCARD_TYPE);
//            i.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(vcfFile));
//            context.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
