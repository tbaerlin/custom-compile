var I18nDictionaries = {};
I18nDictionaries["de"] = {
    language: 'deutsch',
    messageSetLocale: 'Bitte warten - Umstellung auf deutsche Sprache',
    windowTitle: 'Anmeldung',
    username: '<label for="mmLoginUsername" accesskey="b"><span class="accesskey">B</span>enutzername</label>',
    password: '<label for="mmLoginPassword" accesskey="p"><span class="accesskey">P</span>asswort</label>',
    loginButton: '<button style="position: relative;" type="submit" class="x-btn-text" accesskey="a" tabindex="0"><span class="accesskey">A</span>nmelden</button>',
    iceLoginButton: '<button style="position: relative;" type="submit" class="as-button" accesskey="a" tabindex="0"><span class="label"><span class="accesskey">A</span>nmelden</span></button>',
    passwordHelpImage: '<img src="images/icons/info.png" border="0" alt="Passwort Hilfe"/>',
    applicationLoaded: 'Anwendung wird geladen - bitte warten ...',
    browserUnsupported: '<span style="font-size: 14px">Ihre Browser-Version ist veraltet und wird nicht mehr unterstützt.<br/>Bitte verwenden Sie einen aktuelleren Browser.</span><br/><br/>Empfohlene Version (oder höher):<br/>&nbsp;&nbsp;&nbsp;&nbsp;Firefox 31<br/>&nbsp;&nbsp;&nbsp;&nbsp;Google Chrome 36<br/>&nbsp;&nbsp;&nbsp;&nbsp;Safari 7<br/>&nbsp;&nbsp;&nbsp;&nbsp;Microsoft Internet Explorer 9',
    browserUndesirable: '<span style="font-size: 14px">Ihre Browser-Version ist veraltet. In einem aktuellen Browser wird die Anwendung deutlich schneller ausgeführt.<br/>Bitte verwenden Sie einen aktuelleren Browser.</span><br/><br/>Empfohlene Version (oder höher):<br/>&nbsp;&nbsp;&nbsp;&nbsp;Firefox 31<br/>&nbsp;&nbsp;&nbsp;&nbsp;Google Chrome 36<br/>&nbsp;&nbsp;&nbsp;&nbsp;Safari 7<br/>&nbsp;&nbsp;&nbsp;&nbsp;Microsoft Internet Explorer 9'
};
I18nDictionaries["en"] = {
    language: 'english',
    messageSetLocale: 'Please wait - switching to english language',
    windowTitle: 'Login',
    username: '<label for="mmLoginUsername" accesskey="u"><span class="accesskey">U</span>ser name</label>',
    password: '<label for="mmLoginPassword" accesskey="p"><span class="accesskey">P</span>assword</label>',
    loginButton: '<button style="position: relative;" type="submit" class="x-btn-text" accesskey="l" tabindex="0"><span class="accesskey">L</span>ogin</button>',
    iceLoginButton: '<button style="position: relative;" type="submit" class="as-button" accesskey="l" tabindex="0"><span class="label"><span class="accesskey">L</span>ogin</span></button>',
    passwordHelpImage: '<img src="images/icons/info.png" border="0" alt="Password Help"/>',
    applicationLoaded: 'Loading Application - please wait ...',
    browserUnsupported: '<span style="font-size: 14px">Your browser version is outdated. <br/>Please use a current browser.</span><br/><br/>Recommended minimum version:<br/>&nbsp;&nbsp;&nbsp;&nbsp;Firefox 31<br/>&nbsp;&nbsp;&nbsp;&nbsp;Google Chrome 36<br/>&nbsp;&nbsp;&nbsp;&nbsp;Safari 7<br/>&nbsp;&nbsp;&nbsp;&nbsp;Microsoft Internet Explorer 9',
    browserUndesirable: '<span style="font-size: 14px">Your browser version is outdated. Current browsers perform much faster.<br/>Please use a current browser.</span><br/><br/>Recommended minimum version:<br/>&nbsp;&nbsp;&nbsp;&nbsp;Firefox 31<br/>&nbsp;&nbsp;&nbsp;&nbsp;Google Chrome 36<br/>&nbsp;&nbsp;&nbsp;&nbsp;Safari 7<br/>&nbsp;&nbsp;&nbsp;&nbsp;Microsoft Internet Explorer 9'
};
I18nDictionaries["it"] = {
    language: 'italiano',
    messageSetLocale: 'Prego attendere stiamo passando all\'italiano',
    windowTitle: 'Login',
    username: '<label for="mmLoginUsername" accesskey="u"><span class="accesskey">U</span>tente</label>',
    password: '<label for="mmLoginPassword" accesskey="p"><span class="accesskey">P</span>assword</label>',
    loginButton: '<button style="position: relative;" type="submit" class="x-btn-text" accesskey="l" tabindex="0"><span class="accesskey">L</span>ogin</button>',
    iceLoginButton: '<button style="position: relative;" type="submit" class="as-button" accesskey="l" tabindex="0"><span class="label"><span class="accesskey">L</span>ogin</span></button>',
    passwordHelpImage: '<img src="images/icons/info.png" border="0" alt="Password help"/>',
    applicationLoaded: 'Caricamento applicazione, prego attendere ...',
    browserUnsupported: '<span style="font-size: 14px">La versione del Vostro browser è obsoleta. <br/>Si prega di utilizzare un browser aggiornato.</span><br/><br/>Versione minima raccomandata:<br/>&nbsp;&nbsp;&nbsp;&nbsp;Firefox 31<br/>&nbsp;&nbsp;&nbsp;&nbsp;Google Chrome 36<br/>&nbsp;&nbsp;&nbsp;&nbsp;Safari 7<br/>&nbsp;&nbsp;&nbsp;&nbsp;Microsoft Internet Explorer 9',
    browserUndesirable: '<span style="font-size: 14px">La versione del Vostro browser è obsoleta. Le performance di un browser aggiornato sono maggiori.<br/>Si prega di utilizzare un browser aggiornato.</span><br/><br/>Versione minima raccomandata:<br/>&nbsp;&nbsp;&nbsp;&nbsp;Firefox 31<br/>&nbsp;&nbsp;&nbsp;&nbsp;Google Chrome 36<br/>&nbsp;&nbsp;&nbsp;&nbsp;Safari 7<br/>&nbsp;&nbsp;&nbsp;&nbsp;Microsoft Internet Explorer 9'
};

