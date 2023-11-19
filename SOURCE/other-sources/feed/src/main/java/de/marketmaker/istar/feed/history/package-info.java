package de.marketmaker.istar.feed.history;
/**
 * <p>
 * Provides the classes necessary to:
 * <ul>
 *     <li>write and read history files including indexes</li>
 *     <li>produce history file on daily basis or history corrections in patch files</li>
 *     <li>merge small unit of history files onto history files of bigger unit</li>
 *     <li>gather history information from all available history unit files</li>
 * </ul>
 * </p>
 * <p>
 * History file supported by this package has the following structure:
 * <pre>
 *     data> |history-data for key 1|history-data for key 2|...|history-data for key n|
 *     keys> |key 1|offset-length 1|key 2|offset-length 2|...|key n|offset-length n|
 *    index> |B* tree index built from keys|
 *           |start of index|lengthBits|
 * </pre>
 * where lengthBits is used for encoding length, Long.SIZE-lengthBits for offset(thus one long).
 * Encoding and decoding are supported by {@link de.marketmaker.istar.common.io.OffsetLengthCoder}.
 * Writing and reading of such history files are supported by {@link HistoryWriter}
 * and {@link HistoryReader} respectively.
 * </p>
 * <p>
 * Specifically ick history data are organized by the following structure:
 * <pre>
 *     |days from begin (short)|tick number(short)|data length(short)|data(denoted by data length)|
 *     |days-1 from begin (short)|tick number(short)|data length(short)|data(denoted by data length)|
 *     ...
 * </pre>
 * As depicted, history data are stored backward.
 * </p>
 * <p>History consists of different units, which are defined in {@link HistoryUnit}. Only history
 * files for {@link HistoryUnit.Day} and {@link HistoryUnit.Change} can be produced directly. The
 * accumulating of those files will result in other history files of bigger units. All history unit
 * files have the same structure as described above. History unit file is identified by its file
 * extension. A prefix is used to further differentiate the content of such unit files.
 * </p>
 * <p>
 * The creation of daily and change files is application specific. For tick history please see
 * {@link MinuteTicker} and {@link TickPatcher}.
 * </p>
 * <p>
 * The management of history units is done within an instance of {@link HistoryArchive}. After the
 * creation of the smallest unit(Day, Change), they are merged onto bigger units' of history files.
 * <ul>
 *     <li>daily updates are played onto the current month file using fast-forward strategy.
 *     {@link EntryMerger.FF}</li>
 *     <li>changes are played onto the current patch using patch merger. {@link PatchMerger}</li>
 *     <li>depends on concrete implementation of {@link HistoryArchive}, month file(and patch file
 *     if exists) is merged onto year file using history merger. {@link HistoryMerger}. Note that
 *     this only happens on new month begin, or new year begin, new decade begin, etc..</li>
 * </ul>
 * Through such merge operation it is assured that history data for one key for a given time period
 * in one unit file are always stored continuously, so that they can be read in one operation. Note
 * that each merge operation creates new unit file with updated end-date. After successful merging,
 * older unit files are removed and <code>working_unit.txt</code> is updated with available history
 * units, so that reader can switch to the up-to-date unit files.
 * </p>
 * <p>
 * To query history data for a given key from all available history units, different instances of
 * {@link HistoryGatherer} can be used. Such gatherer has access to all unit files. If patch files
 * exist, they are read and will be played onto the query result. By same date, data from patch files
 * will take precedence. Access of gatherer is synchronized among all query operations and update
 * operation, performed by an instance of {@link de.marketmaker.istar.common.monitor.ActiveMonitor},
 * which detects changes in the <code>working_units.txt</code>.
 * </p>
 */