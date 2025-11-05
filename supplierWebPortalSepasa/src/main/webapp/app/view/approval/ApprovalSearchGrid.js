Ext.define('SupplierApp.view.approval.ApprovalSearchGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.approvalSearchGrid',
    loadMask: true,
	frame:false,
	border:false,
	cls: 'extra-large-cell-grid', 
	scroll : false,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto'}
	},
    initComponent: function() {
    	
    	Ext.define('TStatus', {
    	    extend: 'Ext.data.Model',
    	    fields: [
    	        {type: 'string', name: 'status'},
    	        {type: 'string', name: 'description'}
    	    ]
    	});
    	
    	var ticketStatus = [
    		{"status":"","description":"ALL"},
    	    {"status":"PENDIENTE","description":"PENDIENTE"},
    	    {"status":"RECHAZADO","description":"RECHAZADO"},
    	    {"status":"RENEW","description":"ACTUALIZADO"},
    	    {"status":"DRAFT","description":"DRAFT"}

    	];
    	
    	var ticketSatusStore = Ext.create('Ext.data.Store', {
            autoDestroy: true,
            model: 'TStatus',
            data: ticketStatus
        });
    	
    	
    	Ext.define('AppLevel', {
    	    extend: 'Ext.data.Model',
    	    fields: [
    	        {type: 'string', name: 'status'},
    	        {type: 'string', name: 'description'}
    	    ]
    	});
    	
    	var appLelvel = [
    		{"status":"","description":"ALL"},
    	    {"status":"FIRST","description":"FIRST"},
    	    {"status":"SECOND","description":"SECOND"},
    	    {"status":"THIRD","description":"THIRD"}

    	];
    	
    	var apprLevelStore = Ext.create('Ext.data.Store', {
            autoDestroy: true,
            model: 'AppLevel',
            data: appLelvel
        });
    	
		this.store =  storeAndModelFactory('SupplierApp.model.SupplierDTO',
                'approvalSearchModel',
                'approval/search.action', 
                false,
                {
					  ticketId:'',
					  approvalStep:'',
					  approvalStatus:'',
					  fechaAprobacion:'',
					  currentApprover:'',
					  name:''
                },
			    "", 
			    100);
 
		var gridStore = this.store;
		
        this.columns = [
        	{   
        	   text: SuppAppMsg.approvalTicket,
               width: 100,
               dataIndex: 'ticketId'
           },{   
        	   text: SuppAppMsg.suppliersNumber ,
               width: 100,
               dataIndex: 'addresNumber'
           },{
        	   text     : SuppAppMsg.suppliersNameSupplier,
        	   width: 300,
        	   dataIndex: 'razonSocial'
	       },{
	            text     : SuppAppMsg.approvalLevel,
	            width: 120,
	            dataIndex: 'approvalStep'
	       },{
	            text     : SuppAppMsg.approvalCurrentApprover,
	            width: 120,
	            dataIndex: 'currentApprover'
	       },{
	            text     : SuppAppMsg.approvalNextApprover,
	            width: 120,
	            dataIndex: 'nextApprover'
	       },{
	            text     : 'Status',
	            width: 90,
	            dataIndex: 'approvalStatus'
	       },{
	            text     : SuppAppMsg.approvalRequestDate,
	            width: 90,
	            dataIndex: 'fechaSolicitud',
	            renderer : Ext.util.Format.dateRenderer("d-m-Y")
	       },{
	            xtype: 'actioncolumn', 
	            width: 60,
	            header: SuppAppMsg.purchaseTitle30,
	            align: 'center',
				name : 'openAppNotes',
				itemId : 'openAppNotes',
	            style: 'text-align:center;',
	            items: [
	            	{
	            	icon:'resources/images/notepad.png',
	          	     iconCls: 'increaseSize',
	            	  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
	              		  if((r.data.approvalNotes == null || r.data.approvalNotes == '') &&
	              			(r.data.rejectNotes == null || r.data.rejectNotes == '')) {
	        	              return "x-hide-display";
	        	          }else{
	        	        	  return "increaseSize";
	        	          }
	              	  },
	                  handler: function(grid, rowIndex, colIndex) {
	                  	this.fireEvent('buttonclick', grid, rowIndex, colIndex);
	             }}]
	        }/*,{
	            text     : SuppAppMsg.purchaseTitle30,
	            width: 300,
	            dataIndex: 'approvalNotes'
	       }*/,{
	        	xtype: 'actioncolumn', 
	        	hidden:role=='ROLE_ADMIN'?false:true,
	            width: 90,
	            header: SuppAppMsg.reasignRequest,
	            align: 'center',
				name : 'reasignRequest',
				itemId : 'reasignRequest',
	            style: 'text-align:center;',
	            items: [{
	                text: 'REASIGNAR',
	                iconCls:'user-group',
		          	getClass: function(v, metadata, r, rowIndex, colIndex, store) {
		            	if ((r.data.approvalStep !== 'FIRST'
		            		&& r.data.approvalStep !== 'SECOND'
		            		&& r.data.approvalStep !== 'THIRD')
		            		|| r.data.currentApprover === 'REJECT') {
		            		return "x-hide-display";
		            	} else {
		            		return "user-group";
		            	}
		          	},
	                handler: function(grid, rowIndex, colIndex) {
	                	var record = grid.store.getAt(rowIndex);
	                	var me = this;
	                    var usrstore = getUsersByApprovalStepStore(record.data.approvalStep);
	                	var formPanel =  {
	                		        xtype       : 'form',
	                		        height      : 125,
	                		        items       : [
	                		        	{
											fieldLabel : SuppAppMsg.newApprover,
											xtype: 'combobox',
											id:'newApproverId',
											store : usrstore,
							                displayField: 'userName',
							                valueField: 'userName',
							                labelWidth:100,
							                width : 300,
							                margin: '10 10 0 10'
										},{
											xtype: 'button',
											width:100,
											text : SuppAppMsg.reasignRequest,
											margin: '12 10 0 10',
											cls: 'buttonStyle',
											iconCls : 'icon-appgo',
											listeners: {
											    click: function() {
											    	if(Ext.getCmp('newApproverId').getValue() != null && Ext.getCmp('newApproverId').getValue() != ""){
											    		var box = Ext.MessageBox.wait(
								    							SuppAppMsg.supplierProcessRequest,
								    							SuppAppMsg.approvalExecution);
										    			Ext.Ajax.request({
														    url: 'approval/reasignApprover.action',
														    method: 'POST',
														    params: {
														    	id:record.data.id,
														    	newApprover: Ext.getCmp('newApproverId').getValue()
													        },
														    success: function(fp, o) {
														    	var res = Ext.decode(fp.responseText); 
														    	box.hide();
														    	if(res.message == "success"){
														    		grid.store.load();
														    		me.reasignWindow.destroy();
														    		Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.newApproverSuccess);
														    	}
													        	
														    },
														    failure: function() {
														    	box.hide();
														    	Ext.MessageBox.show({
													                title: 'Error',
													                msg: 'Surgio un error al actualizar',
													                buttons: Ext.Msg.OK
													            });
														    }
														});
											    	}
											    	
											    }
										    }
										}
	                		        ]
	                		    };
	                	
	                	
	                	me.reasignWindow = new Ext.Window({
	                		layout : 'fit',
	                		title : SuppAppMsg.reasignapproverRequest + record.data.ticketId ,
	                		width : 350,
	                		height : 120,
	                		modal : true,
	                		closeAction : 'destroy',
	                		resizable : false,
	                		minimizable : false,
	                		maximizable : false,
	                		autoScroll: false,
	                		plain : true,
	                		items : [formPanel]
	                	});
	                	
	                	me.reasignWindow.show();
	                }
	            }]
	        },
			{
				xtype : 'actioncolumn',
				width : 90,
				header : window.navigator.language.startsWith("es", 0)? 'Eliminar':'Delete',
				align : 'center',
				name : 'deleteSupplier',
				hidden : role == 'ROLE_ADMIN' ? false : true,
				itemId : 'deleteSupplier',
				style : 'text-align:center;',
				items : [ {
					icon : 'resources/images/cancel.jpg',
					getClass : function(v, metadata, r, rowIndex, colIndex, store) {
						var nonAddress = false;
						if(r.data.addresNumber=='')nonAddress=true;
						if(r.data.addresNumber=='0')nonAddress=true;
						
						if (!nonAddress) {
							return "x-hide-display";
						}
					},
					text : 'RECHAZAR',
					handler : function(grid, rowIndex,
							colIndex) {
						this.fireEvent('buttonclick', grid,
								rowIndex, colIndex);
					}
				} ]

			}];

        this.tbar = [{
			xtype: 'textfield',
            fieldLabel: SuppAppMsg.suppliersName,
            id: 'supSearchName',
            itemId: 'supSearchName',
            name:'supSearchName',
            width:200,
            labelWidth:70,
            labelAlign:'top',
            margin:'10 20 10 5',
            listeners:{
				change: function(field, newValue, oldValue){
					field.setValue(newValue.toUpperCase());
				}
			}
		},{
			xtype: 'textfield',
            fieldLabel: SuppAppMsg.approvalTicket,
            id: 'supSearchTicket',
            itemId: 'supSearchTicket',
            name:'supSearchTicket',
            width:120,
            labelWidth:70,
            labelAlign:'top',
            margin:'0 20 10 10',
            listeners:{
				change: function(field, newValue, oldValue){
					field.setValue(newValue.toUpperCase());
				}
			}
		},{
			fieldLabel : SuppAppMsg.purchaseOrderStatus,
			name : 'supSearchTicketSts',
			id : 'supSearchTicketSts',
			xtype: 'combobox',
            queryMode: 'local',
			store : ticketSatusStore,
            displayField: 'description',
            valueField: 'status',
            width : 150,
            labelAlign:'top'
		},{
			fieldLabel : SuppAppMsg.approvalLevel,
			name : 'supSearchApprLevel',
			id : 'supSearchApprLevel',
			xtype: 'combobox',
            queryMode: 'local',
			store : apprLevelStore,
            displayField: 'description',
            valueField: 'status',
            width : 150,
            labelAlign:'top',
            margin:'0 20 10 20',
		},{
			xtype: 'textfield',
            fieldLabel: SuppAppMsg.approvalCurrentApprover,
            id: 'supSearchApprover',
            itemId: 'supSearchApprover',
            name:'supSearchApprover',
            width:120,
            labelWidth:70,
            labelAlign:'top',
            margin:'0 20 10 10'
		},{
       		xtype:'button',
            text: 'Buscar',
            action:'searchAppSupplier',
            iconCls: 'icon-doSearch',
            margin:'0 0 0 20'
          }];

        
		this.bbar = Ext.create('Ext.PagingToolbar', {
			store: this.store,
			displayInfo: false
		});
      
        this.callParent(arguments);
    }
});