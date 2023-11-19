/*
 * IconImageMapping.java
 *
 * Created on 16.03.15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author umaurer
 */
@NonNLS
public class IconImageMapping extends de.marketmaker.itools.gwtutil.client.widgets.IconImageMapping {
    private static final String GENERATION = SessionData.isAsDesign() ? "cg" : "mm";
    private static final int TOOLBAR_ICON_SIZE = SessionData.isAsDesign() ? 24 : 16;

    public IconImageMapping() {
        super(SessionData.isAsDesign() ? "as/" : "");
    }

    @Override
    protected void initializeMapping() {
        add("empty-24", "images/mm/empty.png", 24, 24);
        add("mm-home-icon", "images/mm/toolbar/home.png", 0, 0, 16, 16);
        add("page-to-home-icon", "images/mm/toolbar/to-home.png", 0, 0, 18, 16);
        add("mm-reload-icon", "images/mm/reload/16/reload.png", 0, 0, 16, 16);
        add("mm-reload-icon reload-active", "images/mm/reload/16/reload-active.gif", 16, 16).withAnimated(true);
        add("mm-reload-icon-small", "images/mm/reload/13/reload.png", 0, 0, 13, 13);
        add("mm-set-icon", "images/mm/toolbar/set.png", 0, 0, 16, 16);
        add("mm-reset-icon", "images/mm/toolbar/reset.png", 0, 0, 16, 16);
        add("mm-logout-icon", "images/mm/toolbar/logout.png", 0, 0, 16, 16);
        add("mm-print-icon", "images/" + GENERATION + "/toolbar/print.png", 0, 0, TOOLBAR_ICON_SIZE, TOOLBAR_ICON_SIZE);
        add("mm-print-icon disabled", "images/" + GENERATION + "/toolbar/print-disabled.png", 0, 0, TOOLBAR_ICON_SIZE, TOOLBAR_ICON_SIZE);
        add("space-home-icon", "images/mm/toolbar/spaces/home.png", 0, 0, 10, 16);
        add("space-to-home-icon", "images/mm/toolbar/spaces/to-home.png", 0, 0, 18, 16);
        add("space-to-home-icon disabled", "images/mm/toolbar/spaces/to-home-gray.png", 0, 0, 18, 16);
        add("space-new-icon", "images/mm/toolbar/spaces/new.png", 0, 0, 18, 16);
        add("space-new-icon disabled", "images/mm/toolbar/spaces/new-gray.png", 0, 0, 18, 16);
        add("space-edit-icon", "images/mm/toolbar/spaces/edit.png", 0, 0, 18, 16);
        add("space-edit-icon disabled", "images/mm/toolbar/spaces/edit-gray.png", 0, 0, 18, 16);
        add("space-delete-icon", "images/mm/toolbar/spaces/delete.png", 0, 0, 18, 16);
        add("space-delete-icon disabled", "images/mm/toolbar/spaces/delete-gray.png", 0, 0, 18, 16);
        add("space-add-snippet-icon", "images/mm/toolbar/spaces/add-snippet.png", 0, 0, 18, 16);
        add("space-add-snippet-icon disabled", "images/mm/toolbar/spaces/add-snippet-gray.png", 0, 0, 18, 16);
        add("select-from-history", "images/mm/toolbar/select-from-history.png", 0, 0, 16, 16);

        add("warning", "images/mm/elements/warning.png", 0, 0, 14, 14);
        add("mm-element-ok", "images/mm/elements/ok.png", 0, 0, 12, 13);
        add("mm-element-cancel", "images/mm/elements/cancel.png", 0, 0, 12, 13);

        add("mm-bookmark", "images/" + GENERATION + "/toolbar/bookmark.png", 16, 16).withSimpleImg(true);
        add("mm-bookmark disabled", "images/mm/toolbar/bookmark-gray.png", 16, 16).withSimpleImg(true);

        add("mm-icon-dzbank", "images/mm/mm-icon-dzbank.png", 0, 0, 16, 16);
        add("mm-icon-dzbank-pib", "images/zones/web/dzbank-pib.png", 0, 0, 19, 12);
        add("mm-icon-dzbank-rs", "images/zones/web/dzbank-rs.png", 0, 0, 19, 12);
        add("mm-icon-dzbank-page", "images/cg/toolbar/dzbank-page.png", 0, 0, 16, 16);
        add("mm-icon-dzbank-page-quicksearch", "images/cg/toolbar/dzbank-page-quicksearch.png", 0, 0, 16, 16);
        add("mm-icon-raiba", "images/mm/mm-icon-raiba.png", 16, 16).withSimpleImg(true);
        add("mm-icon-vwdpage", "images/" + GENERATION + "/toolbar/vwd-page.png", 0, 0, 16, 16);
        add("mm-icon-vwdpage-quicksearch", "images/" + GENERATION + "/toolbar/vwd-page-quicksearch.png", 0, 0, 16, 16);
        add("mm-icon-comparison-add", "images/mm/toolbar/comparison-all.png", 0, 0, 16, 16);
        add("mm-icon-comparison-delete", "images/mm/toolbar/comparison-all.png", 16, 0, 16, 16);
        add("links-to-vwdpage", "images/mm/toolbar/links-to-vwd-page.png", 0, 0, 16, 16);
        add("mm-icon-finder", "images/" + GENERATION + "/toolbar/top-tb-search-button.png", 0, 0, 15, 15);

        add("x-tbar-pin", "images/" + GENERATION + "/toolbar/news-refresh-inactive.gif", 0, 0, 16, 16).withSimpleImg(true);
        add("x-tbar-unpin", "images/" + GENERATION + "/toolbar/news-refresh-active.gif", 0, 0, 16, 16).withSimpleImg(true);
        add("x-tbar-search", "images/" + GENERATION + "/toolbar/top-tb-search-button.png", 0, 0, 15, 15);

        add("mm-newsdetail-email", "images/mm/toolbar/send-email.png", 0, 0, 16, 16);

        sprite("images/" + GENERATION + "/snippet/snippet-all.png")
                .size(9).icons("mm-menu-diff-negative", "mm-menu-diff-equal", "mm-menu-diff-positive", "mm-contextTrigger-link-9", "mm-contextTrigger-link-9 hover")
                .size(7).xBase(45).yBase(0).icons("mm-minus-7", "mm-plus-7").xBase(0).yBase(9)
                .size(16).colCount(5).icons(
                "mm-diff-16-negative", "mm-diff-16-equal", "mm-diff-16-positive", "mm-contextTrigger-16", "mm-contextTrigger-16 hover",
                "mm-quality-end-of-day", "mm-quality-neartime-20", "mm-quality-neartime-15", "mm-quality-realtime", null,
                "mm-quality-end-of-day-push", "mm-quality-neartime-20-push", "mm-quality-neartime-15-push", "mm-quality-realtime-push", null
        )
                .size(22, 11).xBase(0).iconsKeepY("analysis-strongsell")
                .size(11).xBase(11).iconsKeepY("analysis-sell", "analysis-hold", "analysis-buy")
                .size(22, 11).xBase(33).icons("analysis-strongbuy").xBase(0)
                .size(14, 16).colCount(4).icons(
                "mm-fileType-pdf", "mm-fileType-pdf disabled", "mm-fileType-html", "mm-fileType-chart-image",
                "mm-fileType-txt", "mm-fileType-csv", "mm-fileType-table", null
        )
                .size(16).colCount(4).icons("x-tbar-pdf-with-options");

        addSynonyms("mm-fileType-txt", "mm-fileType-doc");
        addSynonyms("mm-fileType-html", "mm-fileType-unknown");
        addSynonyms("mm-fileType-table", "mm-fileType-xls", "csv-button");
        addSynonyms("mm-fileType-pdf", "x-tbar-pdf", "mm-icon-pdf");
        addSynonyms("mm-fileType-pdf disabled", "x-tbar-pdf disabled");

        sprite("images/mm/tree/tree.png").size(14).icons(
                "mm-tree-folder-closed",
                "mm-tree-folder-open",
                "mm-tree-instrument"
        );

        /*sprite("images/cg/panel/handle.png").size(8, 48).icons(
                "ice-handle-left",
                "ice-handle-right"
        );*/

        add("52w-span", "images/mm/portrait/52w-span.png", 0, 0, 100, 9);
        add("52w-span-180", "images/" + GENERATION + "/portrait/52w-span-180.png", 0, 0, 180, 9);
        add("sell-hold-buy-coeff", "images/mm/portrait/sell-hold-buy-coeff.png", 0, 0, 250, 9);
        add("slider-slider", "images/" + GENERATION + "/portrait/slider-slider.png", 0, 0, 4, 12);

        add("negative-pixel", "images/mm/negative-pixel.png", 0, 0, 1, 1);
        add("neutral-pixel", "images/mm/neutral-pixel.png", 0, 0, 1, 1);
        add("positive-pixel", "images/mm/positive-pixel.png", 0, 0, 1, 1);
        add("trendbar-zero-pixel", "images/mm/trendbar-zero-pixel.png", 0, 0, 1, 1);


        sprite("images/mm/portrait/edg/edg-all.png")
                .size(59, 11).colCount(1).icons(
                "edg-0",
                "edg-1",
                "edg-2",
                "edg-3",
                "edg-4",
                "edg-5",
                "edg-gold-0",
                "edg-gold-1",
                "edg-gold-2",
                "edg-gold-3",
                "edg-gold-4",
                "edg-gold-5"
        )
                .size(84, 28).icons("tiny-edg-badge");

        add("mm-richtext-bold", "images/mm/richtext/all.png", 0, 0, 16, 16);
        add("mm-richtext-italic", "images/mm/richtext/all.png", 16, 0, 16, 16);
        add("mm-richtext-underline", "images/mm/richtext/all.png", 32, 0, 16, 16);
        add("mm-richtext-strikethrough", "images/mm/richtext/all.png", 48, 0, 16, 16);
        add("mm-richtext-textcolor", "images/mm/richtext/all.png", 64, 0, 16, 16);
        add("mm-richtext-unordered", "images/mm/richtext/all.png", 80, 0, 16, 16);
        add("mm-richtext-ordered", "images/mm/richtext/all.png", 96, 0, 16, 16);
        add("mm-richtext-left", "images/mm/richtext/all.png", 112, 0, 16, 16);
        add("mm-richtext-center", "images/mm/richtext/all.png", 128, 0, 16, 16);
        add("mm-richtext-right", "images/mm/richtext/all.png", 144, 0, 16, 16);

        add("to-be-designed-24", "images/cg/toolbar/to-be-designed-24.png", 24, 24);
        add("to-be-designed-24 disabled", "images/cg/toolbar/to-be-designed-24-disabled.png", 24, 24);

        sprite("images/cg/pm/pm-all.png")
                .size(16).colCount(20).icons(
                "pm-investor-prospect",
                "pm-investor-person",
                "pm-investor",
                "pm-investor-group",
                "pm-investor-portfolio",
                "pm-investor-depot",
                "pm-investor-account",
                "pm-filter",
                "pm-subfilter",
                "pm-folder",
                "pm-instrument",
                "pm-chart",
                "pm-activity",
                "pm-box-checked",
                "pm-research-list",
                "pm-investor-new",
                "pm-investor-delete",
                "pm-investor-group-add",
                "pm-investor-group-delete",
                "pm-alert",

                "pm-blue-investor-prospect",
                "pm-blue-investor-person",
                "pm-blue-investor",
                "pm-blue-investor-group",
                "pm-blue-investor-portfolio",
                "pm-blue-investor-depot",
                "pm-blue-investor-account",
                "pm-blue-filter",
                "pm-blue-subfilter",
                "pm-blue-folder",
                "pm-blue-instrument",
                "pm-blue-chart",
                "pm-blue-activity",
                "pm-datastatus-correct",
                "pm-datastatus-incorrect",
                "pm-blue-investor-new",
                "pm-blue-investor-delete",
                "pm-blue-investor-group-add",
                "pm-blue-investor-group-delete",
                "pm-blue-alert"
        )
                .size(32).colCount(10).icons(
                "pm-investor-prospect-32",
                "pm-investor-person-32",
                "pm-investor-32",
                "pm-investor-group-32",
                "pm-investor-portfolio-32",
                "pm-investor-depot-32",
                "pm-investor-account-32",
                "pm-folder-32",
                "pm-instrument-32",
                "pm-activity-32"
        );

        addSynonyms("pm-investor-portfolio", "PmIcon:Portfolio16");

        add("pm-investor-large", "images/mm/pmweb/investor-tree-large.png", 0, 0, 28, 32);
        add("pm-investor-delete-large", "images/mm/pmweb/investor-tree-large.png", 28, 0, 28, 32);
        add("pm-investor-group-large", "images/mm/pmweb/investor-tree-large.png", 56, 0, 28, 32);
        add("pm-investor-group-add-large", "images/mm/pmweb/investor-tree-large.png", 84, 0, 28, 32);
        add("pm-investor-group-delete-large", "images/mm/pmweb/investor-tree-large.png", 112, 0, 28, 32);
        add("pm-investor-portfolio-large", "images/mm/pmweb/investor-tree-large.png", 140, 0, 28, 32);
        add("pm-investor-depot-large", "images/mm/pmweb/investor-tree-large.png", 168, 0, 28, 32);

        sprite("images/cg/pm/portfolio-version.png").size(24).icons(
                "pm-portfolio-version-add", "pm-portfolio-version-add hover",
                "pm-portfolio-version-delete", "pm-portfolio-version-delete hover",
                "pm-portfolio-version-clone", "pm-portfolio-version-clone hover",
                "pm-portfolio-version-edit", "pm-portfolio-version-edit hover",
                "pm-portfolio-edit", "pm-portfolio-edit hover");

        add("as-searchDest-dmxml", "images/mm/pmweb/investor-tree.png", 0, 0, 14, 16);
        add("as-searchDest-pminvestor", "images/mm/pmweb/investor-tree.png", 42, 0, 14, 16);
        add("as-searchDest-pminstrument", "images/mm/pmweb/investor-tree.png", 84, 0, 14, 16);

        add("as-mmweb", "images/as/mmweb.png", 0, 0, 232, 152);
        add("as-pmweb", "images/as/pmweb.png", 0, 0, 229, 152);
        add("as-sps", "images/as/sps.png", 0, 0, 201, 141);

        add("as-west-flyer", "images/mm/toolbar/west/flyer.png", 0, 0, 32, 32);
        add("as-west-megaphon", "images/mm/toolbar/west/megaphon.png", 0, 0, 32, 32);

        add("as-south-developer", "images/mm/toolbar/south/developer-tools-16.png", 0, 0, 16, 16);

        add("celltree-closed-item", "images/mm/tree/celltree-open-closed.png", 15, 0, 15, 15);
        add("celltree-open-item", "images/mm/tree/celltree-open-closed.png", 0, 0, 15, 15);
        add("celltree-loading", "images/mm/tree/celltree-loading.gif", 0, 0, 15, 15).withAnimated(true);
        add("celltree-selected-background", "images/mm/tree/celltree-selected-background.png", 0, 0, 2, 26);
        add("external-link", "images/mm/external-link.png", 0, 0, 15, 15);

        add("as-toptoolbar-sep", "images/cg/toolbar/toptoolbar-separator.png", 0, 0, 1, 48);

        sprite("images/cg/as-icons.png")
                .size(48).colCount(4).icons(
                "cg-customers", "cg-customer", "cg-market", "cg-news",
                "cg-tools", "cg-searchresult", "cg-settings", "cg-help",
                "cg-dashboard")
                .size(24).xBase(48).addYBase(-48).colCount(6).icons(
                "as-reload-24",
                "as-export-24",
                "as-export-24-with-options",
                "as-export-24 disabled",
                "as-print-24",
                "as-print-24 disabled",

                "as-privacymode-start",
                "as-privacymode-start disabled",
                "as-privacymode-stop",
                "ice-push",
                "ice-bookmark-24",
                "ice-alert-24").xBase(0)
                .size(24).colCount(8).iconsKeepY(
                "as-plus-24",
                "as-minus-24",
                "as-logout",
                "thread-menu")
                .size(12).xBase(96).colCount(8).icons("as-edit-12", "as-ok-12")
                .icons("as-plus-12", "as-minus-12").xBase(0)
                .size(24).colCount(3).xBase(120).addYBase(-24).icons(
                "ice-define-home-24"
                )
                .xBase(0).size(16).colCount(12).icons(
                "pm-icon-16",
                "mm-icon-16",
                "list-icon",
                "list-up-16",
                "list-down-16",
                "notification-close",
                "notification-close hover",
                "notifications-hide",
                "as-tool-download",
                "as-tool-export",
                "as-saveToArchive",
                "as-loadFromArchive",

                "as-tool-resize-max",
                "as-tool-resize-min",
                "as-tool-table-aggregation",
                "as-tool-dms",
                "as-tool-print",
                "as-tool-calendar",
                "as-tool-sort",
                "as-tool-settings",
                "x-tool-btn-plus",
                "x-tool-btn-minus",
                "x-tool-btn-edit",
                "x-tool-btn-reset",

                "as-tool-close",
                "x-tool-cancel",
                "x-tool-copy",
                "as-reload-16",
                "as-tool-cellSpan",
                "as-export-with-options",
                "current-dot",
                "mm-news-icon-16",
                "as-tool-send-email"
        );
        addSynonyms("as-export-24", "as-export-pdf-24");
        addSynonyms("as-export-24-with-options", "as-export-pdf-24-with-options");
        addSynonyms("as-export-24 disabled", "as-export-pdf-24 disabled");
        addSynonyms("as-tool-export", "as-tool-export-pdf", "as-tool-export-xls");
        addSynonyms("as-tool-calendar", "sps-calendar");
        addSynonyms("current-dot", "thread-current-dot");

        add("logo-dzbank-48", "images/zones/web/logo-dzbank-48.png" , 0, 0, 48, 48);
        add("gisportal-48", "images/zones/web/gisportal-48.png" , 0, 0, 48, 48);

        add("module-kwt", "images/zones/kwt/module-icon.png", 0, 0, 48, 48);

        add("module-olb", "images/zones/olb/module-icon-48.png" , 0, 0, 48, 48);

        add("as-reload-24 active", "images/cg/reload/24/reload-active.gif", 24, 24).withAnimated(true);
        add("as-reload-48 active", "images/cg/reload/48/reload-active.gif", 48, 48).withAnimated(true);
        if (SessionData.isAsDesign()) {
            add("mm-snippet-reload-16", "images/cg/reload/16/reload.png", 16, 16);
            add("mm-snippet-reload-16-active", "images/cg/reload/16/reload-active.gif", 16, 16).withAnimated(true);
        }
        else {
            add("mm-snippet-reload-16", "images/mm/snippet/reloadSnakeStatic.gif", 16, 16);
            add("mm-snippet-reload-16-active", "images/mm/snippet/reloadSnake.gif", 16, 16).withAnimated(true);
        }
        addSynonyms("mm-snippet-reload-16", "as-reload");
        addSynonyms("mm-snippet-reload-16-active", "as-reload active");

        add("list-up-icon", "images/cg/toolbar/toolbar-all-16.png", 48, 4, 16, 12);
        add("list-down-icon", "images/cg/toolbar/toolbar-all-16.png", 64, 0, 16, 12);
        sprite("images/" + GENERATION + "/dialog/dialog-all.png").size(48).icons(
                "dialog-error",
                "dialog-info",
                "dialog-question",
                "dialog-warning"
        );
        sprite("images/" + GENERATION + "/dialog/dialog-16-all.png").size(16).icons(
                "dialog-error-16",
                "dialog-info-16",
                "dialog-question-16",
                "dialog-warning-16",
                "dialog-forbidden-16",
                "dlg-close"
        );
        add("search-arrow-up", "images/cg/search/arrow-up.png", 0, 0, 30, 24);

        sprite("images/cg/sps/task-icons.png")
                .size(24).colCount(4).icons(
                "sps-section-add", "sps-section-add hover", "sps-section-remove", "sps-section-remove hover",
                "sps-task-back", "sps-task-proceed", "sps-task-commit", null,
                "sps-south-unpinned", "sps-south-pin hover", null, "sps-south-pinned",
                "PmIcon:TaskCommit", "PmIcon:Activity", "sps-section-remove-all", "sps-section-remove-all hover",
                "PmIcon:CheckNotOk", "PmIcon:CheckOk", "PmIcon:CheckUndetermined", "PmIcon:CheckOverruled"
        )
                .size(16).colCount(6).icons(
                "sps-task-error", "sps-task-inactive", "sps-task-active", "sps-task-incomplete", "sps-task-finished", null,
                "sps-field-mandatory", "sps-field-mandatory highlight", "pmSeverity-esvError", "pmSeverity-esvWarning2", "pmSeverity-esvWarning", "pmSeverity-esvHint",
                "sps-plus", "sps-plus hover", "sps-minus", "sps-minus hover"
        );
        addSynonyms("PmIcon:CheckUndetermined", "PmIcon:NoData");
        addSynonyms("pmSeverity-esvError", "mm-infoIcon-error");
        addSynonyms("pmSeverity-esvWarning", "mm-infoIcon-typingError");

        sprite("images/cg/tree/tree-all.png").size(16).colCount(2).icons(
                "expander-right", "expander-down",
                "expander-light-right", "expander-light-down"
        );

        add("sps-activity-instance-delete", "images/cg/sps/trash-15.png", 15, 15);
        add("sps-help", "images/cg/sps/help.png", 16, 16);
        add("sps-attachment-add", "images/cg/sps/attachment-add.png", 16, 16);

        final Sprite mmIcons = sprite("images/" + GENERATION + "/mm-icons.png").size(16).icons(
                "mm-save-icon", "mm-finder-btn-delete",
                "mm-list-move-top", "mm-list-move-up", "mm-list-move-down", "mm-list-move-bottom", "mm-list-move-right", "mm-list-move-left",
                "mm-plus", "mm-minus", "column-config-icon", "mm-watchlist"
        ).icons("x-tbar-page-first", "x-tbar-page-prev", "x-tbar-page-next", "x-tbar-page-last",
                "x-tbar-page-first disabled", "x-tbar-page-prev disabled", "x-tbar-page-next disabled", "x-tbar-page-last disabled",
                "portfolio-comment-expand", "portfolio-comment-collapse", "new-list-entry", "mm-portfolio"
        ).icons("jump-to-url", "mm-limits-icon", "mm-icon-comparison"
        ).size(12).iconsKeepY("jump-to-url-12");
        addSynonyms("mm-list-move-down", "mm-arrow-down");
        addSynonyms("mm-list-move-right", "mm-arrow-right");
        addSynonyms("mm-list-move-left", "mm-arrow-left");
        addRatings(mmIcons, 12, 7, 12, 12, "rating-7stars-");
        addRatings(mmIcons, 36, 5, 12, 12, "rating-5stars-").incYBase();
        addRatings(mmIcons, 0, 5, 12, 12, "rating-5diamonds-").incYBase();
        addRatings(mmIcons, 0, 5, 12, 12, "rating-5crowns-").incYBase();
        addRatingSynonyms("rating-7stars-", "rating-srri-", 7);
        addRatingSynonyms("rating-5stars-", "rating-morningstar-", 5);
        addRatingSynonyms("rating-5diamonds-", "rating-vwd-", 5);
        addRatingSynonyms("rating-5crowns-", "rating-fida-", 5);
        add("mm-save-icon pending", "images/mm/finder/save-pending.gif", 16, 16).withAnimated(true);

        if (SessionData.isAsDesign()) {
            addSynonyms("mm-plus", "watchlist-new-icon", "portfolio-new-icon");
            addSynonyms("x-tool-btn-edit", "watchlist-edit-icon", "portfolio-edit-icon");
            addSynonyms("mm-minus", "watchlist-delete-icon", "portfolio-delete-icon");
            addSynonyms("new-list-entry", "watchlist-add-icon", "position-add-icon", "order-add-icon");

            addSynonyms("as-edit-12", "mm-small-edit");
            addSynonyms("as-ok-12", "mm-small-ok");
            addSynonyms("as-plus-12", "mm-small-add");
            addSynonyms("as-minus-12", "mm-small-remove");
            addSynonyms("jump-to-url", "mm-compactQuoteJumpPortrait");
            addSynonyms("as-tool-close", "x-tool-close");
            addSynonyms("as-tool-settings", "x-tool-gear");

            sprite("images/" + GENERATION + "/panel/tool-sprites.png")
                    .size(15)
                    .yBase(165).icons("ice-handle-right")
                    .yBase(180).icons("ice-handle-left");
        }
        else {
            add("mm-limits-icon limit-active", "images/mm/toolbar/limits-active.png", 0, 0, 16, 16);
            add("push-active", "images/mm/toolbar/push-active.png", 0, 0, 16, 16);
            add("push-inactive", "images/mm/toolbar/push-inactive.png", 0, 0, 16, 16);
            add("watchlist-new-icon", "images/mm/toolbar/watchlist/new.png", 0, 0, 18, 16);
            add("watchlist-new-icon disabled", "images/mm/toolbar/watchlist/new-gray.png", 0, 0, 18, 16);
            add("watchlist-edit-icon", "images/mm/toolbar/watchlist/edit.png", 0, 0, 18, 16);
            add("watchlist-edit-icon disabled", "images/mm/toolbar/watchlist/edit-gray.png", 0, 0, 18, 16);
            add("watchlist-delete-icon", "images/mm/toolbar/watchlist/delete.png", 0, 0, 18, 16);
            add("watchlist-delete-icon disabled", "images/mm/toolbar/watchlist/delete-gray.png", 0, 0, 18, 16);
            add("watchlist-viewMode-table-icon", "images/mm/toolbar/watchlist/viewmode-table.png", 0, 0, 18, 16);
            add("watchlist-viewMode-gallery-icon", "images/mm/toolbar/watchlist/viewmode-gallery.png", 0, 0, 18, 16);
            add("position-add-icon", "images/mm/toolbar/watchlist/add-position.png", 0, 0, 18, 16);
            add("position-add-icon disabled", "images/mm/toolbar/watchlist/add-position-gray.png", 0, 0, 18, 16);
            add("mm-goto-watchlist", "images/mm/toolbar/watchlist/goto-watchlist.png", 0, 0, 18, 16);

            sprite("images/mm/toolbar/portfolio/all-portfolio.png").size(18, 16).colCount(7).icons(
                    null, "portfolio-new-icon", "portfolio-new-icon disabled", "portfolio-edit-icon", "portfolio-edit-icon disabled", "portfolio-delete-icon", "portfolio-delete-icon disabled",
                    "order-add-icon", "order-add-icon disabled", "mm-goto-portfolio", "portfolio-viewMode-table-icon", "portfolio-viewMode-gallery-icon", "portfolio-viewMode-orders-icon", null
            );

            add("mm-small-edit", "images/mm/elements/pencil.png", 0, 0, 13, 12);
            addSynonyms("mm-element-ok", "mm-small-ok");
            addSynonyms("mm-plus-7", "mm-small-add");
            addSynonyms("mm-minus-7", "mm-small-remove");
            add("mm-compactQuoteJumpPortrait", "images/mm/tree/tree.png", 28, 0, 14, 14);
            sprite("images/mm/panel/tool-sprites.png")
                    .size(15).icons("x-tool-close", "x-tool-close hover")
                    .yBase(90).icons("x-tool-gear", "x-tool-gear hover");
        }

        add("mm-icon-apo", "images/mm/mm-icon-apo.gif", 16, 16);
    }

    private void addRatingSynonyms(String namePrefix, String synonymPrefix, int count) {
        for (int i = 1; i <= count; i++) {
            addSynonyms(namePrefix + i, synonymPrefix + i);
        }
    }

    private Sprite addRatings(Sprite sprite, int xBase, int count, int width, int height, String prefix) {
        for (int i = 0; i < count; i++) {
            sprite.size(width, height).xBase(xBase).iconsKeepY(prefix + (i + 1));
            xBase += width;
        }
        return sprite;
    }

}
