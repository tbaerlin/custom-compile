using System;
using System.Windows.Forms;


namespace bew {
    public static class Program {
        /// <summary>
        /// Der Haupteinstiegspunkt für die Anwendung.
        /// </summary>
        [STAThread]
        private static void Main() {

            if (String.IsNullOrEmpty(ConfigSupport.FromConfig("customer"))
                || String.IsNullOrEmpty(ConfigSupport.FromConfig("license"))) {
                MessageBox.Show("No license information found, please contact BEW support",
                    "ERROR", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new Form1());
        }
    }
}