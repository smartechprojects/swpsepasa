Ext.define('SupplierApp.view.approval.ApprovalPanel' ,{
    extend: 'Ext.Panel',
    alias : 'widget.approvalPanel',
    border:false,
    frame:false,
	initComponent: function () {
        Ext.apply(this, {  
            items: [{
           	 xtype: 'approvalGrid',
           	 height:460
            }]
        });
        this.callParent(arguments);
    }
 
});