var SuppAppMsg = {};

Ext.require([
             'Ext.ux.form.NumericField'
         ]);

Ext.application({
    name: 'SupplierApp',
    controllers: ['Main','Udc', 'Users','PurchaseOrder','Supplier', 'Approval','NonComplianceSupplier','CodigosSAT','FiscalDocuments','OutSourcing','Notice','Invoices','Token'],
    messages: {},
    autoCreateViewport: false,
	appFolder: 'app',
	appProperty: 'Current',
	requires: [
		'SupplierApp.widgets.SessionMonitor'
	],
	
	launch: function () {
        _AppGlobSupplierApp = this;
        this.messages = null;
        Ext.QuickTips.init();
        app = this;
        
        var me = this;
		 if (Ext.get('page-loader')) {
			    Ext.get('page-loader').hide();
		 }
		
		 SupplierApp.widgets.SessionMonitor.start();
		 

		var langu = window.navigator.language;
		var lang = "";
		if(langu.startsWith("es", 0)){
			lang = "es";
		}else{
			lang = "en";
		}
		
		Ext.Ajax.request({
		    url: 'getLocalization.action',
		    method: 'GET',
		    params: {
		    	lang : lang
	        },
		    success: function(fp, o) {
		    	var resp = Ext.decode(fp.responseText, true);
		    	SuppAppMsg = resp.data;
                if(!me.mainPanel){
        			me.mainPanel = Ext.create('Ext.panel.Panel', {
        		        autoWidth: true,
        		        height:600,
        		        renderTo: 'content',
        		        layout:'fit',
        		        border:false,
        		        listeners: {
        		            afterrender: function(component, eOpts) {
        		            	if (Ext.get('page-loader')) {
        		                    Ext.get('page-loader').remove();
        		                }
        		            }           
        		          },
        		        frame:false,
        		        items: [{
        		        	xtype: 'mainpanel'
        				}]
        			});
        		}
		    }
		}); 
		
		
	},
	loadController:function(controllerName){
		if (!this.controllers.get(controllerName)) {
			var controller = this.getController(controllerName);
			if(controller){
				controller.init();
			}                
		}
	},
	reLoadController:function(controllerName){
		if (this.controllers.get(controllerName)) {
			var controller = this.getController(controllerName);
			if(controller){
				controller.init();
			}                
		}
	},
	getLoadedController:function(controllerName){
		if (this.controllers.get(controllerName)) {
			var controller = this.getController(controllerName);
			if(controller){
				return controller;
			}else{
				return null;
			}                
		}else
			return null;
	}
});	
