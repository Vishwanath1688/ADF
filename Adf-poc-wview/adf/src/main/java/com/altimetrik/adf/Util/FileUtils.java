package com.altimetrik.adf.Util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by gyordi on 5/20/15.
 */
public class FileUtils {

    private static final String TAG = makeLogTag(FileUtils.class);

    private static final int IO_BUFFER_SIZE = 1024;

    public static float bytesAvailableInternalSpace() {
        long bytesAvailable = Environment.getDataDirectory().getUsableSpace();
        return bytesAvailable;
    }

    public static void moveRecursive(@NonNull File sourceLocation, @NonNull File targetLocation)
            throws IOException {

        copyRecursive(sourceLocation, targetLocation);

        deleteRecursive(sourceLocation);
    }

    public static void deleteRecursive(@NonNull File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public static void copyRecursive(@NonNull File sourceLocation, @NonNull File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {
                copyRecursive(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            LOGI(TAG, "moving " + sourceLocation + " to " + targetLocation);
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            // Copy the bits from instream to outstream
            byte[] buf = new byte[IO_BUFFER_SIZE];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public static long unzip(@NonNull String zipFile, @NonNull String destinationFolder) throws IOException {
        FileInputStream fin;
        fin = new FileInputStream(zipFile);
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry ze;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[IO_BUFFER_SIZE];
        int count;
        long totalSize = 0;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.isDirectory()) {
                File f = new File(destinationFolder + ze.getName());
                if (!f.exists()) {
                    f.mkdirs();
                }
            } else {
                FileOutputStream fout = new FileOutputStream(destinationFolder + ze.getName());
                while ((count = zin.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                    fout.write(baos.toByteArray());
                    baos.reset();
                    totalSize += count;
                }
                zin.closeEntry();
                fout.close();
            }
        }
        zin.close();
        return totalSize;
    }

    public static void copyAssetsFileOrDir(Context context, final String targetBasePath, String path) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] assets = assetManager.list(path);
            if (assets.length == 0) {
                copyAssetsFile(context, targetBasePath, path);
            } else {
                String fullPath = targetBasePath + File.separator + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    if (!dir.mkdirs())
                        LOGW(TAG, "could not create dir " + fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";

                    copyAssetsFileOrDir(context, targetBasePath, p + assets[i]);
                }
            }
        } catch (IOException ex) {
            LOGE(TAG, "I/O Exception", ex);
        }
    }

    public static void copyAssetsFile(Context context, final String targetBasePath, String filename) {
        AssetManager assetManager = context.getAssets();
        InputStream in;
        OutputStream out;
        String newFileName = null;
        try {
            //LOGI(TAG, "copyAssetsFile() " + filename);
            in = assetManager.open(filename);
            newFileName = targetBasePath + File.separator + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            LOGE(TAG, "Exception in copyAssetsFile() of " + newFileName, e);
        }
    }

    public static String readFile(String filePath) {
        String fileString = "";
        try {
            InputStream inputStream = new FileInputStream (new File(filePath));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                fileString = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            LOGE(TAG, "File not found: " + filePath, e);
        } catch (IOException e) {
            LOGE(TAG, "Can not read file: " + filePath, e);
        }
        return fileString;
    }

}
