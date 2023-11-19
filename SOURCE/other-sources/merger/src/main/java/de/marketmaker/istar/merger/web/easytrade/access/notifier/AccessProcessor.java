package de.marketmaker.istar.merger.web.easytrade.access.notifier;

import static de.marketmaker.istar.merger.web.easytrade.MoleculeController.ERROR_VIEWNAME;

import com.google.protobuf.Descriptors.FieldDescriptor;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.easytrade.Error;
import de.marketmaker.istar.merger.web.easytrade.MoleculeController;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.access.Access;
import de.marketmaker.istar.merger.web.easytrade.access.AccessCollector;
import de.marketmaker.istar.merger.web.easytrade.access.AccessNotifier;
import de.marketmaker.istar.merger.web.easytrade.access.AccessStatus;
import dev.infrontfinance.dm.proto.Access.Atom;
import dev.infrontfinance.dm.proto.Access.Molecule;
import dev.infrontfinance.dm.proto.Access.Molecule.Builder;
import dev.infrontfinance.dm.proto.Access.Quote;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * Processes molecule accesses.
 * <p>
 * Intentionally not using streams to keep overhead small.
 * </p>
 *
 * @author zzhao
 */
@Slf4j
@RequiredArgsConstructor
public class AccessProcessor {

  public static final String ROOT_AUTH_PLACEHOLDER = "ALL";

  private static final FieldDescriptor FD_AUTH =
      Molecule.getDescriptor().findFieldByName("auth");

  private static final FieldDescriptor FD_AUTH_TYPE =
      Molecule.getDescriptor().findFieldByName("authType");

  private static final FieldDescriptor FD_VWD_ID =
      Molecule.getDescriptor().findFieldByName("vwdId");

  private static final FieldDescriptor FD_APP_ID =
      Molecule.getDescriptor().findFieldByName("appId");

  private static final FieldDescriptor FD_PRODUCT_ID =
      Molecule.getDescriptor().findFieldByName("productId");

  private static final Set<String> IGNORE_PREFIX = new HashSet<>(Arrays.asList(
      "ED", "PF", "WL"
  ));

  private static final Set<String> IGNORE_BLOCKS = new HashSet<>(Arrays.asList(
      "MSC_NoteEdit", "MSC_GisPages", "NWS_Email", "MSC_PortfolioRatios",
      "MSC_PortfolioVaRLight", "MSC_User_Lists", "MSC_User_Configuration",
      "MSC_VwdCms_KeyGenerator"
  ));

  private final MeterRegistry meterRegistry;

  private final Executor ex;

  private final AccessCollector collector;

  private final AccessNotifier notifier;

  public void process(Zone zone, MoleculeRequest mr, ModelAndView mav) {
    // no notifier or zone or molecule request, no audit
    if (this.notifier == null || zone == null || mr == null) {
      return;
    }
    final Sample sample = Timer.start();
    try {
      doProcess(zone, mr, mav);
    } catch (Throwable e) {
      log.error("<process> failed", e);
    } finally {
      if (this.meterRegistry != null) {
        sample.stop(this.meterRegistry.timer("access.collect", Tags.empty()));
      }
    }
  }

  private void doProcess(Zone zone, MoleculeRequest mr, ModelAndView mav) {
    final Builder builder = createMoleculeBuilder(zone, mr);

    // atom requests and atom model and views at same positions
    // ensured by MoleculeController
    final List<AtomRequest> atomReqs = mr.getAtomRequests();
    final List<ModelAndView> atomMVs = getAtomModelAndViews(mav);

    for (int i = 0; i < atomReqs.size(); i++) {
      final AtomRequest atomReq = atomReqs.get(i);
      final String atomName = atomReq.getName();
      if (IGNORE_BLOCKS.contains(atomName)
          || IGNORE_PREFIX.contains(atomName.substring(0, atomName.indexOf("_")))) {
        continue;
      }

      final ModelAndView atomMV = atomMVs.get(i);

      final Atom.Builder atomBuilder = Atom.newBuilder().setName(atomName);
      // most atoms only support one symbol, i.e. also one status
      // this can be overwritten on quote level

      if (atomMV == null || atomMV.getModel() == null || atomMV.getModel().isEmpty()
          || ERROR_VIEWNAME.equals(atomMV.getViewName())) {
        // should not happen, there should always be a model-and-view and model, just in case
        // if it happens, just use status on atom level and add each found symbol as an instrument
        // if no symbol found, just set status
        atomBuilder.setStatus(getAccessStatus(atomMV).getCode());
        final String[] symbols = this.collector.getMajorSymbols(atomReq);
        for (String symbol : symbols) {
          atomBuilder.addQuotesBuilder().setInstrument(symbol);
        } // if empty, nothing to do
      } else { // successful processing - might be NO_DATA
        final Map<String, Object> am = atomMV.getModel();
        if (log.isDebugEnabled()) {
          log.debug("<doProcess> {}", am.keySet());
        }

        final Object symbolStates = this.collector.collect(atomReq, am);
        if (symbolStates instanceof Access) {
          final Access access = (Access) symbolStates;
          addToAtomBuilder(atomBuilder, access);
        } else if (symbolStates instanceof List) {
          final List<Access> accesses = (List<Access>) symbolStates;
          for (Access access : accesses) {
            addToAtomBuilder(atomBuilder, access);
          }
        }
      }

      builder.addAtoms(atomBuilder);
    }

    if (!builder.getAtomsBuilderList().isEmpty()) {
      this.ex.execute(() -> this.notifier.notify(builder));
    }
  }

