function checkHref(){var result;var href=document.getElementsByTagName("a");for(i in href){var url=href[i].outerHTML;if(typeof(url)=="undefined")continue;result=url.indexOf("topicedit.asp");if(result>0){continue}result=url.indexOf("pm.asp");if(result>0){continue}result=url.indexOf("?fid=1&tid");if(result>0){href[i].href="javascript:return;";continue}result=url.indexOf("?fid=1&tid=");if(result>0){href[i].href="javascript:return;";continue}result=href[i].href.indexOf("#quot");if(result>0){continue}result=href[i].href.indexOf("http:\/\/");if(result!=0){href[i].href="http://"+href[i].href}}}checkHref();window.onload=function(){var height=document.body.scrollHeight;injectedObject.adjustHeight(height)}