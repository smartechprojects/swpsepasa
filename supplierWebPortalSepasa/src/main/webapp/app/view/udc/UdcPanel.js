Ext.define('SupplierApp.view.udc.UdcPanel' ,{
    extend: 'Ext.Panel',
    alias : 'widget.udcPanel',
    border:false,
    frame:false,
	initComponent: function () {
        Ext.apply(this, {
        	layout: {
                type: 'vbox',
                align: 'stretch'
            },   
            items: [{
            	xtype: 'udcForm',
            	height:160

            },{
           	 xtype: 'udcGrid',
           	 height:300
            }]
        });
        this.callParent(arguments);
    }
 
});