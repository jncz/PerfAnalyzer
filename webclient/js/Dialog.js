"use strict"

define(["md/ajax","md/Tree","md/Event"],function(ajax,Tree,event){
	var a = function(id){
		this.obj = document.getElementById(id);
		this.setData = function(ds){
			var datas = ds.names;
			var tree = new Tree("container");
			tree.init();
			for(var i=0;i<datas.length;i++){
				var div = document.createElement("div");
				div.innerHTML = "<a href=\"#\">"+datas[i]+"</a>";
				div.setAttribute("d",datas[i]);
				div.addEventListener("click",function(e){
					var n = this.getAttribute("d");
					
					ajax.openURL("/catalyst/ca/jsoncalltree/"+n,function(h){
						var t2 = h.responseText;
						var obj2 = JSON.parse(t2);
						
						tree.data = obj2.data[0];
						tree.repaint();
						
						event.reset();
						event.tree = tree;
						event.attachFilterEvent();
						event.attacheFilterBtnEvent();
						event.attachFilterClostBtnEvent();
						event.attachFilterRestBtnEvent();
						
						document.getElementById(id).close();
					},function(){
						console.log("fail to load");
					},{},"GET",null,true);
				});
				this.obj.appendChild(div);
			}
		};
		this.open = function(){
			this.obj.classList.toggle("hide");
			this.obj.showModal();
		};
		this.close = function(){
			this.obj.close();
		};
	};
	
	return a;
});