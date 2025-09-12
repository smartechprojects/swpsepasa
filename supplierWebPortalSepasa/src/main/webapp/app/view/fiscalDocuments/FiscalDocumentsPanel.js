Ext.define('SupplierApp.view.fiscalDocuments.FiscalDocumentsPanel' ,{
    extend: 'Ext.Panel',
    alias : 'widget.fiscalDocumentsPanel',
    border:false,
    frame:false,
	initComponent: function () {
        Ext.apply(this, {  
            items: [{
           	 xtype: 'fiscalDocumentsGrid',
           	 height:460
            }]
        });
        this.callParent(arguments);
    }
 
});