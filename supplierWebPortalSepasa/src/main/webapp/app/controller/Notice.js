Ext.define('SupplierApp.controller.Notice', {
    extend: 'Ext.app.Controller',
    stores: ['Notice'],
    models: ['Notice','NoticeDTO'],
    views: ['notice.NoticePanel','notice.NoticeForm','notice.NoticeGrid'],
    refs: [{
        	ref: 'noticeForm',
        	selector: 'noticeForm'
	    },
	    {
	        ref: 'noticeGrid',
	        selector: 'noticeGrid'
	    }],
 
    init: function() {
        this.control({
            'noticeGrid': {
                selectionchange: this.gridSelectionChange
            },
            'noticeForm button[action=saveNotice]': {
                click: this.saveNotice
            },
            'noticeForm button[action=noticeNew]': {
                click: this.resetNoticeForm
            },
            'usersForm button[action=deleteUsers]': {
                click: this.deleteUsers
            },
            'usersForm button[action=updateUsers]': {
                click: this.updateUsers
            },
            'noticeForm button[action=loadNoticeFile]' : {
				click : this.loadDoc
			},
			'#openSuppNotice' : {
				"buttonclick" : this.openSuppNotice
			},
            '#searchNotices' : {
                "ontriggerclick": this.loadSearchList
            }
        });
    },
    
    openSuppNotice : function(grid, rowIndex, colIndex, record) {
    	var me = this;
    	var record = grid.store.getAt(rowIndex);
    	
    	Ext.Ajax.request({
			url : 'notice/statusSuppsNotice.action',
			method : 'GET',
				params : {
					idNotice : record.data.idNotice
				},
				success : function(response,opts) {
					
					
					var list="";
					response = Ext.decode(response.responseText);
					
					var supps = response.data;
					
					/*var taxIdCombo = Ext.getCmp('suppliersNotice');
					var store = taxIdCombo.getStore();
					
					store.removeAll();
		    		store.proxy.extraParams = { 
		    				query:''
		        	    	}
		        	store.load();*/
		    		
					for ( i = 0; i< supps.length; i++){  
						var sup = supps[i];
						var status = "";
						
						if(sup.status =='ACEPTADO'){
							status = '<font color="green">' + sup.status + '</font>';
						} else if(sup.status =='RECHAZADO'){
							status = '<font color="red">' + sup.status + '</font>';
						}else status = sup.status;
						//var item = store.findRecord('addresNumber', sup.addresNumber);
						
						if(sup.attachment != null){
							var fileList = sup.doc;
					    	var hrefList ="";
					    	var r2 = [];
					    	var href ="";
					    	var fileHref="";
					
					    	var r1 = fileList.split("_FILE:");
					    	var inx = r1.length;
					    	for (var index = inx - 1; index >= 0; index--) {
					    		r2 = r1[index].split("_:_");
					        		if(r2[0]  != ""){
						        	href = "notice/openDocument.action?id=" + r2[0];
									fileHref = "<a href= '" + href + "' target='_blank'>" +  r2[1] + "</a>";
									hrefList =  hrefList + fileHref ;
					    		}
					    	}
							
							list = list + sup.addresNumber + ': ' +  sup.razonSocial + '	||	' + status + '	||	' + hrefList + '<br><br>';
						}else list = list + sup.addresNumber + ': ' +  sup.razonSocial + '	||	' + status + '<br><br>';
						
					}
					
					var notesWindow = new Ext.Window({
			    		layout : 'fit',
			    		//title : 'Notas aprobacion y rechazo',
			    		width : 700,
			    		height : 260,
			    		modal : true,
			    		closeAction : 'destroy',
			    		resizable : false,
			    		minimizable : false,
			    		maximizable : false,
			    		autoScroll: true,
			    		plain : true,
			    		html: list
			    	});
			     	
			     	notesWindow.show();
			}
		});
    	
    },

    gridSelectionChange: function(model, records) {
        if (records[0]) {
        	var form = this.getNoticeForm().getForm();
        	var box = Ext.MessageBox.wait(SuppAppMsg.approvalLoadRegistrer, SuppAppMsg.approvalExecution);
        	form.loadRecord(records[0]);

        	/*var roleCombo = form.findField('userRole.id');
			roleCombo.store.load({
                callback: function (r, options, success) {
                		roleCombo.setValue(records[0].data.userRole.id);
                 }
            });		
			
        	var typeCombo = form.findField('userType.id');
        	typeCombo.store.load({
                callback: function (r, options, success) {
                	typeCombo.setValue(records[0].data.userType.id);
                 }
            });
        	
        	Ext.getCmp('usersFormUserName').setReadOnly(true);
        	Ext.getCmp('usersFormName').setReadOnly(true);*/
        	//Ext.getCmp('usersFormEmail').setReadOnly(true);
        	
            //this.enableUpdate();
            box.hide(); 
            Ext.getCmp('saveNotice').setDisabled(true);
        }
    },
    
    loadDoc : function(button) {
    	var supForm = this.getNoticeForm().getForm();
    	//var values = supForm.getFieldValues();
    	var supField = "";
    	switch (button.action) {
    	  case 'loadNoticeFile':
    		    supField = 'noticeFile';
    		    break;
    	  case 'loadDomDoc':
		        supField = 'domDocument';
              break;
    	  default:
            break;
    	};
    	
    	/*if(!values.rfc && !values.taxId){
    		Ext.MessageBox.alert({ maxWidth: 700, minWidth: 650, title: SuppAppMsg.supplierMsgValidationLoad, msg:  SuppAppMsg.supplierLoadDocNot });
    		return false;
    	}*/
    	var me = this;
    	var idNoticeDoc = Ext.getCmp('idNotice').getValue();
    	/*if( values.addresNumber != '0'  && values.addresNumber != '' ){
        	addressNumber = values.addresNumber;
    	}else{
    		if(!values.rfc){
    			addressNumber = "NEW_" + values.taxId;
    		}else{
    			addressNumber = "NEW_" + values.rfc;
    		}
    		
    	}*/

    	var filePanel = Ext.create(
    					'Ext.form.Panel',
    					{
    						width : 900,	
		    				items : [{
										xtype : 'textfield',
										name : 'idNoticeDoc',
										hidden : true,
										margin:'20 0 0 10',
										value : idNoticeDoc 
									},{
    									xtype : 'textfield',
    									name : 'createdBy',
    									hidden : true,
    									margin:'20 0 0 10',
    									value : userName 
    								},{
    									xtype : 'textfield',
    									name : 'documentType',
    									hidden : true,
    									value : button.action
    								},
    								{
    									xtype : 'filefield',
    									name : 'file',
    									fieldLabel :SuppAppMsg.purchaseFile,
    									labelWidth : 120,
    									msgTarget : 'side',
    									allowBlank : false,
    									margin:'20 0 70 0',
    									anchor : '90%',
    									buttonText : SuppAppMsg.suppliersSearch
    								} ],

    						buttons : [ {
    							text : SuppAppMsg.supplierLoad,
    							margin:'10 0 0 0',
    							handler : function() {
    								var form = this.up('form').getForm();
    								if (form.isValid()) {
    									form.submit({
    												url : 'uploadFileNotice.action',
    												waitMsg : SuppAppMsg.supplierLoadFile,
    												success : function(fp, o) {
    													var res = Ext.decode(o.response.responseText);
    													Ext.MessageBox.alert({ maxWidth: 400, minWidth: 400, title: SuppAppMsg.supplierMsgValidationLoad, msg: 'El archivo ha sido cargado exitosamente' });
    													supForm.findField(supField).setValue(res.fileName);

    													me.winLoadInv.close();
    												},       // If you don't pass success:true, it will always go here
    										        failure: function(fp, o) {
    										        	var res = Ext.decode(o.response.responseText);
    										        	Ext.MessageBox.alert({ maxWidth: 700, minWidth: 650, title: SuppAppMsg.supplierMsgValidationLoad, msg:  res.message});
    										        }
    											});
    								}
    							}
    						} ]
    					});

    	this.winLoadInv = new Ext.Window({
    		layout : 'fit',
    		title : SuppAppMsg.supplierLoadDocTitle,
    		width : 600,
    		height : 160,
    		modal : true,
    		closeAction : 'destroy',
    		resizable : false,
    		minimizable : false,
    		maximizable : false,
    		plain : true,
    		items : [ filePanel ]

    	});
    	this.winLoadInv.show();
    },

    loadSearchList: function (event){
    	this.getNoticeGrid().getStore().getProxy().extraParams={
    		query:event.getValue()
    	};
    	this.getNoticeGrid().getStore().load();
    },
    
    saveNotice: function (button) {
    	var form = this.getNoticeForm().getForm(); 
    	/*var values = form.getFieldValues();
    	debugger;*/
    	//var grid = this.getUsersGrid();
    	if (form.isValid()) { 
    		form.submit({
				url : 'notice/save.action',
				waitMsg : SuppAppMsg.supplierLoadFile,
				success : function(fp, o) {
					var res = Ext.decode(o.response.responseText);
					//var res = Ext.decode(fp.responseText);
			    	//grid.store.load();
			    	//box.hide();
	        	    //box.hide();
			    	if(res.message == "Succ"){
			    		form.reset();
			    		Ext.getCmp('suppliersNotice').setValue('');
		        	    Ext.getCmp('idNotice').setValue(Math.floor(1000000 + Math.random() * 900000));
			    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 'Aviso creado con exito.');
			    	}else if(res.message == "Error JDE"){
			    		Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.approvalRespErrorJDE);
			    	}else if(res.message == "Succ Update"){
			    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 
			    				SuppAppMsg.approvalRespUpdateSupp + " " + o.jsonData.addresNumber);
			    	}else if(res.message == "Rejected"){
			    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 
			    				SuppAppMsg.approvalRespRejected + " " + o.jsonData.ticketId);
			    	}else{
			    		Ext.Msg.alert(SuppAppMsg.approvalResponse, res.message);
			    	}
		        	
					
				},       // If you don't pass success:true, it will always go here
		        failure: function(fp, o) {
			    	Ext.MessageBox.show({
		                title: 'Error',
		                msg: SuppAppMsg.approvalUpdateError,
		                buttons: Ext.Msg.OK
		            });
		        }
			});
        	/*var record = Ext.create('SupplierApp.model.Notice',form.getFieldValues());
        	values = form.getFieldValues();
        	updatedRecord = populateObj(record, values);
            
            if (values.id > 0){
            	Ext.Msg.alert( SuppAppMsg.usersSaveError, "Error");
    		} else{
    	    	var box = Ext.MessageBox.wait(SuppAppMsg.usersSaveDataMsj, SuppAppMsg.approvalExecution);
    	    	
    	    	Ext.Ajax.request({
				    url: 'notice/save.action',
				    method: 'POST',
				    jsonData: record.data, 
				    params : {
				    	uploadItem : fileNotice
					},
				    success: function(fp, o) {
				    	var res = Ext.decode(fp.responseText);
				    	//grid.store.load();
				    	box.hide();
				    	form.reset();
		        	    box.hide();
		        	    Ext.getCmp('suppliersNotice').setValue('');
		        	    Ext.getCmp('idNotice').setValue(Math.floor(1000000 + Math.random() * 900000));
				    	if(res.message == "Succ"){
				    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 'Aviso creado con exito.');
				    	}else if(res.message == "Error JDE"){
				    		Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.approvalRespErrorJDE);
				    	}else if(res.message == "Succ Update"){
				    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 
				    				SuppAppMsg.approvalRespUpdateSupp + " " + o.jsonData.addresNumber);
				    	}else if(res.message == "Rejected"){
				    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 
				    				SuppAppMsg.approvalRespRejected + " " + o.jsonData.ticketId);
				    	}
			        	//Ext.Msg.alert('Respuesta', res.message);
				    },
				    failure: function() {
				    	box.hide();
				    	Ext.MessageBox.show({
			                title: 'Error',
			                msg: SuppAppMsg.approvalUpdateError,
			                buttons: Ext.Msg.OK
			            });
				    }
				}); 
    		}*/
    	}
    },
    
    resetNoticeForm: function (button) {
    	var form = this.getNoticeForm().getForm();
    	form.reset();
    	Ext.getCmp('saveNotice').setDisabled(false);
    	Ext.getCmp('suppliersNotice').setValue('');
		Ext.getCmp('idNotice').setValue(Math.floor(1000000 + Math.random() * 900000));
    	//Ext.getCmp('usersFormEmail').setReadOnly(false);
    	//his.enableSave(); 
    },
        
    updateUsers: function (button) {
    	var form = this.getUsersForm().getForm();
    	var grid = this.getUsersGrid();
    	if (form.isValid()) { 
        	var record = form.getRecord();
        	values = form.getFieldValues();
        	updatedRecord = populateObj(record, values);

            if (values.id > 0){
    	    	var box = Ext.MessageBox.wait(SuppAppMsg.usersSaveDataMsj, SuppAppMsg.approvalExecution);
    	    	record.set(updatedRecord);
				record.save();
        	    grid.store.load();
        	    form.reset();
        	    this.enableSave();
        	    box.hide();
    		} else{
    			Ext.Msg.alert(SuppAppMsg.supplierUpdateFail, "Error");
    		}
    	}
    },
    
    deleteUsers: function (button) {
    	var form = this.getUsersForm().getForm();
    	var grid = this.getUsersGrid();
		record = form.getRecord();
		values = form.getFieldValues();
		if (values.id > 0){
			updatedRecord = populateObj(record, values);
			record.set(updatedRecord);
			this.getUsersStore().remove(record);
			this.getUsersStore().sync({callback: function() {},
                success: function() {
                	 grid.store.load();
            	     form.reset();
             },                            
             failure: function(batch, options) {
            	 Ext.Msg.alert(SuppAppMsg.usersSaveErrorTitle);
                }
            });
			this.getUsersStore().reload();
			form.reset();
		} else{
			Ext.Msg.alert(SuppAppMsg.usersDeleteError);
		}
    },

	enableUpdate: function(){
		Ext.getCmp('saveNotice').setDisabled(true);
		Ext.getCmp('updateUsers').setDisabled(false);
		Ext.getCmp('deleteUsers').setDisabled(false);	
	},

	enableSave: function(){
		Ext.getCmp('saveNotice').setDisabled(false);
		Ext.getCmp('updateUsers').setDisabled(true);
		Ext.getCmp('deleteUsers').setDisabled(true);
	},
	
	initController: function(){
	}
});