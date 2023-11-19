package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.user.UpdatePositionCommand;
import de.marketmaker.istar.merger.user.User;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


public class WlPositionAendern extends UserHandler {

    public static final class Command extends UserCommandImpl {

        private Long watchlistid;

        private Long positionid;

        private String symbol;

        public Long getWatchlistid() {
            return watchlistid;
        }

        public void setWatchlistid(Long watchlistid) {
            this.watchlistid = watchlistid;
        }

        /**
         * the position of the item within the list
         */
        @NotNull
        public Long getPositionid() {
            return positionid;
        }

        public void setPositionid(Long positionid) {
            this.positionid = positionid;
        }

        /**
         * the new symbol for the specified position
         * @return the symbol
         */
        @NotNull
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

    }

    public WlPositionAendern() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final User user = getUserContext(cmd).getUser();
        final Long listId = cmd.getWatchlistid();

        final UpdatePositionCommand command = new UpdatePositionCommand();
        command.setUserid(user.getId());
        command.setPortfolioid(listId);
        command.setPositionid(cmd.getPositionid());
        command.setSymbol(cmd.getSymbol());

        getUserProvider().updatePosition(command);

        final Map<String, Object> model = new HashMap<>();
        model.put("watchlistid", listId);
        return new ModelAndView("wlpositionaendern", model);
    }

}
