Ext.define('SupplierApp.store.LogData', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.LogData',
    autoLoad: false,
    proxy: {
        type: 'ajax',
        api: {
            read: 'orders/log/getLog.action'
        },
        extraParams:{
        	fromDate:'',
        	toDate:''
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
    }
});