using System;
using System.ComponentModel;
using System.IO;
using System.Windows.Forms;

namespace bew {
    public partial class Form1 : Form {
        private const string DefaultStatusText =
            "Please fill in the Form and press the Submit-Button";

        private BewTask _task;

        public Form1() {
            InitializeComponent();
            toolStripStatusLabel1.Text = DefaultStatusText;
            Icon = Properties.Resources.vwd;

            requestfile.Text = ConfigSupport.FromConfig(ConfigSupport.KeyRequestfile);
            mappingfile.Text = ConfigSupport.FromConfig(ConfigSupport.KeyMappingfile);
            responsefile.Text = ConfigSupport.FromConfig(ConfigSupport.KeyResponsefile);
            messagefile.Text = ConfigSupport.FromConfig(ConfigSupport.KeyMessagefile);

            string proxy = ConfigSupport.FromConfig(ConfigSupport.KeyProxy, ConfigSupport.ValueProxyDefault);
            if (ConfigSupport.ValueProxyCustom.Equals(proxy)) {
                proxyCustom.Checked = true;
            }
            else if (ConfigSupport.ValueProxyNone.Equals(proxy)) {
                proxyNone.Checked = true;
            }
            else {
                proxyDefault.Checked = true;
            }
            proxyAddress.Text = ConfigSupport.FromConfig(ConfigSupport.KeyProxyaddress);
            proxyPort.Text = ConfigSupport.FromConfig(ConfigSupport.KeyProxyport);

            proxyUser.Text = ConfigSupport.FromConfig(ConfigSupport.KeyProxyUser);
            proxyPassword.Text = ConfigSupport.FromConfig(ConfigSupport.KeyProxyPassword);
            proxyDomain.Text = ConfigSupport.FromConfig(ConfigSupport.KeyProxyDomain);

            EnableSubmitIfValid();
        }

        private void request_Click(object sender, EventArgs e) {
            openFileDialog1.FileName = "request.txt";
            if (openFileDialog1.ShowDialog() == DialogResult.OK) {
                requestfile.Text = openFileDialog1.FileName;
            }
        }

        private void result_Click(object sender, EventArgs e) {
            saveFileDialog1.FileName = "result.csv";
            if (saveFileDialog1.ShowDialog() == DialogResult.OK) {
                responsefile.Text = saveFileDialog1.FileName;
            }
        }

        private void mapping_Click(object sender, EventArgs e) {
            openFileDialog1.FileName = "mappings.txt";
            if (openFileDialog1.ShowDialog() == DialogResult.OK) {
                mappingfile.Text = openFileDialog1.FileName;
            }
        }

        private void message_Click(object sender, EventArgs e) {
            saveFileDialog1.FileName = "messages.txt";
            if (saveFileDialog1.ShowDialog() == DialogResult.OK) {
                messagefile.Text = saveFileDialog1.FileName;
            }
        }

        private void cancel_Click(object sender, EventArgs e) {
            saveSettings();
            Close();
        }

        private void select_Click(object sender, EventArgs e) {
            if (_task != null) {
                backgroundWorker.CancelAsync();
                this.Text = "Close";
            }

            _task = new BewTask {
                                    Request = requestfile.Text,
                                    Mapping = mappingfile.Text,
                                    Response = responsefile.Text,
                                    Messages = messagefile.Text
                                };
            backgroundWorker.RunWorkerAsync(_task);
            submitButton.Text = "Cancel";
            closeButton.Enabled = false;
        }

        private void saveSettings() {
            ConfigSupport.Add(ConfigSupport.KeyRequestfile, requestfile.Text);
            ConfigSupport.Add(ConfigSupport.KeyMappingfile, mappingfile.Text);
            ConfigSupport.Add(ConfigSupport.KeyResponsefile, responsefile.Text);
            ConfigSupport.Add(ConfigSupport.KeyMessagefile, messagefile.Text);
            try {
                ConfigSupport.Save();
            }
            catch (Exception e) {
                MessageBox.Show("Save settings failed: " + e.Message, "ERROR", MessageBoxButtons.OK);                
            }
        }