/* default language for each zone is defined by the first locale */
var LanguageLinksDictionaries = {
    "bancadibologna" : ["it", "en", "de"],
    "bancapopolaredibari" : ["it", "en", "de"],
    "bcc" : ["it", "en", "de"],
    "bcccastiglione" : ["it", "en", "de"],
    "bccgarda" : ["it", "en", "de"],
    "grains" : ["en", "de"],
    "metals" : ["de", "en"],
    "unipol": ["it", "en", "de"],
    "vwd": ["de", "en"],
    "vwd-be": ["en", "de"],
    "vwd-ch": ["de", "en"],
    "vwd-fr": ["en", "de"],
    "vwd-it": ["it", "en", "de"],
    "vwd-nl": ["en", "de"],
    "web": ["de", "en"],
    "kwt": ["de", "en"],
    "apobank": ["de", "en"],
    "default": ["de"]
};

var VwdCustomerServiceContactDictionaries = {};
VwdCustomerServiceContactDictionaries["default"] = {
    key : 'default',
    name: 'Customer Service vwd group Germany',
    email: 'service@vwd.com',
    phone: '+49 69 260 95 760',
    fax: null
};
VwdCustomerServiceContactDictionaries["vwd-it"] = {
    key : 'vwd-it',
    name: 'Customer Service Infront Italy',
    email: 'supporto@infrontfinance.com',
    phone: '+39 02 87330-252',
    fax: '+39 02 87330-250'
};
VwdCustomerServiceContactDictionaries["vwd-nl"] = {
    key: 'vwd-nl',
    name: 'Customer Service Infront Financial Technology B.V.',
    email: 'service@vwd.com',
    phone: '+31(0) 20 7101770',
    fax: null
};
VwdCustomerServiceContactDictionaries["vwd-be"] = {
    key: 'vwd-be',
    name: 'Customer Service Infront Financial Technology NV.',
    email: 'service@vwd.com',
    phone: '+32(0)34 000 770',
    fax: null
};

