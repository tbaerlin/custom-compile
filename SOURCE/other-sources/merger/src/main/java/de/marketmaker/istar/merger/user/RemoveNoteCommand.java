package de.marketmaker.istar.merger.user;


public class RemoveNoteCommand {

    private Long userId;

    private Long listId;  // portfolio or watchlist Id

    private String itemId; // qid or iid


    public RemoveNoteCommand(Long userId, Long listId, String itemId) {
        this.userId = userId;
        this.listId = listId;
        this.itemId = itemId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String toString() {
        return "RemoveNoteCommand["
                + this.userId + "; " + this.listId + "; " + this.itemId + "]";
    }

}
