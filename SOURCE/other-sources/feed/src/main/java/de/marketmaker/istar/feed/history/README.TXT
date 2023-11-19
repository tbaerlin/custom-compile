Tick History Usage:

1) daily export:
java -cp 'lib/*' de.marketmaker.istar.feed.history.TickHistoryTrigger {tick_dir} {target_dir} {negative_tick_markets_file} {tick_types} [markets_filter]

example:
java -cp 'lib/*' de.marketmaker.istar.feed.history.TickHistoryTrigger /export/share/tick1/tdz/data/20210624 /export/share/hist1/data/ /home/meadm/produktion/prog/istar-tickhistory/conf/market-negative-tick.txt TRADE,BID,ASK,SYNTHETIC_TRADE /home/meadm/produktion/prog/istar-tickhistory/conf/future-markets.txt

2) correction of ticks for specific market(s), for one date:
2.1) create a markets_filter.txt file with those specific markets to correct, one market symbol each line
2.2) create a folder with one date, i.e. 20210624 under /export/share/hist1/data/days/, then create sub-folders for each tick type: ask, bid, trade, synthetic_trade
2.3) create history file using:
 java -cp 'lib/*' de.marketmaker.istar.feed.history.TickHistoryTrigger {tick_dir} {target_dir} {negative_tick_markets_file} {tick_types} {future-markets_file} markets_filter.txt
2.4) merge history file using:
 java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileMerger {base_file} {delta_file} {result_file}
2.5) result file should be named {base_file}+extension, e.g. if ASK_20210601-20210627.month.month is the latest month file for June 2021, the new result file should be named ASK_20210601-20210627.month.month
2.5) verify the result file containing the correct tick data using:
 java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileReader /export/share/hist1/data/correction/ask/ASK_20210601-20210627.month.month
2.6) move the result file to production folder
2.7) touch working_units.txt to inform tick history reader to read the newly corrected file
2.8) repeat step 2.4) to 2.6) for all tick types

example:
2.2)
mkdir -p /export/share/hist1/data/days/20210624/trade
mkdir -p /export/share/hist1/data/days/20210624/ask
mkdir -p /export/share/hist1/data/days/20210624/bid
mkdir -p /export/share/hist1/data/days/20210624/synthetic_trade

2.3)
cd produktion/prog/istar-tickhistory
java -cp 'lib/*' de.marketmaker.istar.feed.history.TickHistoryTrigger /export/share/tick1/tdz/data/20210624/ /export/share/hist1/data/days/20210624/ ~/produktion/prog/istar-tickhistory/conf/market-negative-tick.txt ASK,BID,TRADE,SYNTHETIC_TRADE ~/produktion/prog/istar-tickhistory/conf/future-markets.txt /export/share/hist1/data/days/markets_filter.txt

2.4)
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileMerger /export/share/hist1/data/ask/ASK_20210601-20210627.month /export/share/hist1/data/days/20210624/ask/ASK_20210624-20210624.month  /export/share/hist1/data/correction/ask/ASK_20210601-20210627.month.month
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileMerger /export/share/hist1/data/bid/BID_20210601-20210627.month /export/share/hist1/data/days/20210624/bid/BID_20210624-20210624.month  /export/share/hist1/data/correction/bid/BID_20210601-20210627.month.month
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileMerger /export/share/hist1/data/synthetic_trade/SYNTHETIC_TRADE_20210601-20210627.month /export/share/hist1/data/days/20210624/synthetic_trade/SYNTHETIC_TRADE_20210624-20210624.month /export/share/hist1/data/correction/synthetic_trade/SYNTHETIC_TRADE_20210601-20210627.month.month
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileMerger /export/share/hist1/data/trade/TRADE_20210601-20210627.month /export/share/hist1/data/days/20210624/trade/TRADE_20210624-20210624.month /export/share/hist1/data/correction/trade/TRADE_20210601-20210627.month.month

2.5)
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileReader 20210624 /export/share/hist1/data/correction/ask/ASK_20210601-20210627.month.month
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileReader 20210624 /export/share/hist1/data/correction/bid/BID_20210601-20210627.month.month
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileReader 20210624 /export/share/hist1/data/correction/synthetic_trade/SYNTHETIC_TRADE_20210601-20210627.month.month
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryFileReader 20210624 /export/share/hist1/data/correction/trade/TRADE_20210601-20210627.month.month