var doAddSendNewPasswordContainer = true;

function getZone() {
    var url = location.href;
    var hashPos = url.indexOf('#');
    if (hashPos == -1) {
        hashPos = url.length;
    }
    var pos2 = url.lastIndexOf('/', hashPos);
    var pos1 = url.lastIndexOf('/', pos2 - 1);
    return url.substring(pos1 + 1, pos2);
}

var zoneName = getZone();

function addFavicon() {
  var faviconUrl = 'as/images/zones/' + zoneName + '/favicon.png';
  if (!(typeof zoneSpecificFaviconUrl === 'undefined')) {
    faviconUrl = zoneSpecificFaviconUrl
  }
  document.write('<link rel="icon" href="' + faviconUrl + '" type="image/png" />');
}

function addTitle() {
    if (!(typeof zoneSpecificAppTitle === 'undefined')) {
        document.write('<title>' + zoneSpecificAppTitle + '</title>');
    }
    else {
        document.write('<title>Infront Market Manager Financials Web</title>');
    }
}

function addIceGwtLocaleMeta() {
    /* method call is only necessary for icy mmf [web], i.e. no meta tag for a locale is defined */
    if(getMetaLocale()) {
        return;
    }

    function writeLocale(locale) {
        document.write('<meta name="gwt:property" content="locale=' + locale + '" />');
    }

    if(LanguageLinksDictionaries[zoneName]) {
        writeLocale(LanguageLinksDictionaries[zoneName][0])
    }
    else if (LanguageLinksDictionaries['default']) {
        writeLocale(LanguageLinksDictionaries['default'][0]);
    }
    else {
        writeLocale("de");
    }
}

addIceGwtLocaleMeta();

function getMetaContent(name) {
    var metaTags = document.getElementsByTagName("META");
    for (var i = 0; i < metaTags.length; i++) {
        if (name == metaTags[i].getAttribute("name")) {
            return metaTags[i].getAttribute("content");
        }
    }
    return null;
}

function getMetaLocale() {
    var metaTags = document.getElementsByTagName("META");
    for (var i = 0; i < metaTags.length; i++) {
        var name = metaTags[i].getAttribute("name");
        var content = metaTags[i].getAttribute("content");
        if (name == "gwt:property" && content.indexOf("locale=") >= 0) {
            return content.substring(7);
        }
    }
    return null;
}

function getCookieLocale() {
    return getCookie("lastLocale");
}

function getWindowLocationLocale() {
    var params = window.location.search;
    var localeParam = params.match(/\blocale=[a-z]+\b/);
    return localeParam?localeParam[0].substring(7):null;
}

function findCurrentLocale() {
    var locale;
    if ((locale = getWindowLocationLocale() || getCookieLocale() || getMetaLocale())
        && (locale in I18nDictionaries)) {
        return locale;
    }
    return "de";
}

var currentLocale = findCurrentLocale();
var dictionary = I18nDictionaries[currentLocale.substring(0, 2)];
var vwdCustomerServiceContactArray = [VwdCustomerServiceContactDictionaries["default"],
    VwdCustomerServiceContactDictionaries["vwd-nl"], VwdCustomerServiceContactDictionaries["vwd-be"]];
