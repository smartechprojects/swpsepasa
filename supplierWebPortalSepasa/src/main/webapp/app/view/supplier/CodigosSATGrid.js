Ext.define('SupplierApp.view.supplier.CodigosSATGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.codigosSATGrid',
    loadMask: true,
	frame:false,
	border:false,
	cls: 'extra-large-cell-grid', 
	scroll : false,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
    	
		this.store =  storeAndModelFactory('SupplierApp.model.CodigosSAT',
                'codigosSATModel',
                'codsat/searchByCriteria.action', 
                true,
                {
			        query : ''
                },
			    "", 
			    11);
 
        this.columns = [
           {
            text     : SuppAppMsg.purchaseSATKey,
            width: 200,
            dataIndex: 'codigoSAT'
        },{
            text     : 'Tipo',
            width: 200,
            dataIndex: 'tipoCodigo'
        },{
            text     : SuppAppMsg.purchaseTitle29,
            width: 420,
            dataIndex: 'descripcion'
        }];
        
       
        this.tbar = [{
			xtype: 'textfield',
            fieldLabel:SuppAppMsg.complianceSearchText,
            id: 'codigoSatSearch',
            itemId: 'codigoSatSearch',
            name:'codigoSatSearch',
            width:400,
            labelWidth:110,
            margin:'20 20 20 20'
		},{
       		xtype:'button',
            text: SuppAppMsg.suppliersSearch,
            iconCls: 'icon-doSearch',
            action:'codigoSatSearchBtn',
            margin:'20 20 20 20'
		},{
       		xtype:'button',
            hidden: role=='ROLE_ADMIN'?false:true,
            text: SuppAppMsg.suppliersLoadFile,
            iconCls: 'icon-excel',
            action:'uploadCodigosSatFile',
            margin:'20 20 20 40'
		}];

		this.bbar = Ext.create('Ext.PagingToolbar', {
		store: this.store,
		displayInfo: true
		});
      
        this.callParent(arguments);
    }
});