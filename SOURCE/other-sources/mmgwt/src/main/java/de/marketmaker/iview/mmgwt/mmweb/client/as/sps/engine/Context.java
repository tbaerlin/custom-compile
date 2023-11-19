package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.user.client.Command;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.DmxmlContextFacade;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.SharedDmxmlContextFacade;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.expression.ExpressionHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.Validator;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.MainInput;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.TiType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public class Context {
    private final String activityInstanceId;
    private final String taskId;
    private final MainInput mainInput;
    private final String historyContextName;
    private final Map<ParsedTypeInfo, Map<String, String>> mapEnums;
    private final Map<String, List<Validator>> mapValidators;
    private final Map<String, String> mapLabels;
    private final Map<String, TiType> mapTypes;
    private final ExpressionHandler expressionHandler;
    private final DataContainerWrapper<DataContainerLeafNodeDeclaration> decl;
    private final SpsGroupProperty rootProp;
    private final Engine engine;
    private final String activityInstanceGuid;
    private final Command refreshInternalCommand;
    private final Map<String, Map<String, String>> viewState;
    private final Set<ViewStateFeature> viewStateFeatures;
    private boolean forceReadonly = false;
    private boolean forceDisabledLinksIfReadonly = false;

    private HashMap<String, Supplier<DmxmlContextFacade>> sharedDmxmlContextFacadeSuppliers = new HashMap<>();
    private boolean activateSharedDmxmlContextSuppliersByDefault;

    public Context(DataContainerCompositeNode rootDecl, MainInput mainInput, String activityInstanceId, String activityInstanceGuid, String historyContextName, String taskId, Command internalRefreshCommand, Map<String, Map<String, String>> viewState, Set<ViewStateFeature> viewStateFeatures) {
        this(rootDecl, mainInput, activityInstanceId, activityInstanceGuid, historyContextName, taskId,
                internalRefreshCommand, (SpsGroupProperty) createProperties(rootDecl, rootDecl, null), viewState, viewStateFeatures);
    }

    public Context(DataContainerCompositeNode rootDecl, MainInput mainInput, String activityInstanceId, String activityInstanceGuid, String historyContextName, String taskId, Command internalRefreshCommand, SpsGroupProperty rootProp, Map<String, Map<String, String>> viewState, Set<ViewStateFeature> viewStateFeatures) {
        this.mainInput = mainInput;
        this.activityInstanceId = activityInstanceId;
        this.activityInstanceGuid = activityInstanceGuid;
        this.taskId = taskId;
        this.historyContextName = historyContextName;
        this.decl = new DataContainerWrapper<>(rootDecl, true);
        this.refreshInternalCommand = internalRefreshCommand;

        this.mapEnums = new HashMap<>();
        this.mapTypes = null;
        this.mapLabels = null;
        this.mapValidators = new HashMap<>();
        this.expressionHandler = new ExpressionHandler(this);

        this.engine = new Engine(this);

        this.rootProp = rootProp;

        this.viewState = viewState;
        this.viewStateFeatures = viewStateFeatures;
    }

    public Map<String, String> getViewStateForSpsWidget(String descId) {
        if(this.viewState == null) {
            return null;
        }

        return this.viewState.get(descId);
    }

    public Map<String, String> getEnum(BindToken bindToken) {
        final ParsedTypeInfo desc = getLeafDeclaration(bindToken).getDescription();

        final Map<String, String> cachedEnumMap = this.mapEnums.get(desc);
        if (cachedEnumMap != null) {
            return cachedEnumMap;
        }

        final TiType fieldType = desc.getTypeId();
        if (fieldType != TiType.TI_ENUMERATION) {
            throw new IllegalStateException("fieldType of bindkey '" + bindToken + "' must be TI_ENUMERATION, is: " + fieldType); // $NON-NLS$
        }

        final Map<String, String> enumMap = SpsUtil.createEnumMap(desc.getEnumElements());
        this.mapEnums.put(desc, enumMap);

        return enumMap;
    }

    public int getEnumCount(BindToken bindToken) {
        return getEnum(bindToken).size();
    }

    @SuppressWarnings("unused")
    public ExpressionHandler getExpressionHandler() {
        return expressionHandler;
    }

    public String getLabel(String key) {
        if (this.mapLabels == null) {
            return null;
        }
        return this.mapLabels.get(key);
    }

    public TiType getTiType(BindToken bindToken) {
        if (this.mapTypes != null) {
            return this.mapTypes.get(bindToken.getConcatBindKey());
        }
        return getLeafDeclaration(bindToken).getDescription().getTypeId();
    }

    public DataContainerLeafNodeDeclaration getLeafDeclaration(BindToken bindToken) {
        return this.decl.getLeaf(bindToken);
    }

    public DataContainerNode getDeclaration(BindToken bindToken) {
        return this.decl.get(bindToken);
    }

    @SuppressWarnings("unused")
    public boolean isEnum(BindToken bindToken) {
        return isEnum(getDeclaration(bindToken));
    }

    public boolean isEnum(DataContainerNode node) {
        return node instanceof DataContainerLeafNode
                && ((DataContainerLeafNodeDeclaration) node).getDescription().getTypeId() == TiType.TI_ENUMERATION;
    }

    public boolean isList(BindToken bindToken) {
        return isList(getDeclaration(bindToken));
    }

    public boolean isList(DataContainerNode node) {
        return node instanceof DataContainerListNode;
    }

    public boolean isGroup(BindToken bindToken) {
        return getDeclaration(bindToken) instanceof DataContainerGroupNode;
    }

    public static SpsProperty createProperties(DataContainerNode node, DataContainerNode declNode, SpsProperty parentProp) {
        if (node instanceof DataContainerLeafNode) {
            if(declNode == null) {
                throw new IllegalStateException("Context.createProperties(): Data node is leaf '" + node.getNodeLevelName() + "', but decl node is null"); // $NON-NLS$
            }
            if (!(declNode instanceof DataContainerLeafNodeDeclaration)) {
                throw new IllegalStateException("Context.createProperties(): Data node is leaf '" + node.getNodeLevelName() + "', but decl node is " + declNode.getClass().getSimpleName()); // $NON-NLS$
            }
            DataContainerLeafNodeDeclaration declLeafNode = (DataContainerLeafNodeDeclaration) declNode;
            final SpsLeafProperty spsLeafProperty = new SpsLeafProperty(node.getNodeLevelName(), parentProp, declLeafNode.getDescription());
            final MM defaultValue = declLeafNode.getDefaultValue();
            if (defaultValue != null) {
                SpsUtil.transferDataToProperty(defaultValue, spsLeafProperty, false, false);
            }

            return spsLeafProperty;
        }
        else if (node instanceof DataContainerListNode) {
            return new SpsListProperty(node.getNodeLevelName(), parentProp); // TODO: what to do with children???
        }
        else if (node instanceof DataContainerGroupNode) {
            if(declNode == null) {
                throw new IllegalStateException("Context.createProperties(): Data node is group '" + node.getNodeLevelName() + "', but decl node is null"); // $NON-NLS$
            }
            if (!(declNode instanceof DataContainerGroupNode)) {
                throw new IllegalStateException("Data node is group '" + node.getNodeLevelName() + "' , but decl node is " + declNode.getClass().getSimpleName()); // $NON-NLS$
            }
            final SpsGroupProperty prop = new SpsGroupProperty(node.getNodeLevelName(), parentProp);
            for (DataContainerNode childNode : ((DataContainerCompositeNode) node).getChildren()) {
                final String bindKey = childNode.getNodeLevelName();
                final DataContainerNode childDeclNode = SpsUtil.getDeclChild((DataContainerGroupNode) declNode, bindKey);
                prop.put(bindKey, createProperties(childNode, childDeclNode, prop), false);
            }
            return prop;
        }
        throw new IllegalStateException("unhandled node type: " + node.getClass().getName()); // $NON-NLS$
    }

    public SpsGroupProperty getRootProp() {
        return this.rootProp;
    }

    public Engine getEngine() {
        return this.engine;
    }

    public void putValidator(BindToken targetToken, Validator validator) {
        List<Validator> validators = this.mapValidators.get(targetToken.getConcatBindKey());
        if (validators == null) {
            validators = new ArrayList<>();
        }
        validators.add(validator);
        this.mapValidators.put(targetToken.getConcatBindKey(), validators);
    }

    public List<Validator> getValidators(BindToken bindToken) {
        return this.mapValidators.get(bindToken.getConcatBindKey());
    }

    public List<Validator> removeValidator(BindToken bindToken) {
        return this.mapValidators.remove(bindToken.getConcatBindKey());
    }

    public String getHistoryContextName() {
        return this.historyContextName;
    }

    public MainInput getMainInput() {
        return this.mainInput;
    }

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public String getActivityInstanceGuid() {
        return activityInstanceGuid;
    }

    public String getTaskId() {
        return taskId;
    }

    public Command getRefreshInternalCommand() {
        return this.refreshInternalCommand;
    }

    public boolean isForceReadonly() {
        return this.forceReadonly || SessionData.INSTANCE.isUserPropertyTrue("readonly"); // $NON-NLS$
    }

    public void setForceReadonly(boolean forceReadonly) {
        this.forceReadonly = forceReadonly;
    }

    public void setForceDisabledLinksIfReadonly(boolean forceDisabledLinksIfReadonly) {
        this.forceDisabledLinksIfReadonly = forceDisabledLinksIfReadonly;
    }

    public boolean isForceDisabledLinksIfReadonly() {
        return this.forceDisabledLinksIfReadonly;
    }

    public void replayChangeEvents() {
        replayChangeEvents(this.rootProp);
    }

    public void transferDataToProperties(DataContainerNode node, DataContainerNode declNode, boolean fireEvent) {
        this.transferDataToProperties(node, declNode, this.rootProp, fireEvent);
    }

    private void transferDataToProperties(DataContainerNode node, DataContainerNode declNode, SpsProperty prop, boolean fireEvent) {
        if (node instanceof DataContainerLeafNode) {
            if (prop == null) {
                throw new IllegalStateException("transferDataToProperties - property type mismatch for node '" + node.getNodeLevelName() + "': was DataContainerLeafNode, expected " + (declNode != null ? declNode.getClass().getSimpleName() : "null") + " but WEB UI storage object was null"); // $NON-NLS$
            }
            if (!(prop instanceof SpsLeafProperty)) {
                throw new IllegalStateException("transferDataToProperties - property type mismatch for node '" + node.getNodeLevelName() + "': was DataContainerLeafNode, expected " + (declNode != null ? declNode.getClass().getSimpleName() : "null") + " (WEB UI storage object type was " + prop.getClass().getSimpleName() + ")"); // $NON-NLS$
            }
            final SpsLeafProperty lp = (SpsLeafProperty) prop;
            final DataContainerLeafNodeDataItem ln = (DataContainerLeafNodeDataItem) node;
            final MM di = ln.getDataItem();
            SpsUtil.transferDataToProperty(di, lp, false, fireEvent);
        }
        else if (node instanceof DataContainerGroupNode) {
            if (prop == null) {
                throw new IllegalStateException("transferDataToProperties - property type mismatch for node '" + node.getNodeLevelName() + "': was DataContainerGroupNode, expected " + (declNode != null ? declNode.getClass().getSimpleName() : "null") + " but WEB UI storage object was null"); // $NON-NLS$
            }
            if (!(prop instanceof SpsGroupProperty)) {
                throw new IllegalStateException("transferDataToProperties - property type mismatch for node '" + node.getNodeLevelName() + "': was DataContainerGroupNode, expected " + (declNode != null ? declNode.getClass().getSimpleName() : "null") + " (WEB UI storage object type was " + prop.getClass().getSimpleName() + ")"); // $NON-NLS$
            }
            if (declNode == null) {
                throw new IllegalStateException("transferDataToProperties - data node '" + node.getNodeLevelName() + "' is group, but decl node is null - " + prop.getBindToken()); // $NON-NLS$
            }
            if (!(declNode instanceof DataContainerGroupNode)) {
                throw new IllegalStateException("transferDataToProperties - data node '" + node.getNodeLevelName() + "' is group, but decl node is " + declNode.getClass().getSimpleName() + " - " + prop.getBindToken()); // $NON-NLS$
            }
            final DataContainerGroupNode gn = (DataContainerGroupNode) node;
            final SpsGroupProperty gp = (SpsGroupProperty) prop;
            gp.setNodeGUID(gn.getNodeGUID());
            for (DataContainerNode childNode : gn.getChildren()) {
                final String bindKey = childNode.getNodeLevelName();
                final DataContainerNode childDeclNode = SpsUtil.getDeclChild((DataContainerGroupNode) declNode, bindKey);
                final SpsProperty childProp = gp.get(bindKey);
                transferDataToProperties(childNode, childDeclNode, childProp, fireEvent);
            }
        }
        else if (node instanceof DataContainerListNode) {
            if (prop == null) {
                throw new IllegalStateException("transferDataToProperties - property type mismatch for node '" + node.getNodeLevelName() + "': was DataContainerListNode, expected " + (declNode != null ? declNode.getClass().getSimpleName() : "null") + " but WEB UI storage object was null"); // $NON-NLS$
            }
            if (!(prop instanceof SpsListProperty)) {
                throw new IllegalStateException("transferDataToProperties - property type mismatch for node '" + node.getNodeLevelName() + "': was DataContainerListNode, expected " + (declNode != null ? declNode.getClass().getSimpleName() : "null") + " (WEB UI storage object type was " + prop.getClass().getSimpleName() + ")"); // $NON-NLS$
            }
            if (declNode == null) {
                throw new IllegalStateException("transferDataToProperties - data node '" + node.getNodeLevelName() + "' is list, but decl node is null - " + prop.getBindToken()); // $NON-NLS$
            }
            if (!(declNode instanceof DataContainerListNode)) {
                throw new IllegalStateException("transferDataToProperties - data node '" + node.getNodeLevelName() + "' is list, but decl node is " + declNode.getClass().getSimpleName() + " - " + prop.getBindToken()); // $NON-NLS$
            }
            final DataContainerNode childDeclNode = ((DataContainerListNode) declNode).getChildren().get(0);
            final DataContainerListNode ln = (DataContainerListNode) node;
            final SpsListProperty lp = (SpsListProperty) prop;
            for (DataContainerNode childNode : ln.getChildren()) {
                final SpsProperty childProp = Context.createProperties(childNode, childDeclNode, lp);
                transferDataToProperties(childNode, childDeclNode, childProp, fireEvent);
                lp.add(childProp, false, false);
            }
            if (!ln.getChildren().isEmpty()) {
                lp.fireChanged();
            }
        }
    }


    /**
     * Fires change events on all properties.
     * The change event is not fired again on the root property, cf. {@link SpsProperty#setChanged()},
     * because this impacts also the SPS implicit refresh button behaviour .
     *
     * @see SpsProperty#setChanged()
     */
    private void replayChangeEvents(SpsProperty property) {
        if (property == null) {
            return;
        }
        if (property instanceof SpsLeafProperty) {
            property.fireChanged();
        }
        if (property instanceof SpsCompositeProperty) {
            final Collection<SpsProperty> children = ((SpsCompositeProperty) property).getChildren();
            for (SpsProperty child : children) {
                replayChangeEvents(child);
            }
            /*if (property instanceof SpsListProperty && !children.isEmpty()) {
                property.fireChanged();
            }*/
            //TODO: check if firing on all composite properties not only lists will not break existing code
            if (!children.isEmpty() && !property.isRoot()) {
                property.fireChanged();
            }
        }
    }

    public Collection<Supplier<DmxmlContextFacade>> getSharedDmxmlContextFacadeSuppliers() {
        return this.sharedDmxmlContextFacadeSuppliers.values();
    }

    public Supplier<DmxmlContextFacade> getSharedDmxmlContextSupplier(String contextId, boolean cancelable) {
        Supplier<DmxmlContextFacade> supplier = this.sharedDmxmlContextFacadeSuppliers.get(contextId);
        if (supplier == null) {
            final SharedDmxmlContextFacade facade = new SharedDmxmlContextFacade(cancelable);

            if (this.activateSharedDmxmlContextSuppliersByDefault) {
                facade.activate();
            }

            supplier = new Supplier<DmxmlContextFacade>() {
                @Override
                public DmxmlContextFacade get() {
                    return facade;
                }
            };
            this.sharedDmxmlContextFacadeSuppliers.put(contextId, supplier);
        }
        return supplier;
    }

    public void setActivateSharedDmxmlContextSuppliersByDefault(boolean activate) {
        this.activateSharedDmxmlContextSuppliersByDefault = activate;
    }

    public void registerViewStateFeature(ViewStateFeature viewStateFeature) {
        if(this.viewStateFeatures == null) {
            return;
        }
        this.viewStateFeatures.add(viewStateFeature);
    }
}