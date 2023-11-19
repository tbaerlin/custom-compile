package de.marketmaker.istar.merger.user;


import java.util.Map;

public interface UserNotesDao {

    Map<String, String> readPortfolioNotes(long listId);

    long createNote(long listId, String itemId, String content);

    String readNote(long id);

    String readNote(long listId, String itemId);

    void updateNote(long id, String content);

    void updateNote(long listId, String itemId, String content);

    void deleteNote(long id);

    void deleteNote(long listId, String itemId);

    void updateItemId(long portfolioid, String oldItemId, String newItemId);

}
