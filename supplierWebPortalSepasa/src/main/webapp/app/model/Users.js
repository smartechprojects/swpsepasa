Ext.define('SupplierApp.model.Users', {
    extend: 'Ext.data.Model',
    fields: [
			{name: 'id'},
			{name: 'userName'},
			{name: 'name'},
			{name: 'password'},
			{name: 'email'},
			{name: 'role'},
			{name: 'notes'},
			{model: 'SupplierApp.model.Udc', name: 'userRole', mapping:'userRole', convert:modelNull},
			{model: 'SupplierApp.model.Udc', name: 'userType', mapping:'userType', convert:modelNull},
			{name: 'enabled', type:'boolean'},
			{name: 'openOrders', type:'boolean'},
			{name: 'logged', type:'boolean'},
			{name: 'agreementAccept', type:'boolean'},
			{name: 'exepAccesRule', type:'boolean'}
        ],
        proxy: {
            type: 'ajax',
            api: {
                read: 'admin/users/view.action',
                create: 'admin/users/save.action',
                update: 'admin/users/update.action',
                destroy: 'admin/users/delete.action'
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
                	Ext.toggle.msg('Resultado', 'Su operación ha sido completada.');
                }
            }
        },
        root: {
            expanded: false
        }
});