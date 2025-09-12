Ext.define('SupplierApp.store.Invoices', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.ReportInvoices',
    autoLoad: false,
    remoteSort:true,
    pageSize: 10,
    proxy: {
        type: 'ajax',
        api: {
            read: 'invoice/searchReportDocument.action'
        },
        extraParams:{
        	supplierNumber:'',
        	uuid:'',
        	pFolio:'',
        	poFromDate:'',
        	poToDate:''  ,
        	module:''
        },
        reader: {
            type: 'json',
            root: 'data',
            totalProperty: 'total',
            successProperty: 'success'
        },
        writer: {
        	type: 'json',
            writeAllFields: true,
            encode: false
        },
        root: {
            expanded: true
        }

    }
});