var imgPath = "images/mm/locale/";

function addLanguageLinks(dictionaries) {
    if (dictionaries == null || dictionaries.length == 0) {
        return;
    }
    document.write('<tr>');
    document.write('<td class="mm-login-label">&nbsp;</td>');
    document.write('<td>');
    for (var i = 0; i < dictionaries.length; i++) {
        var locale = dictionaries[i];
        var language = I18nDictionaries[locale].language;
        var isCurrentLocale = locale == currentLocale;
        var flagClass = isCurrentLocale ? "selectedLocale" : "unselectedLocale";
        document.write('<span class="' + flagClass + '">');
        document.write('<a onclick="setLocale(\''  + locale + '\')">');
        if (isCurrentLocale) {
            document.write('<img src="' + imgPath + locale + '.png" alt="' + language + '" title="' + language + '"/>');
        }
        else {
            document.write('<img src="' + imgPath + locale + '-gray.png"');
            document.write(' alt="' + language + '" title="' + language + '"');
            document.write(' onmouseover="this.src=\'' + imgPath + locale + '.png\'"');
            document.write(' onmouseout="this.src=\'' + imgPath + locale + '-gray.png\'"/>');
        }
        document.write('</a>');
        document.write('</span>');
        document.write('&nbsp;&nbsp;&nbsp;&nbsp;');
    }
    document.write('</td>');
    document.write('</tr>');
}

function addIceLanguageLinks() {
    if(LanguageLinksDictionaries[zoneName]) {
        addLanguageLinks(LanguageLinksDictionaries[zoneName]);
    }
}

function getQueryParameter(name) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if (pair[0] == name) {
            return pair[1];
        }
    }
    return "";
}

function setCookie(name, value, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = name + "=" + value + "; " + expires;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
    }
    return "";
}

function setLocale(locale) {
    document.getElementById("mmLoginResult").innerHTML = I18nDictionaries[locale].messageSetLocale;
    reloadWithLocale(locale);
    setCookie("lastLocale", locale, 365);
    return false;
}

function reloadWithLocale(locale) {
    var params = window.location.search;
    if (params == "") {
        window.location.search = "?locale=" + locale;
    }
    else {
        var arrParams = params.substr(1).split('&');
        var result = "";
        var divider = "?";
        var found = false;
        for (var i = 0; i < arrParams.length; i++) {
            if (arrParams[i].substr(0, 7) == "locale=") {
                result += divider + "locale=" + locale;
                found = true;
            }
            else {
                result += divider + arrParams[i];
            }
            divider = "&";
        }
        if (!found) {
            result += divider + "locale=" + locale;
        }
        window.location.search = result;
    }
}

function reloadWithLastLocale() {
    var locale = getQueryParameter('locale');
    if (locale == "") {
        var lastLocale = getCookie("lastLocale");
        if (lastLocale != "") {
            reloadWithLocale(lastLocale);
        }
    }
}

reloadWithLastLocale();

function openToolWindow(url) {
    window.open(url, '_blank', 'width=670,height=30,innerWidth=670,innerHeight=30,scrollbars=no,menubar=no,location=no,status=no,toolbar=no,dependent=yes,');
    return false;
}

function openPrintableWindow(url) {
    window.open(url, '_blank', 'menubar=yes,toolbar=yes,location=no,status=no,scrollbars=yes');
    return false;
}

function loadStylesheet(name, insertBeforeStyles) {
    var oLink = document.createElement("link");
    oLink.href = name;
    oLink.rel = "stylesheet";
    oLink.type = "text/css";
    var elHead = document.getElementsByTagName("head")[0];
    var appended = false;
    if (insertBeforeStyles) {
        var listStyles = elHead.getElementsByTagName("style");
        if (listStyles.length > 0) {
            elHead.insertBefore(oLink, listStyles[0]);
            appended = true;
        }
    }
    if (!appended) {
        elHead.appendChild(oLink);
    }
}

