using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;

namespace bew {
    public static class ConfigSupport {
        public const string KeyRequestfile = "requestfile";
        public const string KeyMappingfile = "mappingfile";
        public const string KeyResponsefile = "responsefile";
        public const string KeyMessagefile = "messagefile";
        public const string KeyProxyport = "proxyPort";
        public const string KeyProxyaddress = "proxyAddress";
        public const string KeyProxy = "proxy";
        public const string KeyProxyUser = "proxyUser";
        public const string KeyProxyPassword = "proxyPassword.enc";
        public const string KeyProxyDomain = "proxyDomain";
        public const string ValueProxyDefault = "DEFAULT";
        public const string ValueProxyNone = "NONE";
        public const string ValueProxyCustom = "CUSTOM";


        private static IDictionary<string, string> _config;

        static ConfigSupport() {
            _config = ReadFrom(GetFilename("license.txt"));
        }

        private static string GetFilename(string s) {
            // return "d:\\temp\\bew.exe\\" + s;

            var path = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            return Path.Combine(path, s);
        }

        private static IDictionary<string, string> ReadFrom(string file) {
            IDictionary<string, string> result = new Dictionary<string, string>();
            if (!File.Exists(file)) {
                return result;
            }
            string line;
            using (var reader = File.OpenText(file)) {
                while ((line = reader.ReadLine()) != null) {
                    if (!string.IsNullOrEmpty(line) && !line.StartsWith("#")) {
                        var strings = line.Split(new[] {'='}, 2);
                        if (strings.Length == 2) {
                            result.Add(strings[0].Trim(), strings[1].Trim());
                        }
                    }
                }
            }
            return result;
        }

        public static void Save() {
            string filename = GetFilename("license.txt.new");
            using (StreamWriter writer = File.CreateText(filename)) {
                IDictionary<string, string> copy = new SortedDictionary<string, string>(_config);
                writer.WriteLine("customer=" + GetAndRemove(copy, "customer"));
                writer.WriteLine("license=" + GetAndRemove(copy, "license"));
                writer.WriteLine();
                writer.WriteLine("#-Settings-");
                foreach (var key in copy.Keys) {
                    writer.WriteLine(key + "=" + copy[key]);
                }
            }
            File.Replace(filename, GetFilename("license.txt"), GetFilename("license.txt.bak"));
        }

        private static string GetAndRemove(IDictionary<string, string> d, string key) {
            string result = "";
            if (d.ContainsKey(key)) {
                result = d[key];
                d.Remove(key);
            }
            return result;
        }

        public static void Add(string name, string value) {
            if (name.EndsWith(".enc")) {
                Add(name.Substring(0, name.Length - 4), Encrypt(value));
                return;
            }
            if (string.IsNullOrEmpty(value)) {
                _config.Remove(name);
            }
            else {
                _config[name] = value;
            }
        }

        private static String Encrypt(string value) {
            DESCryptoServiceProvider des = CreateDesCryptoServiceProvider();
            ICryptoTransform encryptor = des.CreateEncryptor();
            byte[] valueBytes = new UTF8Encoding().GetBytes(value);
            byte[] enc = encryptor.TransformFinalBlock(valueBytes, 0, valueBytes.Length);
            return Convert.ToBase64String(enc);
        }

        private static DESCryptoServiceProvider CreateDesCryptoServiceProvider() {
            byte[] key = GetDesKey();
            return new DESCryptoServiceProvider {Key = key, IV = key};
        }

        private static String Decrypt(string value) {
            DESCryptoServiceProvider des = CreateDesCryptoServiceProvider();
            ICryptoTransform decryptor = des.CreateDecryptor();            
            byte[] valueBytes = Convert.FromBase64String(value);
            byte[] enc = decryptor.TransformFinalBlock(valueBytes, 0, valueBytes.Length);
            return new UTF8Encoding().GetString(enc);
        }

        private static byte[] GetDesKey() {
            string lic = FromConfig("license");
            byte[] bytes = new UTF8Encoding().GetBytes(lic);
            byte[] result = new byte[8];
            Array.Copy(bytes, 0, result, 0, 8);
            return result;
        }

        public static string FromConfig(string name) {
            return FromConfig(name, "");
        }

        public static string FromConfig(string name, string defaultValue) {
            if (name.EndsWith(".enc")) {
                string encrypted = FromConfig(name.Substring(0, name.Length - 4), defaultValue);
                if (encrypted != defaultValue) {
                    return Decrypt(encrypted);
                }
                return defaultValue;
            }
            if (_config.ContainsKey(name)) {
                return _config[name];
            }
            return defaultValue;
        }
    }
}