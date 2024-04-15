package tech.xixing.sync.connector.mysql.source;

import com.github.shyiko.mysql.binlog.BinaryLogClient;

import java.io.IOException;

/**
 * @author liuzhifei
 * @since 1.0
 * <p>source means a stream connector</p>
 */
public class MysqlBinlogSource {
    private BinaryLogClient binaryLogClient;

    private final String username;

    private final int port;

    private final String password;

    public MysqlBinlogSource(String host,int port,String username,String password){
        this.username = username;
        this.port = port;
        this.password = password;
        this.binaryLogClient=new BinaryLogClient(host,port,username,password);
    }

    public void addEventListener(BinaryLogClient.EventListener eventListener){
        binaryLogClient.registerEventListener(eventListener);
    }

    public void connect() throws IOException {
        binaryLogClient.connect();
    }

    public static void main(String[] args) throws IOException {
        MysqlBinlogSource mysqlBinlogSource = new MysqlBinlogSource("host", 7034, "root", "password");
        mysqlBinlogSource.addEventListener(new DefaultEventListener(null,null,-1));
        mysqlBinlogSource.connect();
    }
}
