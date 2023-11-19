namespace bew
{
    partial class Form1
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Verwendete Ressourcen bereinigen.
        /// </summary>
        /// <param name="disposing">True, wenn verwaltete Ressourcen gelöscht werden sollen; andernfalls False.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Vom Windows Form-Designer generierter Code

        /// <summary>
        /// Erforderliche Methode für die Designerunterstützung.
        /// Der Inhalt der Methode darf nicht mit dem Code-Editor geändert werden.
        /// </summary>
        private void InitializeComponent()
        {
            this.selectRequest = new System.Windows.Forms.Button();
            this.openFileDialog1 = new System.Windows.Forms.OpenFileDialog();
            this.requestfile = new System.Windows.Forms.TextBox();
            this.responsefile = new System.Windows.Forms.TextBox();
            this.selectResult = new System.Windows.Forms.Button();
            this.submitButton = new System.Windows.Forms.Button();
            this.closeButton = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.mappingfile = new System.Windows.Forms.TextBox();
            this.selectMapping = new System.Windows.Forms.Button();
            this.groupBox4 = new System.Windows.Forms.GroupBox();
            this.messagefile = new System.Windows.Forms.TextBox();
            this.selectMessage = new System.Windows.Forms.Button();
            this.statusStrip1 = new System.Windows.Forms.StatusStrip();
            this.toolStripProgressBar1 = new System.Windows.Forms.ToolStripProgressBar();
            this.toolStripStatusLabel1 = new System.Windows.Forms.ToolStripStatusLabel();
            this.backgroundWorker = new System.ComponentModel.BackgroundWorker();
            this.saveFileDialog1 = new System.Windows.Forms.SaveFileDialog();
            this.groupBox5 = new System.Windows.Forms.GroupBox();
            this.proxyPort = new System.Windows.Forms.TextBox();
            this.proxyAddress = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.label1 = new System.Windows.Forms.Label();
            this.proxyCustom = new System.Windows.Forms.RadioButton();
            this.proxyNone = new System.Windows.Forms.RadioButton();
            this.proxyDefault = new System.Windows.Forms.RadioButton();
            this.tabControl1 = new System.Windows.Forms.TabControl();
            this.tabPage1 = new System.Windows.Forms.TabPage();
            this.tabPage2 = new System.Windows.Forms.TabPage();
            this.groupBox6 = new System.Windows.Forms.GroupBox();
            this.proxyPassword = new System.Windows.Forms.TextBox();
            this.proxyDomain = new System.Windows.Forms.TextBox();
            this.proxyUser = new System.Windows.Forms.TextBox();
            this.proxyDomainLabel = new System.Windows.Forms.Label();
            this.proxyPassLabel = new System.Windows.Forms.Label();
            this.proxyUserLabel = new System.Windows.Forms.Label();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.groupBox3.SuspendLayout();
            this.groupBox4.SuspendLayout();
            this.statusStrip1.SuspendLayout();
            this.groupBox5.SuspendLayout();
            this.tabControl1.SuspendLayout();
            this.tabPage1.SuspendLayout();
            this.tabPage2.SuspendLayout();
            this.groupBox6.SuspendLayout();
            this.SuspendLayout();
            // 
            // selectRequest
            // 
            this.selectRequest.Location = new System.Drawing.Point(437, 17);
            this.selectRequest.Name = "selectRequest";
            this.selectRequest.Size = new System.Drawing.Size(29, 23);
            this.selectRequest.TabIndex = 1;
            this.selectRequest.Text = "...";
            this.selectRequest.UseVisualStyleBackColor = true;
            this.selectRequest.Click += new System.EventHandler(this.request_Click);
            // 
            // openFileDialog1
            // 
            this.openFileDialog1.FileName = "openFileDialog1";
            this.openFileDialog1.InitialDirectory = "d:\\temp\\bew";
            // 
            // requestfile
            // 
            this.requestfile.Location = new System.Drawing.Point(8, 19);
            this.requestfile.Name = "requestfile";
            this.requestfile.Size = new System.Drawing.Size(419, 20);
            this.requestfile.TabIndex = 2;
            this.requestfile.TextChanged += new System.EventHandler(this.requestfile_TextChanged);
            // 
            // responsefile
            // 
            this.responsefile.Location = new System.Drawing.Point(8, 19);
            this.responsefile.Name = "responsefile";
            this.responsefile.Size = new System.Drawing.Size(419, 20);
            this.responsefile.TabIndex = 6;
            this.responsefile.TextChanged += new System.EventHandler(this.responsefile_TextChanged);
            // 
            // selectResult
            // 
            this.selectResult.Location = new System.Drawing.Point(437, 17);
            this.selectResult.Name = "selectResult";
            this.selectResult.Size = new System.Drawing.Size(29, 23);
            this.selectResult.TabIndex = 5;
            this.selectResult.Text = "...";
            this.selectResult.UseVisualStyleBackColor = true;
            this.selectResult.Click += new System.EventHandler(this.result_Click);
            // 
            // submitButton
            // 
            this.submitButton.Enabled = false;
            this.submitButton.Location = new System.Drawing.Point(345, 297);
            this.submitButton.Name = "submitButton";
            this.submitButton.Size = new System.Drawing.Size(75, 23);
            this.submitButton.TabIndex = 14;
            this.submitButton.Text = "Submit";
            this.submitButton.UseVisualStyleBackColor = true;
            this.submitButton.Click += new System.EventHandler(this.select_Click);
            // 
            // closeButton
            // 
            this.closeButton.Location = new System.Drawing.Point(426, 297);
            this.closeButton.Name = "closeButton";
            this.closeButton.Size = new System.Drawing.Size(75, 23);
            this.closeButton.TabIndex = 15;
            this.closeButton.Text = "Close";
            this.closeButton.UseVisualStyleBackColor = true;
            this.closeButton.Click += new System.EventHandler(this.cancel_Click);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.requestfile);
            this.groupBox1.Controls.Add(this.selectRequest);
            this.groupBox1.Location = new System.Drawing.Point(8, 6);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(472, 51);
            this.groupBox1.TabIndex = 8;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Selection File";
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.responsefile);
            this.groupBox2.Controls.Add(this.selectResult);
            this.groupBox2.Location = new System.Drawing.Point(8, 136);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(472, 51);
            this.groupBox2.TabIndex = 9;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Result File";
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.mappingfile);
            this.groupBox3.Controls.Add(this.selectMapping);
            this.groupBox3.Location = new System.Drawing.Point(8, 71);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(472, 51);
            this.groupBox3.TabIndex = 10;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "Translation Table";
            // 
            // mappingfile
            // 
            this.mappingfile.Location = new System.Drawing.Point(8, 20);
            this.mappingfile.Name = "mappingfile";
            this.mappingfile.Size = new System.Drawing.Size(419, 20);
            this.mappingfile.TabIndex = 4;
            // 
            // selectMapping
            // 
            this.selectMapping.Location = new System.Drawing.Point(437, 18);
            this.selectMapping.Name = "selectMapping";
            this.selectMapping.Size = new System.Drawing.Size(29, 23);
            this.selectMapping.TabIndex = 3;
            this.selectMapping.Text = "...";
            this.selectMapping.UseVisualStyleBackColor = true;
            this.selectMapping.Click += new System.EventHandler(this.mapping_Click);
            // 
            // groupBox4
            // 
            this.groupBox4.Controls.Add(this.messagefile);
            this.groupBox4.Controls.Add(this.selectMessage);
            this.groupBox4.Location = new System.Drawing.Point(8, 201);
            this.groupBox4.Name = "groupBox4";
            this.groupBox4.Size = new System.Drawing.Size(472, 51);
            this.groupBox4.TabIndex = 11;
            this.groupBox4.TabStop = false;
            this.groupBox4.Text = "Message File";
            // 
            // messagefile
            // 
            this.messagefile.Location = new System.Drawing.Point(8, 18);
            this.messagefile.Name = "messagefile";
            this.messagefile.Size = new System.Drawing.Size(419, 20);
            this.messagefile.TabIndex = 8;
            // 
            // selectMessage
            // 
            this.selectMessage.Location = new System.Drawing.Point(437, 16);
            this.selectMessage.Name = "selectMessage";
            this.selectMessage.Size = new System.Drawing.Size(29, 23);
            this.selectMessage.TabIndex = 7;
            this.selectMessage.Text = "...";
            this.selectMessage.UseVisualStyleBackColor = true;
            this.selectMessage.Click += new System.EventHandler(this.message_Click);
            // 
            // statusStrip1
            // 
            this.statusStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripProgressBar1,
            this.toolStripStatusLabel1});
            this.statusStrip1.Location = new System.Drawing.Point(0, 328);
            this.statusStrip1.Name = "statusStrip1";
            this.statusStrip1.Size = new System.Drawing.Size(504, 22);
            this.statusStrip1.SizingGrip = false;
            this.statusStrip1.TabIndex = 13;
            this.statusStrip1.Text = "statusStrip1";
            // 
            // toolStripProgressBar1
            // 
            this.toolStripProgressBar1.Enabled = false;
            this.toolStripProgressBar1.Name = "toolStripProgressBar1";
            this.toolStripProgressBar1.Size = new System.Drawing.Size(100, 16);
            // 
            // toolStripStatusLabel1
            // 
            this.toolStripStatusLabel1.Name = "toolStripStatusLabel1";
            this.toolStripStatusLabel1.Size = new System.Drawing.Size(0, 17);
            // 
            // backgroundWorker
            // 
            this.backgroundWorker.WorkerReportsProgress = true;
            this.backgroundWorker.WorkerSupportsCancellation = true;
            this.backgroundWorker.DoWork += new System.ComponentModel.DoWorkEventHandler(this.backgroundWorker_DoWork);
            this.backgroundWorker.RunWorkerCompleted += new System.ComponentModel.RunWorkerCompletedEventHandler(this.backgroundWorker_RunWorkerCompleted);
            this.backgroundWorker.ProgressChanged += new System.ComponentModel.ProgressChangedEventHandler(this.backgroundWorker_ProgressChanged);
            // 
            // saveFileDialog1
            // 
            this.saveFileDialog1.FileName = "result.csv";
            this.saveFileDialog1.InitialDirectory = "d:\\temp\\bew\\";
            // 
            // groupBox5
            // 
            this.groupBox5.Controls.Add(this.proxyPort);
            this.groupBox5.Controls.Add(this.proxyAddress);
            this.groupBox5.Controls.Add(this.label2);
            this.groupBox5.Controls.Add(this.label1);
            this.groupBox5.Controls.Add(this.proxyCustom);
            this.groupBox5.Controls.Add(this.proxyNone);
            this.groupBox5.Controls.Add(this.proxyDefault);
            this.groupBox5.Location = new System.Drawing.Point(6, 6);
            this.groupBox5.Name = "groupBox5";
            this.groupBox5.Size = new System.Drawing.Size(481, 83);
            this.groupBox5.TabIndex = 14;
            this.groupBox5.TabStop = false;
            this.groupBox5.Text = "Proxy-Server";
            // 
            // proxyPort
            // 
            this.proxyPort.Enabled = false;
            this.proxyPort.Location = new System.Drawing.Point(336, 49);
            this.proxyPort.MaxLength = 5;
            this.proxyPort.Name = "proxyPort";
            this.proxyPort.Size = new System.Drawing.Size(44, 20);
            this.proxyPort.TabIndex = 13;
            this.proxyPort.Leave += new System.EventHandler(this.proxyPort_Leave);
            // 
            // proxyAddress
            // 
            this.proxyAddress.Enabled = false;
            this.proxyAddress.Location = new System.Drawing.Point(70, 49);
            this.proxyAddress.MaxLength = 64;
            this.proxyAddress.Name = "proxyAddress";
            this.proxyAddress.Size = new System.Drawing.Size(206, 20);
            this.proxyAddress.TabIndex = 12;
            this.proxyAddress.Leave += new System.EventHandler(this.proxyAddress_Leave);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(304, 52);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(26, 13);
            this.label2.TabIndex = 4;
            this.label2.Text = "Port";
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(7, 52);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(45, 13);
            this.label1.TabIndex = 3;
            this.label1.Text = "Address";
            // 
            // proxyCustom
            // 
            this.proxyCustom.AutoSize = true;
            this.proxyCustom.Location = new System.Drawing.Point(182, 19);
            this.proxyCustom.Name = "proxyCustom";
            this.proxyCustom.Size = new System.Drawing.Size(60, 17);
            this.proxyCustom.TabIndex = 11;
            this.proxyCustom.Text = "Custom";
            this.proxyCustom.UseVisualStyleBackColor = true;
            this.proxyCustom.CheckedChanged += new System.EventHandler(this.proxyCustom_CheckedChanged);
            // 
            // proxyNone
            // 
            this.proxyNone.AutoSize = true;
            this.proxyNone.Location = new System.Drawing.Point(117, 19);
            this.proxyNone.Name = "proxyNone";
            this.proxyNone.Size = new System.Drawing.Size(51, 17);
            this.proxyNone.TabIndex = 10;
            this.proxyNone.Text = "None";
            this.proxyNone.UseVisualStyleBackColor = true;
            this.proxyNone.CheckedChanged += new System.EventHandler(this.proxyNone_CheckedChanged);
            // 
            // proxyDefault
            // 
            this.proxyDefault.AutoSize = true;
            this.proxyDefault.Checked = true;
            this.proxyDefault.Location = new System.Drawing.Point(7, 19);
            this.proxyDefault.Name = "proxyDefault";
            this.proxyDefault.Size = new System.Drawing.Size(96, 17);
            this.proxyDefault.TabIndex = 9;
            this.proxyDefault.TabStop = true;
            this.proxyDefault.Text = "System Default";
            this.proxyDefault.UseVisualStyleBackColor = true;
            this.proxyDefault.CheckedChanged += new System.EventHandler(this.proxyDefault_CheckedChanged);
            // 
            // tabControl1
            // 
            this.tabControl1.Controls.Add(this.tabPage1);
            this.tabControl1.Controls.Add(this.tabPage2);
            this.tabControl1.Location = new System.Drawing.Point(0, 1);
            this.tabControl1.Name = "tabControl1";
            this.tabControl1.SelectedIndex = 0;
            this.tabControl1.Size = new System.Drawing.Size(501, 290);
            this.tabControl1.TabIndex = 16;
            // 
            // tabPage1
            // 
            this.tabPage1.Controls.Add(this.groupBox1);
            this.tabPage1.Controls.Add(this.groupBox2);
            this.tabPage1.Controls.Add(this.groupBox3);
            this.tabPage1.Controls.Add(this.groupBox4);
            this.tabPage1.Location = new System.Drawing.Point(4, 22);
            this.tabPage1.Name = "tabPage1";
            this.tabPage1.Padding = new System.Windows.Forms.Padding(3);
            this.tabPage1.Size = new System.Drawing.Size(493, 264);
            this.tabPage1.TabIndex = 0;
            this.tabPage1.Text = "Data Download";
            this.tabPage1.UseVisualStyleBackColor = true;
            // 
            // tabPage2
            // 
            this.tabPage2.Controls.Add(this.groupBox6);
            this.tabPage2.Controls.Add(this.groupBox5);
            this.tabPage2.Location = new System.Drawing.Point(4, 22);
            this.tabPage2.Name = "tabPage2";
            this.tabPage2.Padding = new System.Windows.Forms.Padding(3);
            this.tabPage2.Size = new System.Drawing.Size(493, 264);
            this.tabPage2.TabIndex = 1;
            this.tabPage2.Text = "Settings";
            this.tabPage2.UseVisualStyleBackColor = true;
            // 
            // groupBox6
            // 
            this.groupBox6.Controls.Add(this.proxyPassword);
            this.groupBox6.Controls.Add(this.proxyDomain);
            this.groupBox6.Controls.Add(this.proxyUser);
            this.groupBox6.Controls.Add(this.proxyDomainLabel);
            this.groupBox6.Controls.Add(this.proxyPassLabel);
            this.groupBox6.Controls.Add(this.proxyUserLabel);
            this.groupBox6.Location = new System.Drawing.Point(6, 96);
            this.groupBox6.Name = "groupBox6";
            this.groupBox6.Size = new System.Drawing.Size(481, 165);
            this.groupBox6.TabIndex = 15;
            this.groupBox6.TabStop = false;
            this.groupBox6.Text = "Proxy Authentication";
            // 
            // proxyPassword
            // 
            this.proxyPassword.Location = new System.Drawing.Point(70, 55);
            this.proxyPassword.Name = "proxyPassword";
            this.proxyPassword.PasswordChar = '#';
            this.proxyPassword.Size = new System.Drawing.Size(206, 20);
            this.proxyPassword.TabIndex = 5;
            this.proxyPassword.Leave += new System.EventHandler(this.proxyPassword_Leave);
            // 
            // proxyDomain
            // 
            this.proxyDomain.Location = new System.Drawing.Point(70, 87);
            this.proxyDomain.Name = "proxyDomain";
            this.proxyDomain.Size = new System.Drawing.Size(206, 20);
            this.proxyDomain.TabIndex = 6;
            this.proxyDomain.Leave += new System.EventHandler(this.proxyDomain_Leave);
            // 
            // proxyUser
            // 
            this.proxyUser.Location = new System.Drawing.Point(70, 22);
            this.proxyUser.Name = "proxyUser";
            this.proxyUser.Size = new System.Drawing.Size(206, 20);
            this.proxyUser.TabIndex = 4;
            this.proxyUser.Leave += new System.EventHandler(this.proxyUser_Leave);
            // 
            // proxyDomainLabel
            // 
            this.proxyDomainLabel.AutoSize = true;
            this.proxyDomainLabel.Location = new System.Drawing.Point(7, 90);
            this.proxyDomainLabel.Name = "proxyDomainLabel";
            this.proxyDomainLabel.Size = new System.Drawing.Size(43, 13);
            this.proxyDomainLabel.TabIndex = 2;
            this.proxyDomainLabel.Text = "Domain";
            // 
            // proxyPassLabel
            // 
            this.proxyPassLabel.AutoSize = true;
            this.proxyPassLabel.Location = new System.Drawing.Point(7, 58);
            this.proxyPassLabel.Name = "proxyPassLabel";
            this.proxyPassLabel.Size = new System.Drawing.Size(53, 13);
            this.proxyPassLabel.TabIndex = 1;
            this.proxyPassLabel.Text = "Password";
            // 
            // proxyUserLabel
            // 
            this.proxyUserLabel.AutoSize = true;
            this.proxyUserLabel.Location = new System.Drawing.Point(7, 26);
            this.proxyUserLabel.Name = "proxyUserLabel";
            this.proxyUserLabel.Size = new System.Drawing.Size(55, 13);
            this.proxyUserLabel.TabIndex = 0;
            this.proxyUserLabel.Text = "Username";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(504, 350);
            this.Controls.Add(this.tabControl1);
            this.Controls.Add(this.statusStrip1);
            this.Controls.Add(this.closeButton);
            this.Controls.Add(this.submitButton);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedToolWindow;
            this.Name = "Form1";
            this.Text = "vwd data manager[bew]";
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            this.groupBox4.ResumeLayout(false);
            this.groupBox4.PerformLayout();
            this.statusStrip1.ResumeLayout(false);
            this.statusStrip1.PerformLayout();
            this.groupBox5.ResumeLayout(false);
            this.groupBox5.PerformLayout();
            this.tabControl1.ResumeLayout(false);
            this.tabPage1.ResumeLayout(false);
            this.tabPage2.ResumeLayout(false);
            this.groupBox6.ResumeLayout(false);
            this.groupBox6.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button selectRequest;
        private System.Windows.Forms.OpenFileDialog openFileDialog1;
        private System.Windows.Forms.TextBox requestfile;
        private System.Windows.Forms.TextBox responsefile;
        private System.Windows.Forms.Button selectResult;
        private System.Windows.Forms.Button submitButton;
        private System.Windows.Forms.Button closeButton;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.TextBox mappingfile;
        private System.Windows.Forms.Button selectMapping;
        private System.Windows.Forms.GroupBox groupBox4;
        private System.Windows.Forms.TextBox messagefile;
        private System.Windows.Forms.Button selectMessage;
        private System.Windows.Forms.StatusStrip statusStrip1;
        private System.Windows.Forms.ToolStripProgressBar toolStripProgressBar1;
        private System.Windows.Forms.ToolStripStatusLabel toolStripStatusLabel1;
        private System.ComponentModel.BackgroundWorker backgroundWorker;
        private System.Windows.Forms.SaveFileDialog saveFileDialog1;
        private System.Windows.Forms.GroupBox groupBox5;
        private System.Windows.Forms.RadioButton proxyNone;
        private System.Windows.Forms.RadioButton proxyDefault;
        private System.Windows.Forms.RadioButton proxyCustom;
        private System.Windows.Forms.TextBox proxyPort;
        private System.Windows.Forms.TextBox proxyAddress;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TabControl tabControl1;
        private System.Windows.Forms.TabPage tabPage1;
        private System.Windows.Forms.TabPage tabPage2;
        private System.Windows.Forms.GroupBox groupBox6;
        private System.Windows.Forms.Label proxyUserLabel;
        private System.Windows.Forms.Label proxyDomainLabel;
        private System.Windows.Forms.Label proxyPassLabel;
        private System.Windows.Forms.TextBox proxyDomain;
        private System.Windows.Forms.TextBox proxyUser;
        private System.Windows.Forms.TextBox proxyPassword;
    }
}

