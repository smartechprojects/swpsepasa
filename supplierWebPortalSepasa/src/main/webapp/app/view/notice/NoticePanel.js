Ext.define('SupplierApp.view.notice.NoticePanel' ,{
    extend: 'Ext.Panel',
    alias : 'widget.noticePanel',
    border:true,
    frame:false,
	initComponent: function () {
        Ext.apply(this, {
        	layout: {
                type: 'hbox',
                align: 'stretch'
            },   
            items: [{
            	xtype: 'noticeForm',
            	flex:5,
            	height:480

            },{
           	 xtype: 'noticeGrid',
           	flex:5
            }]
        });
        this.callParent(arguments);
    }
 
});