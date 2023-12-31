/*
 * Ext GWT 2.2.4 - Ext for GWT
 * Copyright(c) 2007-2010, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package com.extjs.gxt.ui.client.data;

import java.util.List;

/**
 * A <code>XmlReader</code> implementation that reads XML data using a
 * <code>ModelType</code> definition and returns a list load result instance.
 * 
 * @param <D> the type of list load result being returned by the reader
 */
public class XmlLoadResultReader<D extends ListLoadResult<? extends ModelData>> extends XmlReader<D> {
  /**
   * Creates a new reader.
   * 
   * @param modelType the model type definition
   */
  public XmlLoadResultReader(ModelType modelType) {
    super(modelType);
  }

  @Override
  protected Object createReturnData(Object loadConfig, List<ModelData> records, int totalCount) {
    return newLoadResult(loadConfig, records);
  }

  /**
   * Template method that provides load result.
   * 
   * @param models the models
   * @return the load result
   */
  protected ListLoadResult<ModelData> newLoadResult(Object loadConfig, List<ModelData> models) {
    return new BaseListLoadResult<ModelData>(models);
  }
}
