using System;
using System.ComponentModel;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Net;
using System.IO;

namespace bew {
    public class BewTask {
        public static Uri Url
            = new Uri("https://bew.vwd.com/dmxml-1/bew/submit.bew");

        private readonly CookieContainer _cookieContainer = new CookieContainer();

        private BackgroundWorker _worker;

        private DoWorkEventArgs _eventArgs;

        public string Request { get; set; }

        public string Mapping { get; set; }

        public string Response { get; set; }

        public string Messages { get; set; }

        public string JobId { get; set; }

        public void Run(BackgroundWorker worker, DoWorkEventArgs e) {
            _worker = worker;
            _eventArgs = e;

            if (JobId == null) {
                worker.ReportProgress(5, "Submitting Files");
                JobId = DoSubmit();

                int ready = WaitForResult();
                if (CancellationPending()) return;

                if (ready < 0) {
                    throw new Exception("Server failed to create results");
                }
            }
            DownloadResults();
            worker.ReportProgress(100, "Result is available");
            e.Result = "Success";
        }

        private bool CancellationPending() {
            if (_worker.CancellationPending) {
                _eventArgs.Cancel = true;
                return true;
            }
            return false;
        }

        private void DownloadResults() {
            _worker.ReportProgress(90, "Downloading Result");
            DownloadResult(Response);
            DownloadResult(Messages);
        }

        private void DownloadResult(string filename) {
            if (string.IsNullOrEmpty(filename)) return;

            string suffix = filename.Equals(Messages) ? "&messages=true" : "";
            WebRequest request = CreateRequest(new Uri(Url, "result.bew?jobId=" + JobId + Credentials() + suffix), _cookieContainer);
            var tmpfilename = filename + ".tmp";
            using (WebResponse response = request.GetResponse())
            using (Stream output = File.Open(tmpfilename, FileMode.Create)) {
                CopyStream(response.GetResponseStream(), output);
            }

            if (File.Exists(filename)) {
                File.Replace(tmpfilename, filename, filename + ".bak");
            }
            else {
                File.Move(tmpfilename, filename);
            }
        }

        private int WaitForResult() {
            _worker.ReportProgress(10, "Waiting for Result");

            var statusUri = new Uri(Url, "query.bew?jobId=" + JobId + Credentials());            
            int ready;
            do {
                if (CancellationPending()) return 0;
                System.Threading.Thread.Sleep(1000);
                ready = int.Parse(QueryStatus(statusUri));
                _worker.ReportProgress(10 + (ready * 80 / 100), null);
            } while (ready >= 0 && ready < 100);
            return ready;
        }

        public static WebRequest CreateRequest(Uri uri, CookieContainer cookies) {
            HttpWebRequest result = (HttpWebRequest) WebRequest.Create(uri);
            result.CookieContainer = cookies;

            string proxy = ConfigSupport.FromConfig(ConfigSupport.KeyProxy);
            
            if (ConfigSupport.ValueProxyNone.Equals(proxy)) {
                // a null value would be interpreted to use the DefaultWebProxy!
                result.Proxy = new WebProxy();
            }
            else if (ConfigSupport.ValueProxyCustom.Equals(proxy)) {
                result.Proxy = new WebProxy(ConfigSupport.FromConfig(ConfigSupport.KeyProxyaddress),
                                            int.Parse(ConfigSupport.FromConfig(ConfigSupport.KeyProxyport, "80")));
            }
            else {
                result.Proxy = WebRequest.DefaultWebProxy;
            }
            String username = ConfigSupport.FromConfig(ConfigSupport.KeyProxyUser, null);
            String password = ConfigSupport.FromConfig(ConfigSupport.KeyProxyPassword, null);
            String domain = ConfigSupport.FromConfig(ConfigSupport.KeyProxyDomain, null);
            if (!String.IsNullOrEmpty(username) && !String.IsNullOrEmpty(password)) {
                NetworkCredential credential = String.IsNullOrEmpty(domain)
                                                   ? new NetworkCredential(username, password)
                                                   : new NetworkCredential(username, password, domain);
                result.Proxy.Credentials = credential;
            }
            return result;
        }

        private static string Credentials() {
            return "&customer=" + ConfigSupport.FromConfig("customer")
                   + "&license=" + ConfigSupport.FromConfig("license");
        }

        private string QueryStatus(Uri statusUri) {
            using (var r = CreateRequest(statusUri, _cookieContainer).GetResponse()) {
                return ReadResponse(r);
            }
        }

        private string DoSubmit() {
            NameValueCollection postData = GetPostData();
            List<Upload.FileInfo> infos = GetFileInfos();

            var uri = new Uri(Url, "submit.bew");
            using (var r = Upload.PostFile(uri, postData, _cookieContainer, null, infos.ToArray())) {
                return ReadResponse(r);
            }
        }

        private static string ReadResponse(WebResponse r) {
            using (var s = r.GetResponseStream()) {
                using (var sr = new StreamReader(s)) {
                    return sr.ReadToEnd().Trim();
                }
            }
        }

        private List<Upload.FileInfo> GetFileInfos() {
            var result = new List<Upload.FileInfo> {new Upload.FileInfo(Request, null, "request")};
            if (!string.IsNullOrEmpty(Mapping)) {
                result.Add(new Upload.FileInfo(Mapping, null, "mappings"));
            }
            return result;
        }

        private static NameValueCollection GetPostData() {
            return new NameValueCollection {
                {"debug", "true"},
                {"version", System.Reflection.Assembly.GetExecutingAssembly().GetName().Version.ToString()},
                {"customer", ConfigSupport.FromConfig("customer")},
                {"license", ConfigSupport.FromConfig("license")},
            };
        }

        private static void CopyStream(Stream input, Stream output) {
            var buffer = new byte[32768];
            int read;
            while ((read = input.Read(buffer, 0, buffer.Length)) > 0) {
                output.Write(buffer, 0, read);
            }
        }
    }
}