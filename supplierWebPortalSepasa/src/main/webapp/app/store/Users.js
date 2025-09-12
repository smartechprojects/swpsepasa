Ext.define('SupplierApp.store.Users', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.Users',
    autoLoad: false,
    remoteSort:true,
    pageSize: 12,
    proxy: {
        type: 'ajax',
        api: {
            read: 'admin/users/view.action',
            create: 'admin/users/save.action',
            update: 'admin/users/update.action',
            destroy: 'admin/users/delete.action'
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
        },
        afterRequest:function(request,success){
            if(success && request.method != 'GET'){
            	Ext.toggle.msg(SuppAppMsg.approvalResponse,SuppAppMsg.udcMsg1);
            }
        }
    },
    root: {
        expanded: false
    }
});