        private void requestfile_TextChanged(object sender, EventArgs e) {
            EnableSubmitIfValid();
        }

        private void responsefile_TextChanged(object sender, EventArgs e) {
            EnableSubmitIfValid();
        }

        private void EnableSubmitIfValid() {
            submitButton.Enabled = (!string.IsNullOrEmpty(requestfile.Text) && File.Exists(requestfile.Text))
                                   && !string.IsNullOrEmpty(responsefile.Text);
        }

        public void UpdateProgress(int m) {
            UpdateProgress(m, null);
        }

        public void UpdateProgress(int m, string status) {
            toolStripProgressBar1.Enabled = (m >= 0);
            toolStripProgressBar1.Value = m > 0 ? m : 0;
            UpdateStatus(status);
        }

        public void UpdateStatus(string status) {
            if (status != null) {
                this.toolStripStatusLabel1.Text = status;
            }
        }

        private void backgroundWorker_DoWork(object sender, DoWorkEventArgs e) {
            ((BewTask) e.Argument).Run(backgroundWorker, e);
        }

        private void backgroundWorker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e) {
            if (e.Cancelled) {
                MessageBox.Show("Evaluation has been cancelled", "Info");
            }
            else if (e.Error != null) {
                MessageBox.Show("Evaluation failed: " + e.Error.Message, "Error");
            }
            else {
                MessageBox.Show("Evaluation completed", "Info");
            }
            _task = null;
            toolStripProgressBar1.Value = 0;
            toolStripStatusLabel1.Text = DefaultStatusText;
            submitButton.Text = "Submit";
            closeButton.Enabled = true;
        }

        private void backgroundWorker_ProgressChanged(object sender, ProgressChangedEventArgs e) {
            UpdateProgress(e.ProgressPercentage, (string) e.UserState);
        }

        private void proxyCustom_CheckedChanged(object sender, EventArgs e) {
            proxyAddress.Enabled = proxyCustom.Checked;
            proxyPort.Enabled = proxyCustom.Checked;
            if (proxyCustom.Checked) UpdateConfig();
        }

        private void proxyNone_CheckedChanged(object sender, EventArgs e) {
            if (proxyNone.Checked) UpdateConfig();
        }

        private void proxyDefault_CheckedChanged(object sender, EventArgs e) {
            if (proxyDefault.Checked) UpdateConfig();
        }

        private void UpdateConfig() {
            if (proxyDefault.Checked) {
                ConfigSupport.Add(ConfigSupport.KeyProxy, ConfigSupport.ValueProxyDefault);
            }
            else if (proxyNone.Checked) {
                ConfigSupport.Add(ConfigSupport.KeyProxy, ConfigSupport.ValueProxyNone);
            }
            else if (proxyCustom.Checked) {
                ConfigSupport.Add(ConfigSupport.KeyProxy, ConfigSupport.ValueProxyCustom);
            }
        }

        private void proxyPort_Leave(object sender, EventArgs e) {
            ConfigSupport.Add(ConfigSupport.KeyProxyport, proxyPort.Text);
        }

        private void proxyAddress_Leave(object sender, EventArgs e) {
            ConfigSupport.Add(ConfigSupport.KeyProxyaddress, proxyAddress.Text);
        }

        private void proxyUser_Leave(object sender, EventArgs e)
        {
            ConfigSupport.Add(ConfigSupport.KeyProxyUser, proxyUser.Text);
        }

        private void proxyPassword_Leave(object sender, EventArgs e)
        {
            ConfigSupport.Add(ConfigSupport.KeyProxyPassword, proxyPassword.Text);
        }

        private void proxyDomain_Leave(object sender, EventArgs e)
        {
            ConfigSupport.Add(ConfigSupport.KeyProxyDomain, proxyDomain.Text);
        }
    }
}