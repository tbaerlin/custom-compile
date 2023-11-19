using System;
using System.IO;
using System.ComponentModel;
using System.Reflection;

namespace bew {
    public static class Program {
        private const string Bwi = "/bwi=";
        private const string Bwjid = "/bwjid=";
        private const string Bwo = "/bwo=";
        private const string Bwc = "/bwc=";
        private const string Bwm = "/bwm=";
        private const string Bwu = "/url=";
        private const string Lic = "/lic=";
        private const string Cus = "/cus=";

        private static volatile bool _failed;

        /// <summary>
        /// Der Haupteinstiegspunkt für die Anwendung.
        /// </summary>
        [STAThread]
        private static int Main(string[] args) {
            BewTask task = CreateTask(args);
            if (task == null) {
                Usage();
                return 1;
            }
            
            if (String.IsNullOrEmpty(ConfigSupport.FromConfig("customer"))
                || String.IsNullOrEmpty(ConfigSupport.FromConfig("license"))) {
                Console.Error.WriteLine("No license information found, please contact BEW support");
                return 1;
            }


            var bw = new BackgroundWorker {WorkerReportsProgress = true};
            bw.DoWork += (s, e) => task.Run(s as BackgroundWorker, e);
            bw.ProgressChanged += (s, e) => {
                                      var msg = (string) (e.UserState);
                                      if (!string.IsNullOrEmpty(msg)) {
                                          Console.WriteLine(msg);
                                      }
                                  };
            bw.RunWorkerCompleted += (s, e) => {
                                         if (e.Error != null) {
                                             Console.Error.WriteLine("Evaluation failed: " + e.Error.Message);
                                             _failed = true;
                                         }
                                         else {
                                             Console.WriteLine("Evaluation completed");
                                         }
                                     };

            bw.RunWorkerAsync();
            do {
                System.Threading.Thread.Sleep(1000);
            } while (bw.IsBusy);

            return _failed ? 2 : 0;
        }

        private static void Usage() {
            Console.Error.WriteLine();
            Console.Error.WriteLine("Usage: BewConsole.exe <options>");
            Console.Error.WriteLine(" options are (+=required. ?=optional)");
            Console.Error.WriteLine(" + /bwi=<filename> -- name of local request file");
            Console.Error.WriteLine(" ? /bwc=<filename> -- name of local mapping file (symbol to isin)");
            Console.Error.WriteLine(" + /bwo=<filename> -- name of result file");
            Console.Error.WriteLine(" ? /bwm=<filename> -- name of file with messages");
            Console.Error.WriteLine(" ? /lic=<license>  -- overrides license from license.txt");
            Console.Error.WriteLine(" ? /cus=<customer> -- overrides customer from license.txt");
            Console.Error.WriteLine();
            Console.Error.WriteLine("BewConsole returns the following error codes");
            Console.Error.WriteLine(" 0 -- success");
            Console.Error.WriteLine(" 1 -- invalid parameters");
            Console.Error.WriteLine(" 2 -- internal error");
            Console.Error.WriteLine();
            Console.Error.WriteLine("Version: " + Assembly.GetExecutingAssembly().GetName().Version);
        }

        private static Boolean CanRead(String name) {
            if (!File.Exists(name)) {
                Console.Error.WriteLine("Not readable: " + Path.GetFullPath(name));
                return false;
            }
            return true;
        }

        private static Boolean CanWrite(String name) {
            var path = Path.GetDirectoryName(name);
            if (path == null || !Directory.Exists(path)) {
                Console.Error.WriteLine("No such directory: " + path);
                return false;
            }
            if (Directory.Exists(name)) {
                Console.Error.WriteLine("Is a directory " + name);
                return false;
            }
            return true;
        }

        private static String GetFullPath(String name) {
            return Path.GetFullPath(name);
        }

        private static BewTask CreateTask(string[] args) {
            var task = new BewTask();
            var i = 0;
            while (i < args.Length) {
                if (args[i].StartsWith(Bwi)) {
                    task.Request = GetFullPath(args[i].Substring(Bwi.Length));
                    if (!CanRead(task.Request)) {
                        return null;
                    }
                }
                else if (args[i].StartsWith(Bwo)) {
                    task.Response = GetFullPath(args[i].Substring(Bwo.Length));
                    if (!CanWrite(task.Response)) {
                        return null;
                    }
                }
                else if (args[i].StartsWith(Bwc)) {
                    task.Mapping = GetFullPath(args[i].Substring(Bwc.Length));
                    if (!CanRead(task.Mapping)) {
                        return null;
                    }
                }
                else if (args[i].StartsWith(Bwm)) {
                    task.Messages = GetFullPath(args[i].Substring(Bwm.Length));
                    if (!CanWrite(task.Messages)) {
                        return null;
                    }
                }
                else if (args[i].StartsWith(Bwjid)) {
                    task.JobId = args[i].Substring(Bwjid.Length);
                }
                else if (args[i].StartsWith(Bwu)) {
                    BewTask.Url = new Uri(args[i].Substring(Bwu.Length));
                }
                else if (args[i].StartsWith(Lic)) {
                    ConfigSupport.Add("license", args[i].Substring(Lic.Length));
                }
                else if (args[i].StartsWith(Cus)) {
                    ConfigSupport.Add("customer", args[i].Substring(Cus.Length));
                }
                else {
                    if (!"/?".Equals(args[i]) && !"/h".Equals(args[i])) {
                        Console.Error.WriteLine("Invalid parameter " + args[i]);
                    }
                    return null;
                }
                i++;
            }

            return IsValid(task) ? task : null;
        }

        private static bool IsValid(BewTask task) {
            bool valid = true;
            if (task.Request == null && task.JobId == null) {
                Console.Error.WriteLine("parameter /bwi is missing");
                valid = false;
            }
            if (task.Response == null) {
                Console.Error.WriteLine("parameter /bwo is missing");
                valid = false;
            }
            return valid;
        }
    }
}