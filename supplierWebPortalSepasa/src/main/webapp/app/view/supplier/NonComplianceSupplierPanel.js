Ext.define('SupplierApp.view.supplier.NonComplianceSupplierPanel' ,{
    extend: 'Ext.Panel',
    alias : 'widget.nonComplianceSupplierPanel',
    border:false,
    frame:false,
	initComponent: function () {
        Ext.apply(this, {  
            items: [{
           	 xtype: 'nonComplianceSupplierGrid',
           	 height:460
            }]
        });
        this.callParent(arguments);
    }
 
});