package tech.xixing.sql.maven;

import com.sun.jna.Platform;
import tech.xixing.sql.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class MavenUtil {

    public static final String APPEND_CHAR = "/";
    /**
     * 下载 jar
     *
     * @param gav
     * @param workBasePath
     * @return
     */
    public static void downloadJarFile(String jobName, String gav, String workBasePath) {
        // 编译jar包地址
        String classJarFileUrl = PomConvert.getClassJarFileUrl(jobName, gav);
        String classFileName = classJarFileUrl.substring(classJarFileUrl.lastIndexOf(APPEND_CHAR) + 1);

        try {
            // 下载，存放在指定的目录下。
            File classFile = FileUtils.downLoadFile(classJarFileUrl, workBasePath, classFileName);

            if (classFile.length() == 0) {

            }
        } catch (FileNotFoundException fileNotFoundException) {

        } catch (Exception e) {

        }
    }

    /**
     * @return
     */
    public static String getJarBasePath() {
        if(Platform.isWindows()){
            return "D:/data/udfjars";
        }
        return "/data/udfjars";
    }
}
