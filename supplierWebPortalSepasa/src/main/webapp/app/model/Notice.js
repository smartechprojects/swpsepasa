Ext.define('SupplierApp.model.Notice', {
    extend: 'Ext.data.Model',
    fields: [
    		{name: 'id'},
    		{name: 'idNotice'},
    		{name: 'createdBy'},
			{name: 'noticeTitle'},
			{name: 'frequency'},
			{name: 'filters'},
			{name: 'suppliersNotice'},
			{name: 'noticeFile'},
			{name: 'noticeFromDate', type: 'date'},
			{name: 'noticeToDate', type: 'date', dateFormat: 'c'},
			//{model: 'SupplierApp.model.Udc', name: 'userRole', mapping:'userRole', convert:modelNull},
			{name: 'enabled', type:'boolean'},
			{name: 'required', type:'boolean'},
			{name: 'emailNotif', type:'boolean'},
			{name: 'docSupplier', type:'boolean'},
			{name: 'fechaCreacion', type: 'date', dateFormat: 'c'},
			{name: 'fechaActualizacion', type: 'date', dateFormat: 'c'},
        ],
        proxy: {
            type: 'ajax',
            api: {
                read: 'admin/users/view.action',
                create: 'notice/save.action',
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