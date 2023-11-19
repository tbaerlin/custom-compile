package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCNoteEdit;
import de.marketmaker.iview.dmxml.PortfolioPositionElement;
import de.marketmaker.iview.dmxml.PositionUserNote;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.dmxml.WatchlistPositionElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

public class PositionNoteController implements AsyncCallback<ResponseType>,
        PositionNoteView.Presenter, LinkListener<Link> {

    private final DmxmlContext context = new DmxmlContext();

    private final DmxmlContext.Block<MSCNoteEdit> noteEditBlock = context.addBlock("MSC_NoteEdit");  //$NON-NLS$

    private final String listId;

    private final String itemId;

    private final String content;

    private final PageController callback;

    public PositionNoteController(String listId, String itemId, PageController callback) {
        this(listId, itemId, "", callback);
    }

    public PositionNoteController(PositionUserNote existingNote, PageController callback) {
        this(existingNote.getListId(), existingNote.getItemId(), existingNote.getContent(), callback);
    }

    private PositionNoteController(String listId, String itemId, String content,
            PageController callback) {
        this.listId = listId;
        this.itemId = itemId;
        this.content = content;
        this.callback = callback;
    }

    @Override
    public void onFailure(Throwable caught) {
        AbstractMainController.INSTANCE.showMessage(I18n.I.error());
    }

    @Override
    public void onSuccess(ResponseType result) {
        this.callback.refresh();
        AbstractMainController.INSTANCE.showMessage(I18n.I.ok());
    }

    @Override
    public void setContent(String content) {
        this.noteEditBlock.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); //$NON-NLS$
        this.noteEditBlock.setParameter("listId", this.listId);    //$NON-NLS$
        this.noteEditBlock.setParameter("itemId", this.itemId);   //$NON-NLS$
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendEscaped(content);
        this.noteEditBlock.setParameter("content", builder.toSafeHtml().asString());   //$NON-NLS$

        this.context.issueRequest(this);
    }

    public void showNote() {
        String noteText = new HTML(this.content).getText();
        PositionNoteView.INSTANCE.showDialog(noteText, this);
    }

    @Override
    public void onClick(LinkContext<Link> context, Element e) {
        showNote();
    }

    public abstract static class PositionNoteRenderer implements TableCellRenderer {

        public enum CommentViewMode {
            SHORT, FULL;

            public CommentViewMode toggle() {
                return (this == SHORT ? FULL : SHORT);
            }
        }

        protected final CommentViewMode commentViewMode;

        protected final PageController callback;

        protected final String listId;

        protected final TableCellRenderers.IconLinkWithTextRenderer linkWithIconRenderer;

        protected PositionNoteRenderer(PageController callback, String listId,
                CommentViewMode commentViewMode) {
            this.callback = callback;
            this.listId = listId;
            this.commentViewMode = commentViewMode;
            this.linkWithIconRenderer = new TableCellRenderers.IconLinkWithTextRenderer(100, "", commentViewMode == CommentViewMode.FULL);
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (data == null) {
                return;
            }

            PositionWrapper position = (PositionWrapper) data;
            List<PositionUserNote> notes = position.getPositionUserNote();

            if (hasMainNote(notes)) {
                PositionUserNote mainNote = getMainNote(notes);
                renderNote(sb, context, mainNote);
            }
            else {
                renderNewNoteBtn(sb, context, position);
            }

            if (this.commentViewMode == CommentViewMode.FULL) {
                for (PositionUserNote note : getSecondaryNotes(notes)) {
                    renderSeparator(sb);
                    renderNote(sb, context, note);
                }
            }
        }

        protected abstract String getMainNoteSuffix();

        private List<PositionUserNote> getSecondaryNotes(List<PositionUserNote> notes) {
            List<PositionUserNote> secondaryNotes = new ArrayList<>();

            for (PositionUserNote note : notes) {
                if (!note.getItemId().endsWith(getMainNoteSuffix())) {
                    secondaryNotes.add(note);
                }
            }

            return secondaryNotes;
        }

        private void renderSeparator(StringBuffer sb) {
            sb.append("<hr style=\"border-top: dashed 1px; margin:0;\"/>");  //$NON-NLS$
        }

        private void renderNote(StringBuffer sb, Context context,
                PositionUserNote note) {
            String noteExplanation = "";
            if (note.getItemId().endsWith(".qid") && note.getItemName() != null && !note.getItemName().isEmpty()) {  //$NON-NLS$
                noteExplanation = "<b>" + note.getItemName() + ":</b> "; //$NON-NLS$
            }

            String noteWithExplanation = noteExplanation + note.getContent();

            PositionNoteController noteController = new PositionNoteController(note, callback);
            Link link = new Link(noteController, noteWithExplanation, noteWithExplanation)
                    .withStyle("mm-floatRight")  //$NON-NLS$
                    .withIcon("mm-small-edit", I18n.I.editComment());  //$NON-NLS$

            renderLink(sb, context, link);
        }

        private void renderLink(StringBuffer sb, Context context, Link link) {
            sb.append("<div style=\"max-width: 300px; min-height: 13px;\">");  //$NON-NLS$
            this.linkWithIconRenderer.render(link, sb, context);
            sb.append("</div>");  //$NON-NLS$
        }

        @Override
        public boolean isPushRenderer() {
            return false;
        }

        @Override
        public String getContentClass() {
            return null;
        }

        public PositionUserNote getMainNote(List<PositionUserNote> notes) {
            if (notes == null) {
                return null;
            }

            for (PositionUserNote note : notes) {
                if (note.getItemId().endsWith(getMainNoteSuffix())) {
                    return note;
                }
            }

            return null;
        }

        public boolean hasMainNote(List<PositionUserNote> notes) {
            return getMainNote(notes) != null;
        }

        public void renderNewNoteBtn(StringBuffer sb, Context context,
                PositionWrapper position) {
            PositionNoteController noteController = new PositionNoteController(listId, getItemId(position), callback);

            AbstractImagePrototype icon = IconImage.get("mm-small-add");    //$NON-NLS$
            String iconTooltip = I18n.I.addComment();

            Link link = new Link(noteController, "", "")
                    .withStyle("mm-floatRight") //$NON-NLS$
                    .withIcon(icon, iconTooltip);

            renderLink(sb, context, link);
        }

        public abstract String getItemId(PositionWrapper position);
    }

    public static class InstrumentPositionNoteRenderer extends PositionNoteRenderer {

        protected InstrumentPositionNoteRenderer(PageController callback, String listId,
                CommentViewMode commentViewMode) {
            super(callback, listId, commentViewMode);
        }

        @Override
        protected String getMainNoteSuffix() {
            return ".iid";  //$NON-NLS$
        }

        @Override
        public String getItemId(PositionWrapper position) {
            return position.getInstrumentdata().getIid();
        }

    }

    public static class QuotePositionNoteRenderer extends PositionNoteRenderer {

        protected QuotePositionNoteRenderer(PageController callback, String listId,
                CommentViewMode commentViewMode) {
            super(callback, listId, commentViewMode);
        }

        @Override
        protected String getMainNoteSuffix() {
            return ".qid";  //$NON-NLS$
        }

        @Override
        public String getItemId(PositionWrapper position) {
            return position.getQuotedata().getQid();
        }
    }

    public interface PositionWrapper {
        InstrumentData getInstrumentdata();

        QuoteData getQuotedata();

        List<PositionUserNote> getPositionUserNote();
    }

    public static class WatchlistPositionWrapper implements PositionWrapper {

        private final WatchlistPositionElement position;

        public WatchlistPositionWrapper(WatchlistPositionElement position) {
            this.position = position;
        }

        @Override
        public InstrumentData getInstrumentdata() {
            return this.position.getInstrumentdata();
        }

        @Override
        public QuoteData getQuotedata() {
            return this.position.getQuotedata();
        }

        @Override
        public List<PositionUserNote> getPositionUserNote() {
            List<PositionUserNote> notes = new java.util.ArrayList<>();
            if (this.position.getPositionUserNote() != null) {
                notes.add(this.position.getPositionUserNote());
            }

            return notes;
        }
    }

    public static class PortfolioPositionWrapper implements PositionWrapper {

        private final PortfolioPositionElement position;

        public PortfolioPositionWrapper(PortfolioPositionElement position) {
            this.position = position;
        }

        @Override
        public InstrumentData getInstrumentdata() {
            return position.getInstrumentdata();
        }

        @Override
        public QuoteData getQuotedata() {
            return position.getQuotedata();
        }

        @Override
        public List<PositionUserNote> getPositionUserNote() {
            return position.getPositionUserNote();
        }
    }
}
