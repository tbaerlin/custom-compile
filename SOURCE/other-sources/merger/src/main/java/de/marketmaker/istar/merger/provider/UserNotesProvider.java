package de.marketmaker.istar.merger.provider;


import de.marketmaker.istar.merger.user.AddNoteCommand;
import de.marketmaker.istar.merger.user.RemoveNoteCommand;
import de.marketmaker.istar.merger.user.UpdateNoteCommand;

import java.util.Map;

public interface UserNotesProvider {

    void createNote(AddNoteCommand cmd);

    void updateNote(UpdateNoteCommand cmd);

    void deleteNote(RemoveNoteCommand cmd);

    String readNote(long listId, String itemId);

}
