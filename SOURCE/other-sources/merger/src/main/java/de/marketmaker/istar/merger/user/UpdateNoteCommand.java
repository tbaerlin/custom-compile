package de.marketmaker.istar.merger.user;


public class UpdateNoteCommand {

    private Long userId;

    private Long listId;  // portfolio or watchlist Id

    private String itemId; // qid or iid

    private String content;


    public UpdateNoteCommand(Long userId, Long listId, String itemId, String content) {
        this.userId = userId;
        this.listId = listId;
        this.itemId = itemId;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return "UpdateNoteCommand["
                + this.userId + "; " + this.listId + "; " + this.itemId
                + "' => '" + this.content + "]";
    }

}
