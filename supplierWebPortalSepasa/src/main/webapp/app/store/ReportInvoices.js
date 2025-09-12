Ext.define('SupplierApp.store.ReportInvoices', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.ReportInvoices',
    autoLoad: false,
    remoteSort:true,
    pageSize: 10,
    proxy: {
        enablePaging: true,
        type: 'ajax',
        api: {
        	read: 'invoice/searchReportInvoices.action'
        },
        extraParams:{
        	
        	supplierNumber:'',
        	rfc:'',
        	poFromDate:'',
        	poToDate:''      		
        },
        reader: {
            type: 'json',
            rootProperty: 'data',
            totalProperty: 'total',
            successProperty: 'success'
        },
        writer: {
        	type: 'json',
            writeAllFields: true,
            encode: false
        }
    },
    
    root: {
        expanded: true
    }
});