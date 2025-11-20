Ext.define('SupplierApp.view.fiscalDocuments.FiscalDocumentsGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.fiscalDocumentsGrid',
    loadMask: true,
	frame:false,
	border:false,
	cls: 'extra-large-cell-grid',  
    store:'FiscalDocuments',
	scroll : true,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
    	
    	var docType = null;
    	var invStatus = null;
    	
    	docType = Ext.create('Ext.data.Store', {
        	    fields: ['id', 'name'],
        	    data : [
        	        {"id":"FACTURA", "name":"Factura"},
        	        {"id":"FACT EXTRANJERO", "name":"Factura Extranjeros"}//,{"id":"NOTACREDITO", "name":"Nota de Credito"}
        	    ]
        	});

    	invStatus = Ext.create('Ext.data.Store', {
    	    fields: ['id', 'name'],
    	    data : [    	        
    	        {"id":"PENDIENTE", "name":"PENDIENTE"},
    	        {"id":"APROBADO", "name":"APROBADO"},
    	        //{"id":"RECHAZADO", "name":"RECHAZADO"},
    	        {"id":"PAGADO", "name":"PAGADO"},
    	        {"id":"COMPLEMENTO", "name":"COMPLEMENTO"}
    	    ]
    	});
    	
    	Ext.define('docTypeCombo', {
    	    extend: 'Ext.form.ComboBox',
    	    fieldLabel: SuppAppMsg.fiscalTitle21,
    	    store: docType,
    	    alias: 'widget.combodoctype',
    	    queryMode: 'local',
    	    allowBlank:false,
    	    editable: false,
    	    displayField: 'name',
			width:230,
    	    labelWidth:90,
    	    valueField: 'id',
    	    margin:'20 20 0 10',
    	    id:'comboDocumentType',    	    
    	   /* listeners: {
    	        afterrender: function() {
    	           if(role == 'ROLE_WNS'){
    	        	   this.setValue("STATUS_OC_PROCESSED");    
    	           }
    	        }
    	    }*/
    	});
    	
    	Ext.define('statusCombo', {
    	    extend: 'Ext.form.ComboBox',
    	    fieldLabel: SuppAppMsg.fiscalTitle22,
    	    store: invStatus,
    	    alias: 'widget.combostatus',
    	    queryMode: 'local',
    	    allowBlank:false,
    	    editable: false,
    	    displayField: 'name',
			width:150,
    	    labelWidth:40,
    	    valueField: 'id',
    	    margin:'20 20 0 10',
    	    id:'comboInvoiceStatus',
    	   /* listeners: {
    	        afterrender: function() {
    	           if(role == 'ROLE_WNS'){
    	        	   this.setValue("STATUS_OC_PROCESSED");    
    	           }
    	        }
    	    }*/
    	});
    	
        this.columns = [
			           {
			            text     : SuppAppMsg.suppliersNumber,
			            width: 90,
			            dataIndex: 'addressNumber'
			        },{
			            text     : SuppAppMsg.suppliersNameSupplier,
			            width: 230,
			            dataIndex: 'supplierName',
			            hidden: true,
			            hideable: false,//Para que no aparezca en la lista de "Columnas"
			        },{
			            text     : SuppAppMsg.fiscalTitle30,
			            width: 90,
			            dataIndex: 'rfcReceptor'
			        },{
			            text     : SuppAppMsg.purchaseTitle5,
			            width: 90,
			            dataIndex: 'orderType'
			        },{
			            text     : SuppAppMsg.fiscalTitle3,
			            width: 90,
			            dataIndex: 'folio'
			        },{
			            text     : SuppAppMsg.fiscalTitle4,
			            width: 80,
			            dataIndex: 'serie'
			        } ,{
			            text     : SuppAppMsg.fiscalTitle26,
			            width: 90,
			            dataIndex: 'invoiceUploadDate',
			            renderer : Ext.util.Format.dateRenderer("d-m-Y")
			        } ,{
			            text     : SuppAppMsg.fiscalTitle29,
			            width: 90,
			            dataIndex: 'paymentDate',
						renderer: function(value, metaData, record, row, col, store, gridView){
							if(value) {
								return Ext.util.Format.date(new Date(value), 'd-m-Y');
							} else {
								return null;
							}
						}
			        },{
			            text     : SuppAppMsg.fiscalTitle27,
			            width: 100,
			            dataIndex: 'amount',
						align: 'center',
			            renderer : Ext.util.Format.numberRenderer('0,0.00')
			        },{
			            text     : SuppAppMsg.fiscalTitle20,
			            width: 110,
						align: 'center',
			            dataIndex: 'conceptTotalAmount',
			            renderer : Ext.util.Format.numberRenderer('0,0.00'),
			            hidden: true,
			            hideable: false,//Para que no aparezca en la lista de "Columnas"
			        },{
			            text     : SuppAppMsg.fiscalTitle28,
			            width: 100,
						align: 'center',
			            renderer : function(value, metadata, record, rowIndex, colIndex, store){
									return Ext.util.Format.number((record.data.amount + record.data.conceptTotalAmount),'0,0.00')
						}
			        },{
			            text     : SuppAppMsg.purchaseOrderCurrency,
			            width: 90,
			            dataIndex: 'moneda'
			        },{
			            text     : SuppAppMsg.fiscalTitle5,
			            width: 250,
			            dataIndex: 'uuidFactura'
			        },{
			            text     : SuppAppMsg.fiscalTitle22,
			            width: 110,
			            dataIndex: 'status'
			        },{
			            text     : SuppAppMsg.approvalLevel,
			            width: 120,
			            dataIndex: 'approvalStep'
			        },{
			            text     : SuppAppMsg.approvalCurrentApprover,
			            width: 200,
			            dataIndex: 'currentApprover',
			            hidden: true,
			            hideable: false,//Para que no aparezca en la lista de "Columnas"
			        },{
			        	xtype: 'actioncolumn', 
			            width: 90,
			            header: SuppAppMsg.approvalApprove,
			            align: 'center',
						name : 'approveInvoiceFD',
						id : 'approveInvoiceFD',
						hidden: role=='ROLE_ADMIN' || role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' ?false:true,
						itemId : 'approveInvoiceFD',
			            style: 'text-align:center;',
			            hideable: false,//Para que no aparezca en la lista de "Columnas"
			            items: [{
			            	icon:'resources/images/accept.png',
			            	getClass: function(v, metadata, r, rowIndex, colIndex, store) {
			              		if(!(r.data.status == "PENDIENTE" && (userName.toUpperCase()==r.data.currentApprover.toUpperCase() || role=='ROLE_ADMIN'))) {
				              		return "x-hide-display";
				              	}
			              	},
			              	text: SuppAppMsg.approvalApprove,
			              	handler: function(grid, rowIndex, colIndex) {
			              		this.fireEvent('buttonclick', grid, rowIndex, colIndex);
			              	}
			            }]
			        },{
			        	xtype: 'actioncolumn', 
			            width: 90,
			            header: SuppAppMsg.approvalReject,
			            align: 'center',
						name : 'rejectInvoiceFD',
						hidden: role=='ROLE_ADMIN' || role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' ?false:true,
						itemId : 'rejectInvoiceFD',
						id: 'rejectInvoiceFD',
			            style: 'text-align:center;',
			            hideable: false,//Para que no aparezca en la lista de "Columnas"
			            items: [
			            	{
			            	icon:'resources/images/close.png',
			            	getClass: function(v, metadata, r, rowIndex, colIndex, store) {
			              		if(!(r.data.status == "PENDIENTE" && (userName.toUpperCase()==r.data.currentApprover.toUpperCase() || role=='ROLE_ADMIN'))) {
				              		return "x-hide-display";
				              	}
			            	},
			            	text: SuppAppMsg.approvalReject,
			            	handler: function(grid, rowIndex, colIndex) {
			            		this.fireEvent('buttonclick', grid, rowIndex, colIndex);
			            	}
			            }]
			        }];
        
        this.tbar = [{
			iconCls : 'icon-save',
			itemId : 'uploadNewFiscalDoc',
			id : 'uploadNewFiscalDoc',
			text : SuppAppMsg.fiscalTitle18,
			//hidden:true,
			action : 'uploadNewFiscalDoc',
			hidden: true
		}, {
			name : 'searchFiscalDocuments',
			itemId : 'searchFiscalDocuments',
			emptyText : SuppAppMsg.fiscalTitle19,
			xtype : 'trigger',
			width : 300,
			margin: '5 0 10 0',
			hidden:true,
			triggerCls : 'x-form-search-trigger',
			onTriggerClick : function(e) {
				this.fireEvent("ontriggerclick", this, event);
			},
			enableKeyEvents : true,
			listeners : {
				specialkey : function(field, e) {
					if (e.ENTER === e.getKey()) {
						field.onTriggerClick();
					}
				}
			}
		},{ 
			xtype: 'combodoctype'
		},{ 
			xtype: 'textfield',
            fieldLabel: SuppAppMsg.purchaseInvoiceNumber,
            id: 'pFolio',
            itemId: 'pFolio',
            name:'pFolio',
            width:200,
            labelWidth:80,
            margin:'20 20 0 10'
		},{
			xtype: 'textfield',
            fieldLabel: 'UUID',
            id: 'fdUUID',
            itemId: 'fdUUID',
            name:'fdUUID',
            //value: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?addressNumber:'',
            //fieldStyle: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?'border:none;background-color: #ddd; background-image: none;':'',
            //readOnly: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?true:false,
            width:300,
            labelWidth:30,
            margin:'20 20 0 10'
		},{ 
			xtype: 'combostatus'
		},{
			xtype: 'textfield',
            fieldLabel: SuppAppMsg.suppliersNumber,
            id: 'supNumberFD',
            itemId: 'supNumberFD',
            name:'supNumberFD',
            value: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?addressNumber:'',
            fieldStyle: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?'border:none;background-color: #ddd; background-image: none;':'',
            readOnly: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?true:false,
            width:200,
            labelWidth:80,
            margin:'20 20 0 10'
		},{
       		xtype:'button',
            text: SuppAppMsg.suppliersSearch,
            iconCls: 'icon-appgo',
            action:'fdSearch',
            cls: 'buttonStyle',
            margin:'0 20 0 10'
		},{
       		xtype:'button',
            text: SuppAppMsg.purchaseTitle58,
            iconCls: 'icon-accept',
            action:'poUploadInvoiceFile',
            cls: 'buttonStyle',
            hidden:true,
            margin:'0 20 0 10'
		},{
			xtype:'button',
			text: SuppAppMsg.purchaseOrderCCPG,
			iconCls: 'icon-accept',
			action:'fdLoadCompl',
			cls: 'buttonStyle',
			margin:'0 20 0 10',
			hidden: true
		}/*,{
       		xtype:'button',
            text: SuppAppMsg.purchaseTitle59,
            iconCls: 'icon-accept',
            action:'poUploadCreditNoteFile',
            cls: 'buttonStyle',
            margin:'0 20 0 10'
		}*/ 
		];
        
		this.bbar = Ext.create('Ext.PagingToolbar', {
			store: this.store,
			displayInfo: true
		});
		
        this.callParent(arguments);
    }
});