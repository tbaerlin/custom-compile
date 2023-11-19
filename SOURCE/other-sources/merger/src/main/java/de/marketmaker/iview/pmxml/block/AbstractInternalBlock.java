package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.pmxml.PmxmlExchangeDataImpl;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * Created on 15.07.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 *         An {@link AbstractInternalBlock} is a block without an explicit corresponding StringTemplate for rendering its result type.
 *         Its data object is transported to {@link de.marketmaker.istar.merger.web.easytrade.MoleculeController} via
 *         the model and is marshalled by a renderer which can handle subclasses of {@link de.marketmaker.iview.dmxml.BlockType}.
 *         So in the end, it looks like an usual block.
 *         <p/>
 *         Use this type of block, e.g. if there is no reason for a xsd or if the block only manipulates content and doesn't
 *         define new structures. This type of block is introduced in the context of advisory solution with the need of a further
 *         opinion of data manipulation. It will run in the customers local tomcat, so there's no necessity for inter-process exchange
 *         and no use by 3rd-party (so no xsd consequently). Even so, it is favored to have a xml-shape of the data object to be able
 *         to read the response as with the other blocks.
 *         <p/>
 *         The result type T must be a subclass of {@link de.marketmaker.iview.dmxml.BlockType}, so that it can be
 *         integrated into molecules response structure. The command C must be a implementation of {@link AbstractInternalBlock.InternalCommand}
 *         thus the attributes key and correlationId can be handled by {@link AbstractInternalBlock}.
 *         Key and correlationid are a part of the model, actually. But in this special case, only the data object in the model
 *         matters because the whole xml is generated out of it. So if there's any data in the model that must result in xml,
 *         put it into the data object like key and correlationid.
 */

@MmInternal
public abstract class AbstractInternalBlock<T extends BlockType, C extends AbstractInternalBlock.InternalCommand> extends EasytradeCommandController {
    public static final String VIEW = "blocktypedata";
    public static final String DATA = "data";

    public interface InternalCommand extends Serializable {
        InternalCommandFeature getInternalCmdFeature();
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class InternalCommandFeature implements Serializable {
        private String correlationId;
        private String atomname;
        private String ttl = "PT1M";

        @NotNull
        String getCorrelationId() {
            return correlationId;
        }

        @NotNull
        String getAtomname() {
            return this.atomname;
        }

        public String getTtl() {
            return this.ttl;
        }

        public InternalCommandFeature withCorrelationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public InternalCommandFeature withAtomname(String atomname) {
            this.atomname = atomname;
            return this;
        }

        public InternalCommandFeature withTtl(String ttl) {
            this.ttl = ttl;
            return this;
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    public static class Command implements InternalCommand, Serializable {
        private String correlationId;
        private String atomname;

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }

        public void setAtomname(String atomname) {
            this.atomname = atomname;
        }

        @Override
        public InternalCommandFeature getInternalCmdFeature() {
            return new InternalCommandFeature().withAtomname(atomname).withCorrelationId(correlationId);
        }
    }


    protected PmxmlExchangeDataImpl pmxmlImpl;

    public void setPmxmlImpl(PmxmlExchangeDataImpl pmxmlImpl) {
        this.pmxmlImpl = pmxmlImpl;
    }

    protected AbstractInternalBlock(Class aClass) {
        super(aClass);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        final String authToken = (String) WebUtils.getSessionAttribute(httpServletRequest, ProfileResolver.PM_AUTHENTICATION_KEY);
        this.pmxmlImpl.setAuthToken(authToken);
        //noinspection unchecked
        final T result = internalDoHandle(httpServletRequest, httpServletResponse, (C) o, e);
        final InternalCommandFeature internalCmd = ((InternalCommand) o).getInternalCmdFeature();
        result.setTtl(internalCmd.getTtl());
        result.setKey(internalCmd.getAtomname());
        result.setCorrelationId(internalCmd.getCorrelationId());
        return new ModelAndView(AbstractInternalBlock.VIEW, AbstractInternalBlock.DATA, result);
    }

    protected abstract T internalDoHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, C cmd, BindException e);
}