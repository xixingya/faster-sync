package tech.xixing.sql.udf;

import cn.hutool.core.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import tech.xixing.sql.anno.EnableCustomUdf;
import tech.xixing.sql.anno.EnableUdfClass;
import tech.xixing.sql.anno.Udf;
import tech.xixing.sql.config.UdfConfig;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author liuzhifei
 * @since 0.1
 */
@Slf4j
public class UdfFactory {

    private static Set<UdfConfig> globalUdf = new HashSet<>();

    static {
        init();
    }

    /**
     * key是tableName
     * value是对应table的udf
     */
    private static Map<String, Set<UdfConfig>> tableCustomUdf = new HashMap<>();


    public static Set<UdfConfig> getUdfByTable(String tableName) {
        Set<UdfConfig> res = new HashSet<>(globalUdf);
        Set<UdfConfig> udfConfigs = tableCustomUdf.computeIfAbsent(tableName, k -> new HashSet<>());
        res.addAll(udfConfigs);
        return res;
    }

    /**
     * init udf
     */
    public static void init() {
        Class<?> mainApplicationClass = deduceMainApplicationClass();
        if (mainApplicationClass != null && mainApplicationClass.isAnnotationPresent(EnableCustomUdf.class)) {
            EnableCustomUdf enableCustomUdf = mainApplicationClass.getAnnotation(EnableCustomUdf.class);
            String[] scanPackages = enableCustomUdf.scanPackage();
            for (String scanPackage : scanPackages) {
                setUdfByPackage(scanPackage);
            }
        }
        // 默认的udf仓库
        setUdfByPackage("tech.xixing.sql.udf");
    }


    /**
     * 获取 main方法所在类
     *
     * @return 返回所在类
     */
    private static Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        } catch (ClassNotFoundException ex) {
            // Swallow and continue
        }
        return null;
    }

    /**
     * set udf by packageName
     *
     * @param packageName packageName
     */
    private static void setUdfByPackage(String packageName) {
        Set<Class<?>> classes = ClassUtil.scanPackage(packageName);
        for (Class<?> aClass : classes) {
            Method[] methods = aClass.getMethods();
            for (Method declaredMethod : methods) {
                if (declaredMethod.isAnnotationPresent(Udf.class)) {
                    Udf udf = declaredMethod.getAnnotation(Udf.class);
                    UdfConfig udfConfig = new UdfConfig();
                    udfConfig.setMethod(declaredMethod);
                    udfConfig.setName(udf.name());
                    String tableName = udf.tableName();
                    if ("".equals(tableName)) {
                        globalUdf.add(udfConfig);
                    } else {
                        Set<UdfConfig> udfConfigs = tableCustomUdf.computeIfAbsent(tableName, key -> new HashSet<>());
                        udfConfigs.add(udfConfig);
                    }
                }
            }
        }
    }

    public static void setUdfByTableNameAndConfigs(String tableName, Set<UdfConfig> udfConfigs) {
        tableCustomUdf.put(tableName, udfConfigs);
    }

    public static Set<UdfConfig> loadUdfConfigsByClasses(List<Class<?>> classes) throws InstantiationException, IllegalAccessException {
        if (classes == null || classes.size() == 0) {
            return null;
        }
        Set<UdfConfig> res = new HashSet<>();
        for (Class<?> aClass : classes) {
            boolean annotationPresent = aClass.isAnnotationPresent(EnableUdfClass.class);
            if (annotationPresent) {
                Method[] methods = aClass.getMethods();
                for (Method declaredMethod : methods) {
                    if (declaredMethod.isAnnotationPresent(Udf.class)) {
                        Udf udf = declaredMethod.getAnnotation(Udf.class);
                        UdfConfig udfConfig = new UdfConfig();
                        udfConfig.setMethod(declaredMethod);
                        udfConfig.setName(udf.name());
                        udfConfig.setClassLoader((URLClassLoader) aClass.getClassLoader());
                        String tableName = udf.tableName();
                        if ("".equals(tableName)) {
                            globalUdf.add(udfConfig);
                        } else {
                            Set<UdfConfig> udfConfigs = tableCustomUdf.computeIfAbsent(tableName, key -> new HashSet<>());
                            udfConfigs.add(udfConfig);
                        }
                        log.info("[setUdfByPackage] load udf config name={} tableName={}", udf.name(), tableName);
                        res.add(udfConfig);
                    }
                }
            }
        }
        return res;
    }
}
