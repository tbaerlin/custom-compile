/*
 * UserProviderImpl.java
 *
 * Created on 02.08.2006 10:10:16
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.RemoteCacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.CompanyProvider;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.UserNotesProvider;
import de.marketmaker.istar.merger.provider.UserProvider;
import de.marketmaker.istar.merger.util.SymbolUtil;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

import static de.marketmaker.istar.merger.Constants.*;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class UserProviderImpl implements UserProvider, UserNotesProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UserDao userDao;

    private UserNotesDao notesDao;

    private CompanyProvider companyProvider;

    private InstrumentProvider instrumentProvider;

    private Ehcache userCache;

    private boolean allowRemovePositionWithOrders = false;

    public void setUserCache(Ehcache userCache) {
        this.userCache = userCache;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setNotesDao(UserNotesDao notesDao) {
        this.notesDao = notesDao;
    }

    public void setCompanyProvider(CompanyProvider companyProvider) {
        this.companyProvider = companyProvider;
    }

    public void setInstrumentProvider(
            InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setAllowRemovePositionWithOrders(boolean allowRemovePositionWithOrders) {
        this.allowRemovePositionWithOrders = allowRemovePositionWithOrders;
    }

    @Override
    public UserContext getUserContext(long id) {
        final User user = getUser(id);
        final Company company = getCompany(user.getCompanyid());
        return createUserContext(user, company);
    }

    private User getUserForUpdate(long id) {
        return getUser(id).deepCopy();
    }

    @Override
    public List<Long> getUserIds(long companyId) {
        return this.userDao.getUserIds(companyId);
    }

    private User getUser(long id) {
        final Element userElement = this.userCache.get(id);
        final User user;
        if (userElement == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getUser> not in cache: " + id);
            }
            user = this.userDao.selectUser(id);
            if (user == null) {
                throw new NoSuchUserException("Invalid user id: " + id, id);
            }
            addToCache(user);
        }
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getUser> found in cache: " + id);
            }
            user = (User) userElement.getValue();
        }
        return user;
    }

    @Override
    public UserContext retrieveUserContext(String login, long companyid) {
        final Company company = getCompany(companyid);
        return getUserContext(login, company, false);
    }

    @Override
    public UserContext getUserContext(String login, long companyid) {
        final Company company = getCompany(companyid);
        return getUserContext(login, company, "true".equals(company.getProperty("is.user.autocreate")));
    }

    private UserContext getUserContext(String login, Company company, boolean autocreate) {
        final User user;
        synchronized (this) { // make sure only one thread at a time tries to create a user
            user = this.userDao.selectUser(login, company.getId());
            if (user == null) {
                if (autocreate && login != null) {
                    return createUserForLogin(login, company);
                }

                this.logger.warn("<getUser> no user in " + company + " with login '" + login + "'");
                return null;
            }
        }

        final Element userElement = this.userCache.get(user.getId());
        if (userElement != null) {
            // no need to put user into cache as that would possibly distribute it
            return createUserContext((User) userElement.getValue(), company);
        }

        addToCache(user);
        return createUserContext(user, company);
    }

    @Override
    public void updateLogin(UpdateLoginCommand command) {
        final Company company = getCompany(command.getCompanyid());
        final User user = this.userDao.selectUser(command.getOldLogin(), company.getId());
        if (user == null) {
            throw new NoSuchUserException(null, command.getOldLogin(), company.getId());
        }

        final User existingUser = this.userDao.selectUser(command.getNewLogin(), company.getId());
        if (existingUser != null) {
            throw new LoginExistsException("Login exists", command.getNewLogin());
        }

        this.userDao.updateLogin(user.getId(), command.getNewLogin());
    }

    private Company getCompany(long companyid) {
        return this.companyProvider.getCompany(companyid);
    }

    private UserContext createUserForLogin(String login, Company company) {
        this.logger.info("<createUserForLogin> " + login + " in " + company.getName());
        final User tmp = new User();
        tmp.setLogin(login);
        tmp.setPassword("");
        tmp.setCreatedon(new DateTime());
        tmp.setCompanyid(company.getId());
        final long id = this.userDao.insertUser(tmp);
        tmp.setId(id);
        createInitialPortfolios(company, tmp);
        return getUserContext(id);
    }

    private void createInitialPortfolios(Company company, User user) {
        String name;
        int n = 0;
        while ((name = company.getProperty("user.autocreate.watchlist" + ++n)) != null) {
            Portfolio p = new Portfolio();
            p.setName(name);
            p.setWatchlist(true);
            this.userDao.insertPortfolio(user, p);
            this.logger.info("<createInitialPortfolios> created watchlist " + name + " for user " + user.getId());
            // TODO: add pre-configured positions
        }
        n = 0;
        while ((name = company.getProperty("user.autocreate.portfolio" + ++n)) != null) {
            Portfolio p = new Portfolio();
            p.setName(name);
            p.setWatchlist(false);
            p.setCurrencyCode("EUR"); // TODO: make configurable
            p.setCash(BigDecimal.ZERO); // TODO: make configurable
            this.userDao.insertPortfolio(user, p);
            this.logger.info("<createInitialPortfolios> created portfolio " + name + " for user " + user.getId());
            // TODO: add pre-configured positions with orders
        }
    }

    @Override  // FIXME: we should really use transactions here
    public void updatePosition(UpdatePositionCommand command) {
        final User user = getUserForUpdate(command.getUserid());
        final Long portfolioid = command.getPortfolioid();
        final Portfolio portfolio = getPortfolio(user, portfolioid);
        final Long positionid = command.getPositionid();
        final PortfolioPosition position = portfolio.getPosition(positionid);
        if (position == null) {
            throw new NoSuchPositionException("Invalid positionId: " + positionid, positionid);
        }

        final String symbol = command.getSymbol();
        final SymbolStrategyEnum symbolStrategy = SymbolUtil.guessStrategy(symbol);

        // if the new symbol is a quote we have to make sure
        // this quote is not yet in the portfolio
        if (SymbolStrategyEnum.QID == symbolStrategy) {
            long symbolId = getSymbolId(symbol, SymbolUtil.QID_SUFFIX);
            for (Long elemQid : portfolio.getQuoteIds()) {
                if (elemQid == symbolId) {
                    throw new UnsupportedOperationException("the quote is already in this portfolio ('" + symbol + "')");
                }
            }
        }

        // the symbol includes the suffix and is used in notes
        // in portfolioposition we have 2 long columns without suffix,
        // in notes we have a single column with suffix, thats what this hackery is about:
        // needed to attach the old note to the changed portfolio positions
        final String oldQid = position.getQid() + SymbolUtil.QID_SUFFIX;
        final String oldIid = position.getIid() + SymbolUtil.IID_SUFFIX;
        final String oldItemId;
        switch (symbolStrategy) {
            case IID:
                position.setIid(getSymbolId(command.getSymbol(), SymbolUtil.IID_SUFFIX));
                oldItemId = oldIid;
                break;
            case QID:
                position.setQid(getSymbolId(command.getSymbol(), SymbolUtil.QID_SUFFIX));
                oldItemId = oldQid;
                break;
            default:
                throw new NoSuchPositionException("invalid new symbol: " + symbol, positionid);
        }

        final int numRows = userDao.updatePosition(positionid, position);
        if (numRows != 1) {
            this.logger.warn("<updatePosition> note for position "
                    + positionid + " affected " + numRows + " rows?!");
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<updatePosition> note for position " + positionid);
        }
        notesDao.updateItemId(portfolioid, oldItemId, symbol);
        addToCache(user);
    }

    private long getSymbolId(String symbol, String suffix) {
        try {
            return Long.parseLong(symbol.substring(0, symbol.length() - suffix.length()));
        } catch (NumberFormatException e) {
            throw new UnknownSymbolException("invalid symbol: '" + symbol + "'");
        }
    }

    private Portfolio getPortfolio(User user, Long portfolioid) {
        final Portfolio portfolio = user.getPortfolioOrWatchlist(portfolioid);
        if (portfolio == null) {
            throw new NoSuchPortfolioException(portfolioid);
        }
        return portfolio;
    }

    @Override
    public void insertPosition(AddPositionCommand command) {
        final User user = getUserForUpdate(command.getUserid());
        final Long portfolioid = command.getPortfolioid();
        final Portfolio portfolio = getPortfolio(user, portfolioid);
        if (portfolio.hasPositionForQuote(command.getQuoteid())) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<insertPosition> duplicate " + command.getQuoteid()
                        + " for " + portfolioid + ", returning");
            }
            return; // already there, no duplicates
        }
        doAddPosition(user, portfolio, command);

        addToCache(user);
    }

    private PortfolioPosition doAddPosition(User user, Portfolio portfolio,
            AddPositionCommand command) {
        final Company c = getCompany(user.getCompanyid());
        if (portfolio.isWatchlist()) {
            final int maxNum = c.getMaxNumPositionsPerWatchlist();
            final int num = portfolio.getPositions().size();
            if (num >= maxNum) {
                throw new WatchlistPositionLimitExceededException("Maximum number of watchlist positions ("
                        + maxNum + ") exceeded", maxNum);
            }
        }
        else {
            final int maxNum = c.getMaxNumPositionsPerPortfolio();
            final List<PortfolioPosition> nonEmptyPositions = portfolio.getNonEmptyPositions();
            //only check for positions with valid quotes if we have to, since it requires requests to instrument server
            if (nonEmptyPositions.size() >= maxNum) {
                if (countPositionsWithValidQuotes(nonEmptyPositions) >= maxNum) {
                    throw new PortfolioPositionLimitExceededException("Maximum number of portfolio positions ("
                            + maxNum + ") exceeded", maxNum);
                }
            }
        }

        final PortfolioPosition pp = new PortfolioPosition();
        pp.setIid(command.getInstrumentid());
        pp.setQid(command.getQuoteid());
        pp.setQuotedPerPercent(command.isQuotedPerPercent());
        pp.setInstrumentType(command.getQuote().getInstrument().getInstrumentType());

        final long id = this.userDao.insertPosition(command, pp);

        pp.setId(id);
        portfolio.addPosition(pp);
        return pp;
    }

    private int countPositionsWithValidQuotes(List<PortfolioPosition> positions) {
        List<Long> usedIids = positions.stream().map(PortfolioPosition::getIid).collect(Collectors.toList());
        List<Instrument> instruments = this.instrumentProvider.identifyInstruments(usedIids);

        int validPositions = 0;
        for (int i = 0; i < positions.size(); i++) {
            final PortfolioPosition pp = positions.get(i);
            final Instrument instrument = instruments.get(i);
            if (instrument == null) {
                continue;
            }
            final Quote quote = instrument.getQuote(pp.getQid());
            if (quote == null) {
                continue;
            }

            validPositions++;
        }

        return validPositions;
    }

    @Override
    public Long addPortfolio(AddPortfolioCommand awc) {
        final User user = getUserForUpdate(awc.getUserid());
        final Company c = getCompany(user.getCompanyid());
        if (awc.isWatchlist()) {
            final int maxNum = c.getMaxNumWatchlistsPerUser();
            final int num = user.getWatchlists().size();
            if (num >= maxNum) {
                throw new WatchlistLimitExceededException("Maximum number of watchlists ("
                        + maxNum + ") exceeded", maxNum);
            }
        }
        else {
            final int maxNum = c.getMaxNumPortfoliosPerUser();
            final int num = user.getPortfolios().size();
            if (num >= maxNum) {
                throw new PortfolioLimitExceededException("Maximum number of portfolios ("
                        + maxNum + ") exceeded", maxNum);
            }
        }

        final Portfolio pf = new Portfolio();
        pf.setName(awc.getName());
        pf.setWatchlist(awc.isWatchlist());
        if (!awc.isWatchlist()) {
            pf.setCash(awc.getCash());
            pf.setCurrencyCode(awc.getCurrencycode());
        }

        final long id = this.userDao.insertPortfolio(user, pf);
        pf.setId(id);
        user.add(pf);

        addToCache(user);
        return pf.getId();
    }

    @Override
    public void updatePortfolio(UpdatePortfolioCommand upc) {
        final User user = getUserForUpdate(upc.getUserid());
        final Portfolio pf = getPortfolio(user, upc.getPortfolioid());
        pf.setName(upc.getName());
        if (!pf.isWatchlist()) {
            pf.setCash(upc.getCash());
        }
        upc.setWatchlist(pf.isWatchlist());

        this.userDao.updatePortfolio(upc);
        addToCache(user);
    }

    @Override
    public void removePortfolio(RemovePortfolioCommand rwc) {
        final User user = getUserForUpdate(rwc.getUserid());
        final Portfolio pf = getPortfolio(user, rwc.getPortfolioid());
        user.remove(pf);

        final int numRows = this.userDao.deletePortfolio(rwc);
        if (numRows != 1) {
            this.logger.warn("<deletePortfolio> for " + rwc.getPortfolioid()
                    + " affected " + numRows + " rows?!");
        }

        addToCache(user);
    }

    private PortfolioPosition getPositionWithOrder(Portfolio portfolio, final Long orderid) {
        final PortfolioPosition position = portfolio.getPositionWithOrder(orderid);
        if (position == null) {
            throw new NoSuchOrderException("Invalid orderid " + orderid + " for portfolio "
                    + portfolio.getId(), orderid);
        }
        return position;
    }

    private PortfolioPosition getPosition(Portfolio portfolio, final Long ppid) {
        final PortfolioPosition position = portfolio.getPosition(ppid);
        if (position == null) {
            throw new NoSuchOrderException("Invalid positionid " + ppid + " for portfolio "
                    + portfolio.getId(), ppid);
        }
        return position;
    }

    @Override
    public void removeOrder(RemoveOrderCommand command) {
        final User user = getUserForUpdate(command.getUserid());
        final Long portfolioid = command.getPortfolioid();
        final Portfolio portfolio = getPortfolio(user, portfolioid);

        final PortfolioPosition position = getPositionWithOrder(portfolio, command.getOrderid());

        final Order order = position.getOrder(command.getOrderid());
        // erase cash generated by order to be deleted
        portfolio.addCash(getOrderBalance(position, order).multiply(MINUS_ONE, MC));
        this.userDao.updatePortfolioCash(portfolio);

        position.removeOrder(command.getOrderid());
        final int numRows;
        if (position.getOrders().isEmpty()) {
            portfolio.removePosition(position.getId());
            numRows = this.userDao.deleteOrder(command, position.getId());
        }
        else {
            numRows = this.userDao.deleteOrder(command, -1L);
        }

        if (numRows != 1) {
            this.logger.warn("<deletePosition> for " + command.getOrderid()
                    + " affected " + numRows + " rows?!");
        }

        addToCache(user);
    }

    @Override
    public void updateOrder(UpdateOrderCommand command) {
        final User user = getUserForUpdate(command.getUserid());
        final Long portfolioid = command.getPortfolioid();
        final Portfolio portfolio = getPortfolio(user, portfolioid);

        final PortfolioPosition position = getPositionWithOrder(portfolio, command.getOrderid());

        final Order order = position.getOrder(command.getOrderid());
        // erase cash generated by order to be changed
        portfolio.addCash(getOrderBalance(position, order).multiply(MINUS_ONE, MC));

        final Order o = new Order();
        o.setBuy(command.isBuy());
        o.setCharge(command.getCharge());
        o.setDate(command.getDate());
        o.setExchangerate(command.getExchangeRate() != null ? command.getExchangeRate() : order.getExchangerate());
        o.setId(order.getId());
        o.setPrice(command.getPrice());
        o.setVolume(command.getVolume());

        position.updateOrder(o);
        this.userDao.updateOrder(command);

        portfolio.addCash(getOrderBalance(position, order));
        this.userDao.updatePortfolioCash(portfolio);

        addToCache(user);
    }

    @Override
    public long addOrder(AddOrderCommand command) {
        final User user = getUserForUpdate(command.getUserid());
        final Long portfolioid = command.getPortfolioid();
        final Portfolio portfolio = getPortfolio(user, portfolioid);
        PortfolioPosition position = portfolio.getPositionForQuote(command.getQid());

        if (command.isOnlyWithPosition() && position == null) {
            throw new IllegalStateException("portfolio position missing");
        }
        else if (position == null) {
            final AddPositionCommand apc = new AddPositionCommand();
            apc.setQuote(command.getQuote());
            apc.setUserid(command.getUserid());
            apc.setPortfolioid(command.getPortfolioid());
            position = doAddPosition(user, portfolio, apc);
        }

        final Order o = new Order();
        o.setBuy(command.isBuy());
        o.setCharge(command.getCharge());
        o.setDate(command.getDate());
        o.setExchangerate(command.getExchangeRate());
        o.setPrice(command.getPrice());
        o.setVolume(command.getVolume());
        o.setPositionId(position.getId());

        final long id = this.userDao.insertOrder(user, position, o);
        o.setId(id);
        position.addOrder(o);

        if (position.getTotalVolume().compareTo(BigDecimal.ZERO) == 0) {
            notesDao.deleteNote(portfolioid, position.getQid() + ".qid");
        }


        List<PortfolioPosition> positions = getPortfolioPositionsByInstrument(user.getId(), portfolioid, position.getIid());
        boolean allPositionsAreEmpty = true;
        for (PortfolioPosition pos : positions) {
            if (pos.getTotalVolume().compareTo(BigDecimal.ZERO) != 0) {
                allPositionsAreEmpty = false;
            }
        }

        if (allPositionsAreEmpty) {
            notesDao.deleteNote(portfolioid, position.getIid() + ".iid");
        }

        portfolio.addCash(getOrderBalance(position, o));
        this.userDao.updatePortfolioCash(portfolio);

        addToCache(user);
        return id;
    }

    /* Distributes a cell order on multiple positions of the same instrument */
    public List<AddOrderCommand> distributeOrderWithinInstrument(AddOrderCommand command,
            EasytradeInstrumentProvider instrumentProvider) {

        List<AddOrderCommand> orderCommands = new ArrayList<>();

        final User user = getUserForUpdate(command.getUserid());
        final Portfolio portfolio = getPortfolio(user, command.getPortfolioid());

        List<PortfolioPosition> instrumentPositions = getPortfolioPositionsByInstrument(command.getUserid(), command.getPortfolioid(), command.getIid());
        List<PortfolioPosition> positiveInstrumentPositions = Portfolio.filterPositivePositions(instrumentPositions);

        //make Assets independent class?
        List<SingleEvaluatedPosition.Assets> assetsList = positiveInstrumentPositions.stream().map(SingleEvaluatedPosition.Assets::new).collect(Collectors.toList());

        List<SingleEvaluatedPosition.Asset> allAssets = new ArrayList<>();
        for (SingleEvaluatedPosition.Assets assets : assetsList) {
            allAssets.addAll(assets.getAssets());
        }

        allAssets.sort(SingleEvaluatedPosition.Asset.BY_ORDER_DATE_COMPARATOR);

        BigDecimal totalVolumeToCell = command.getVolume();


        int assetIndex = 0;
        while (totalVolumeToCell.compareTo(BigDecimal.ZERO) > 0) {
            if (assetIndex >= allAssets.size()) {
                break;
            }

            SingleEvaluatedPosition.Asset asset = allAssets.get(assetIndex);
            BigDecimal assetVolume = asset.getVolume();
            BigDecimal volumeToCellForAsset = totalVolumeToCell.min(assetVolume);

            Order order = asset.getOrder();
            PortfolioPosition position = getPosition(portfolio, order.getPositionId());
            List<Quote> quotes = instrumentProvider.identifyQuotes(Collections.singleton(position.getQid()));

            AddOrderCommand newCommand = command.deepCopy();
            newCommand.setQuote(quotes.get(0));
            newCommand.setVolume(volumeToCellForAsset);
            orderCommands.add(newCommand);

            totalVolumeToCell = totalVolumeToCell.subtract(volumeToCellForAsset);
            assetIndex++;
        }

        if (totalVolumeToCell.compareTo(BigDecimal.ZERO) > 0) {
            AddOrderCommand newCommand = command.deepCopy();
            newCommand.setVolume(totalVolumeToCell);
            orderCommands.add(newCommand);
        }

        return orderCommands;
    }

    private List<PortfolioPosition> getPortfolioPositionsByInstrument(Long userId, Long portfolioId,
            Long iid) {
        final User user = getUserForUpdate(userId);
        final Portfolio portfolio = getPortfolio(user, portfolioId);
        return portfolio.getPositionsForInstrument(iid);
    }

    @Override
    public void removeUser(long id) {
        if (this.userDao.removeUser(id) > 0) {
            this.logger.info("<removeUser> removed " + id);
        }
    }

    @Override
    public List<AlternativeIid> getAlternativeIids(long iid, User user) {
        if (iid < Integer.MIN_VALUE || iid > Integer.MAX_VALUE) {
            throw new IllegalStateException("Can´t cast long Iid " + iid + " to int!");
        }
        return this.userDao.getAlternativeIids((int) iid, user);
    }

    private static BigDecimal getOrderBalance(PortfolioPosition pp, Order o) {
        BigDecimal result = o.getPrice().multiply(o.getVolume(), MC).multiply(o.getExchangerate(), MC);
        if (o.isBuy()) {
            result = result.multiply(MINUS_ONE, MC);
        }
        if (pp.isQuotedPerPercent()) {
            result = result.multiply(ONE_PERCENT, MC);
        }
        result = result.subtract(o.getCharge(), MC);
        return result;
    }

    @Override
    public void removePosition(RemovePositionCommand command) {
        final User user = getUser(command.getUserid());
        final Long portfolioid = command.getPortfolioid();
        final Portfolio portfolio = getPortfolio(user, portfolioid);

        if (portfolio.isWatchlist()) {
            removeWatchlistPosition(command);
            return;
        }

        final PortfolioPosition position = portfolio.getPosition(command.getPositionid());
        if (position == null) {
            throw new NoSuchPositionException("Invalid positionid " + command.getPositionid()
                    + " for user " + command.getUserid(), command.getPositionid());
        }
        if (!this.allowRemovePositionWithOrders && !position.getOrders().isEmpty()) {
            // todo: temporary hack, remove when postbank corrects their code
            // todo: currently, postbank always calls this method whenever they change orders
            throw new DeletePositionException("Position " + command.getPositionid()
                    + " for user " + command.getUserid()
                    + " cannot be deleted due to existing order(s)", command.getPositionid());
        }

        for (Order order : position.getOrders()) {
            final RemoveOrderCommand roc = new RemoveOrderCommand();
            roc.setUserid(command.getUserid());
            roc.setPortfolioid(command.getPortfolioid());
            roc.setOrderid(order.getId());

            removeOrder(roc);
        }
    }

    private void removeWatchlistPosition(RemovePositionCommand command) {
        final User user = getUserForUpdate(command.getUserid());
        final Long portfolioid = command.getPortfolioid();
        final Portfolio portfolio = getPortfolio(user, portfolioid);

        final PortfolioPosition position = portfolio.getPosition(command.getPositionid());
        if (position == null) {
            throw new NoSuchPositionException("Invalid positionid " + command.getPositionid()
                    + " for user " + command.getUserid(), command.getPositionid());
        }

        portfolio.removePosition(position.getId());

        final int numRows = this.userDao.deletePosition(command);
        if (numRows != 1) {
            this.logger.warn("<deletePosition> for " + command.getPositionid()
                    + " affected " + numRows + " rows?!");
        }

        addToCache(user);
    }

    private void addToCache(User user) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<addToCache> user " + user.getId());
        }
        try {
            this.userCache.put(new Element(user.getId(), user));
        } catch (RemoteCacheException e) {
            this.logger.warn("<addToCache> remote cache exception: " + e.getMessage());
        }
    }

    private UserContext createUserContext(User user, Company company) {
        return new UserContext(user, company, null);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "uid", description = "user id")
    })
    public String getCachedUserAsString(long uid) {
        final Element e = this.userCache.get(uid);
        if (e == null) {
            return "not found";
        }
        final String s = String.valueOf(e.getValue());
        this.logger.info(s);
        return s;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "uid", description = "user id")
    })
    public String removeUserFromCache(long uid) {
        return this.userCache.remove(uid) ? "removed" : "not found";
    }

    @Override
    public void createNote(AddNoteCommand cmd) {
        notesDao.createNote(cmd.getListId(), cmd.getItemId(), cmd.getContent());
        refreshCachedPortfolio(cmd.getUserId());
    }

    @Override
    public void updateNote(UpdateNoteCommand cmd) {
        notesDao.updateNote(cmd.getListId(), cmd.getItemId(), cmd.getContent());
        refreshCachedPortfolio(cmd.getUserId());
    }

    @Override
    public void deleteNote(RemoveNoteCommand cmd) {
        notesDao.deleteNote(cmd.getListId(), cmd.getItemId());
        refreshCachedPortfolio(cmd.getUserId());
    }

    @Override
    public String readNote(long listId, String itemId) {
        return notesDao.readNote(listId, itemId);
    }

    private void refreshCachedPortfolio(long userId) {
        removeUserFromCache(userId);
    }
}
