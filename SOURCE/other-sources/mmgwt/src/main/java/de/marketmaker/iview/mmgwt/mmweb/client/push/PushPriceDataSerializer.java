package de.marketmaker.iview.mmgwt.mmweb.client.push;

import java.util.ArrayList;

import de.marketmaker.itools.gwtcomet.comet.client.CometSerializer;
import de.marketmaker.itools.gwtcomet.comet.client.SerialTypes;

/**
 * Created on 04.02.2010 11:47:00
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
@SerialTypes({ PushData.class })
public abstract class PushPriceDataSerializer extends CometSerializer {
}
