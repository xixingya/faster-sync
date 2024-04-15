package tech.xixing.sync.connector.mysql.source;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;

import java.io.Serializable;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class DefaultEventListener implements BinaryLogClient.EventListener {

    private String username;

    private String password;

    private int port;

    public DefaultEventListener(String username, String password, int port) {
        this.username = username;
        this.password = password;
        this.port = port;
    }

    @Override
    public void onEvent(Event event) {
        System.out.println(event.toString());
        EventHeader header = event.getHeader();
        EventType eventType = header.getEventType();
        // insert
        if (eventType == EventType.EXT_WRITE_ROWS) {
            WriteRowsEventData data = event.getData();
            List<Serializable[]> rows = data.getRows();
            MysqlConverter.convertRow2Str(rows);
            System.out.println("write rows" + data);
        }
        // update
        if (eventType == EventType.EXT_UPDATE_ROWS) {
            UpdateRowsEventData data = event.getData();
            List<Map.Entry<Serializable[], Serializable[]>> rows = data.getRows();
            System.out.println("update rows" + event.getData());
        }
        // delete
        if (eventType == EventType.EXT_DELETE_ROWS) {
            DeleteRowsEventData data = event.getData();
            System.out.println("delete rows" + event.getData());
        }
        EventData data = event.getData();
    }
}
