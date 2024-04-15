package tech.xixing.sql.exception;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class PluginException extends RuntimeException {

    private String msg;

    public PluginException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public PluginException(String msg, Throwable throwable) {
        super(msg, throwable);
        this.msg = msg;
    }


    @Override
    public String toString() {
        return "PluginException{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
