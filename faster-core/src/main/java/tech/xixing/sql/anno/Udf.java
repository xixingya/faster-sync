package tech.xixing.sql.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liuzhifei
 * @since 0.1
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Udf {
    String name();
    String tableName() default "";
    String type() default "udf";
}
