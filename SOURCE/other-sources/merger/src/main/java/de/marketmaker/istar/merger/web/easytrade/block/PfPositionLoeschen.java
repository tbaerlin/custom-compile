/*
 * PfPositionLoeschen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.user.RemovePositionCommand;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Deletes a position postition of a watchlist
 */
public class PfPositionLoeschen extends UserHandler {
    public static final class Command extends UserCommandImpl {
        private Long portfolioid;
        private Long positionid;

        @NotNull
        public Long getPositionid() {
            return positionid;
        }

        public void setPositionid(Long positionid) {
            this.positionid = positionid;
        }

        @NotNull
        public Long getPortfolioid() {
            return portfolioid;
        }

        public void setPortfolioid(Long portfolioid) {
            this.portfolioid = portfolioid;
        }
    }

    public PfPositionLoeschen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Long localUserId = getLocalUserId(cmd);

        final RemovePositionCommand dpc = new RemovePositionCommand();
        dpc.setUserid(localUserId);
        dpc.setPortfolioid(cmd.getPortfolioid());
        dpc.setPositionid(cmd.getPositionid());

        getUserProvider().removePosition(dpc);

        final Map<String, Object> model = new HashMap<>();
        model.put("portfolioid", cmd.getPortfolioid());
        return new ModelAndView("pfpositionloeschen", model);
    }
}
