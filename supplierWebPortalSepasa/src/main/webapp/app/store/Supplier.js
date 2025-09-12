Ext.define('SupplierApp.store.Supplier', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.Supplier',
    autoLoad: false,
    remoteSort:false,
    pageSize: 12,
    proxy: {
        type: 'ajax',
        api: {
            read: 'supplier/view.action',
            create: 'supplier/save.action',
            update: 'supplier/update.action'
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
        listeners: {
            exception: function(proxy, response, operation){
            	obj = Ext.decode(response.responseText);
                Ext.MessageBox.show({
                	title: SuppAppMsg.udcMsgError3,
                    msg: obj.message,
                    icon: Ext.MessageBox.ERROR,
                    buttons: Ext.Msg.OK
                });
            }
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