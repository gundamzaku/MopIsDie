function checkHref(){
	//带不带http://，不带的话全部加上http://
	var result;
	var href = document.getElementsByTagName("a");
	for(i in href){
		//console.log(href[i].outerHTML);
		//console.log(href[i].href);
		//console.log(href[i]);
		var url = href[i].outerHTML;
		//奇怪，用outerHtml取不到值。
		if(typeof(url) == "undefined")continue;

		result = url.indexOf("topicedit.asp");
		if(result>0){
			continue;
		}
		result = url.indexOf("pm.asp");
		if(result>0){
			continue;
		}

		result = url.indexOf("?fid=1&tid");
		if(result>0){
			href[i].href = "javascript:return;";
			continue;
		}

		result = url.indexOf("?fid=1&amp;tid=");
		if(result>0){
			//href[i].addEventListener('click', function(){}, false);
			href[i].href = "javascript:return;";
			continue;
		}

		//不知道为什么，这个一查一个准
		result = href[i].href.indexOf("#quot");
		if(result>0){
			continue;
		}

		result = href[i].href.indexOf("http:\/\/");

		if(result!=0){
			href[i].href = "http://"+href[i].href;
		}
	}
}
checkHref();
window.onload=function(){
	var height = document.body.scrollHeight;
	injectedObject.adjustHeight(height);
}