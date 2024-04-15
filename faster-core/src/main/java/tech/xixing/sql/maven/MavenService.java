package tech.xixing.sql.maven;

import com.sun.jna.Platform;
import tech.xixing.sql.exception.PluginException;
import tech.xixing.sql.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class MavenService {

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
                throw new PluginException("maven plugin is not exists " + jobName);
            }
        } catch (FileNotFoundException fileNotFoundException) {
           // log.error("target jar is empty of file ,jobName:{}, classJarFileUrl:{}, classFileName:{}, e:{}", jobName, classJarFileUrl, classFileName, fileNotFoundException);
            throw new PluginException("maven plugin is not exists " + jobName);
        } catch (Exception e) {
           // log.error("target jar is empty of file ,jobName:{}, classJarFileUrl:{}, classFileName:{} ,e:{}", jobName, classJarFileUrl, classFileName, e);
            throw new PluginException("download plugin exception" + jobName);
        }
    }

    /**
     * @return
     */
    public static String getJarBasePath() {
        if (Platform.isWindows()) {
            return "D:/data/faster-sync-plugins";
        }
        return "/data/faster-sync-plugins";
    }

}
