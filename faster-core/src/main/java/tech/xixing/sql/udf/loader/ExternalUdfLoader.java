package tech.xixing.sql.udf.loader;

import tech.xixing.sql.udf.UdfFactory;

import java.util.List;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class ExternalUdfLoader implements Loader {

    static {
        System.out.println("aaa");
    }

    public void loadByMaven(String pom) {

    }

    public void notifyUdfFactory(List<Class<?>> classes) throws InstantiationException, IllegalAccessException {
        UdfFactory.loadUdfConfigsByClasses(classes);
    }

}
