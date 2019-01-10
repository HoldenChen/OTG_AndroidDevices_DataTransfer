package com.threedi.simulatewritevideofile;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static String getFullPathFromTreeUri(@Nullable final Uri treeUri,final Context context){
        if(treeUri == null){
            return null;
        }

        String volumeBasePath = getVolumePath(getVolumeIdFromTreeUri(treeUri),context);

        if(volumeBasePath == null){
            return File.separator;
        }
        String volumePath = volumeBasePath;

        if(volumeBasePath.endsWith(File.separator)){
            volumePath = volumePath.substring(0,volumePath.length() -1);
        }

        String documentPath = FileUtil.getDocumentPathFromTreeUri(treeUri);
        if(documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0,documentPath.length()-1);
        }

        if(documentPath.length() > 0){
            if(documentPath.startsWith(File.separator)){
                return volumePath + documentPath;
            }else{
                return volumePath + File.separator + documentPath;
            }
        }else{
            return volumePath;
        }


    }

    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        }
        else {
            return null;
        }
    }


    /**
     * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
     *
     * @param treeUri The tree URI.
     * @return the document path.
     */
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        }
        else {
            return File.separator;
        }
    }

    private static String getVolumePath(final String volumeId,Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = storageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(storageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
//                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
//                    return (String) getPath.invoke(storageVolumeElement);
//                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        }
        catch (Exception ex) {
            return null;
        }
    }


    public static DocumentFile getDocumentFileIfAllowedToWrite(File file, Context con) {
        List<UriPermission> permissionUris = con.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permissionUri : permissionUris) {
            Uri treeUri = permissionUri.getUri();
            DocumentFile rootDocFile = DocumentFile.fromTreeUri(con, treeUri);
            String rootDocFilePath = FileUtil.getFullPathFromTreeUri(treeUri, con);

            if (file.getAbsolutePath().startsWith(rootDocFilePath)) {

                ArrayList<String> pathInRootDocParts = new ArrayList<String>();
                while (!rootDocFilePath.equals(file.getAbsolutePath())) {
                    pathInRootDocParts.add(file.getName());
                    file = file.getParentFile();
                }
                DocumentFile docFile = null;
                if (pathInRootDocParts.size() == 0) {
                    docFile = DocumentFile.fromTreeUri(con, rootDocFile.getUri());
                } else {
                    for (int i = pathInRootDocParts.size() - 1; i >= 0; i--) {
                        if (docFile == null) {
                            docFile = rootDocFile.findFile(pathInRootDocParts.get(i));
                        } else {
                            docFile = docFile.findFile(pathInRootDocParts.get(i));
                        }
                    }
                }
                if (docFile != null && docFile.canWrite()) {
                    return docFile;
                } else {
                    return null;
                }

            }
        }
        return null;
    }


    public static String mime(String URI) {
        String type = "";
        String extention = MimeTypeMap.getFileExtensionFromUrl(URI);
        if (extention != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
        }
        return type;
    }


}
