Ext.define('SupplierApp.view.receipt.ReceiptMultiOrderPanel' ,{
    extend: 'Ext.Panel',
    alias : 'widget.receiptMultiOrderPanel',
    border:false,
    frame:false,
	initComponent: function () {
        Ext.apply(this, {
        	layout: {
                type: 'vbox',
                align: 'stretch'
            },   
            items: [{
            	xtype: 'receiptMultiOrderForm',
            	height:80

            },{
           	 xtype: 'receiptMultiOrderGrid',
           	 height:380
            }]
        });
        this.callParent(arguments);
    }
});