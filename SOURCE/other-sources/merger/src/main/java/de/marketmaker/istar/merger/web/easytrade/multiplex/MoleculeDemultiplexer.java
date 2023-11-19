/*
 * MoleculeDemultiplexer.java
 *
 * Created on 04.02.13 13:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.multiplex;

import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.easytrade.MoleculeController;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.iview.dmxml.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static de.marketmaker.istar.merger.web.ZoneDispatcherServlet.ZONE_ATTRIBUTE;
import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_ATTRIBUTE_NAME;
import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_TYPE_ATTRIBUTE_NAME;

/**
 * It sometimes makes sense to split an application into separate webapps, where certain atoms
 * can be processed within the current webapp and others have to be forwarded to another
 * webapp (e.g., web-as, where pm-Atoms can be handled locally and dmmxl-Atoms have to be
 * dispatched to a remote webapp).
 *
 * @author oflege
 */
public class MoleculeDemultiplexer extends AbstractController implements InitializingBean, DisposableBean {
    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String debugId;
    private LocalTarget localTarget;
    private List<MultiplexerTarget> targets;
    private ExecutorService executorService;

    public void setDebugId(String debugId) {
        this.debugId = debugId;
    }

    @Required
    public void setTargets(List<MultiplexerTarget> targets) {
        this.targets = targets;
        for (MultiplexerTarget target : targets) {
            if (target instanceof LocalTarget) {
                this.localTarget = (LocalTarget) target;
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, this.getClass().getSimpleName() + "." + debugId + "." + THREAD_ID.incrementAndGet());
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        this.executorService.shutdown();
    }

    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              final HttpServletResponse response) throws Exception {

        final Zone zone = (Zone) request.getAttribute(ZONE_ATTRIBUTE);
        final MoleculeRequest mr = (MoleculeRequest) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
        final RequestType rt = (RequestType) request.getAttribute(REQUEST_TYPE_ATTRIBUTE_NAME);

        if (rt == null) {
            if (this.localTarget == null) {
                throw new IllegalStateException("no LocalTarget specified (RequestType == null) (debugId=" + this.debugId + ")");
            }
            return this.localTarget.handleStraightRequest(request, response, zone, mr);
        }

        final int straightTarget = getStraightTarget(mr, zone);
        if (straightTarget > -1) {
            final MultiplexerTarget target = this.targets.get(straightTarget);
            return target.handleStraightRequest(request, response, zone, mr);
        }
        return handleRequestMixed(request, response, zone, mr);
    }

    class IndexedFuture {
        int id;
        Future<List<ModelAndView>> future;

        public IndexedFuture(int id, Future<List<ModelAndView>> future) {
            this.id = id;
            this.future = future;
        }
    }

    private ModelAndView handleRequestMixed(HttpServletRequest request, HttpServletResponse response, Zone zone, MoleculeRequest mr) throws Exception {
        final List<MoleculeRequest.AtomRequest> atoms = mr.getAtomRequests();
        final int[] targetIdxs = new int[atoms.size()];
        final TargetContext[] contexts = new TargetContext[this.targets.size()];
        for (int a = 0, atomsSize = atoms.size(); a < atomsSize; a++) {
            targetIdxs[a] = -1;
            final MoleculeRequest.AtomRequest atom = atoms.get(a);
            for (int i = 0; i < this.targets.size() && targetIdxs[a] == -1; i++) {
                final MultiplexerTarget target = this.targets.get(i);
                if (target.supports(zone, atom)) {
                    if (contexts[i] == null) {
                        contexts[i] = new TargetContext(mr);
                    }
                    targetIdxs[a] = i;
                    contexts[i].addAtom(atom);
                }
            }
            if (targetIdxs[a] == -1) {
                throw new IllegalStateException("no target configured for atom: " + atom.getName() + " (debugId=" + this.debugId  + ")");
            }
        }

        final List<IndexedFuture> listFutures = new ArrayList<>(this.targets.size());
        for (int i = 0; i < this.targets.size(); i++) {
            final MultiplexerTarget target = this.targets.get(i);
            if (contexts[i] != null) {
                listFutures.add(new IndexedFuture(i, target.handleMixedRequest(request, response, zone, contexts[i].getRequest(), this.executorService)));
            }
        }
        for (IndexedFuture idf : listFutures) {
            contexts[idf.id].setMav(idf.future.get());
        }

        final HashMap<String, Object> map = new HashMap<>();
        map.put(MoleculeController.KEY_ATOMS, getMergedAtomList(contexts, targetIdxs));
        map.put(MoleculeController.KEY_REQUEST, mr);

        return new ModelAndView("molecule", map);
    }

    private int getStraightTarget(MoleculeRequest mr, Zone zone) {
        int targetIdx = -1;
        for (MoleculeRequest.AtomRequest atom : mr.getAtomRequests()) {
            int idx = findTargetIdx(this.targets, atom, zone);
            if (targetIdx == -1) {
                targetIdx = idx;
                continue;
            }
            if (targetIdx != idx) {
                return -1;
            }
        }
        return targetIdx;
    }

    private int findTargetIdx(List<MultiplexerTarget> targets, MoleculeRequest.AtomRequest atom, Zone zone) {
        for (int i = 0, size = targets.size(); i < size; i++) {
            if (targets.get(i).supports(zone, atom)) {
                return i;
            }
        }
        throw new IllegalStateException("no target supports atom " + atom.getName() + " (debugId=" + this.debugId  + ")");
    }

    private List<ModelAndView> getMergedAtomList(TargetContext[] contexts, int[] targetIdxs) {
        final ArrayList<ModelAndView> atoms = new ArrayList<>(targetIdxs.length);
        for (int targetIdx : targetIdxs) {
            atoms.add(contexts[targetIdx].next());
        }
        return atoms;
    }
}
