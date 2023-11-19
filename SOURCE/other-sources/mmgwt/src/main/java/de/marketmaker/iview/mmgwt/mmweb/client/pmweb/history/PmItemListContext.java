package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContextProducer;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmPlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.AbstractOwner;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer.FolderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer.FolderLinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityNavPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;

import java.util.List;

/**
 * Created on 24.04.13 08:55
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class PmItemListContext<T extends ContextItem> extends ItemListContext<T> {

    protected PmItemListContext(ContextList<T> list, String contextName, String iconKey, boolean breadCrumb) {
        super(list, contextName, iconKey, breadCrumb);
    }

    public static class ShellMMInfoItemListContext extends PmItemListContext<ShellMMInfoItem> {
        public ShellMMInfoItemListContext(ContextList<ShellMMInfoItem> list, String contextName, String iconKey, boolean breadCrumb) {
            super(list, contextName, iconKey, breadCrumb);
        }

        @Override
        public void action() {
            doAction(withoutBreadCrumb());
        }

        public void doAction(ItemListContext<ShellMMInfoItem> context) {
            PmPlaceUtil.goTo(getValue().getSelected().getShellMMInfo(), context);
        }
    }

    public static class ActivityItemListContext extends ItemListContext<ActivityInfoItem> {
        public ActivityItemListContext(ContextList<ActivityInfoItem> list, String contextName, String iconKey, boolean breadCrumb) {
            super(list, contextName, iconKey, breadCrumb);
        }

        @Override
        public void action() {
            doAction(withoutBreadCrumb());
        }

        public void doAction(ItemListContext<ActivityInfoItem> context) {
            HistoryToken.builder(PmWebModule.HISTORY_TOKEN_ACTIVITY)
                    .with(ActivityNavPageController.PARAM_ACTIVITY_INSTANCE, getValue().getSelected().getId())
                    .fire(context);
        }
    }

    public static ShellMMInfoItemListContext createForShellMMInfo(String contextName, ShellMMInfo selected,
                                                                  List<ShellMMInfo> infos) {

        final ShellMMInfoItemList shellMMInfoItemList = new ShellMMInfoItemList(ShellMMInfoItem.asInfoItems(infos));
        shellMMInfoItemList.setSelected(selected.getId());

        return new ShellMMInfoItemListContext(shellMMInfoItemList, contextName, null, true);
    }

    public static ItemListContext<ShellMMInfoItem> createForShellMMInfo(final String shellMMControllerId,
                                                                        String contextName,
                                                                        final ShellMMInfo selected,
                                                                        List<ShellMMInfo> infos) {
        final ShellMMInfoItemList shellMMInfoItemList = new ShellMMInfoItemList(ShellMMInfoItem.asInfoItems(infos));
        shellMMInfoItemList.setSelected(selected.getId());

        return new ItemListContext<ShellMMInfoItem>(shellMMInfoItemList, contextName, null, true) {
            @Override
            public void action() {
                final String id = shellMMInfoItemList.getSelected().getId();
                PlaceUtil.goTo(shellMMControllerId + "/objectid=" + id, withoutBreadCrumb());  //$NON-NLS$
            }
        };
    }

    private static <T extends AbstractOwner> ItemListContext<T> createForOwner(final String contextName, final String iconKey, T selected, List<T> owner, final String historyToken) {
        final OwnerList<T> ownerList = new OwnerList<>(owner);
        ownerList.setSelected(selected);
        return new ItemListContext<T>(ownerList, contextName, iconKey, true) {
            @Override
            public void action() {
                PlaceUtil.goTo(historyToken + "/objectid=" + ownerList.getSelected().getId(), // $NON-NLS$
                        withoutBreadCrumb());
            }
        };
    }

    public static <T extends AbstractOwner> HistoryContextProducer createProducerForOwner(final String contextName,
                                                                                          final String iconKey,
                                                                                          final T selected,
                                                                                          final List<T> owner,
                                                                                          final String historyToken) {
        return new HistoryContextProducer() {
            @Override
            public HistoryContext produce() {
                return createForOwner(contextName, iconKey, selected, owner, historyToken);
            }
        };
    }


    private static ItemListContext<Portfolio> createForPortfolio(String contextName, final String iconKey, Portfolio selected, List<Portfolio> portfolios) {
        final PortfolioList portfolioList = new PortfolioList(portfolios);
        portfolioList.setSelected(selected);
        return new ItemListContext<Portfolio>(portfolioList, contextName, iconKey, true) {
            @Override
            public void action() {
                PlaceUtil.goTo(PmWebModule.HISTORY_TOKEN_PORTFOLIO + "/objectid=" + portfolioList.getSelected().getId(), // $NON-NLS$
                        withoutBreadCrumb());
            }
        };
    }

    public static HistoryContextProducer createProducerForPortfolio(final String contextName, final String iconKey,
                                                                    final Portfolio selected,
                                                                    final List<Portfolio> portfolios) {
        return new HistoryContextProducer() {
            @Override
            public HistoryContext produce() {
                return PmItemListContext.createForPortfolio(contextName, iconKey, selected, portfolios);
            }
        };
    }

    private static ItemListContext<Depot> createForDepot(String contextName, final String iconKey, Depot selected, List<Depot> depots) {
        final DepotList depotList = new DepotList(depots);
        depotList.setSelected(selected);
        return new ItemListContext<Depot>(depotList, contextName, iconKey, true) {
            @Override
            public void action() {
                PlaceUtil.goTo(PmWebModule.HISTORY_TOKEN_DEPOT + "/objectid=" + depotList.getSelected().getId(), // $NON-NLS$
                        withoutBreadCrumb());
            }
        };
    }

    public static HistoryContextProducer createProducerForDepot(final String contextName, final String iconKey,
                                                                final Depot selected, final List<Depot> depots) {
        return new HistoryContextProducer() {
            @Override
            public HistoryContext produce() {
                return PmItemListContext.createForDepot(contextName, iconKey, selected, depots);
            }
        };
    }

    private static ItemListContext<Account> createForAccount(String contextName, final String iconKey, Account selected, List<Account> accounts) {
        final AccountList accountList = new AccountList(accounts);
        accountList.setSelected(selected);
        return new ItemListContext<Account>(accountList, contextName, iconKey, true) {
            @Override
            public void action() {
                PlaceUtil.goTo(PmWebModule.HISTORY_TOKEN_ACCOUNT + "/objectid=" + accountList.getSelected().getId(), // $NON-NLS$
                        withoutBreadCrumb());
            }
        };
    }

    public static HistoryContextProducer createProducerForAccount(final String contextName, final String iconKey,
                                                                  final Account selected, final List<Account> accounts) {
        return new HistoryContextProducer() {
            @Override
            public HistoryContext produce() {
                return PmItemListContext.createForAccount(contextName, iconKey, selected, accounts);
            }
        };
    }

    public static ActivityItemListContext createForActivity(String contextName, String iconKey, ActivityInstanceInfo selected, List<ActivityInstanceInfo> activities) {
        final ActivityList activityList = ActivityList.create(activities);
        activityList.setSelected(new ActivityInfoItem(selected));
        return new ActivityItemListContext(activityList, contextName, iconKey, true);
    }

    private static ItemListContext<Person> createForPerson(String contextName, final String iconKey, Person selected, List<Person> persons) {
        final ContextList<Person> contextList = new PersonList(persons);
        contextList.setSelected(selected);
        return new ItemListContext<Person>(contextList, contextName, iconKey, true) {
            @Override
            public void action() {
                PlaceUtil.goTo(PmWebModule.HISTORY_TOKEN_PERSON + "/objectid=" + contextList.getSelected().getId(), // $NON-NLS$
                        withoutBreadCrumb());
            }
        };
    }

    public static HistoryContextProducer createProducerForPerson(final String contextName, final String iconKey,
                                                                 final Person selected, final List<Person> persons) {
        return new HistoryContextProducer() {
            @Override
            public HistoryContext produce() {
                return createForPerson(contextName, iconKey, selected, persons);
            }
        };
    }

    public static ItemListContext<FolderItem> createForFolder(String contextName, final String iconKey, FolderItem selected, List<FolderItem> folderItems) {
        final FolderItemList folderList = new FolderItemList(folderItems);
        folderList.setSelected(selected);
        return new ItemListContext<FolderItem>(folderList, contextName, iconKey, true) {
            @Override
            public void action() {
                FolderLinkListener.goTo(folderList.getSelected(), withoutBreadCrumb());
            }
        };
    }
}