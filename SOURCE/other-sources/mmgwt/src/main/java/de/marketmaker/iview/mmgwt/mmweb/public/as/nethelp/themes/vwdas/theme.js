(function(n,t,i,r){i.defaultSettings({stringsMap:{c1tabTocLabel:"toc.label",c1tabIndexLabel:"index.label",c1tabSearchLabel:"search.label",c1headerText:"pageHeaderText"}});var f,s=["toc","index","search"],o={toc:0,index:1,search:2,0:0,1:1,2:2},u={0:"toc",1:"index",2:"search",toc:0,index:1,search:2},e={};i.bind("writebody",function(){var s=t.designer,e=i.isTopicOnlyMode(),u,o;f=n("#c1page").parent(),f.length||(f=n("body")),e&&f.addClass("topic-only"),i.setting("general.rightToLeft")&&f.attr("dir","rtl").css("direction","rtl"),u=i.header={_header:n("#c1header"),_main:n("#c1main"),visible:function(n){var i=u._header;if(n==r)return i.is(":visible");n?e||(i.show(),u._main.css("top",i.height()+2)):(i.hide(),u._main.css("top",0))},height:function(n){var t=u._header;if(n==r)return u.visible()?t.height():0;n=+n,!n||n<0?u.visible(!1):e||(t.height(n).show(),u._main.css("top",t.height()+2))},logo:function(n){var i=u._header.find(".c1-header-logo");if(n==r)return i.is(":visible")?i.attr("src"):!1;n?(t.isString(n)&&i.attr("src",n),i.show()):i.hide()},text:function(n){var i=u._header.find(".c1-header-text");if(n==r)return i.text();n?(t.isString(n)&&i.text(n),i.show()):i.hide()},html:function(n){if(n==r)return u._header.html();u._header.html(n)}},u.logo(t.str(i.setting("pageHeader.logoImage"),"")),o=i.setting("pageHeader.visible"),o?(u.height(i.setting("pageHeader.height")),i.setting("pageHeader.showText",{types:"boolean"})===!1&&n("#c1headerText").hide()):u.visible(!1)}),i.plugin({name:"theme",create:function(){var g=i.theme=i.theme||{},c,nt=t.designer,p,y,l;if(i.setting("topic.applyStylesheet")!==!1&&n("#c1topicPanel").addClass("ui-widget-content"),i.isTopicOnlyMode())setTimeout(function(){f.addClass("topic-only")},100);else{var s=n("#c1sideTabs"),b=s.children("h3"),k=!!i.setting("theme.rememberActiveTab"),w="c1sideActiveTab",h=k&&n.cookie&&n.cookie(w)||o[((/(?:\?|&|^)tab=([^&]+)(?:&|$)/i.exec(location.search)||[])[1]||"0").toLowerCase()],tt=n("<div/>").hide().appendTo("body"),v=i.index;c=v&&i.setting("index.visible")&&(!i.setting("index.hideEmpty")||v.hasKeywords()),c||(b.eq(1).next().andSelf().hide(),u[1]="search",u[2]=r,u.search=1,u.index=r,h>0&&(h-=1)),c=i.setting("search.visible"),c||(u[h]==="search"&&(h=0),b.eq(2).next().andSelf().hide(),u[u.search]=r,u.search=r,o[2]=0),s.show();function a(){s.accordion({header:"h3:visible",heightStyle:"fill"}),setTimeout(function(){s.accordion("refresh")},10),h&&!e[h]&&s.accordion("option","active",+h),s.bind("accordionactivate",function(t,r){var e=s.accordion("option","active"),f;k&&n.cookie&&n.cookie(w,e||null,{expires:365}),f=r.newPanel.find("input"),f.length||(f=r.newPanel.find("a").first()),f.focus(),i.trigger("tab",t,{index:e,tab:u[e],panel:r.newPanel,label:r.newHeader})}),n(window).resize(function(){s.accordion("refresh")}),i.switchTab=function(n){n=o[n],n==r||e[n]||s.accordion("option","active",+n)}}s.is(":visible")?a():(p=100,y=setInterval(function(){(s.is(":visible")||!p--)&&(clearInterval(y),a())},100)),l=i.splitter=t.splitter("#c1splitter",t.splitter.settings2options(t.extend({rightSide:!!i.setting("general.rightToLeft"),side:"#c1side",main:"#c1content"},i.setting("splitter")))),l.bind("showside",function(){s.accordion("refresh"),setTimeout(function(){s.accordion("refresh")},1e3)}).bind("*",function(n,t){n=n.originalEvent,i.trigger(n.type,n,t)}),l.position(l.position());function d(){var t=n("#c1topic .topic-frame");t.length&&t.css("top",n("#c1topicBar").outerHeight()+2)}n(window).resize(d),i.bind("topicupdate breadcrumbsupdate",d)}}}),i.driver("themedesigner",function(){var r=t.designer;if(r){r.hooks["topic.spinner"]=function(t,r){i.topicSpin(r),n(".preview-popup").toggle(!r)},i.bind("tab",function(n,t){r.isDesigntime()&&r.select(t.panel)}),n.each(s,function(t,u){r.hooks[u]=function(t,r){r&&!e[n.inArray(u,s)]&&i.switchTab(u)}});var o=100,u=!1,f=!1;r.hooks.splitter=function(n,t){t?u||(o=r.selecter.css("z-index"),f=i.splitter.options.disabled&&!i.splitter.options.locked,i.splitter.enable(),u=!0,r.selecter.css("z-index",99)):(r.selecter.css("z-index",o),i.splitter[f?"disable":"lock"](),u=!1)},i.bind("splitter",function(n,t){u&&(r.trigger("setting",n,{role:"splitter",setting:"splitter.position",value:t.position}),collapsed=t.collapsed===!0?!0:!1,i.splitter.options.collapsed=collapsed,r.trigger("setting",n,{role:"splitter",setting:"splitter.collapsed",value:collapsed}),r.select(i.splitter.splitter))}).bind("splitterstart splitterstop",function(n){r.lock(n.type==="shellsplitterstart")})}})})(jQuery,nethelp,nethelpshell)