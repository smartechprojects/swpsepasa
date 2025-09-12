Ext.define('SupplierApp.store.AccessTokenRegister', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.AccessTokenRegister',
    autoLoad: false,
    remoteSort:true,
    pageSize: 12,
    proxy: {
        type: 'ajax',
        api: {
            read: 'supplier/token/listAccessTokenRegister.action'
        },
        extraParams:{
        	query:''
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