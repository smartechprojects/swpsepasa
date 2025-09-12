Ext.define('SupplierApp.view.invoices.InvoicesPanel' ,{
    extend: 'Ext.Panel',
    alias : 'widget.invoicesPanel',
    border:false,
    frame:false,
	initComponent: function () {
        Ext.apply(this, {  
            items: [{
           	 xtype: 'invoicesGrid',
           	 height:460
            }]
        });
        this.callParent(arguments);
    }
 
});