function mmLoginButtonHover(tbl, over) {
    var style = tbl.className;
    var disabled = style.indexOf("x-item-disabled") >= 0;
    style = "x-btn x-btn-noicon";
    if (over) {
        style = style + " x-btn-over";
    }
    if (disabled) {
        style = style + " x-item-disabled";
    }
    tbl.className = style;
}

function mmLoginResetError() {
    // will be overwritten by GWT code
}
function mmLoginOnSubmit() {
    // will be overwritten by GWT code
    document.getElementById("mmLoginSubmitted").value = "true";
    document.getElementById("mmLoginResult").innerHTML = dictionary.applicationLoaded;
}
function mmShowLoginHelp() {
    // will be overwritten by GWT code
}
function mmShowSendPasswordDialog() {
    // will be overwritten by GWT code
}
function pmIsinLink(isin) {
    // will be overwritten by GWT code
    alert("gwt callback missing: pmIsinLink(" + isin + ")");
}
function mmSetHistory(token) {
    // will be overwritten by GWT code
    alert("nicht angemeldet");
}
/*
 * ctrlKey:
 * - M_S: instrument
 * - N_S: news
 * - P_V: vwd page
 */
function mmSearch(ctrlKey, query) {
    // will be overwritten by GWT code
    alert("nicht angemeldet");
}

function mmShowReport(inputObject, reportId) {
    alert("mmShowReport() not initialized");
}

function mmLogin(username, password) {
    alert("mmLogin() not initialized");
}

function mmErrorCallback(message) {
    alert(message);
}

var moduleLoaded = false;
function mmModuleLoadCallback() {
    moduleLoaded = true;
}

function mmInitializedCallback() {
}

function initializeCallbackFunctions(mmModuleLoad, mmError, mmInitialized) {
    mmModuleLoadCallback = mmModuleLoad;
    mmErrorCallback = mmError;
    mmInitializedCallback = mmInitialized;
    if (moduleLoaded) {
        mmModuleLoadCallback();
    }
}

function mmGetUrlParam( name ) {
    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");

    var regexS = "[\\?&]"+name+"=([^&#]*)";
    var regex = new RegExp( regexS );
    var results = regex.exec( window.location.search );

    if ( results != null ) {
        return results[1];
    }

    regex = new RegExp( "[#/]" + name + "=([^/]*)" );
    results = regex.exec( window.location.hash );
    if ( results != null ) {
        return results[1];
    }
    return null;
}

function isCanvasSupported(){
    var elem = document.createElement('canvas');
    return !!(elem.getContext && elem.getContext('2d'));
}

function isCanvasRequired() {
    return getMetaContent("mm:canvasRequired") == "true";
}

function isLocalStorageSupported() {
    if (window.localStorage) {
        return true;
    }
    return false;
}

function addMotd(motdHtml) {
    var eltMotdRoot = document.getElementById('message_of_the_day');
    if (eltMotdRoot == null) {
        window.alert(motdHtml.replace(/<br\/{0,1}>/g, "\n").replace(/<[^>]*>/g, "").replace(/&nbsp;/g, " "));
        return;
    }
    var motd = document.createElement("div");
    motd.innerHTML = motdHtml;
    eltMotdRoot.appendChild(motd);
    eltMotdRoot.style.visibility = 'visible';
}

function showUnsupportedMessage() {
    if (!isLocalStorageSupported() || (isCanvasRequired() && !isCanvasSupported())) {
        addMotd(dictionary.browserUnsupported);
        addLoginRootStyle('mm-browserUnsupported noIe6');
        removeLoginForm();
    }
    else if (!isCanvasSupported()) {
        addMotd(dictionary.browserUndesirable);
        addLoginRootStyle('mm-browserUndesirable');
    }
    else {
        addLoginRootStyle('mm-browserOk');
    }
}

