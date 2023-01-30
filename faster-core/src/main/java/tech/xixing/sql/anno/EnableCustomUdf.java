package tech.xixing.sql.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liuzhifei
 * @since 0.1
 * @see tech.xixing.sql.udf.UdfFactory
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableCustomUdf {
    String[] scanPackage();
}
