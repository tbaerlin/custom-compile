var zoneVariables = {
    Controller: "MainController",
    View: "AsView"
};

vwdCustomerServiceContactArray = [VwdCustomerServiceContactDictionaries["vwd-it"]];

doAddSendNewPasswordContainer = false;

var specialHeaderFieldsHook = function() {
    var path = window.location.pathname;
    var moduleName = path.replace(/.*\/([\w-]+)\.html/, "$1");
    if (moduleName != "index" && moduleName != "test") {
        document.write("<meta name=\"mmModuleName\" content=\"bcc" + moduleName + "\">");
        document.write("<meta name=\"rightLogoUri\" content=\"images/zones/bcc/logo-" + moduleName + ".png\">");
        document.write("<meta name=\"pdfLogo\" content=\"svg/images/zones/bcc/" + moduleName + ".svg\">");

        // write style for bcc subsidiary specific login logos
        document.write("<style> .ice-login-logo { background-image: url(images/zones/bcc/login-logo-" + moduleName + ".png); }</style>");
    }
};