function removeLoginForm() {
    var mmLoginRoot = document.getElementById('mmLoginRoot');
    if (mmLoginRoot != null) {
        var childNodes = mmLoginRoot.childNodes;
        for (var i = 0; i < childNodes.length; i++) {
            if (childNodes[i].tagName == "TABLE" || childNodes[i].tagName == "FORM") {
                mmLoginRoot.removeChild(childNodes[i]);
            }
        }
    }
}

function addLoginRootStyle(styleName) {
    var mmLoginRoot = document.getElementById('mmLoginRoot');
    if (mmLoginRoot != null) {
        mmLoginRoot.setAttribute("class", styleName);
    }
}

function mmDirectLogin() {
    var login = mmGetUrlParam("login");
    if (login != null) {
        document.getElementById('mmLoginUsername').value = decodeURIComponent(login);
        document.getElementById("mmLoginSubmitted").value = "true";
        document.getElementById("mmLoginResult").innerHTML = dictionary.applicationLoaded;
        var password = mmGetUrlParam("password");
        if (password != null) {
            document.getElementById('mmLoginPassword').value = decodeURIComponent(password);
        }
    }
    showUnsupportedMessage();
}

function addSendNewPasswordContainer() {
    if(!doAddSendNewPasswordContainer) {
        return;
    }
    document.write('<tr><td colspan="2"><div id="mmLoginSendPasswordContainer" style="text-align: right;"></div></td></tr>');
}

function addInfrontLoginSendNewPasswordContainer() {
    if(!doAddSendNewPasswordContainer) {
        return;
    }
    document.write('<div id="mmLoginSendPasswordContainer" style="text-align: right;"></div>');
}

function addInfrontLoginLanguageLinks() {
    if(LanguageLinksDictionaries[zoneName]) {
      var dictionaries = LanguageLinksDictionaries[zoneName]
      if (dictionaries == null || dictionaries.length == 0) {
            return;
        }
        document.write('<div id="mmLoginLanguageLinks">');
        document.write('<td style="text-align: center">');
        for (var i = 0; i < dictionaries.length; i++) {
            var locale = dictionaries[i];
            var language = I18nDictionaries[locale].language;
            var isCurrentLocale = locale == currentLocale;
            var flagClass = isCurrentLocale ? "selectedLocale" : "unselectedLocale";
            document.write('<span class="' + flagClass + '">');
            document.write('<a onclick="setLocale(\''  + locale + '\')">');
            if (isCurrentLocale) {
                document.write('<img src="' + imgPath + locale + '.png" alt="' + language + '" title="' + language + '"/>');
            }
            else {
                document.write('<img src="' + imgPath + locale + '-gray.png"');
                document.write(' alt="' + language + '" title="' + language + '"');
                document.write(' onmouseover="this.src=\'' + imgPath + locale + '.png\'"');
                document.write(' onmouseout="this.src=\'' + imgPath + locale + '-gray.png\'"/>');
            }
            document.write('</a>');
            document.write('</span>');
            document.write('&nbsp;&nbsp;&nbsp;&nbsp;');
        }
        document.write('</div>');
    }
}

function addInfrontLoginCustomerServiceContainer() {
    var cs = VwdCustomerServiceContactDictionaries[zoneName];
    if (!cs) {
        cs = VwdCustomerServiceContactDictionaries['default'];
    }
    if(!cs) {
        return;
    }
    document.write('<div class="supportInformation">');
    document.write(cs.name);
    document.write('<br/>');
    document.write(cs.phone);
    document.write('<br/>');
    document.write(cs.email);
    document.write('</div>')
}

function addSpecialHeaderFields() {
    if(typeof specialHeaderFieldsHook === 'function') {
        specialHeaderFieldsHook();
    }
}

function addZoneSpecificUsernameHint() {
    if(typeof zoneSpecificUsernameHintHook === 'function') {
        zoneSpecificUsernameHintHook();
    }
}
