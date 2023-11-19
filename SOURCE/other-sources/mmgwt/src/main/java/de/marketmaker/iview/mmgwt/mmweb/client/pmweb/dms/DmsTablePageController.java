package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms;

import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.pmxml.internaltypes.DMSSearchResult;

/**
 * Created on 21.04.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DmsTablePageController extends AbstractPageController implements PageLoader {

    private final DmsPresenter presenter;
    private final DmsTableView view;
    private final PagingFeature pagingFeature;
    private final DmsDisplay.Presenter.Config config;
    private final DmsDisplay.Presenter.Config defaultConfig;

    public DmsTablePageController() {
        final MmJsDate to = new MmJsDate();
        final MmJsDate from = new MmJsDate(to.getFullYear() - 5, to.getMonth(), to.getDay()); //to minus 5 years
        this.defaultConfig = new DmsDisplay.Presenter.Config(from, to);
        this.config = new DmsDisplay.Presenter.Config(this.defaultConfig);

        this.presenter = new DmsPresenter(this.context);
        this.pagingFeature = new PagingFeature(this, this.presenter.searchBlock, 20);
        this.view = new DmsTableView(this, this.presenter);
        this.presenter.setDisplay(this.view);
        this.presenter.addSearchResultListener(new DmsDisplay.Presenter.SearchResultListener() {
            @Override
            public void onSearchResult(DMSSearchResult result) {
                pagingFeature.onResult();
            }
        });
        this.view.setSortLinkListener(new SortLinkSupport(this.presenter.searchBlock, new Command() {
            public void execute() {
                reload();
            }
        }, true));
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final String objectid = event.getHistoryToken().get(AbstractDepotObjectPortraitController.OBJECTID_KEY);
        this.presenter.requestDmsMetadata(objectid, new DmsDisplay.DmsMetadataCallback() {
            @Override
            public void metadataAvailable(DmsMetadata metadata) {
                presenter.update(metadata, null, defaultConfig.dateFrom, defaultConfig.dateTo);
                MainController.INSTANCE.getView().setContent(view);
                pagingFeature.resetPaging();
            }
        });
    }

    public PagingFeature getPagingFeature() {
        return this.pagingFeature;
    }

    public DmsDisplay.Presenter.Config getConfig() {
        return this.config;
    }

    public DmsDisplay.Presenter.Config getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public void reload() {
        this.presenter.updateForPaging();
    }

    public void update() {
        presenter.update(this.config);
    }

    @Override
    public void destroy() {
        this.presenter.destroy();
    }
}