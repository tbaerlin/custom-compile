var zoneVariables = {
    Controller: "MainController",
    View: "AsView"
};

var specialHeaderFieldsHook = function() {
    var path = window.location.pathname;
    var moduleName = path.replace(/.*\/([\w-]+)\.html/, "$1");
    if (moduleName != "index") {
        document.write("<meta name=\"mmModuleName\" content=\"" + moduleName + "\">");
    }
};
