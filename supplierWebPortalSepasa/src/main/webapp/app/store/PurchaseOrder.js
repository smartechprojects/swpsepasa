Ext.define('SupplierApp.store.PurchaseOrder', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.PurchaseOrder',
    autoLoad: false,
    remoteSort:true,
    pageSize: 12,
    proxy: {
        type: 'ajax',
        api: {
            //read: 'supplier/orders/view.action',
            create: 'supplier/orders/save.action',
            update: 'supplier/orders/update.action',
            destroy: 'supplier/orders/delete.action'
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