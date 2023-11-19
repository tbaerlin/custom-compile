package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.pmxml.HasId;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.SimpleMM;
import de.marketmaker.iview.pmxml.TiType;

/**
 * Author: umaurer
 * Created: 17.02.14
 */
public class SpsLeafProperty extends SpsProperty {
    private final ParsedTypeInfo parsedTypeInfo;
    private MM dataItem = null;

    public SpsLeafProperty(String bindKey, SpsProperty parent, ParsedTypeInfo parsedTypeInfo) {
        super(bindKey, parent);
        this.parsedTypeInfo = parsedTypeInfo;
    }

    public MM getDataItem() {
        return this.dataItem;
    }

    public boolean isShellMMInfo() {
        return this.parsedTypeInfo.getTypeId() == TiType.TI_SHELL_MM || this.parsedTypeInfo.getTypeId() == TiType.TI_FOLDER;
    }

    public ShellMMInfo getShellMMInfo() {
        if (this.parsedTypeInfo.getTypeId() != TiType.TI_SHELL_MM && this.parsedTypeInfo.getTypeId() != TiType.TI_FOLDER) {
            throw new IllegalStateException("SpsLeafProperty.getShellMMInfo(): Cannot return ShellMMInfo for SpsLeafProperty with type " + this.parsedTypeInfo.getTypeId() + " - " + getBindToken()); // $NON-NLS$
        }
        if (this.dataItem == null) {
            return null;
        }
        return (ShellMMInfo) this.dataItem;
    }

    public MMDateTime getDate() {
        if (this.parsedTypeInfo.getTypeId() != TiType.TI_DATE) {
            throw new IllegalStateException("SpsLeafProperty.getDate(): Cannot return MMDateTime for SpsLeafProperty with type " + this.parsedTypeInfo.getTypeId() + " - " + getBindToken()); // $NON-NLS$
        }
        return (MMDateTime) this.dataItem;
    }

    public String getStringValue() {
        if (this.dataItem == null) {
            return null;
        }
        switch (this.parsedTypeInfo.getTypeId()) {
            case TI_SHELL_MM:
            case TI_FOLDER:
            case TI_DATE:
                return DataItemFormatter.format(this.parsedTypeInfo, this.dataItem);
            case TI_ENUMERATION:
                return MmTalkHelper.asCode(this.dataItem);
            case TI_DYNAMIC:
                if(this.dataItem instanceof HasId) {
                    return ((HasId) this.dataItem).getId();
                }
            case TI_BOOLEAN:
            case TI_NUMBER:
            case TI_STRING:
            case TI_MEMO:
            default:
                return MmTalkHelper.asString(this.dataItem);
        }
    }

    public void setNullValue() {
        setNullValue(true, true);
    }

    public void setValue(String value) {
        setValue(value, true, true);
    }

    public void setValue(SimpleMM value) {
        setValue(value, true, true);
    }

    public void setValue(ShellMMInfo value) {
        setValue(value, true, true);
    }

    public void setNullValue(boolean setChangeIndicator, boolean fireEvent) {
        _setValue(null, setChangeIndicator, fireEvent);
    }

    public void setValue(String value, boolean setChangeIndicator, boolean fireEvent) {
        _setValue(MmTalkHelper.asMMType(value, this.parsedTypeInfo.getTypeId()), setChangeIndicator, fireEvent);
    }

    public void setValue(MM value, boolean setChangeIndicator, boolean fireEvent) {
        final TiType tiType = this.parsedTypeInfo.getTypeId();
        if (!MmTalkHelper.isSameDataType(tiType, value)) {
            Notifications.add("decl-data mismatch", "data: " + value.getClass().getSimpleName() + "\ndecl: " + tiType + "\n" + getBindToken()); // $NON-NLS$
        }
        _setValue(value, setChangeIndicator, fireEvent);
    }

    private void _setValue(MM value, boolean setChangeIndicator, boolean fireEvent) {
        if (MmTalkHelper.equals(this.parsedTypeInfo.getTypeId(), this.dataItem, value)) {
            return;
        }
        this.dataItem = value;
        if (setChangeIndicator) {
            setChanged();
        }
        if (fireEvent) {
            fireChanged();
        }
    }

    public ParsedTypeInfo getParsedTypeInfo() {
        return this.parsedTypeInfo;
    }

    @Override
    public String toString() {
        return super.toString() + " | LeafProperty value: " + this.dataItem; // $NON-NLS$
    }
}