2.6)
mv /export/share/hist1/data/correction/ask/ASK_20210601-20210627.month.month   /export/share/hist1/data/ask/
mv /export/share/hist1/data/correction/bid/BID_20210601-20210627.month.month   /export/share/hist1/data/bid/
mv /export/share/hist1/data/correction/synthetic_trade/SYNTHETIC_TRADE_20210601-20210627.month.month   /export/share/hist1/data/synthetic_trade/
mv /export/share/hist1/data/correction/trade/TRADE_20210601-20210627.month.month   /export/share/hist1/data/trade/

2.7)
touch /export/share/hist1/data/ask/working_units.txt
touch /export/share/hist1/data/trade/working_units.txt
touch /export/share/hist1/data/bid/working_units.txt
touch /export/share/hist1/data/synthetic_trade/working_units.txt

##############

Note: if the resulting day-files are not bigger than 50M and the corresponding patch files are not bigger than 50M,
consider merge the resulting day-files onto the patch files instead, which is faster. But pay attention to
 the naming convention of patch files. For example: with resulting day-file "TRADE_20140828-20140828.month"
 and patch file "1410341456644_TRADE_20140815-20140815.patch", name the merge-result-file
 "1410341456645_TRADE_20140815-20140828.patch" [millis+1]_TRADE_start_end.patch
 Then copy the merge result file into production folder and touch working-units.txt
 (If, however, patch files are not in use, just rename the resulting day-file to patch file using
  the convention and add "Patch" into working-units.txt)

Note: if the resulting day-files are not bigger than 50M and the corresponding patch files are not bigger than 50M,
consider merge the resulting day-files onto the patch files instead, which is faster. But pay attention to
 the naming convention of patch files. For example: with resulting day-file "TRADE_20140828-20140828.month"
 and patch file "1410341456644_TRADE_20140815-20140815.patch", name the merge-result-file
 "1410341456645_TRADE_20140815-20140828.patch" [millis+1]_TRADE_start_end.patch
 Then copy the merge result file into production folder and touch working-units.txt
 (If, however, patch files are not in use(working-units.txt does not contain Patch), just rename the
  resulting day-file to patch file using the convention and add "Patch" into working-units.txt)

3) correction of ticks for all markets, for one date:
similar as 2), only without markets_filter.txt

4) correction of ticks for specific symbols, for one date:
just use Tick Patcher
java -cp 'lib/*' de.marketmaker.istar.feed.history.TickHistoryPatcher {target_dir} {tick_dir} {file_market_neg} {tick_type} [symbol 1] [symbol 2] ...

example:
 java -cp 'lib/*' de.marketmaker.istar.feed.history.TickHistoryPatcher /export/share/tickhist2/data/trade /export/share/tick8/data/20131031 conf/market-negative-tick.txt TRADE 965238.DTB.CON

Note: only one tick type is allowed once

5) New history file requires one trailing bit denoting number of bits used encoding history entry length
to extend the older history file with this bit, use following command line:
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryWriter {path_to_history_file_folder} length

5.1) tick history file uses 24 bits to encode entry length
5.1) end history file uses 24 bits to encode entry length
5.3) trade screen history file uses 26 bits to encode entry length

example:
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryWriter /export/share/tickhist1/data/trade 24

Misc:
Usage for TickerJMXCli:
java -cp 'lib/*' de.marketmaker.istar.feed.history.TickerJMXCli {jmxUrl} {ObjectName} {path_to_share_folder}
java -cp 'lib/*' de.marketmaker.istar.feed.history.TickerJMXCli service:jmx:rmi://bustum.market-maker.de:9861/jndi/rmi://bustum.market-maker.de:9860/jmxrmi de.marketmaker.istar:type=TickHistoryController,name=tickHistorycontroller {path_to_share_folder}

6) delete aggregated ticks from tick history file for specific date(s) for specific symbols a/o markets
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryTickFilePurger {file to be purged} {purge result dir} {date(s)} {symbols a/o markets}

example:
java -cp 'lib/*' de.marketmaker.istar.feed.history.HistoryTickFilePurger /export/share/hist1/data/trade/TRADE_20170101-20170112.month /export/share/hist1/data/correction/trade 20170102 TK,NIKKEI