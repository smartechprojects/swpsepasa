	Ext.define('SupplierApp.view.purchaseOrder.PurchaseOrderGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.purchaseOrderGrid',
    loadMask: true,
	frame:false,
	border:false,
	selModel: {
        checkOnly: true,
        mode: 'SIMPLE'
    },
    selType: 'checkboxmodel',
	cls: 'extra-large-cell-grid',  
	scroll : false,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
	 
    	var status = null;
    	
    	if(role == 'ROLE_SUPPLIER'){
        	status = Ext.create('Ext.data.Store', {
        	    fields: ['id', 'name'],
        	    data : [
        	    	{"id":"STATUS_OC_REQUESTED", "name":SuppAppMsg.purchaseStatus8},        	    	
        	    	{"id":"STATUS_OC_RECEIVED", "name":SuppAppMsg.purchaseStatus6},
        	        {"id":"STATUS_OC_APPROVED", "name":SuppAppMsg.purchaseStatus7},
        	        {"id":"STATUS_OC_REJECTED", "name":SuppAppMsg.purchaseStatus9},
        	        {"id":"STATUS_OC_PENDING", "name":SuppAppMsg.purchaseStatus10},
        	        {"id":"STATUS_OC_INVOICED", "name":SuppAppMsg.purchaseStatus2},
        	        {"id":"STATUS_OC_PAID", "name":SuppAppMsg.purchaseStatus3},
        	        {"id":"STATUS_OC_PAYMENT_COMPL", "name":SuppAppMsg.purchaseStatus4}
        	        //{"id":"STATUS_OC_CANCEL", "name":SuppAppMsg.purchaseStatus5}
        	    ]
        	});
    	}else{
    		status = Ext.create('Ext.data.Store', {
        	    fields: ['id', 'name'],
        	    data : [
        	    	{"id":"STATUS_OC_REQUESTED", "name":SuppAppMsg.purchaseStatus8},
        	        {"id":"STATUS_OC_RECEIVED", "name":SuppAppMsg.purchaseStatus6},
        	        {"id":"STATUS_OC_APPROVED", "name":SuppAppMsg.purchaseStatus7},
        	        {"id":"STATUS_OC_REJECTED", "name":SuppAppMsg.purchaseStatus9},
        	        {"id":"STATUS_OC_PENDING", "name":SuppAppMsg.purchaseStatus10},
        	        {"id":"STATUS_OC_INVOICED", "name":SuppAppMsg.purchaseStatus2},
        	        {"id":"STATUS_OC_PAID", "name":SuppAppMsg.purchaseStatus3},
        	        {"id":"STATUS_OC_PAYMENT_COMPL", "name":SuppAppMsg.purchaseStatus4}
        	        //{"id":"STATUS_OC_CANCEL", "name":SuppAppMsg.purchaseStatus5}
        	    ]
        	});
    	}

    	
    	Ext.define('statusCombo', {
    	    extend: 'Ext.form.ComboBox',
    	    fieldLabel: 'Status',
    	    store: status,
    	    alias: 'widget.combostatus',
    	    queryMode: 'local',
    	    displayField: 'name',
    	    labelWidth:40,
    	    valueField: 'id',
    	    margin:'20 20 0 0',
    	    id:'combostatus',
    	    width:250,
    	    listeners: {
    	        afterrender: function() {
    	           if(role == 'ROLE_WNS'){
    	        	   this.setValue("STATUS_OC_PROCESSED");    
    	           }
    	        }
    	    }
    	});
    	
		this.store =  storeAndModelFactory('SupplierApp.model.PurchaseOrder',
                'orderModel',
                'supplier/orders/searchOrders.action', 
                false,
                {
					poNumber:'0',
					supNumber:'',
					poFromDate:'',
					poToDate:'',
					status:'OC ENVIADA',
					userName:userName
                },
			    "", 
			    10);
 
        this.columns = [
           {
        	text     : SuppAppMsg.paymentTitle1,
			width: 70,
			dataIndex: 'orderCompany'
		},{
            text     : SuppAppMsg.purchaseOrderNumber,
            width: 80,
            dataIndex: 'orderNumber'
        },{
            text     : SuppAppMsg.purchaseOrderType,
            width: 40,
            dataIndex: 'orderType'
        },{
            text     : SuppAppMsg.purchaseOrderSupplier,
            width: 80,
            dataIndex: 'addressNumber'
        },{
        	text     : SuppAppMsg.suppliersName,
            width: 280,
            dataIndex: 'longCompanyName'
        },{
            text     : SuppAppMsg.purchaseOrderFechaAprovacion,
            width: 120,
            dataIndex: 'promiseDelivery',
            renderer : Ext.util.Format.dateRenderer("d-m-Y")
        },{
            text     : 'Fecha estimada de recibo',
            width: 150,
            dataIndex: 'promiseDelivery',
            renderer : Ext.util.Format.dateRenderer("d-m-Y"),
            hidden:true
        },{
            text     : SuppAppMsg.purchaseOrderÌmporteTotal,
            width: 110,
            dataIndex: 'orderAmount',
            renderer: function(value, meta, record) {
            	if(record.data.currecyCode == "PME"){
            		return Ext.util.Format.number(record.data.orderAmount,'0,0.00');
            	}else{
            		return Ext.util.Format.number(record.data.foreignAmount,'0,0.00');
            	}
            }
        },{
            text     : SuppAppMsg.purchaseOrderCurrency,
            width: 60,
            dataIndex: 'currecyCode'
        },{
            hidden:true,
            dataIndex: 'invoiceUuid'
        },{
            hidden:true,
            dataIndex: 'supplierEmail'
        },{
            hidden:true,
            dataIndex: 'paymentUuid'
        },{
            hidden:true,
            dataIndex: 'paymentType'
        },{
            hidden:true,
            dataIndex: 'invoiceNumber'
        },{
            text     : 'Status',
            width: 120,
            dataIndex: 'orderStauts',
            hidden:true
        },{
            align: 'left',
            text     : 'Status',
            width: 200,
            renderer: function(value, meta, record) {
            	var status = {
            			STATUS_OC_REJECTED: 'OC RECHAZADA',
            			STATUS_OC_REQUESTED: 'OC SOLICITADA',
            	        STATUS_OC_RECEIVED: 'OC RECIBIDA',
            	        STATUS_OC_APPROVED: 'OC APROBADA',
            	        STATUS_OC_SENT: 'OC ENVIADA',
            	        STATUS_OC_CLOSED: 'OC CERRADA',
            	        STATUS_OC_PENDING: 'OC PENDIENTE',
            	        STATUS_OC_INVOICED: 'OC FACTURADA',
            	        STATUS_OC_PROCESSED: 'OC PROCESADA',
            	        STATUS_OC_PAID: 'OC PAGADA',
            	        STATUS_OC_PAYMENT_COMPL: 'OC COMPLEMENTO',
            	        STATUS_OC_CANCEL: 'OC CANCELADA',
            	        STATUS_OC_OBSOLETE: 'OC OBSOLETA'
            	};
            	            	
            	switch (record.data.orderStauts) {
            	  case status.STATUS_OC_REJECTED:
	          	    return "ORDEN RECHAZADA";
	          	    break;					
	          	  case status.STATUS_OC_REQUESTED:
	          	    return "ORDEN SOLICITADA";
	          	    break;
            	  case status.STATUS_OC_RECEIVED:
            	    return "ORDEN LIBERADA";
            	    break;
            	  case status.STATUS_OC_APPROVED:
              	    return "ORDEN APROBADA";
              	    break;
            	  case status.STATUS_OC_SENT:
              	    return "ORDEN LIBERADA";
              	    break;
            	  case status.STATUS_OC_PENDING:
            		return "ORDEN CON FACTURAS PENDIENTES";
            		break;
            	  case status.STATUS_OC_INVOICED:
              	    return "ORDEN CON FACTURAS";
              	    break;
            	  case status.STATUS_OC_RECEIVED:
              	    return "ORDEN RECIBIDA";
              	    break;
            	  case status.STATUS_OC_PROCESSED:
              	    return "FACTURA ACEPTADA";
              	    break;
            	  case status.STATUS_OC_PAID:
              	    return "FACTURA CON PAGO";
              	    break;
            	  case status.STATUS_OC_PAYMENT_COMPL:
                	return "PAGOS Y COMPL CARGADO";
              	    break;
            	  case status.STATUS_OC_CANCEL:
              	    return "ORDEN CANCELADA";
              	    break;
            	  case status.STATUS_OC_OBSOLETE:
                	return "ORDEN OBSOLETA";
                	break;
              	  default:
              		break;
            	}

             }
        },{
            text     : 'Status de Factura',
            width: 100,
            dataIndex: 'status',
            hidden:true
        },{
            text     : SuppAppMsg.purchaseOrderRecibosFacturas,
            align: 'center',
            width: 120,
            renderer: function(value, meta, record) {
            	if(record.data.orderStauts == "OC SOLICITADA" || record.data.orderStauts == "OC RECHAZADA" || record.data.orderType == "P0") {
            		return "";
            	}
            	
            	var id = Ext.id();
        		 Ext.defer(function(){
        			 
                     // Buscar y destruir cualquier componente previo
                     var existingComponent = Ext.getCmp(id);
                     if (existingComponent) {
                         existingComponent.destroy();
                     }
                     
                     new Ext.Button({
             			 name : 'showReceipts',
            			 itemId : 'showReceipts',
            			 iconCls:'icon-document',
                         text: SuppAppMsg.purchaseOpen,
                         handler: function(grid, rowIndex, colIndex) {
                         	this.fireEvent('buttonclick', grid, record);
                         }
                     }).render(document.body, id);
                 },50);
                 return Ext.String.format('<div id="{0}"></div>', id);
		            	
             }
        },{
            text     : SuppAppMsg.purchaseOrderCreditNotes,
            align: 'center',
            width: 120,
            renderer: function(value, meta, record) {
            	if(record.data.orderStauts == "OC SOLICITADA" || record.data.orderStauts == "OC RECHAZADA" || record.data.orderType == "P0") {
            		return "";
            	}
            	        		  
              	var id = Ext.id();
              	Ext.defer(function(){
              		
                    // Buscar y destruir cualquier componente previo
                    var existingComponent = Ext.getCmp(id);
                    if (existingComponent) {
                        existingComponent.destroy();
                    }
                    
                    new Ext.Button({
                    	name : 'showCreditNotes',
                    	itemId : 'showCreditNotes',
                    	iconCls:'icon-cancel',
                        text: SuppAppMsg.purchaseOpen,
                        handler: function(grid, rowIndex, colIndex) {
                        	this.fireEvent('buttonclick', grid, record);
                        }
                    }).render(document.body, id);
                },50);
                return Ext.String.format('<div id="{0}"></div>', id);
             }
        },
        
        {
        	xtype: 'actioncolumn', 
        	//hidden:role=='ROLE_ADMIN' || role.includes('ROLE_RH') || role.includes('ROLE_TAX') || role.includes('ROLE_LEGAL') || role.includes('ROLE_3RDPARTY')?false:true,
        	hidden:role=='ROLE_ADMIN' || role.includes('ROLE_SUPPLIER')?false:true,
        	width: 90,
            header: SuppAppMsg.approvalApprove,
            align: 'center',
			name : 'approveDoc',
			itemId : 'approveDoc',
            style: 'text-align:center;',
            items: [
            	{
            		icon:'resources/images/accept.png',
            	  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
              		  if(r.data.orderStauts != "OC SOLICITADA" ||  r.data.orderType == "P0") {
        	              return "x-hide-display";
        	          }else{
        	        	  return "increaseSize";
        	          }
              	   },
                  handler: function(grid, rowIndex, colIndex) {
                  	this.fireEvent('buttonclick', grid, rowIndex, colIndex);
             }}]        	
        },
        {
        	xtype: 'actioncolumn', 
            //hidden:role=='ROLE_ADMIN' || role.includes('ROLE_RH') || role.includes('ROLE_TAX') || role.includes('ROLE_LEGAL') || role.includes('ROLE_3RDPARTY')?false:true,
            hidden:role=='ROLE_ADMIN' || role.includes('ROLE_SUPPLIER')?false:true,            
        	width: 90,
            header: SuppAppMsg.approvalReject,
            align: 'center',
			name : 'rejectDoc',
			itemId : 'rejectDoc',
            style: 'text-align:center;',
            items: [
            	{
            	  icon:'resources/images/close.png',
            	  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
              		  if(r.data.orderStauts != "OC SOLICITADA" || r.data.orderType == "P0") {
        	              return "x-hide-display";
        	          }else{
        	        	  return "increaseSize";
        	          }
              	  },
                  handler: function(grid, rowIndex, colIndex) {
                  	this.fireEvent('buttonclick', grid, rowIndex, colIndex);
             }}]        	
        },{
        	text     : SuppAppMsg.purchaseTitle36,
            align: 'center',
            width: 120,
            hidden:role=='ROLE_ADMIN' || role.includes('ROLE_SUPPLIER') || role.includes('ROLE_CXP') ?false:true, 
            renderer: function(value, meta, record) {
                if (record.data.orderType == "P0" && record.data.orderStauts == "OC SOLICITADA" ) {
                    // Generar un ID único para el botón en esta celda
                    var id = Ext.id();
                    
                    Ext.defer(function() {
                    	
                        // Buscar y destruir cualquier componente previo
                        var existingComponent = Ext.getCmp(id);
                        if (existingComponent) {
                            existingComponent.destroy();
                        }
                        
                        new Ext.Button({
                            renderTo: id,
                            name: 'uploadInvoiceWithoutReceipt',
                            itemId: 'uploadInvoiceWithoutReceipt',
                            iconCls: 'icon-save',
                            text: SuppAppMsg.purchaseTitle36,
                            handler: function(grid, rowIndex, colIndex) {
                                this.fireEvent('buttonclick', grid, rowIndex, colIndex, record);
                            }
                        });
                        
                    }, 50);

                    // Retornar un contenedor para que el botón se renderice
                    return Ext.String.format('<div id="{0}"></div>', id);
                }

                return ''; // Si no se cumplen las condiciones, dejar vacío  	
             }
        }
        /*
        ,{
            align: 'center',
            width: 140,
            hidden:true,
            renderer: function(value, meta, record) {
	            	var id = Ext.id();
	            	var status = {
	            			STATUS_OC_REJECTED: 'OC RECHAZADA',
	            			STATUS_OC_REQUESTED: 'OC SOLICITADA',	            			
	            	        STATUS_OC_RECEIVED: 'OC RECIBIDA',
	            	        STATUS_OC_APPROVED: 'OC APROBADA',
	            	        STATUS_OC_SENT: 'OC ENVIADA',
	            	        STATUS_OC_CLOSED: 'OC CERRADA',
	            	        STATUS_OC_PENDING: 'OC PENDIENTE',
	            	        STATUS_OC_INVOICED: 'OC FACTURADA',
	            	        STATUS_OC_PROCESSED: 'OC PROCESADA',
	            	        STATUS_OC_PAID: 'OC PAGADA',
	            	        STATUS_OC_PAYMENT_COMPL: 'OC COMPLEMENTO',
	            	        STATUS_OC_CANCEL: 'OC CANCELADA',
	            	        STATUS_OC_OBSOLETA: 'OC OBSOLETA'
	            	};
	            	
	            	var showButton = false;
	            	
	            	switch (record.data.orderStauts) {
	            	  case status.STATUS_OC_REJECTED:
		            		showButton = false;
		            	    break;
		              case status.STATUS_OC_REQUESTED:
			            	showButton = false;
			            	break;	            	
	            	  case status.STATUS_OC_RECEIVED:
	            		showButton = false;
	            	    break;
	            	  case status.STATUS_OC_APPROVED:
		            	showButton = false;
		            	break;
	            	  case status.STATUS_OC_SENT:
	            		showButton = false;
	              	    break;
	            	  case status.STATUS_OC_PENDING:
	            		showButton = true;
	            		break;
	            	  case status.STATUS_OC_INVOICED:
	            		showButton = true;
	              	    break;
	            	  case status.STATUS_OC_RECEIVED:
	            		showButton = true;
	              	    break;
	            	  case status.STATUS_OC_PROCESSED:
	            		  showButton = true;
	              	    break;
	            	  case status.STATUS_OC_PAID:
	            		  showButton = true;
	              	    break;
	            	  case status.STATUS_OC_PAYMENT_COMPL:
	            		  showButton = false;
	              	    break;
	            	  case status.STATUS_OC_CANCEL:
	            		  showButton = false;
	              	    break;
	            	  case status.STATUS_OC_OBSOLETA:
	            		  showButton = false;
	              	  break;
	              	  default:
	              		break;
	            	}
	
	            	if(role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role == 'ROLE_ADMIN' || role == 'ROLE_MANAGER'){
		            	if(showButton){
			        		 Ext.defer(function(){
			                     new Ext.Button({
			             			 name : 'uploadPayment',
			            			 itemId : 'uploadPayment',
			            			 iconCls:'icon-add',
			                         text: SuppAppMsg.suppliersLoadFile,
			                         handler: function(grid, rowIndex, colIndex) {
			                         	this.fireEvent('buttonclick', grid, record);
			                         }
			                     }).render(document.body, id);
			                 },50);
			
			                 return Ext.String.format('<div id="{0}"></div>', id);
		            	}
	            	}
             }
        }*/
        ];
        
        this.dockedItems = [
            {
                xtype: 'toolbar',
                style: {
                    background: 'white'
                  },
                dock: 'top',
                items: [
                	{
						xtype: 'textfield',
			            fieldLabel: SuppAppMsg.purchaseOrderNumber,
			            id: 'poNumber',
			            itemId: 'poNumber',
			            name:'poNumber',
			            width:200,
			            labelWidth:70,
			            margin:'0 20 0 10'
					},{
						xtype: 'textfield',
			            fieldLabel: SuppAppMsg.purchaseInvoiceNumber,
			            id: 'pfolio',
			            itemId: 'pfolio',
			            name:'pfolio',
			            width:200,
			            labelWidth:70,
			            margin:'0 20 0 10'
					},{
						xtype: 'textfield',
			            fieldLabel: SuppAppMsg.suppliersNumber,
			            id: 'supNumber',
			            itemId: 'supNumber',
			            name:'supNumber',
			            value: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?addressNumber:'',
			            fieldStyle: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?'border:none;background-color: #ddd; background-image: none;':'',
			            readOnly: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?true:false,
			            width:200,
			            labelWidth:90,
			            margin:'20 20 0 10'
					},{
						xtype: 'datefield',
			            fieldLabel: SuppAppMsg.purchaseOrderDesde,
			            id: 'poFromDate',
			            itemId: 'poFromDate',
			            name:'poFromDate',
			            width:160,
			            labelWidth:35,
			            margin:'0 20 0 10'
					},{
						xtype: 'datefield',
			            fieldLabel: SuppAppMsg.purchaseOrderHasta,
			            id: 'poToDate',
			            itemId: 'poToDate',
			            name:'poToDate',
			            width:160,
			            labelWidth:35,
			            margin:'0 40 0 10'
					},{ 
						xtype: 'combostatus'
					},{
		           		xtype:'button',
			            text: SuppAppMsg.suppliersSearch,
			            iconCls: 'icon-appgo',
			            action:'poSearch',
			            cls: 'buttonStyle',
			            margin:'0 20 0 10'
					}
                ]
            },
            {
                xtype: 'toolbar',
                dock: 'top',
                style: {
                    background: 'white'
                  },
                items: [
                	{
		           		xtype:'button',
			            text: 'Aprobar facturas seleccionadas',
			            iconCls: 'icon-accept',
			            action:'poInvAccept',
			            cls: 'buttonStyle',
			            //hidden:role == 'ROLE_SUPPLIER' || role == 'ROLE_WNS'?true:false,
			            hidden : true,
			            margin:'2 20 5 10'
					},{
		           		xtype:'button',
			            text: 'Rechazar facturas seleccionadas',
			            iconCls: 'icon-delete',
			            action:'poInvReject',
			            //hidden:role == 'ROLE_SUPPLIER'?true:false,
			            hidden : true,
			            cls: 'buttonStyle',
			            margin:'2 20 5 10'
					},
					{
		           		xtype:'button',
			            text: SuppAppMsg.purchaseOrderCCPG,
			            iconCls: 'icon-accept',
			            action:'poLoadCompl',
			            id:'poLoadCompl',
			            itemId:'poLoadCompl',
			            cls: 'buttonStyle',
			            margin:'2 20 5 10',
			            hidden:role == 'ROLE_WNS'?true:false
					},
					{
		           		xtype:'button',
			            text: SuppAppMsg.purchaseOrderRO,
			            iconCls: 'icon-accept',
			            action:'poReasignPurchases',
			            hidden:true,
			            //hidden:role == 'ROLE_ADMIN' || role == 'ROLE_MANAGER' ?false:true,
			            cls: 'buttonStyle',
			            margin:'2 20 5 10'
					},
					{
		           		xtype:'button',
			            text: SuppAppMsg.purchaseOrderCP,
			            iconCls: 'icon-accept',
			            action:'poPaymentCalendar',
			            hidden:role == 'ROLE_ADMIN' || role == 'ROLE_MANAGER' ?false:true,
			            cls: 'buttonStyle',
			            margin:'2 20 5 10'
					},
					{
		           		xtype:'button',
			            text: SuppAppMsg.purchaseOrderFTP,
			            iconCls: 'icon-accept',
			            action:'poLoadFTPInv',
			            hidden:true,
			            cls: 'buttonStyle',
			            margin:'2 20 5 10'
					},{
		           		xtype:'button',
			            text: 'Facturas VS JDE',
			            iconCls: 'icon-accept',
			            action:'factVsJde',
			            id:'factVsJde',
			            itemId:'factVsJde',
			            hidden:true,
			            cls: 'buttonStyle',
			            margin:'2 20 5 10'
					}
					/*
					,'->',
					{
		           		xtype:'button',
			            text: SuppAppMsg.purchaseOrderIP,
			            iconCls: 'icon-accept',
			            action:'poLoadPayment',
			            id:'poLoadPaymentImport',
			            itemId:'poLoadPaymentImport',
			            hidden:true,
			            cls: 'buttonStyle',
			            margin:'2 20 5 10'
					},{
						xtype: 'displayfield',
			            value: 'Replicación',
			            id:'lblReplicacion',
			            itemId:'lblReplicacion',
			            width : 100,
			            hidden:true
		            	},{
						xtype: 'datefield',
			            fieldLabel: 'Desde',
			            id: 'fromDate',
			            itemId:'fromDate',
			            maxValue: new Date(),
			            format: 'd/m/Y',
			            width:140,
			            labelWidth:30,
			            hidden:true
					},{
						xtype: 'datefield',
			            fieldLabel: 'Hasta',
			            margin:'2 10 5 15',
			            id: 'toDate',
			            itemId:'toDate',
			            width:140,			            
			            maxValue: new Date(),
			            labelWidth:30,
			            hidden:true
					},{
						xtype: 'textfield',
			            fieldLabel: 'Proveedor',
			            id: 'addressNumber',
			            itemId:'addressNumber',
			            width:120,
			            labelWidth:50,
			            margin:'2 10 5 15',
			            hidden:true
					},{
						xtype: 'numberfield',
			            fieldLabel: 'OC',
			            //id: 'orderNumber',
			            itemId:'orderNumber',
			            hideTrigger:'true', 
			            width:100,
			            labelWidth:20,
			            margin:'2 10 5 15',
			            hidden:true
					},{
		           		xtype:'button',
			            text: 'Importar',
			            iconCls: 'icon-accept',
			            action:'poLoadPurchases',
			            id: 'poLoadPurchases',
			            itemId:'poLoadPurchases',
			            cls: 'buttonStyle',
			            margin:'2 10 5 10',
			            hidden:true
					}
					*/
                ]
            }
        ];
        

		this.bbar = Ext.create('Ext.PagingToolbar', {
			store: this.store,
			displayInfo: true
		});
      
        this.callParent(arguments);
    }
});