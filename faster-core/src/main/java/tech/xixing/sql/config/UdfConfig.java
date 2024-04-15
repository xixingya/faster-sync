package tech.xixing.sql.config;


import lombok.Data;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * @author liuzhifei
 * @since 0.1
 */
@Data
public class UdfConfig {
    private String name;
    private Method method;
    private URLClassLoader classLoader;

    private Object instance;
}
