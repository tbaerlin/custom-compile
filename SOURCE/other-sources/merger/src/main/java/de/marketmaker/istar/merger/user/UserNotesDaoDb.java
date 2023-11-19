package de.marketmaker.istar.merger.user;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.MappingSqlQueryWithParameters;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;


public class UserNotesDaoDb extends JdbcDaoSupport implements UserNotesDao {

    private CreateNote createNote;
    private ReadNote readNote;
    private UpdateNote updateNote;
    private DeleteNote deleteNote;

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        this.createNote = new CreateNote();
        this.readNote = new ReadNote();
        this.updateNote = new UpdateNote();
        this.deleteNote = new DeleteNote();
    }

    /**
     * create note action returns the unique DB id of the new note
     */
    private class CreateNote {

        private final SqlUpdate sqlUpdate;

        public CreateNote() {
            sqlUpdate = new SqlUpdate(getDataSource(), "INSERT INTO notes "
                    + "(portfolioid, itemid, content)"
                    + " VALUES (?, ?, ?)");
            sqlUpdate.declareParameter(new SqlParameter(Types.INTEGER));
            sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
            sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
            sqlUpdate.setReturnGeneratedKeys(true);
            sqlUpdate.compile();
        }

        public long create(long portfolioId, String itemid, String content) {
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            sqlUpdate.update(new Object[]{portfolioId, itemid, content}, keyHolder);
            return keyHolder.getKey().longValue();
        }
    }

    /**
     * read note, used with primary key or portfolioid/watchlistid and itemid (=instrument/quote/iid)
     */
    private class ReadNote {

        private final MappingSqlQuery idRead;
        private final MappingSqlQuery portfolioRead;
        private final MappingSqlQueryWithParameters portfolioIdRead;

        public ReadNote() {
            idRead = new MappingSqlQuery(getDataSource(),
                    "SELECT content FROM notes WHERE id=?") {
                @Override
                protected Object mapRow(ResultSet rs, int i) throws SQLException {
                    return rs.getString(1);
                }
            };
            idRead.declareParameter(new SqlParameter(Types.INTEGER));
            idRead.compile();

            portfolioRead = new MappingSqlQuery(getDataSource(),
                    "SELECT content FROM notes WHERE portfolioId=? AND itemid=?") {
                @Override
                protected Object mapRow(ResultSet rs, int i) throws SQLException {
                    return rs.getString(1);
                }
            };
            portfolioRead.declareParameter(new SqlParameter(Types.INTEGER));
            portfolioRead.declareParameter(new SqlParameter(Types.VARCHAR));
            portfolioRead.compile();

            portfolioIdRead = new MappingSqlQueryWithParameters(getDataSource(),
                    "SELECT itemid, content FROM notes WHERE portfolioId=?") {
                @Override
                protected Object mapRow(ResultSet rs, int rowNum,
                                        Object[] parameters, Map result) throws SQLException {
                    result.put(rs.getString(1), rs.getString(2));
                    return null;
                }
            };
            portfolioIdRead.declareParameter(new SqlParameter(Types.INTEGER));
        }

        public String read(long id) {
            return (String) idRead.findObject(id);
        }

        public String read(long portfolioId, String itemid) {
            return (String) portfolioRead.findObject(portfolioId, itemid);
        }

        public Map<String, String> readPortfolioNotes(long portfolioId) {
            Map<String, String> result = new HashMap<>();
            portfolioIdRead.findObject(portfolioId, result);
            return result;
        }
    }

    /**
     * update note, returns number of changed rows
     * used with primary key or portfolioId and itemid (=instrument/quote)
     */
    private class UpdateNote {

        private final SqlUpdate idUpdate;
        private final SqlUpdate portfolioUpdate;
        private final SqlUpdate itemIdUpdate;

        public UpdateNote() {
            idUpdate = new SqlUpdate(getDataSource(),
                    "UPDATE notes set content=? WHERE id=?");
            idUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
            idUpdate.declareParameter(new SqlParameter(Types.INTEGER));
            idUpdate.compile();

            portfolioUpdate = new SqlUpdate(getDataSource(),
                    "UPDATE notes set content=? WHERE portfolioId=? AND itemid=?");
            portfolioUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
            portfolioUpdate.declareParameter(new SqlParameter(Types.INTEGER));
            portfolioUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
            portfolioUpdate.compile();

            itemIdUpdate = new SqlUpdate(getDataSource(),
                    "UPDATE notes set itemid=? WHERE portfolioId=? AND itemid=?");
            itemIdUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
            itemIdUpdate.declareParameter(new SqlParameter(Types.INTEGER));
            itemIdUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
            itemIdUpdate.compile();
        }

        public int update(long id, String content) {
            return idUpdate.update(content, id);
        }

        public int update(long portfolioId, String itemid, String content) {
            return portfolioUpdate.update(content, portfolioId, itemid);
        }

        public int updateItemId(long portfolioid, String oldItemId, String newItemId) {
            return itemIdUpdate.update(newItemId, portfolioid, oldItemId);
        }
    }

    /**
     * delete note, returns number of deleted rows
     * used with primary key or portfolioId and itemid (=instrument/quote)
     */
    private class DeleteNote {

        private final SqlUpdate idDelete;
        private final SqlUpdate portfolioDelete;

        public DeleteNote() {
            idDelete = new SqlUpdate(getDataSource(), "DELETE from notes WHERE id=?");
            idDelete.declareParameter(new SqlParameter(Types.INTEGER));
            idDelete.compile();

            portfolioDelete = new SqlUpdate(getDataSource(), "DELETE from notes WHERE portfolioId=? AND itemid=?");
            portfolioDelete.declareParameter(new SqlParameter(Types.INTEGER));
            portfolioDelete.declareParameter(new SqlParameter(Types.VARCHAR));
            portfolioDelete.compile();
        }

        public int delete(long id) {
            return idDelete.update(id);
        }

        public int delete(long portfolioId, String itemid) {
            return portfolioDelete.update(portfolioId, itemid);
        }
    }



    @Override
    public long createNote(long portfolioId, String itemid, String content) {
        return createNote.create(portfolioId, itemid, content);
    }

    @Override
    public String readNote(long id) {
        return readNote.read(id);
    }

    @Override
    public String readNote(long portfolioId, String itemid) {
        return readNote.read(portfolioId, itemid);
    }

    @Override
    public Map<String, String> readPortfolioNotes(long portfolioId) {
        return readNote.readPortfolioNotes(portfolioId);
    }

    @Override
    public void updateNote(long id, String content) {
        updateNote.update(id, content);
    }

    @Override
    public void updateNote(long portfolioId, String itemid, String content) {
        updateNote.update(portfolioId, itemid, content);
    }

    @Override
    public void deleteNote(long id) {
        deleteNote.delete(id);
    }

    @Override
    public void deleteNote(long portfolioId, String itemid) {
        deleteNote.delete(portfolioId, itemid);
    }

    @Override
    public void updateItemId(long portfolioid, String oldItemId, String symbol) {
        updateNote.updateItemId(portfolioid, oldItemId, symbol);
    }

}
