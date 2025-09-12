Ext.define('SupplierApp.store.FiscalDocuments', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.FiscalDocuments',
    autoLoad: false,
    pageSize: 10000,
    proxy: {
        enablePaging: true,
        type: 'ajax',
        api: {
        	read: 'fiscalDocuments/view.action'
        },
        extraParams:{
        	addressNumber:'',
        	status:'',
        	uuid:'',
        	documentType:'',
        	invoiceStatus:'',
        	pFolio:''	
        },
        reader: {
            type: 'json',
            root: 'data',
            successProperty: 'success'
        },
        writer: {
        	type: 'json',
            writeAllFields: true,
            encode: false
        }
    },

    root: {
        expanded: false
    }
});