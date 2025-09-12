Ext.define('SupplierApp.view.invoices.InvoicesGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.invoicesGrid',
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
	 
    	var moduleType = Ext.create('Ext.data.Store', {
    	    fields: ['id', 'name'],
    	    data : [
    	        {"id":"PO", "name":"Órden de Compra"},
    	        {"id":"FD", "name":"Factura sin OC"}
    	    ]
    	});
    	
		this.store =  storeAndModelFactory('SupplierApp.model.ReportInvoices',
                'reportInvoicesModel',
                'invoice/searchReportDocument.action', 
                false,
                {
					supplierNumber:'',
					uuid:'',
					pFolio:'',
					poFromDate:'',
					poToDate:'' ,
					module:''
                },
			    "", 
			    10);
		
		Ext.define('moduleTypeCombo', {
    	    extend: 'Ext.form.ComboBox',
    	    fieldLabel: SuppAppMsg.module,
    	    store: moduleType,
    	    alias: 'widget.combomoduletype',
    	    queryMode: 'local',
    	    allowBlank:false,
    	    editable: false,
    	    displayField: 'name',
			width:230,
    	    labelWidth:90,
    	    valueField: 'id',
    	    margin:'20 20 0 10',
    	    id:'comboModuleType'});
 
        this.columns = [
           {
        	text     : SuppAppMsg.suppliersNumber,
			width: 110,
			dataIndex: 'addressBook'
		},{
            text     : SuppAppMsg.fiscalTitle30,
            width: 110,
            dataIndex: 'rfcReceptor'
        },{
            text     : SuppAppMsg.fiscalTitle4,
            width: 80,
            dataIndex: 'serie'
        },{
            text     : SuppAppMsg.fiscalTitle3,
            width: 80,
            dataIndex: 'folio',
        },{
        	text     : SuppAppMsg.fiscalTitle5,
            width: 280,
            dataIndex: 'uuid'
        },{
            text     : SuppAppMsg.fiscalTitle15,
            width: 120,
            dataIndex: 'invoiceDate'
        },{
            text     : SuppAppMsg.fiscalTitle28,
            width: 150,
            dataIndex: 'total',
            renderer : Ext.util.Format.numberRenderer('0,0.00')
        },{
            text     : SuppAppMsg.purchaseOrderCurrency,
            width: 110,
            dataIndex: 'moneda'
        }];
        
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
			            fieldLabel: SuppAppMsg.suppliersNumber,
			            id: 'supNumberInvoice',
			            itemId: 'supNumberInvoice',
			            name:'supNumberInvoice',
			            value: role == 'ROLE_SUPPLIER' || role=='ROLE_SUPPLIER_OPEN'?addressNumber:'',
			            width:160,
			            labelWidth:65,
			            margin:'0 20 0 10'
					},{
						xtype: 'textfield',
			            fieldLabel: SuppAppMsg.purchaseInvoiceNumber,
			            id: 'invoiceFolio',
			            itemId: 'invoiceFolio',
			            name:'invoiceFolio',
			            width:200,
			            labelWidth:70,
			            margin:'0 20 0 10'
					},{
						xtype: 'datefield',
			            fieldLabel: SuppAppMsg.purchaseOrderDesde,
			            id: 'invoiceFromDate',
			            itemId: 'invoiceFromDate',
			            name:'invoiceFromDate',
			            width:200,
			            labelWidth:70,
			            margin:'0 20 0 10'
					},{
						xtype: 'datefield',
			            fieldLabel: SuppAppMsg.purchaseOrderHasta,
			            id: 'invoiceToDate',
			            itemId: 'invoiceToDate',
			            name:'invoiceToDate',
			            width:200,
			            labelWidth:70,
			            margin:'0 20 0 10'
					},{
						xtype: 'textfield',
			            fieldLabel: 'UUID',
			            id: 'invoiceUUID',
			            itemId: 'invoiceUUID',
			            name:'invoiceUUID',
			            width:350,
			            labelWidth:90,
			            margin:'20 20 0 10'
					},{ 
						xtype: 'combomoduletype'
					},{
		           		xtype:'button',
			            text: SuppAppMsg.suppliersSearch,
			            iconCls: 'icon-appgo',
			            action:'invoiceSearch',
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
			            text: SuppAppMsg.invoiceDownloadTitle,
			            iconCls: 'icon-zip',
			            action:'downloadInv',
			            itemId : 'downloadInv',
	           			id : 'downloadInv',
			            cls: 'buttonStyle',
			            margin:'2 20 5 10'
					}
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