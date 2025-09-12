Ext.define('SupplierApp.store.Receipt', {
    extend: 'Ext.data.Store',
    model: 'SupplierApp.model.Receipt',
    autoLoad: false,
    remoteSort:true,
    pageSize: 12,
    proxy: {
        type: 'ajax',
        api: {
            read: 'receipt/getOrderReceipts.action'
        },
        extraParams:{
        	orderNumber:'',
        	orderType:'',
        	addressBook:'',
        	orderCompany:''
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
    }
});