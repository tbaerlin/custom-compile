#region

using System;
using System.Collections.Specialized;
using System.Globalization;
using System.IO;
using System.Net;
using System.Text;
using Microsoft.Win32;

#endregion

namespace bew {
    /// <summary>
    /// This class contains methods excepted from Salient.Web.HttpLib.HttpRequestUtility
    /// for demonstration purposes. Please see http://salient.codeplex.com for full 
    /// implementation
    /// </summary>
    public static class Upload {
        public struct FileInfo {
            public readonly string Name;
            public readonly string ContentType;
            public readonly string ParameterName;

            public FileInfo(string name, string contentType, string parameterName) {
                Name = name;
                ContentType = contentType;
                ParameterName = parameterName;
            }
        }

        /// <summary>
        /// Uploads a stream using a multipart/form-data POST.
        /// </summary>
        /// <param name="requestUri"></param>
        /// <param name="postData">A NameValueCollection containing form fields 
        /// to post with file data</param>
        /// <param name="cookies">Optional, can pass null. Used to send and retrieve cookies. 
        /// Pass the same instance to subsequent calls to maintain state if required.</param>
        /// <param name="headers">Optional, headers to be added to request.</param>
        /// <param name="fileInfos">Files to be submitted as part of the post request</param>
        /// <returns></returns>
        /// Reference: 
        /// http://tools.ietf.org/html/rfc1867
        /// http://tools.ietf.org/html/rfc2388
        /// http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2
        public static WebResponse PostFile
            (Uri requestUri, NameValueCollection postData, CookieContainer cookies,
             NameValueCollection headers, params FileInfo[] fileInfos) {
            // var webrequest = (HttpWebRequest) WebRequest.Create(requestUri);
            var webrequest = (HttpWebRequest) BewTask.CreateRequest(requestUri, cookies);

            webrequest.Method = "POST";
            SetHeaders(headers, webrequest);
            if (fileInfos == null) throw new ArgumentNullException("fileInfos");

            var boundary = "----------" + DateTime.Now.Ticks.ToString("x", CultureInfo.InvariantCulture);

            webrequest.ContentType = "multipart/form-data; boundary=" + boundary;

            using (var ms = new MemoryStream(1024 * 256)) {
                Append(ms, EncodePostData(postData, boundary));

                Array.ForEach(fileInfos, info => AppendFile(ms, info, boundary));

                Append(ms, Encoding.ASCII.GetBytes("--" + boundary + "--\r\n"));

                webrequest.ContentLength = ms.Length;

                using (Stream requestStream = webrequest.GetRequestStream()) {
                    ms.WriteTo(requestStream);
                    return webrequest.GetResponse();
                }
            }
        }

        private static void AppendFile(MemoryStream ms, FileInfo info, string boundary) {
            var fileName = info.Name;
            var fileFieldName = info.ParameterName;

            var data = File.ReadAllBytes(fileName);

            var fileContentType = GetContentType(info.ContentType, fileName);

            fileFieldName = string.IsNullOrEmpty(fileFieldName) ? "file" : fileFieldName;
            var sb = new StringBuilder(200);
            sb.AppendFormat("--{0}\r\n", boundary);
            sb.AppendFormat("Content-Disposition: form-data; name=\"{0}\"; {1}\r\n", fileFieldName,
                            string.IsNullOrEmpty(fileName)
                                ? ""
                                : string.Format(CultureInfo.InvariantCulture,
                                                "filename=\"{0}\";", Path.GetFileName(fileName)));

            sb.AppendFormat("Content-Type: {0}\r\n\r\n", fileContentType);
            Append(ms, Encoding.UTF8.GetBytes(sb.ToString()));
            Append(ms, data);
            Append(ms, Encoding.UTF8.GetBytes("\r\n"));
        }

        private static void SetHeaders(NameValueCollection headers, WebRequest webrequest) {
            if (headers != null) {
                Array.ForEach(headers.AllKeys, key => SetHeader(key, headers.GetValues(key), webrequest));
            }
        }

        private static void SetHeader(string key, string[] values, WebRequest webrequest) {
            if (values != null) {
                Array.ForEach(values, v => webrequest.Headers.Add(key, v));                
            }
        }

        private static string GetContentType(string fileContentType, string fileName) {
            string ctype;
            return string.IsNullOrEmpty(fileContentType)
                       ? (TryGetContentType(fileName, out ctype) ? ctype : "application/octet-stream")
                       : fileContentType;
        }

        private static void Append(MemoryStream ms, byte[] data) {
            ms.Write(data, 0, data.Length);
        }

        private static byte[] EncodePostData(NameValueCollection postData, string boundary) {
            if (postData == null) return new byte[0];

            var sb = new StringBuilder(1024);

            foreach (string key in postData.AllKeys) {
                string[] values = postData.GetValues(key);
                if (values != null)
                    foreach (string value in values) {
                        sb.AppendFormat("--{0}\r\n", boundary);
                        sb.AppendFormat("Content-Disposition: form-data; name=\"{0}\";\r\n\r\n{1}\r\n", key,
                                        value);
                    }
            }
            return Encoding.UTF8.GetBytes(sb.ToString());
        }

        /// <summary>
        /// Attempts to query registry for content-type of supplied file name.
        /// </summary>
        /// <param name="fileName"></param>
        /// <param name="contentType"></param>
        /// <returns></returns>
        public static bool TryGetContentType(string fileName, out string contentType) {
            try {
                var key = Registry.ClassesRoot.OpenSubKey(@"MIME\Database\Content Type");

                if (key != null) {
                    foreach (string keyName in key.GetSubKeyNames()) {
                        RegistryKey subKey = key.OpenSubKey(keyName);
                        if (subKey != null) {
                            var subKeyValue = (string) subKey.GetValue("Extension");

                            if (!string.IsNullOrEmpty(subKeyValue)) {
                                if (string.Compare(Path.GetExtension(fileName).ToUpperInvariant(),
                                                   subKeyValue.ToUpperInvariant(), StringComparison.OrdinalIgnoreCase) ==
                                    0) {
                                    contentType = keyName;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
                // ReSharper disable EmptyGeneralCatchClause
            catch {
                // fail silently
                // TODO: rethrow registry access denied errors
            }
            // ReSharper restore EmptyGeneralCatchClause
            contentType = "";
            return false;
        }
    }
}