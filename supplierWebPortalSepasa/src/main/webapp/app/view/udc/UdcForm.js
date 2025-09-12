Ext.define('SupplierApp.view.udc.UdcForm' ,{
	extend: 'Ext.form.Panel',
	alias : 'widget.udcForm',
	border:true,
	  initComponent: function() {
			this.items= [{
				xtype: 'container',
				layout: 'hbox',
				margin: '15 15 0 10',
        		style:'border-bottom: 1px dotted #fff;padding-bottom:10px',
				defaults: { 
					labelWidth: 50, 
					align: 'stretch'
				},
				items       :[{
					xtype: 'hidden',
					name: 'id'

				},{
     				xtype : 'hidden',
     				name : 'createdBy',
     				//id:'createdBy'
     			},{
     				xtype : 'hidden',
     				name : 'creationDate',
     				//id:'creationDate',
     				format : 'd-M-Y',
     			},{
					xtype:'textfield',
					fieldLabel: 'System',
					name: 'udcSystem',
					id: 'udcSystem',
					width:300,
					disabled:false,
					allowBlank:false,
					fieldCls: 'outlineField',
					listeners:{
						change: function(field, newValue, oldValue){
							field.setValue(newValue.toUpperCase());
						}
					}
				},
				{
					xtype:'textfield',
					labelWidth: 40,
					fieldLabel: 'Llave',
					name: 'udcKey',
					id: 'udcKey',
					width:200,
					padding:'0 0 0 35',
					disabled:false,
					allowBlank:false,
					fieldCls: 'outlineField',
					listeners:{
						change: function(field, newValue, oldValue){
							//field.setValue(newValue.toUpperCase());
						}
					}
				}]
			},
			{
				xtype: 'container',
				layout: 'hbox',
				margin: '0 15 0 10',
				width:'100%',
				defaults: { 
					labelWidth: 60, 
					align: 'stretch'
				},
				items :[{
					xtype:'textfield',
					fieldLabel: 'strValue1',
					name: 'strValue1',
					width:550

				},{

					xtype:'textfield',
					fieldLabel: 'strValue2',
					name: 'strValue2',
					width:250,
					padding:'0 0 0 15'
				}
				]
			},
			{
				xtype: 'container',
				layout: 'hbox',
				margin: '10 15 0 0',
				defaults: { 
					labelWidth: 75, 
					align: 'stretch'
				},
				items:[,{
					xtype: 'numberfield',
					fieldLabel: 'intValue',
					name: 'intValue',
					width:150,
					padding:'0 0 0 10',
					hideTrigger : true
				},
				{
					xtype: 'datefield',
					fieldLabel: 'dateValue',
					name: 'dateValue',
					width: 200,
					format: 'd-M-Y',
					padding:'0 0 0 25'
				},{
			    	   xtype: 'checkbox',
			    	   boxLabel:'boolValue',
			    	   name: 'booleanValue',
			    	   padding:'0 0 0 45'
			       },{
				    	   xtype:'textfield',
				    	   fieldLabel: 'KeyRef',
				    	   name: 'keyRef',
				    	   width:250,
				    	   padding:'0 0 0 25',
				            listeners:{
								change: function(field, newValue, oldValue){
									field.setValue(newValue.toLowerCase());
								}
							}
				       },{
				    	   xtype:'textfield',
				    	   fieldLabel: 'SysRef',
				    	   name: 'SystemRef',
				    	   width:250,
				    	   padding:'0 0 0 30'
				       }]
			}];
			
			this.tbar=[      
			      {
			    	  iconCls: 'icon-save',
			    	  id: 'udcSave',
			    	  itemId: 'save',
			    	  text: SuppAppMsg.usersSave,
			    	  action: 'save'
			      },'-',
			      {
			    	  iconCls: 'icon-delete',
			    	  id: 'udcDelete',
			    	  itemId: 'delete',
			    	  text: 'Eliminar',
			    	  action: 'delete',
			    	  hidden: true
			      },'-',
			      {
			    	  iconCls: 'icon-accept',
			    	  id: 'udcUpdate',
			    	  itemId: 'update',
			    	  text: SuppAppMsg.usersUpdate,
			    	  action: 'update',
			    	  disabled: true
			      },'-',
			      {
			    	  iconCls: 'icon-add',
			    	  itemId: 'udcNew',
			    	  text: SuppAppMsg.usersNew,
			    	  action: 'new'
			      },'-',
			      {
                      name: 'searchUdc',
                      itemId: 'searchUdc',
                      emptyText: SuppAppMsg.suppliersSearch,
                      xtype: 'trigger',
                      triggerCls: 'x-form-search-trigger',
                      onTriggerClick: function(e) {
                    	  this.fireEvent("ontriggerclick", this, event);
                      },
                      enableKeyEvents: true,
                      listeners: {
                    	  specialkey: function (field, e) {
                    		  if (e.ENTER === e.getKey()) {
                    			  field.onTriggerClick();
                    		  }
                    	  }
                      }
              }];
		  this.callParent(arguments);	    
	  }

});