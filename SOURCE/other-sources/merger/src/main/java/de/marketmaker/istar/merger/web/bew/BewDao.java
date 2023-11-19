/*
 * BewDao.java
 *
 * Created on 05.10.2010 15:13:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.util.List;

import org.joda.time.DateTime;

/**
 * @author oflege
 */
public interface BewDao {
    /**
     * Creates a new task with a unique task id
     * @param customer customer name
     * @return taskId to be used for subsequent calls of {@link #ackTaskProgress(int, int)} and
     * {@link #addItems(int, java.util.List)}
     */
    int createTask(String customer);

    /**
     * Stores information for the given items under the given taskId;
     * @param taskId identifies task the items belong to
     * @param items to be stored
     */
    void addItems(int taskId, List<ResultItem> items);

    /**
     * @param taskId identifies task
     * @return info about the task or null if no such info is available
     */
    TaskInfo getTask(int taskId);

    /**
     * Acknowledges progress of task with the given id; if the task failed, all symbol data stored
     * for the task will be deleted.
     * @param taskId identifies task
     * @param percentage a negative value indicates failure, 100 indicates completion, values between
     * 0..99 indicate that processing is under way
     */
    void ackTaskProgress(int taskId, int percentage);

    /**
     * Returns infos about completed tasks in the past.
     * @param customer if not null, return only TaskInfos for this customer
     * @param from earliest request date (incl)
     * @param to latest request date (excl)
     * @return infos for tasks between from and to for customer, or, if customer is null, all customers
     */
    List<TaskInfo> getTasks(String customer, DateTime from, DateTime to);

    /**
     * Returns info about symbols requested and returned for a given task
     * @param taskId identifies task
     * @return infos for all symbols that were part of the task
     */
    List<SymbolInfo> getSymbols(int taskId);

    /**
     * Returns a list of all customer names
     * @return customer names
     */
    List<String> getCustomerNames();
}
