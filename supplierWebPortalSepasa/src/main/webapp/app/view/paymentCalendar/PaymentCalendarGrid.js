Ext.define('SupplierApp.view.paymentCalendar.PaymentCalendarGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.paymentCalendarGrid',
    loadMask: true,
	frame:false,
	border:false,
	store: 'PaymentCalendar',
	cls: 'extra-large-cell-grid', 
	scroll : true,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {

        this.columns = [
           {
            text     : 'id',
            dataIndex: 'id',
            hidden:true
        },{
            text     : SuppAppMsg.paymentTitle1,
            width: 120,
            dataIndex: 'company'
        },{
            text     : SuppAppMsg.paymentTitle2,
            width: 220,
            dataIndex: 'paymentDate',
            renderer : Ext.util.Format.dateRenderer("d-m-Y")
        }];

		this.bbar = Ext.create('Ext.PagingToolbar', {
		store: this.store,
		displayInfo: true
		});
      
        this.callParent(arguments);
    }
});