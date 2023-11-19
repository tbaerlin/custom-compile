var zoneVariables = {
    Controller: "AsMainController",
    isWithPmBackend: true
};

var pmxml = "pmxml-1";
if (window.location.hostname == "as-demo.vwd.com") {
    pmxml = "demo-pmxml-1";
}

var serverSettings = {
    webapp: pmxml
};

function createLoginLogo() {
    var logoHost = document.getElementsByClassName("loginLogo")[0];
    var logo = document.createElement("IMG");

    new function() {
        var self = this;
        self.target = logo;

        self.remove = function() {
            self.target.removeEventListener("error", self.errorListener);
        };

        self.errorListener = function() {
            self.remove();
            self.target.src = "/" + zoneName + "/images/as/as-default-login-logo.png";
        };

        self.target.addEventListener("error", self.errorListener);
    };

    logoHost.appendChild(logo);
    logo.src = "images/zones/" + zoneName + "/as-login-logo.png";
}

var constLocalStorageServerPrefix = "serverPrefix";

function isPhoneGap() { // JS: Detect if webfrontend is running in phonegap
    return navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry|IEMobile)/)
        && document.URL.indexOf("http://") === -1
        && document.URL.indexOf("https://") === -1
}

// todo: JS - implement
//function openPdf(pdfUrl) {
//    if (isPhoneGap()) {
//        console.log("Opening PDF at " + pdfUrl);
//        window.open(pdfUrl, '_system', 'location=no');
//    }
//}

function resetServerPrefix() {
    if (isPhoneGap()) {
        localStorage.removeItem(constLocalStorageServerPrefix);
        location.reload();
    }
}

if (isPhoneGap()) { // JS: In PhoneGap

    function autoDetectAndSwitchLanguage() {

        var defaultLocale="de";

        if (document.URL.indexOf("locale=") === -1) { // JS: If locale string is not already already present in URL

            navigator.globalization.getLocaleName(
                function (locale) {

                    var systemLocale=locale.value.substr(0,2);

                    if (systemLocale != defaultLocale) {
                        if  (!(I18nDictionaries[systemLocale] === undefined)) { // JS: Check if system locale is supported
                            location.replace(document.URL + "?locale=" + systemLocale);
                        }
                    }

                },
                function () {} // do nothing
            );

        }
    }

    function refreshServerSettings() {

        // JS: try reading serverPrefix from local storage

        var serverPrefix = localStorage.getItem(constLocalStorageServerPrefix);

        if (serverPrefix == null) {

            // JS: try reading serverPrefix preset from phonegap's config.xml

            var serverPrefixPreset = ""; // JS: quasi-safe default

            xmlhttp=new XMLHttpRequest();
            xmlhttp.open("GET","../config.xml",false);
            xmlhttp.send();
            xmlDoc=xmlhttp.responseXML;

            var configPreferences = xmlDoc.getElementsByTagName("widget")[0].getElementsByTagName("preference");
            var currentPreferenceName = null;

            for(var i=0; i<configPreferences.length; i++){
                currentPreferenceName = configPreferences[i].attributes.getNamedItem("name").value;

                if (currentPreferenceName == "vwdServerPrefix" ) {
                    serverPrefixPreset = configPreferences[i].attributes.getNamedItem("value").value;
                    break;
                }
            }

            // JS: prompt user for the URL and save it in local storage

            serverPrefix = prompt("Server", serverPrefixPreset);
            if (serverPrefix != null) {
                if ((serverPrefix.indexOf("http://") === -1) && (serverPrefix.indexOf("https://") === -1)) {
                    serverPrefix="http://" + serverPrefix;
                }
            } else { // JS: fallback to preset
                serverPrefix = serverPrefixPreset;
            }

            localStorage.setItem(constLocalStorageServerPrefix, serverPrefix );
        }

        // JS: apply own settings for request routing while running in phonegap
        //     caution: this won't typically work in a browser if the domain differs from the webserver
        //              because of the same-domain-policy

        console.log("Using server: " + serverPrefix);

        serverSettings = {
            webapp: pmxml,
            serverPrefix: serverPrefix
        };
    }

    // JS: perform necessary initializations when running in phonegap

    function onDeviceReady() {

        console.log("PhoneGap: Device ready.");
        autoDetectAndSwitchLanguage();

    }

    document.addEventListener("deviceready", onDeviceReady, false);

    refreshServerSettings();
}