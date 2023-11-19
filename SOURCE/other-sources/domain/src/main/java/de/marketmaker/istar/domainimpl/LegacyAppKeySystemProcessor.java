package de.marketmaker.istar.domainimpl;

import de.marketmaker.istar.domain.ItemWithSymbols;
import de.marketmaker.istar.domain.KeysystemEnum;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Extending {@link KeysystemEnum#values()} requires to update all AMQP clients first.
 * This poses a problem for legacy apps, which are difficult to update with new istar versions.
 *
 * Therefore added {@link KeysystemEnum#values()} can be hidden from legacy systems by filtering them.
 * Should be applied to all methods of AMQP queue "istar.instrument".
 */
public class LegacyAppKeySystemProcessor implements Consumer<ItemWithSymbols> {

    private final KeysystemEnum[] notAllowed = new KeysystemEnum[]{KeysystemEnum.MIC, KeysystemEnum.OPERATING_MIC};

    @Override
    public void accept(ItemWithSymbols itemWithSymbols) {
        if (itemWithSymbols instanceof ItemWithSymbolsDp2) {
            Arrays.stream(notAllowed).forEach(((ItemWithSymbolsDp2) itemWithSymbols)::remove);
        }
    }
}
