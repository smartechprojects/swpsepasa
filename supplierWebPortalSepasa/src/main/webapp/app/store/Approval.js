Ext.define('SupplierApp.store.Approval', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.SupplierDTO',
    autoLoad: false,
    remoteSort:false,
    pageSize: 12,
    proxy: {
        type: 'ajax',
        api: {
            read: 'approval/view.action',
            update: 'approval/update.action'
        },
        extraParams:{
        	currentApprover:'',
            status:'',
            step:'',
            notes:''
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