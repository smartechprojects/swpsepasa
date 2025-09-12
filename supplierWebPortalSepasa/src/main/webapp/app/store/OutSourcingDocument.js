Ext.define('SupplierApp.store.OutSourcingDocument', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.OutSourcingDocument',
    autoLoad: false,
    pageSize: 11,
    proxy: {
        type: 'ajax',
        api: {
        	read: 'supplier/searchOSDocuments.action'
        },
        extraParams:{
        	status:'',
        	supplierName:'',
        	supplierNumber:'',
        	documentType:'',
        	fromDate:'',
        	toDate:'',
        	roleType:''
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