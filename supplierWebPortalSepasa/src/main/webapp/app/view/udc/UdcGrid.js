Ext.define('SupplierApp.view.udc.UdcGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.udcGrid',
    loadMask: true,
	frame:false,
	border:false,
	id:'udcgrid',
	cls: 'extra-large-cell-grid', 
    store:'Udc',
	scroll : false,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
 
        this.columns = [
           {
            text     : SuppAppMsg.udcSystem,
            flex: 1,
//            width: 100,
            dataIndex: 'udcSystem'
        },{
            text     : SuppAppMsg.udcKey,
            flex: 1,
//            width: 100,
            dataIndex: 'udcKey'
        },{
            text     : 'strValue1',
            flex: 1.5,
//            width: 165,
            dataIndex: 'strValue1'
        },{
            text     : 'strValue2',
            flex: 1.5,
//            width: 150,
            dataIndex: 'strValue2'
        },{
            text     : 'intValue',
            flex: 0.5,
//            width: 50,
            dataIndex: 'intValue'
        },{
            text     : 'boolValue',
            flex: 0.5,
//            width: 50,
            dataIndex: 'booleanValue'
        },{
            text     : SuppAppMsg.udcDate,
            flex: 1,
//            width    : 100,
            renderer : Ext.util.Format.dateRenderer('d-M-Y'),
            dataIndex: 'dateValue'
        },{  
            text: 'SystemRef',
            flex: 1.25,
//            width: 125,
            dataIndex: 'systemRef'
        },{
            text     : 'KeyRef',
            flex: 1.10,
//            width: 110,
            dataIndex: 'keyRef'
        }];
        
		this.bbar = Ext.create('Ext.PagingToolbar', {
			store: 'Udc',
			displayInfo: true
		});
      
        this.callParent(arguments);
    }
});