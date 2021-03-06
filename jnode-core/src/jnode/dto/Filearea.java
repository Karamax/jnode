package jnode.dto;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "filearea")
public class Filearea {
    @DatabaseField(generatedId = true)
    private Long id;
    @DatabaseField(columnName = "name", canBeNull = false, uniqueIndex = true)
    private String name;
    @DatabaseField(columnName = "description", dataType = DataType.STRING, width = 1000)
    private String description;
    @DatabaseField(columnName = "wlevel", canBeNull = false, defaultValue = "0")
    private Long writelevel;
    @DatabaseField(columnName = "rlevel", canBeNull = false, defaultValue = "0")
    private Long readlevel;
    @DatabaseField(columnName = "grp", canBeNull = false, defaultValue = "")
    private String group;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getWritelevel() {
        return writelevel;
    }

    public void setWritelevel(Long writelevel) {
        this.writelevel = writelevel;
    }

    public Long getReadlevel() {
        return readlevel;
    }

    public void setReadlevel(Long readlevel) {
        this.readlevel = readlevel;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Filearea filearea = (Filearea) o;

        if (id != null ? !id.equals(filearea.id) : filearea.id != null) return false;
        if (name != null ? !name.equals(filearea.name) : filearea.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
