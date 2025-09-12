Ext.define('SupplierApp.view.supplier.SupplierGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.supplierGrid',
    loadMask: true,
	frame:false,
	border:false,
	cls: 'extra-large-cell-grid', 
	scroll : false,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
    	
		this.store =  storeAndModelFactory('SupplierApp.model.SupplierDTO',
                'supplierModel',
                'supplier/searchSupplier.action', 
                false,
                {
			        supAddNbr : '',
	    			supAddName :''
                },
			    "", 
			    11);
		 var me = this.store;

						        this.columns = [
								{
									text : SuppAppMsg.suppliersNumber,
									width : 120,
									dataIndex : 'addresNumber'
								},
								{
									text : SuppAppMsg.suppliersNameSupplier,
									width : 420,
									dataIndex : 'razonSocial'
								},
								{
									xtype : 'checkcolumn',
									text : SuppAppMsg.suppliersRepse,
									width : 110,
									dataIndex : 'repse',
									hidden : role == 'ROLE_ADMIN' ? false
											: true,
									change : function(cb, newValue, oldValue,
											eOpts) {
										console.log(cb, cb.myFlag);
										console.log(newValue);
										console.log(oldValue);
										console.log(eOpts);
									},
									listeners : {
										checkchange : function(component,
												rowIndex, checked, eOpts) {
											var store =me;
						                    var record = store.getAt(rowIndex);
						                    
						                	Ext.MessageBox.show({
				                                title:checked?'Activar':'Desactivar',
				                                msg: '¿Desea modificar la condición del proveedor? ',
				                                buttons: Ext.MessageBox.YESNO,
				                                fn: function showResult(btn){
				                                    if(btn == 'yes'){
				                                    	Ext.Ajax.request({
					            						    url: 'supplier/update.action',
					            						    method: 'POST',
					            						    async: false,
					            						    jsonData: record.data,
					            						    success: function(fp, o) {
					            						    	debugger;
					            						    	var res = Ext.decode(fp.responseText);
					            						    	if(res.message == "Success"){
					            						    		if(!checked){
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm82);
					            						    		}else{
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm83);
					            						    		}
					            						    		
					            						    	}else{
					            						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, res.message);
					            						    	}
//					            						    	record.set('outSourcingMonthlyAccept',checked);
					            						    },
					            						    failure: function() {
					            						    	//box.hide();
					            						    	Ext.MessageBox.show({
					            					                title: 'Error',
					            					                msg: SuppAppMsg.approvalUpdateError,
					            					                buttons: Ext.Msg.OK
					            					            });
					            						    }
					            						}); 
				                    				
				                                    }else{
				                                    	debugger
				                                    	record.set('repse',!checked);
				                                    }
				                                }
				                    	            
				                    	 });
						                    
											
										}
									},
								},
								{
									xtype : 'checkcolumn',
									text : SuppAppMsg.outSourcingAccept,
									width : 110,
									dataIndex : 'outSourcingAccept',
									hidden : role == 'ROLE_ADMIN' ? false
											: true,
									change : function(cb, newValue, oldValue,
											eOpts) {
										console.log(cb, cb.myFlag);
										console.log(newValue);
										console.log(oldValue);
										console.log(eOpts);
									},
									listeners : {
										checkchange : function(component,
												rowIndex, checked, eOpts) {
											var store =me;
						                    var record = store.getAt(rowIndex);
						                    
						                	Ext.MessageBox.show({
				                                title:checked?'Activar':'Desactivar',
				                                msg: checked? '¿Desea dejar de solicitar esta información?': '¿Desea solicitar esta información?',
				                                buttons: Ext.MessageBox.YESNO,
				                                fn: function showResult(btn){
				                                    if(btn == 'yes'){
				                                    	Ext.Ajax.request({
					            						    url: 'supplier/update.action',
					            						    method: 'POST',
					            						    async: false,
					            						    jsonData: record.data,
					            						    success: function(fp, o) {
					            						    	debugger;
					            						    	var res = Ext.decode(fp.responseText);
					            						    	if(res.message == "Success"){
					            						    		if(!checked){
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm82);
					            						    		}else{
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm83);
					            						    		}
					            						    		
					            						    	}else{
					            						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, res.message);
					            						    	}
//					            						    	record.set('outSourcingMonthlyAccept',checked);
					            						    },
					            						    failure: function() {
					            						    	//box.hide();
					            						    	Ext.MessageBox.show({
					            					                title: 'Error',
					            					                msg: SuppAppMsg.approvalUpdateError,
					            					                buttons: Ext.Msg.OK
					            					            });
					            						    }
					            						}); 
				                    				
				                                    }else{
				                                    	debugger
				                                    	record.set('outSourcingAccept',!checked);
				                                    }
				                                }
				                    	            
				                    	 });
						                    
											
											
										}
									},
								},
								{
									xtype : 'checkcolumn',
									text : SuppAppMsg.outSourcingMonthlyAccept,
									width : 110,
									dataIndex : 'outSourcingMonthlyAccept',
									hidden : role == 'ROLE_ADMIN' ? false
											: true,
									change : function(cb, newValue, oldValue,
											eOpts) {
										console.log(cb, cb.myFlag);
										console.log(newValue);
										console.log(oldValue);
										console.log(eOpts);
									},
									listeners : {
										checkchange : function(component,
												rowIndex, checked, eOpts) {
											var store =me;
						                    var record = store.getAt(rowIndex);
						                    
						                	Ext.MessageBox.show({
				                                title:checked?'Activar':'Desactivar',
				                                msg: checked? '¿Desea dejar de solicitar esta información?': '¿Desea solicitar esta información?',
				                                buttons: Ext.MessageBox.YESNO,
				                                fn: function showResult(btn){
				                                    if(btn == 'yes'){
				                                    	Ext.Ajax.request({
					            						    url: 'supplier/update.action',
					            						    method: 'POST',
					            						    async: false,
					            						    jsonData: record.data,
					            						    success: function(fp, o) {
					            						    	debugger;
					            						    	var res = Ext.decode(fp.responseText);
					            						    	if(res.message == "Success"){
					            						    		if(!checked){
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm82);
					            						    		}else{
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm83);
					            						    		}
					            						    		
					            						    	}else{
					            						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, res.message);
					            						    	}
//					            						    	record.set('outSourcingMonthlyAccept',checked);
					            						    },
					            						    failure: function() {
					            						    	//box.hide();
					            						    	Ext.MessageBox.show({
					            					                title: 'Error',
					            					                msg: SuppAppMsg.approvalUpdateError,
					            					                buttons: Ext.Msg.OK
					            					            });
					            						    }
					            						}); 
				                    				
				                                    }else{
				                                    	debugger
				                                    	record.set('outSourcingMonthlyAccept',!checked);
				                                    }
				                                }
				                    	            
				                    	 });
						                    
											
										}
									},
								},
								{
									xtype : 'checkcolumn',
									text : SuppAppMsg.outSourcingBimonthlyAccept,
									width : 110,
									dataIndex : 'outSourcingBimonthlyAccept',
									hidden : role == 'ROLE_ADMIN' ? false
											: true,
									change : function(cb, newValue, oldValue,
											eOpts) {
										console.log(cb, cb.myFlag);
										console.log(newValue);
										console.log(oldValue);
										console.log(eOpts);
									},
									listeners : {
										checkchange : function(component,
												rowIndex, checked, eOpts) {
											var store =me;
						                    var record = store.getAt(rowIndex);
						                    
						                	Ext.MessageBox.show({
				                                title:checked?'Activar':'Desactivar',
				                                msg: checked? '¿Desea dejar de solicitar esta información?': '¿Desea solicitar esta información?',
				                                buttons: Ext.MessageBox.YESNO,
				                                fn: function showResult(btn){
				                                    if(btn == 'yes'){
				                                    	Ext.Ajax.request({
					            						    url: 'supplier/update.action',
					            						    method: 'POST',
					            						    async: false,
					            						    jsonData: record.data,
					            						    success: function(fp, o) {
					            						    	debugger;
					            						    	var res = Ext.decode(fp.responseText);
					            						    	if(res.message == "Success"){
					            						    		if(!checked){
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm82);
					            						    		}else{
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm83);
					            						    		}
					            						    		
					            						    	}else{
					            						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, res.message);
					            						    	}
//					            						    	record.set('outSourcingMonthlyAccept',checked);
					            						    },
					            						    failure: function() {
					            						    	//box.hide();
					            						    	Ext.MessageBox.show({
					            					                title: 'Error',
					            					                msg: SuppAppMsg.approvalUpdateError,
					            					                buttons: Ext.Msg.OK
					            					            });
					            						    }
					            						}); 
				                    				
				                                    }else{
				                                    	debugger
				                                    	record.set('outSourcingBimonthlyAccept',!checked);
				                                    }
				                                }
				                    	            
				                    	 });
						                    
											
										}
									},
								},
								{
									xtype : 'checkcolumn',
									text : SuppAppMsg.outSourcingQuarterlyAccept,
									width : 110,
									dataIndex : 'outSourcingQuarterlyAccept',
									hidden : role == 'ROLE_ADMIN' ? false
											: true,
									change : function(cb, newValue, oldValue,
											eOpts) {
										console.log(cb, cb.myFlag);
										console.log(newValue);
										console.log(oldValue);
										console.log(eOpts);
									},
									listeners : {
										checkchange : function(component,
												rowIndex, checked, eOpts) {
											var store =me;
						                    var record = store.getAt(rowIndex);
						                    
						                	Ext.MessageBox.show({
				                                title:checked?'Activar':'Desactivar',
				                                msg: checked? '¿Desea dejar de solicitar esta información?': '¿Desea solicitar esta información?',
				                                buttons: Ext.MessageBox.YESNO,
				                                fn: function showResult(btn){
				                                    if(btn == 'yes'){
				                                    	Ext.Ajax.request({
					            						    url: 'supplier/update.action',
					            						    method: 'POST',
					            						    async: false,
					            						    jsonData: record.data,
					            						    success: function(fp, o) {
					            						    	debugger;
					            						    	var res = Ext.decode(fp.responseText);
					            						    	if(res.message == "Success"){
					            						    		if(!checked){
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm82);
					            						    		}else{
					            						    			Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.supplierForm83);
					            						    		}
					            						    		
					            						    	}else{
					            						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, res.message);
					            						    	}
//					            						    	record.set('outSourcingMonthlyAccept',checked);
					            						    },
					            						    failure: function() {
					            						    	//box.hide();
					            						    	Ext.MessageBox.show({
					            					                title: 'Error',
					            					                msg: SuppAppMsg.approvalUpdateError,
					            					                buttons: Ext.Msg.OK
					            					            });
					            						    }
					            						}); 
				                    				
				                                    }else{
				                                    	debugger
				                                    	record.set('outSourcingQuarterlyAccept',!checked);
				                                    }
				                                }
				                    	            
				                    	 });
						                    
											
										}
									},
								},
								{
									xtype : 'actioncolumn',
									width : 90,
									header : SuppAppMsg.suppliersDisable,
									align : 'center',
									name : 'disableSupplier',
									hidden : role == 'ROLE_ADMIN' ? false
											: true,
									itemId : 'disableSupplier',
									style : 'text-align:center;',
									items : [ {
										icon : 'resources/images/cancel.jpg',
										getClass : function(v, metadata, r,
												rowIndex, colIndex, store) {
											if (r.data.observaciones == "INHABILITADO") {
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

								} ];
        
        
        this.tbar = [{
			xtype: 'textfield',
            fieldLabel: SuppAppMsg.suppliersNumber,
            id: 'supAddNbr',
            itemId: 'supAddNbr',
            name:'supAddNbr',
            value: role.includes('ROLE_SUPPLIER')?addressNumber:'',
            fieldStyle: role.includes('ROLE_SUPPLIER')?'border:none;background-color: #ddd; background-image: none;':'',
            readOnly: role.includes('ROLE_SUPPLIER')?true:false,
            width:200,
            labelWidth:90,
            margin:'20 20 20 20'
		},{
			xtype: 'textfield',
            fieldLabel: SuppAppMsg.suppliersName,
            id: 'supAddName',
            itemId: 'supAddName',
            name:'supAddName',
            value: role.includes('ROLE_SUPPLIER')?addressNumber:'',
            fieldStyle: role.includes('ROLE_SUPPLIER')?'border:none;background-color: #ddd; background-image: none;':'',
            readOnly: role.includes('ROLE_SUPPLIER')?true:false,
            width:500,
            labelWidth:70,
            margin:'20 20 20 0'
		},{
       		xtype:'button',
            hidden: role.includes('ROLE_SUPPLIER')?true:false,
            text: SuppAppMsg.suppliersSearch,
            iconCls: 'icon-doSearch',
            action:'supAddNbrSrch',
            margin:'20 20 20 20'
		},'-',{
       		xtype:'button',
            hidden: role=='ROLE_ADMIN'?false:true,
            text: SuppAppMsg.suppliersLoadFile,
            iconCls: 'icon-excel',
            action:'uploadSuppliersFile',
            margin:'20 20 20 20'
		},'-',{
       		xtype:'button',
            hidden: true,
            text: 'Replicar proveedores',
            action:'replicateSupplier',
            iconCls: 'icon-doSearch',
            margin:'20 20 20 20'
          }];

		this.bbar = Ext.create('Ext.PagingToolbar', {
		store: this.store,
		displayInfo: true
		});
      
        this.callParent(arguments);
    }
});