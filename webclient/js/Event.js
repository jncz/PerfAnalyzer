"use strict"

define(["md/ajax","md/StatsDialog"],function(ajax,statsDialog){
	var keycode_f = 70;
	var keycode_a = 65;
	var keycode_s = 83;
	var keycode_n = 78;
	var keycode_p = 80;
	var dialogOpen = function(id){
		var ele = $(id);
		ele.classList.toggle("hide");
		a.detachFilterEvent();
		
		ele.showModal();
	};
	var getStatsData = function(tree,direction){
		var p = new Promise(function(resolve,reject){
			var exename = document.body.getAttribute("d");
			var timestamp = document.body.getAttribute("ct");
			ajax.openURL("calltree/"+exename+"/"+timestamp+"/"+direction,function(http){
				var jsonText = http.responseText;
				var obj = JSON.parse(jsonText);
				resolve(obj);
			},function(){
				reject(-1);
			},{"Accecpt":"application/json","Content-Type":"application/json"},"GET",null,true);
		});
		$("progressDialog").showModal();
		p.then(function(obj){
			$("progressDialog").close();
			tree.data = obj.data[0];
			tree.repaint();
			
			if(statsDialog.opened){
				statsDialog.close();
				statsDialog.open();
			}
		},function(){
			$("progressDialog").close();
		});
	};
	var a = {
		tree : null,
		reset : function(){
			this.detachFilterEvent();
		},
		filterEventListener : function(e){
			console.log(e.keyCode);
			if(e.keyCode == keycode_f){
				dialogOpen("filterDialog");
			}
			if(e.keyCode == keycode_a){
				$("caseDialog").showModal();
			}
			if(e.keyCode == keycode_s){
				if(statsDialog.opened){
					statsDialog.close();
				}else{
					statsDialog.open();
				}
			}
			if(e.keyCode == keycode_n){
				getStatsData(a.tree,"n");
			}
			if(e.keyCode == keycode_p){
				getStatsData(a.tree,"p");
			}
		},
		attachFilterEvent : function(){
			console.log("attache key down");
			document.addEventListener("keydown",this.filterEventListener);
		},
		detachFilterEvent : function(){
			document.removeEventListener("keydown",this.filterEventListener);
		},
		
		attachFilterBtnEvent: function(){
			var btn = $("filterBtn");
			var that = this;
			btn.addEventListener("click",function(){
				var cost = $("cost").value;
				var times = $("times").value;
				var condition = $("and").checked;
				that.tree.filter(cost,times,condition);
				that.tree.repaint();
			});
		},
		attachFilterClostBtnEvent:function(){
			var btn = $("clostBtn");
			var that = this;
			btn.addEventListener("click",function(){
				console.log("close");
				that.attachFilterEvent();
				$("filterDialog").classList.toggle("hide");
				if($("filterDialog").open){
					$("filterDialog").close();
				}
			});
		},
		attachFilterRestBtnEvent:function(){
			var btn = $("resetBtn");
			var that = this;
			btn.addEventListener("click",function(){
				that.tree.filter(null,null,null);
				that.tree.repaint();
			});
		},
		attachArchiveBtnEvent:function(){
			var btn = $("archiveBtn");
			btn.addEventListener("click",function(){
				var p = new Promise(function(resolve,reject){
					ajax.openURL("calltree/archive",function(http){
						var jsonText = http.responseText;
						var obj = JSON.parse(jsonText);
						resolve(obj);
					},function(){
						reject(-1);
					},{"Accecpt":"application/json","Content-Type":"application/json"},"GET",null,true);
				});
				$("progressDialog").showModal();
				p.then(function(obj){
					$("progressDialog").close();
				},function(){
					$("progressDialog").close();
				});
			});
		},
		attachReArchiveBtnEvent:function(){
			var btn = $("reArchiveBtn");
			btn.addEventListener("click",function(){
				var p = new Promise(function(resolve,reject){
					ajax.openURL("calltree/archive/rebuild",function(http){
						var jsonText = http.responseText;
						var obj = JSON.parse(jsonText);
						resolve(obj);
					},function(){
						reject(-1);
					},{"Accecpt":"application/json","Content-Type":"application/json"},"GET",null,true);
				});
				$("progressDialog").showModal();
				p.then(function(obj){
					console.log(obj);
					$("progressDialog").close();
				},function(){
					$("progressDialog").close();
				});
			});
		}
	};

	return a;
});