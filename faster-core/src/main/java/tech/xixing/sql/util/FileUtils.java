package tech.xixing.sql.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author liuzhifei
 * @since 1.0
 */
@Slf4j
public class FileUtils {

    public static File downLoadFile(String remoteUrl, String savePath, String fileName)
            throws IOException {
        if (StringUtils.isBlank(remoteUrl)) {
            log.error("downLoadFile parameter empty ,remoteUrl:{}, savePath:{}, fileName:{}", remoteUrl, savePath, fileName);
            throw new RuntimeException("remoteUrl is blank");
        }
        if (StringUtils.isBlank(savePath) || StringUtils.isBlank(fileName)) {
            log.error("downLoadFile parameter empty ,remoteUrl:{}, savePath:{}, fileName:{}", remoteUrl, savePath, fileName);
            throw new RuntimeException("localPath or filename is blank");
        }
        File file = new File(savePath + fileName);
        org.apache.commons.io.FileUtils.copyURLToFile(new URL(remoteUrl), file);
        return file;
    }


    public static String pluginFileName(String path) {
        if (StringUtils.isBlank(path)) {
            throw new RuntimeException();
        }
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();

                if (ArrayUtils.isNotEmpty(files)) {
                    return Arrays.stream(files).map(File::getName).collect(Collectors.toList()).get(0);
                }
            }
        }
        throw new RuntimeException();
    }

    public static String buildUploadFileName(String version) {
        return "datax_" + version + "_.zip";
    }

    /**
     * 解压压缩文件到指定目录，并返回所有解压文件的绝对路径
     *
     * @param zip       压缩文件对象
     * @param unzipPath 解压目的文件夹
     * @return
     * @throws IOException
     */
    public static List<String> unZipFiles(ZipFile zip, String unzipPath) throws IOException {
        List<String> fileLoc = new ArrayList<String>();
        for (Enumeration<?> entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();

            InputStream in = null;
            OutputStream out = null;
            try {
                in = zip.getInputStream(entry);
                String outPath = (unzipPath + "/" + zipEntryName).replaceAll("\\*", "/");
                //判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!file.exists()) {
                    file.mkdirs();
                }
                //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[1024];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }

                fileLoc.add(outPath);
            } catch (Exception e) {
                log.error("unZipFiles exception ,unzipPath:{}, e:{}", unzipPath, e);
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
        return fileLoc;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     */
    public static void deleteDirRecursion(String path) {
        if (StringUtils.isBlank(path)) {
            return;
        }
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length != 0) {
                    for (File subFile : files) {
                        if (subFile.isDirectory()) {
                            deleteDirRecursion(subFile.getPath());
                        } else {
                            subFile.delete();
                        }
                    }
                }
            }
            file.delete();
        }
    }

    /**
     * 创建目录
     *
     * @param path
     * @throws Exception
     */
    public static void mkdir(String path) throws Exception {
        File file = new File(path);
        org.apache.commons.io.FileUtils.forceMkdir(file);
    }

    /**
     * 创建目录
     *
     * @param path
     * @throws Exception
     */
    public static void writeConfigToDisk(String path, String content) throws Exception {
        File file = new File(path);
        org.apache.commons.io.FileUtils.write(file, content, StandardCharsets.UTF_8);
    }
}
