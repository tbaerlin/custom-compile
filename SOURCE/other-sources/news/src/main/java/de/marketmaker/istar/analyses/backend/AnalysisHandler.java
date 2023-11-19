/*
 * AnalysisHandler.java
 *
 * Created on 23.03.12 15:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

/**
 * used for DAO callbacks when polling the DB
 *
 * @author oflege
 */
public interface AnalysisHandler {

    void handle(Protos.Analysis builder);

}
