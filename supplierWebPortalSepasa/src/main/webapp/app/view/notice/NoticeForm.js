var mx=false;
var nomx=false;
var repse=false;
var norepse=false;
var fisica=false;
var moral=false;

Ext.define('SupplierApp.view.notice.NoticeForm', {
	extend : 'Ext.form.Panel',
	alias : 'widget.noticeForm',
	border : false,
	frame : false,
	style: 'border: solid #ccc 1px',
	autoScroll : true,
	initComponent : function() {
        
		this.items = [ {
			xtype : 'container',
			layout : 'vbox',
			margin : '15 15 0 10',
			style : 'border-bottom: 1px dotted #fff;padding-bottom:10px',
			defaults : {
				labelWidth : 150,
				margin : '5 15 5 0',
				xtype : 'textfield',
				labelAlign: 'left'
			},
			items : [ {
				xtype : 'hidden',
				name : 'id'
			},{
				fieldLabel : 'Id Notice', 
				name : 'idNotice',
				id : 'idNotice',
				width : 500,
				readOnly:true
				//hidden:true,
			},{
				fieldLabel : 'created By', 
				name : 'createdBy',
				id : 'createdBy',
				width : 500,
				value : userName, 
				hidden:true,
			}, {
				fieldLabel : 'Titulo de aviso', 
				name : 'noticeTitle',
				//id : 'usersFormUserName',
				width : 500,
				allowBlank : false
			},{
				xtype: 'container',
				layout: 'hbox',
				id:'docConteinerNotice',
				colspan:3,
				width:800,
				//hidden:role=='ANONYMOUS'?false:true,
				defaults : {
					labelWidth : 150,
					xtype : 'textfield',
					margin: '0 0 0 0'
				},
		        items:[{
		        	xtype : 'textfield',
					fieldLabel : 'Archivo del Aviso',
					name : 'noticeFile',
					id : 'noticeFile',
					width:400,
					readOnly:true,
					margin: '0 10 0 0',
					allowBlank:false
				    },{
						xtype: 'button',
						width:90,
						itemId : 'loadNoticeFile',
						id : 'loadNoticeFile',
						text : SuppAppMsg.supplierLoad,
						action : 'loadNoticeFile'
					}]
		    
		    },{
				fieldLabel : 'Periodicidad',
				name : 'frequency',
				id : 'frequency',
				xtype: 'combobox',
				typeAhead: true,
                typeAheadDelay: 100,
                allowBlank:false,
                minChars: 1,
                queryMode: 'local',
                //forceSelection: true,
				store : getAutoLoadUDCStore('FREQUENCY', '', '', ''),
                displayField: 'strValue1',
                valueField: 'udcKey',
                width : 300,
                editable: false,
				colspan:3,
				listeners: {
			    	select: function (comboBox, records, eOpts) {
			    		var contrib = records[0].data.udcKey;
			    		/*if(contrib == 'OTHER'){
			    			Ext.getCmp('noticeFromDate').show();
			    			Ext.getCmp('noticeFromDate').allowBlank=false;
			    			Ext.getCmp('noticeToDate').show();
			    			Ext.getCmp('noticeToDate').allowBlank=false;
			    		}else{
			    			Ext.getCmp('noticeFromDate').hide();
			    			Ext.getCmp('noticeFromDate').allowBlank=true;
			    			Ext.getCmp('noticeToDate').hide();
			    			Ext.getCmp('noticeToDate').allowBlank=true;
			    		}*/
			    	}
			    }
			
		    }/*,{
				xtype : 'displayfield',
				value : 'Vigencia',
				height:20,
				//margin: '50 0 0 10',
				colspan:3,
				//fieldStyle: 'font-weight:bold'
		   },{
				xtype: 'datefield',
	            fieldLabel: SuppAppMsg.purchaseOrderDesde,
	            id: 'noticeFromDate',
	            itemId: 'noticeFromDate',
	            name:'noticeFromDate',
	            allowBlank:true,
	            editable: false,
	            width:300,
	            labelWidth:150,
	            	listeners:{
						change: function(field, newValue, oldValue){
							Ext.getCmp("noticeToDate").setMinValue(newValue);
						}
					},
			},{
				xtype: 'datefield',
	            fieldLabel: SuppAppMsg.purchaseOrderHasta,
	            id: 'noticeToDate',
	            itemId: 'noticeToDate',
	            name:'noticeToDate',
	            //minValue: new Date(),
	            editable: false,
	            allowBlank:true,
	            width:300,
	            labelWidth:150,
	            //margin:'0 40 0 10'
			}*/,{

				xtype: 'container',
				layout: 'hbox',
				id:'docConteinerDatesNotice',
				colspan:3,
				width:500,
				//hidden:role=='ANONYMOUS'?false:true,
				defaults : {
					labelWidth : 150,
					xtype : 'textfield',
					margin: '0 0 0 0'
				},
		        items:[{
					xtype : 'displayfield',
					value : 'Vigencia',
					height:20,
					margin: '0 110 0 0',
					colspan:3,
					//fieldStyle: 'font-weight:bold'
			   
		        },{
					xtype: 'datefield',
		            fieldLabel: SuppAppMsg.purchaseOrderDesde,
		            format: 'm/d/Y',
		            id: 'noticeFromDate',
		            itemId: 'noticeFromDate',
		            name:'noticeFromDate',
		            allowBlank:true,
		            editable: false,
		            margin: '0 20 0 0',
		            width:150,
		            labelWidth:50,
		            	listeners:{
							change: function(field, newValue, oldValue){
								Ext.getCmp("noticeToDate").setMinValue(newValue);
							}
						},
					},{
						xtype: 'datefield',
			            fieldLabel: SuppAppMsg.purchaseOrderHasta,
			            format: 'm/d/Y',
			            id: 'noticeToDate',
			            itemId: 'noticeToDate',
			            name:'noticeToDate',
			            //minValue: new Date(),
			            editable: false,
			            allowBlank:true,
			            width:150,
			            labelWidth:50,
			            //margin:'0 40 0 10'
					}]
		    
		    
			}/*,{
				fieldLabel : 'Vigencia',
				name : 'days',
				//id : 'usersFormUserName',
				width : 300,
				allowBlank : false
			}*/,{
				xtype : 'checkbox',
				fieldLabel : 'Obligatorio',
				name : 'required',
				width : 300,
				checked: false
			},{
				xtype : 'checkbox',
				fieldLabel : 'Proveedores por layout',
				name : 'suppLayout',
				id : 'suppLayout',
				width : 300,
				checked: false,
				listeners: {
	                change: function (check, records, eOpts) {
				      	var value = Ext.getCmp('suppLayout').getValue();
				      	
				      	if(value == true){
				      		Ext.getCmp('fileNotice').show();
			    			Ext.getCmp('fileNotice').allowBlank=false;
			    			
				      		Ext.getCmp('filters').hide();
			    			Ext.getCmp('filters').allowBlank=true;
			    			Ext.getCmp('suppliersNotice').hide();
			    			Ext.getCmp('suppliersNotice').allowBlank=true;
			    			
			    			Ext.getCmp('all_sup').hide();
			    			
			    			Ext.getCmp("filters").setValue('');
			    			Ext.getCmp("suppliersNotice").setValue('');
			    			Ext.getCmp("all_sup").setValue(false);
			    		}else{
			    			Ext.getCmp('fileNotice').hide();
			    			Ext.getCmp('fileNotice').allowBlank=true;
			    			
			    			Ext.getCmp('filters').show();
			    			Ext.getCmp('filters').allowBlank=false;
			    			Ext.getCmp('suppliersNotice').show();
			    			Ext.getCmp('suppliersNotice').allowBlank=false;
			    			Ext.getCmp('noticeToDate').show();
			    			
			    			Ext.getCmp('all_sup').show();
			    			
			    			Ext.getCmp("filters").setValue('');
			    			Ext.getCmp("suppliersNotice").setValue('');
			    			Ext.getCmp("all_sup").setValue(false);
			    		}
	                }
			    },
			},{
				xtype : 'filefield',
				name : 'file', 
				id : 'fileNotice',
				fieldLabel :SuppAppMsg.purchaseFile,
				labelWidth : 150,
				width : 500,
				msgTarget : 'side',
				allowBlank : true,
				//allowBlank : response.data.docSupplier == true ?false:true,
				//hidden: response.data.docSupplier == true ?false:true,
				hidden: true,
				margin:'10 0 8 0',
				anchor : '80%',
				buttonText : SuppAppMsg.suppliersSearch
			},{
				xtype: 'combobox',
                name: 'filters',
                id: 'filters',
                fieldLabel: 'Tipo de proveedor',
                typeAhead: true,
                typeAheadDelay: 100,
                allowBlank:false,
                //margin:'10 0 0 10',
                minChars: 1,
                multiSelect: true,
                colspan:3,
                queryMode: 'local',
                //forceSelection: true,
                store : getAutoLoadUDCStore('SUPPLIERTYPE', '', '', ''),
                displayField: 'strValue1',
                valueField: 'udcKey',
                width:400,
                readOnly:role =='ROLE_SUPPLIER' ?true:false,
                editable: false,
			    listeners: {
			    	beforedeselect: function(comboBox, records, index, eOpts) {
			    		switch (records.data.udcKey) {
				      	  case 'MX':
					      		var record = comboBox.store.findRecord('udcKey', 'EXT');
					    		record.set('disabled', !record.get('disabled'));
				      		    break;
				      	  case 'EXT':
					      		var record = comboBox.store.findRecord('udcKey', 'MX');
					    		record.set('disabled', !record.get('disabled'));
				                break;
				      	  case 'REPSE':
					      		var record = comboBox.store.findRecord('udcKey', 'NO_OUT');
					    		record.set('disabled', !record.get('disabled'));
				                break;
				      	  case 'NO_OUT':
					      		var record = comboBox.store.findRecord('udcKey', 'REPSE');
					    		record.set('disabled', !record.get('disabled'));
				                break;
				      	  case 'FISICA':
					      		var record = comboBox.store.findRecord('udcKey', 'MORAL');
					    		record.set('disabled', !record.get('disabled'));
				                break;
				      	  case 'MORAL':
					      		var record = comboBox.store.findRecord('udcKey', 'FISICA');
					    		record.set('disabled', !record.get('disabled'));
				                break;
				      	  case 'FILTER':
				      		if(records.get('disabled')==false || records.get('disabled')=='' || records.get('disabled')== undefined){
				      			var record = comboBox.store.findRecord('udcKey', 'FISICA');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'MORAL');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'REPSE');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'NO_OUT');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'MX');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'EXT');
					    		record.set('disabled', !record.get('disabled'));
				      		}
				      			break;
				      	  default:
				              break;
				      	};
		            },
			    	beforeselect: function( comboBox, records, index, eOpts ) {
			    		switch (records.data.udcKey) {
				      	  case 'MX':
				      		if(records.get('disabled')==false || records.get('disabled')=='' || records.get('disabled')== undefined){
				      			var record = comboBox.store.findRecord('udcKey', 'EXT');
					    		record.set('disabled', !record.get('disabled'));
				      		}
				      		    break;
				      	  case 'EXT':
				      		if(records.get('disabled')==false || records.get('disabled')=='' || records.get('disabled')== undefined){
				      			var record = comboBox.store.findRecord('udcKey', 'MX');
					    		record.set('disabled', !record.get('disabled'));
				      		}	
				                break;
				      	  case 'REPSE':
				      		if(records.get('disabled')==false || records.get('disabled')=='' || records.get('disabled')== undefined){
				      			var record = comboBox.store.findRecord('udcKey', 'NO_OUT');
					    		record.set('disabled', !record.get('disabled'));
				      		}	
				                break;
				      	  case 'NO_OUT':
				      		if(records.get('disabled')==false || records.get('disabled')=='' || records.get('disabled')== undefined){
				      			var record = comboBox.store.findRecord('udcKey', 'REPSE');
					    		record.set('disabled', !record.get('disabled'));
				      		}	
				                break;
				      	  case 'FISICA':
				      		if(records.get('disabled')==false || records.get('disabled')=='' || records.get('disabled')== undefined){
				      			var record = comboBox.store.findRecord('udcKey', 'MORAL');
					    		record.set('disabled', !record.get('disabled'));
				      		}	
				                break;
				      	  case 'MORAL':
				      		if(records.get('disabled')==false || records.get('disabled')=='' || records.get('disabled')== undefined){
				      			var record = comboBox.store.findRecord('udcKey', 'FISICA');
					    		record.set('disabled', !record.get('disabled'));
				      		}	
				                break;
				      	  case 'FILTER':
				      		if(records.get('disabled')==false || records.get('disabled')=='' || records.get('disabled')== undefined){
				      			var record = comboBox.store.findRecord('udcKey', 'FISICA');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'MORAL');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'REPSE');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'NO_OUT');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'MX');
					    		record.set('disabled', !record.get('disabled'));
					    		
					    		var record = comboBox.store.findRecord('udcKey', 'EXT');
					    		record.set('disabled', !record.get('disabled'));
				      		}
				      			break;
				      	  default:
				              break;
				      	};
				      	
				      	return !records.get('disabled');
	                },
	                change: function (comboBox, records, eOpts) {
				      	var value = Ext.getCmp('filters').getValue();
				      	var taxIdCombo = Ext.getCmp('suppliersNotice');
				      	Ext.getCmp("suppliersNotice").setValue('');
			    		var store = taxIdCombo.getStore();
			    		
			    		store.removeAll();
			    		store.proxy.extraParams = { 
			    				query:value.length>0?value:''
			        	    	}
			        	store.load();
	                }
			    },
			    tpl: Ext.create('Ext.XTemplate',
		                '<tpl for=".">',
		                    '<div class="x-boundlist-item" ',
		                    '<tpl if="disabled">',
		                        ' style="color: #ddd;"',
		                    '</tpl>',
		                    '>{strValue1}</div>',
		                '</tpl>'
		            ),
			},{
				xtype : 'checkbox',
				fieldLabel : 'Todos los proveedores',
				name : 'all_sup',
				id : 'all_sup',
				width : 300,
				checked: false,
				listeners: {
	                change: function (check, records, eOpts) {
	                	
				      	var value = Ext.getCmp('all_sup').getValue();
				      	var taxIdCombo = Ext.getCmp('suppliersNotice');
				      	
				      	if(value == true){
				      		var store = taxIdCombo.getStore();
				    		var ids_data = store.data.keys;
				    		const items = [];
				    		for(var i=0; i < ids_data.length; i++){
				    			var item = store.findRecord('id', ids_data[i]);
				    			items.push(item.data.addresNumber);
							}
				    		
				    		Ext.getCmp("suppliersNotice").setValue(items);
				    		Ext.getCmp('suppliersNotice').setReadOnly(true);
				      	}else{
				      		Ext.getCmp("suppliersNotice").setValue('');
				    		Ext.getCmp('suppliersNotice').setReadOnly(false);
				      	}
			    		
	                }
			    },
			},{
				xtype: 'combobox',
                name: 'suppliersNotice',
                id: 'suppliersNotice',
                fieldLabel: 'Proveedores', 
                typeAhead: true,
                typeAheadDelay: 100,
                allowBlank:false,
                //margin:'10 0 0 10',
                minChars: 1,
                multiSelect: true,
                colspan:3,
                queryMode: 'local',
                //forceSelection: true,
                store : getSuppliersByFilter(''),
                displayField: 'razonSocial',
                valueField: 'addresNumber',
                width:400,
                readOnly:role =='ROLE_SUPPLIER' ?true:false,
                editable: false
			},{
				xtype : 'checkbox',
				fieldLabel : 'Notificacion por email',
				name : 'emailNotif',
				width : 300,
				checked: false
			},{
				xtype : 'checkbox',
				fieldLabel : 'Documento de proveedor',
				name : 'docSupplier',
				width : 300,
				checked: false
			},{
				xtype : 'checkbox',
				fieldLabel : 'Activo',
				name : 'enabled',
				width : 300,
				readOnly:true,
				checked: true
			}			
			]
		} ];

		this.tbar = [ {
			iconCls : 'icon-save',
			itemId : 'saveNotice',
			id : 'saveNotice',
			text : SuppAppMsg.usersSave,
			action : 'saveNotice'
		}, {
			iconCls : 'icon-delete',
			//itemId : 'deleteUsers',
			//id : 'deleteUsers',
			text : 'Eliminar',
			action : 'deleteUsers',
			disabled : true,
			hidden:true
		}, '-', {
			iconCls : 'icon-accept',
			//itemId : 'updateUsers',
			//id : 'updateUsers',
			text : SuppAppMsg.usersUpdate,
			action : 'updateUsers',
			disabled : true,
			hidden:true
		}, '-', {
			iconCls : 'icon-add',
			//itemId : 'usersNew',
			text : SuppAppMsg.usersNew,
			action : 'noticeNew',
			margin : '5 0 10 0'
		} ];
		this.callParent(arguments);
	}

});