Extend EOD price history with new ADF price fields:
1) extend eod_history.proto with new ADF field by appending one field like following:

    optional    bytes       adf_1398    = 51;

with field id incremented. (Appending to the end of EodPrice, since the ordering is critical)

2) run protoc.exe to re-generate ProtoBuf classes
3) update merger.jar for the following projects:
    3.1) istar-eodhistory-controller
    3.2) istar-eodhistory-provider
    3.3) dm-xml web application
    3.4) istar-history-mdp
4) make sure one can query the field "adf_1398" from MDP-View V_EOD
5) update SQL queries in all properties files /home/confadm/produktion/mdp/istar-history-mdp/conf
on config machine by adding the new ADF field "adf_1398". Synchronize the configuration to the
pro-machines where the MDP-export process for EOD history is configured.
6) if historic data for the extended field exist:
    6.1) if not large, produce EOD_S using manual trigger
    6.2) if large, produce EOD_E using manual trigger
then merge the produced extension file onto eod-history using EodPriceHistoryISM:
java -cp 'lib/*' de.marketmaker.istar.merger.provider.history.eod.write.EodPriceHistoryISM {eod_dir} {hist_file}

example:
java -cp 'lib/*' de.marketmaker.istar.merger.provider.history.eod.write.EodPriceHistoryISM ~/produktion/var/data/eod/sbs/ ~/tmp/eod_s_20140710-20140714.buf.gz

7) execute JMX operation on EodTermRepo after eod_meta.properties file is updated with new mappings
for the new ADF field with its corresponding quotedef.