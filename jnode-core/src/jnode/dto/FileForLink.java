package jnode.dto;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filesforlink")
public class FileForLink {
    @DatabaseField(foreign = true, columnName = "link_id", uniqueIndexName = "idnameidx")
    private Link link;
    @DatabaseField(uniqueIndexName = "idnameidx", dataType = DataType.STRING, width = 255)
    private String filename;

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileForLink{");
        sb.append("link=").append(link);
        sb.append(", filename='").append(filename).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
