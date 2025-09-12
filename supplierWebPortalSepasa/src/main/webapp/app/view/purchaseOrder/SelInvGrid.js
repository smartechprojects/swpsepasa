	Ext.define('SupplierApp.view.purchaseOrder.SelInvGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.selInvGrid',
    loadMask: true,
	frame:true,
	border:false,
	title:'Selección de órdenes',
	cls: 'extra-large-cell-grid',  
	scroll : true,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
	 
		this.store =  storeAndModelFactory('SupplierApp.model.Receipt',
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
            width: 260,
            dataIndex: 'uuid'
        },{
            text     : SuppAppMsg.purchaseTitle54,
            width: 90,
            dataIndex: 'folio'
        },{
            renderer: function(value, meta, record) {
               var id = Ext.id();
               Ext.defer(function() {
                  Ext.widget('button', {
                     renderTo: Ext.query("#"+id)[0],
                     text: SuppAppMsg.purchaseTitle55,
                     name : 'acceptSelInv',
        			 itemId : 'acceptSelInv',
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
                items: [
                	{
						xtype: 'textfield',
			            fieldLabel:SuppAppMsg.purchaseTitle56,
			            id: 'invNbr',
			            itemId: 'invNbr',
			            name:'invNbr',
			            width:200,
			            labelWidth:90,
			            margin:'10 20 10 10',
			            hidden:true
					},{
		           		xtype:'button',
			            text: SuppAppMsg.suppliersSearch,
			            iconCls: 'icon-appgo',
			            action:'invNbrSearch',
			            cls: 'buttonStyle',
			            margin:'10 20 10 10',
			            hidden:true
					}
                ]
            }
        ];
        
      
        this.callParent(arguments);
    }
});