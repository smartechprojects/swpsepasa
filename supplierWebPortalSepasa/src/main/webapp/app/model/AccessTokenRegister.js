Ext.define('SupplierApp.model.AccessTokenRegister', {
    extend: 'Ext.data.Model',
    fields: [
			{name:  'id'},
			{name:  'company'},
			{name:  'code'},
			{name:  'password'},
			{name:  'creationDate', type: 'date', dateFormat: 'c'},
			{name:  'updatedDate', type: 'date', dateFormat: 'c'},
			{name:  'expirationDate', type: 'date', dateFormat: 'c'},
			{name: 'registerName'},
			{name: 'email'},
			{name:  'token'},
			{name:  'createdBy'},
			{name:  'updatedBy'},
			{name:  'rfc'},
			{name:  'searchType'},
			{name: 'enabled', type:'boolean'},
			{name: 'assigned', type:'boolean'}
        ],
        proxy: {
            type: 'ajax',
            api: {
                create: 'supplier/token/saveAccessTokenRegister.action',
                update: 'supplier/token/updateAccessTokenRegister.action'
            },
            reader: {
                type: 'json',
                root: 'data',
                successProperty: 'success',
                messageProperty: 'message'
            },
            writer: {
            	type: 'json',
                writeAllFields: true,
                encode: false
            }
        }
});