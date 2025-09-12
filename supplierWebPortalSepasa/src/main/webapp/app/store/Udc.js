Ext.define('SupplierApp.store.Udc', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.Udc',
    autoLoad: false,
    remoteSort:true,
    pageSize: 12,
    proxy: {
        type: 'ajax',
        api: {
            read: 'admin/udc/view.action',
            create: 'admin/udc/save.action',
            update: 'admin/udc/update.action',
            destroy: 'admin/udc/delete.action'
        },
        extraParams:{
        	query:'',
        	udcSystem:'',
        	udcKey:'',
        	strValue1:'',
        	strValue2:''
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
            	Ext.toggle.msg(SuppAppMsg.approvalResponse,  SuppAppMsg.udcMsg1);
            }
        }
    },
    root: {
        expanded: false
    }
});