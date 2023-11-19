package de.marketmaker.istar.merger.web.easytrade.block;


import de.marketmaker.istar.merger.provider.UserNotesProvider;
import de.marketmaker.istar.merger.user.AddNoteCommand;
import de.marketmaker.istar.merger.user.RemoveNoteCommand;
import de.marketmaker.istar.merger.user.UpdateNoteCommand;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class MscNoteEdit extends UserHandler {

    private UserNotesProvider userNotesProvider;

    public static final class Command extends UserCommandImpl {

        private long listId;
        private String itemId;
        private String content;

        public long getListId() {
            return listId;
        }

        public void setListId(long listId) {
            this.listId = listId;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public MscNoteEdit() {
        super(Command.class);
    }

    public void setUserNotesProvider(UserNotesProvider userNotesProvider) {
        this.userNotesProvider = userNotesProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;
        final Long localUserId = getLocalUserId(cmd);

        if (cmd.getContent() == null || cmd.getContent().trim().isEmpty()) {
            userNotesProvider.deleteNote(new RemoveNoteCommand(localUserId,
                    cmd.getListId(), cmd.getItemId()));
        } else {
            if (null == userNotesProvider.readNote(cmd.getListId(), cmd.getItemId())) {
                userNotesProvider.createNote(new AddNoteCommand(localUserId,
                        cmd.getListId(), cmd.getItemId(), cmd.getContent()));
            } else {
                userNotesProvider.updateNote(new UpdateNoteCommand(localUserId,
                        cmd.getListId(), cmd.getItemId(), cmd.getContent()));
            }
        }

        String content = userNotesProvider.readNote(cmd.getListId(), cmd.getItemId());
        final Map<String, Object> model = new HashMap<>();
        model.put("listId", cmd.getListId());
        model.put("itemId", cmd.getItemId());
        model.put("content", content);
        return new ModelAndView("mscnoteedit", model);
    }
}
