package tech.xixing.sync.connector.mysql.source.schema;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class Column {
    private String name;

    Integer position;

    private String type;


    public Column(String name, Integer position,String type) {
        this.name = name;
        this.position = position;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer index) {
        this.position = index;
    }
}
