package de.marketmaker.istar.merger.qos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

public class QosDaoDb extends JdbcDaoSupport implements QosDao {

    private InsertQosFilterValues insertQosFilterValues;

    private SelectValueByKey selectValueByKey;

    protected void initDao() throws Exception {
        super.initDao();
        this.selectValueByKey = new SelectValueByKey();
        this.insertQosFilterValues = new InsertQosFilterValues();
    }

    protected void insertValue(String key, byte[] object) {
        try {
            this.insertQosFilterValues.insert(key, object);
        } catch (IOException e) {
            this.logger.warn("<insert> insertion failed", e);
        }
    }

    protected byte[] selectValue(String key) {
        return this.selectValueByKey.findValue(key);
    }

    private class InsertQosFilterValues extends SqlUpdate {
        protected InsertQosFilterValues() {
            super(getDataSource(), "INSERT INTO cached_values " +
                    "(id, data, updated) VALUES (?, compress(?), now())" +
                    " ON DUPLICATE KEY UPDATE data=VALUES(data), num_updates=num_updates+1");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.BLOB));    //Binary Large Object
            compile();
        }

        protected void insert(String key, byte[] value) throws IOException {
            this.update(new Object[]{key, value});
        }
    }

    private class SelectValueByKey extends MappingSqlQuery {
        protected SelectValueByKey() {
            super(getDataSource(), "SELECT uncompress(data) FROM cached_values WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getBytes(1);
        }

        protected byte[] findValue(String key) {
            return (byte[]) findObject(key);
        }
    }

    private byte[] serialize(Serializable object) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        return baos.toByteArray();
    }

    private Object deSerialize(byte[] data) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(data);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final Object result = ois.readObject();
        ois.close();
        return result;
    }

    public void store(String key, Serializable value) throws Exception {
        if (value != null) {
            insertValue(key, serialize(value));
        }
    }

    public Serializable retrieve(String key) throws ClassNotFoundException, IOException {
        return (Serializable) deSerialize(selectValue(key));
    }
}
