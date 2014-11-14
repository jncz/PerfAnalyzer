"use strict"

define(["md/ajax","md/StatsDialog"],function(ajax,statsDialog){
	var keycode_f = 70;
	var keycode_a = 65;
	var keycode_s = 83;
	var dialogOpen = function(id){
		var ele = $(id);
		ele.classList.toggle("hide");
		a.detachFilterEvent();
		
		ele.showModal();
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
					ajax.openURL("/catalyst/ca/jsoncalltree/archive",function(http){
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
					ajax.openURL("/catalyst/ca/jsoncalltree/archive/rebuild",function(http){
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