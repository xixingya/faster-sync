package tech.xixing.sql.maven;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import tech.xixing.sql.util.HttpClientUtil;


/**
 * @author liuzhifei
 * @since 1.0
 */
public class PomConvert {
    public static final String SNAPSHOT_MAVEN_OVERALL_META_DATA_PATH = "http://maven.aliyun.com/nexus/content/repositories/snapshots/@groupId@/@exposeApiOfArtifactId@/@version@/maven-metadata.xml";

    public static final String SNAPSHOT_URL = "http://maven.aliyun.com/nexus/content/groups/public/";

    public static final String RELEASE_URL = "http://maven.aliyun.com/nexus/content/repositories/releases/";


    public static Document parseGAVToDocument(String jobName, String gav) {
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(gav);
        } catch (DocumentException e) {
            throw new RuntimeException();
        }
        return doc;
    }

    /**
     * 转换获取 jar 的 url ，可以 进行下载。
     *
     * @param gav
     * @return
     */
    public static String getClassJarFileUrl(String jobName, String gav) {
        Document doc = parseGAVToDocument(jobName, gav);


        Element rootElt = doc.getRootElement();
        Element groupEl = rootElt.element("groupId");
        Element artEl = rootElt.element("artifactId");
        Element verEl = rootElt.element("version");
        if (groupEl == null || artEl == null || verEl == null) {
            throw new RuntimeException("pom信息有误");
        }
        boolean isShot = verEl.getStringValue().toUpperCase().indexOf("SNAPSHOT") >= 0;
        StringBuffer sb = new StringBuffer();

        sb.append(isShot ? SNAPSHOT_URL : RELEASE_URL);

        sb.append(groupEl.getStringValue().trim().replace(".", "/")).append("/");
        sb.append(artEl.getStringValue() + "/");
        sb.append(verEl.getStringValue() + "/");
        sb.append(artEl.getStringValue() + "-");

        if (isShot) {
            String latestShotTimeStamp = latestShotTimeStamp(jobName, groupEl.getStringValue(), artEl.getStringValue(), verEl.getStringValue());
            sb.append(verEl.getStringValue().replace("SNAPSHOT", "")).append(latestShotTimeStamp);
        } else {
            sb.append(verEl.getStringValue());
        }
        return sb.append(".jar").toString();
    }

    /**
     * 获取快照版本的时间戳信息
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     */
    public static String latestShotTimeStamp(String jobName, String groupId, String artifactId, String version) {
        groupId = groupId.replaceAll("\\.", "/");
        Document doc = null;

        // POM Meta 的路径
        String snapshotMavenMetaDataPath = snapshotMavenOverallMetaDataPath(groupId, artifactId, version);

        try {
            // 下载其 Meta 信息
            String body = HttpClientUtil.get(snapshotMavenMetaDataPath);
            if (body.contains("404 - Path")) {
                throw new RuntimeException();
            }                                     
            doc = DocumentHelper.parseText(body);

            Element metaRootElement = doc.getRootElement();

            Element latestShotTimeStamp = metaRootElement.element("versioning").element("snapshot");

            String timestamp = latestShotTimeStamp.element("timestamp").getStringValue();
            String buildNumber = latestShotTimeStamp.element("buildNumber").getStringValue();

            return timestamp + "-" + buildNumber;
        } catch (DocumentException e) {
            throw new RuntimeException();
        }
    }


    /**
     * 获取 meta-data.xml 文件的路径
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     */
    public static String snapshotMavenOverallMetaDataPath(String groupId, String artifactId, String version) {
        return SNAPSHOT_MAVEN_OVERALL_META_DATA_PATH.replace("@groupId@", groupId)
                .replace("@exposeApiOfArtifactId@", artifactId)
                .replace("@version@", version);
    }
}
