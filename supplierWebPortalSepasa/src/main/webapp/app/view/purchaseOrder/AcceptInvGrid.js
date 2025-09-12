Ext.define('SupplierApp.view.purchaseOrder.AcceptInvGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.acceptInvGrid',
    loadMask: true,
	frame:true,
	border:false,
	title:'Órdenes seleccionadas',
	cls: 'extra-large-cell-grid',  
	scroll : true,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
	 
		this.store =  storeAndModelFactory('SupplierApp.model.PurchaseOrder',
                'receiptModel',
                'receipt/getComplReceiptsByStatus.action',
                false,
                {
			        addressBook:''
                },
			    "", 
			    100);
 
        this.columns = [
           {
            text     : SuppAppMsg.acceptTitle1,
            width: 90,
            dataIndex: 'documentNumber'
        },{
            text     : 'UUID',
            width: 250,
            dataIndex: 'uuid'
        },{
            renderer: function(value, meta, record) {
                var id = Ext.id();
                Ext.defer(function() {
                   Ext.widget('button', {
                      renderTo: Ext.query("#"+id)[0],
                      text: SuppAppMsg.acceptTitle2,
                      name : 'rejectSelInv',
         			  itemId : 'rejectSelInv',
                      scale: 'small',
                      handler: function(grid, rowIndex, colIndex) {
                       	this.fireEvent('buttonclick', grid, record);
                       }
                   });
                }, 50);
                return Ext.String.format('<div id="{0}"></div>', id);
             }
          }];
        
        this.dockedItems = [
            {
                xtype: 'toolbar',
                style: {
                    background: 'white'
                  },
                dock: 'top',
                items: [{
		           		xtype:'button',
			            text: SuppAppMsg.acceptTitle3,
			            iconCls: 'icon-appgo',
			            action:'loadComplFile',
			            cls: 'buttonStyle',
			            margin:'10 20 10 10'
					}
                ]
            }
        ];
        
      
        this.callParent(arguments);
    }
});