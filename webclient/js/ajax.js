"use strict"
define(function(){
	//these two callback have default parameter XMLHttpRequest object
	return {
		openURL : function(url,successCallback,failCallback,headers,method,body,async){
			var h = new XMLHttpRequest();
			h.onreadystatechange=function(){
				if (h.readyState==4){// 4 = "loaded"
					if (h.status==200){// 200 = OK
						successCallback && successCallback(h);
					}else{
						failCallback && failCallback(h);
						//console.log("Fail to access URL");
					}
				}
			};
			h.open(method,url,async);
			for(var i in headers){
				h.setRequestHeader(i,headers[i]);
			}
			h.send(body);
		},
	};
});