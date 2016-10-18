package com.altimetrik.adf.Core.Managers.ContactManager;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.provider.ContactsContract;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 5/6/15.
 */
public class ATKContactManager {

    private static final String TAG = makeLogTag(ATKContactManager.class);

    public static void addContact(JSONObject contactData, Context context) {
        String displayName = "";
        try {
            displayName = String.format("%s %s", contactData.has("firstName") ? contactData.getString("firstName") : "", contactData.has("lastName") ? contactData.getString("lastName") : "");
            JSONArray phones = contactData.has("phones") ? contactData.getJSONArray("phones") : new JSONArray();

            if (phones.length() > 0) {
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();

                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());

                //Display Name
                if (!displayName.isEmpty()) {
                    ops.add(ContentProviderOperation.newInsert(
                            ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(
                                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                    displayName).build());
                }

                for (int i = 0; i < phones.length(); i++) {
                    JSONObject phone = (JSONObject) phones.get(i);
                    Iterator<String> keys = phone.keys();

                    while(keys.hasNext() ) {
                        String key = keys.next();
                        if (key.contains("fax")) {
                            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                    .withValue(ContactsContract.Data.MIMETYPE,
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.optString(key))
                                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK)
                                    .build());
                        } else if (key.contains("phone")) {
                            ops.add(ContentProviderOperation.
                                    newInsert(ContactsContract.Data.CONTENT_URI)
                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                    .withValue(ContactsContract.Data.MIMETYPE,
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.optString(key))
                                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                                    .build());
                        }
                    }
                }
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(context, String.format("%s added to contacts.", displayName), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            LOGD(TAG, "addContact", e);
            Toast.makeText(context, String.format("Error adding %s to contacts. Please try again later.", displayName), Toast.LENGTH_LONG).show();
        }
    }

}
