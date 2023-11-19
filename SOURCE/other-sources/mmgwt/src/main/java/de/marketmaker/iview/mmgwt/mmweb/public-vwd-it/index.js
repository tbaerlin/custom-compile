var zoneVariables = {
    Controller: "MainController",
    View: "AsView"
};

vwdCustomerServiceContactArray = [VwdCustomerServiceContactDictionaries["vwd-it"]];

doAddSendNewPasswordContainer = false;

var specialHeaderFieldsHook = function () {
    var path = window.location.pathname;
    var moduleName = path.replace(/.*\/([\w-]+)\.html/, "$1");
    if (moduleName != "index" && moduleName != "test") {
        document.write("<meta name=\"mmModuleName\" content=\"" + moduleName + "\">");
    }
};
