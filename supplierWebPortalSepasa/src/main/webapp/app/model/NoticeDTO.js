Ext.define('SupplierApp.model.NoticeDTO', {
    extend: 'Ext.data.Model',
    fields: [
			{name: 'id'},
			{name: 'idNotice'},
			{name: 'docNotice'},
			{name: 'noticeTitle'},
			{name: 'required', type:'boolean'},
			{name: 'docSupplier', type:'boolean'}
        ]
});