  private void addToAtomBuilder(Atom.Builder atomBuilder, Access access) {
    atomBuilder.setStatus(access.getStatus().getCode());
    if (StringUtils.isNotBlank(access.getInstrument())) {
      final Quote.Builder quoteBuilder = atomBuilder.addQuotesBuilder();
      quoteBuilder.setInstrument(access.getInstrument());
      quoteBuilder.setType(access.getType());
      if (StringUtils.isNotBlank(access.getMarket())) {
        quoteBuilder.setMarket(access.getMarket());
      }
      quoteBuilder.setStatus(access.getStatus().getCode());
    }
  }

  private Builder createMoleculeBuilder(Zone zone, MoleculeRequest mr) {
    String authType = mr.getAuthenticationType();
    if (ProfileResolver.ROOT_AUTHENTICATION_TYPE.equals(authType)) {
      authType = ROOT_AUTH_PLACEHOLDER;
    }

    final Builder builder = Molecule.newBuilder()
        .setTs(System.currentTimeMillis())
        .setZone(zone.getName());

    setIfAvailable(builder, FD_AUTH, mr.getAuthentication());
    setIfAvailable(builder, FD_AUTH_TYPE, authType);

    final RequestContext reqCtx = RequestContextHolder.getRequestContext();
    if (reqCtx != null) {
      builder.setUid(reqCtx.getUniqueId()).setLanguage(reqCtx.getLocale().getLanguage());
      if (authType == null) {
        if (log.isDebugEnabled()) {
          log.debug("<createMoleculeBuilder> null authType, {} {} {}",
              zone.getName(), mr.getAuthentication(), reqCtx.getUniqueId());
        }
      }

      final Profile profile = reqCtx.getProfile();
      if (profile != null) {
        builder.setProfileType(profile.getClass().getSimpleName());
      }

      if (profile instanceof VwdProfile) {
        final VwdProfile vp = (VwdProfile) profile;
        setIfAvailable(builder, FD_VWD_ID, vp.getVwdId());
        setIfAvailable(builder, FD_APP_ID, vp.getAppId());
        setIfAvailable(builder, FD_PRODUCT_ID, vp.getProduktId());
      }
    }
    return builder;
  }

  private AccessStatus getAccessStatus(ModelAndView atomMV) {
    if (atomMV == null) {
      return AccessStatus.NOT_PROCESSED;
    }

    final Map<String, Object> model = atomMV.getModel();
    if (model == null || model.isEmpty()) {
      return AccessStatus.NOT_PROCESSED;
    }

    if (ERROR_VIEWNAME.equals(atomMV.getViewName())) {
      final Object err = model.get(ERROR_VIEWNAME);
      if (err instanceof Error) {
        final Error error = (Error) err;
        if (error.getCode() != null && error.getCode().startsWith("validator")) {
          return AccessStatus.BAD_REQUEST;
        }

        switch (error.getCode()) {
          case "unspecified.error":
          case "FATAL_ERROR":
          case "ImgRatiosUniverse.failed":
          case "ratios.searchfailed":
          case "page.error":
            return AccessStatus.ERROR; // keep to track used error code
          case "instrument.symbol.unknown":
          case "quote.unknown":
            return AccessStatus.UNKNOWN_SYMBOL;
          case "atom.cancelled":
            return AccessStatus.CANCELED;
          case "no.content":
          case "data.missing":
          case "fta.requestfailed":
          case "page.notfound":
          case "performances.missing":
            return AccessStatus.NO_DATA;
          case "timeout":
            return AccessStatus.TIMEOUT;
          case "request.invalid":
          case "parameter.missing":
          case "typeMismatch":
          case "instrument.type.illegal":
          case "search.failed":
            return AccessStatus.BAD_REQUEST;
          case "request.no-license":
          case "request.no-profile":
          case "permission.denied":
          case "quote.notallowed":
            return AccessStatus.DENIED;
          default:
            return AccessStatus.ERROR;
        }
      } else {
        return AccessStatus.ERROR;
      }
    }

    return AccessStatus.ERROR;
  }

  @SuppressWarnings("all")
  private List<ModelAndView> getAtomModelAndViews(ModelAndView mav) {
    if (mav == null || mav.getModel() == null) {
      return Collections.emptyList();
    }
    final List<ModelAndView> atomMVs =
        (List<ModelAndView>) mav.getModel().get(MoleculeController.KEY_ATOMS);
    return atomMVs == null || atomMVs.isEmpty() ? Collections.emptyList() : atomMVs;
  }

  private void setIfAvailable(Builder builder, FieldDescriptor fd, String val) {
    if (StringUtils.isNotBlank(val)) {
      builder.setField(fd, val);
    }
  }
}
