Ext.define('SupplierApp.view.purchaseOrder.DeliverPurchaseOrderPanel' ,{
    extend: 'Ext.Panel',
    alias : 'widget.deliverPurchaseOrderPanel',
    border:false,
    frame:false,
	initComponent: function () {
        Ext.apply(this, {  
            items: [{
           	 xtype: 'deliverPurchaseOrderGrid',
           	 height:460
            }]
        });
        this.callParent(arguments);
    }
 
});