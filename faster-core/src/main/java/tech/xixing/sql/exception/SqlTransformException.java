package tech.xixing.sql.exception;

/**
 * Exception thrown during SQL transformation operations
 * 
 * @author liuzhifei
 * @since 0.2
 */
public class SqlTransformException extends RuntimeException {
    
    public SqlTransformException(String message) {
        super(message);
    }
    
    public SqlTransformException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SqlTransformException(Throwable cause) {
        super(cause);
    }
}
