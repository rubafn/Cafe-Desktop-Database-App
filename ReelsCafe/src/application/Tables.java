package application;

import java.util.ArrayList;
import java.util.List;

public class Tables {
	private String tableName;
    private List<String> primaryKey;
    private List<String> columns;
    private Tables detailTable;
    
    public Tables(String tableName) {
        this.tableName = tableName;
        this.primaryKey = new ArrayList<>();
        this.columns = new ArrayList<>();
    }
    public Tables addPK(String pk) {
    	primaryKey.add(pk);
    	columns.add(pk);
    	return this;
    }

    public Tables addColumn(String name) {
        columns.add(new String(name));
        return this;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getPrimaryKey() {
        return primaryKey;
    }

    public Tables setDetailTable(Tables t) {
        this.detailTable = t;
        return this;
    }

    public Tables getDetailTable() {
        return detailTable;
    }

    public boolean hasDetails() {
        return detailTable != null;
    }
}
    
