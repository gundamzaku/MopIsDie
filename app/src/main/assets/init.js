function checkHref(){
	//带不带http://，不带的话全部加上http://
	var result;
	var href = document.getElementsByTagName("a");
	for(i in href){
		console.log(href[i].outerHTML);
		console.log(href[i].href);
		console.log(href[i]);
		//奇怪，用outerHtml取不到值。
		if(typeof(href[i].href) == "undefined")continue;

		result = href[i].href.indexOf("topicedit.asp");
		if(result<0){
			continue;
		}
		result = href[i].href.indexOf("pm.asp");
		if(result<0){
			continue;
		}
		result = href[i].href.indexOf("?fid=1&tid");

		if(result<0){
			//href[i].addEventListener('click', function(){}, false);
			//href[i].href = "javascript:return;";
			continue;
		}
		result = href[i].href.indexOf("#quot");
		if(result<0){
			continue;
		}

		result = href[i].href.indexOf("http:\/\/");

		if(result!=0){
			href[i].href = "http://"+href[i].href;
		}

	}
}
window.onload=function(){
	checkHref();
}